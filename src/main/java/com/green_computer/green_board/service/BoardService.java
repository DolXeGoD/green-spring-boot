package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.PostCreateRequest;
import com.green_computer.green_board.dto.PostResponse;
import com.green_computer.green_board.dto.PostUpdateRequest;
import com.green_computer.green_board.dto.BoardListResponse;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.Like;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.BoardType;
import com.green_computer.green_board.exceptions.AuthenticationFailureException;
import com.green_computer.green_board.exceptions.AuthorizationFailureException;
import com.green_computer.green_board.exceptions.InvalidStateException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.BoardRepository;
import com.green_computer.green_board.repository.LikeRepository;
import com.green_computer.green_board.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllBoards(int page, int size, String order) {
        return getBoardPage(BoardType.GENERAL, createPageable(page, size, order));
    }

    @Transactional(readOnly = true)
    public BoardListResponse getBoardHome(int page, int size, String order) {
        Pageable noticePageable = Pageable.unpaged(Sort.by(Sort.Direction.DESC, "createdDatetime", "id"));
        List<PostResponse> notices = getBoardPage(BoardType.NOTICE, noticePageable).getContent();
        Page<PostResponse> posts = getBoardPage(BoardType.GENERAL, createPageable(page, size, order));
        return new BoardListResponse(notices, posts);
    }

    private Pageable createPageable(int page, int size, String order) {
        if (page < 0 || size < 1) {
            throw new InvalidStateException("페이지 번호와 크기를 확인해 주세요.");
        }

        Sort sort;
        if ("views".equals(order)) {
            sort = Sort.by(Sort.Direction.DESC, "hits");
        } else if ("likes".equals(order)) {
            sort = Sort.by(Sort.Direction.DESC, "likeCount");
        } else if ("latest".equals(order)) {
            sort = Sort.by(Sort.Direction.DESC, "createdDatetime");
        } else {
            throw new InvalidStateException("정렬 기준은 latest, views, likes 중 하나여야 합니다.");
        }

        sort = sort.and(Sort.by(Sort.Direction.DESC, "id"));
        return PageRequest.of(page, size, sort);
    }

    private Page<PostResponse> getBoardPage(BoardType type, Pageable pageable) {
        Page<Board> boardPage = boardRepository.findByIsDeletedFalseAndType(type, pageable);
        List<PostResponse> responses = new ArrayList<>();

        for (Board board : boardPage.getContent()) {
            responses.add(new PostResponse(
                    board.getId(),
                    board.getTitle(),
                    board.getContent(),
                    board.getAuthor().getName(),
                    board.getHits(),
                    board.getLikeCount(),
                    board.getCreatedDatetime(),
                    board.getUpdatedDatetime()
            ));
        }

        return new PageImpl<>(responses, pageable, boardPage.getTotalElements());
    }

    @Transactional
    public PostResponse getDetailPost(int id) {

        Optional<Board> boardOptional = boardRepository.findById(id);

        if(boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        Board board = boardOptional.get();

        if(board.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }

        User writer = board.getAuthor();
        String writerName = writer.getName(); // <- 글 작성자의 이름 가져왔음.

        // 조회수 늘려주는 로직 : 조회랑은 관련 없음
        board.setHits(board.getHits() + 1); // 조회수 1 늘리기
        boardRepository.saveAndFlush(board); // 수정일도 갱신된 뒤에 응답을 만든다.

        // DTO 만들어서, DTO를 응답해야된다.
        return new PostResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                writerName,
                board.getHits(),
                board.getLikeCount(),
                board.getCreatedDatetime(),
                board.getUpdatedDatetime()
        );
    }

    public int createNewPost(PostCreateRequest request, BoardType type) {
        // 지금 로그인한 유저의 정보
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        Board board1 = new Board();
        board1.setTitle(request.getTitle());
        board1.setContent(request.getContent());
        board1.setAuthor(user);
        board1.setType(type);

        Board newPost = boardRepository.save(board1);
        return newPost.getId();
    }

    @Transactional
    public void updatePost(int id, PostUpdateRequest request) {
        // 게시글을 찾지 못하면 null 대신 빈 Optional이 반환된다.
        // 값이 없는 상태에서 get()을 호출하면 예외가 나므로 먼저 확인한다.
        Optional<Board> boardOptional = boardRepository.findById(id);
        if(boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시물 못찾음");
        }

        // 수정할 대상 게시글을 DB에서 가져옴
        Board board = boardOptional.get();

        if (board.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }

        // 요청자의 User Id
        int requestUserId = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        // 게시글 원 작성자의 User Id
        int writerId = board.getAuthor().getId();

        // 요청자 ID와 게시글 작성자 ID가 다르면 수정을 거절한다.
        if(requestUserId != writerId){
            throw new AuthorizationFailureException("작성자 이외에는 수정 못한다");
        }

        // 프론트엔드랑 협의 -> 수정된 값만 json에 채워 보내주고, 수정 안된 값은 null로 채워라
        if(request.getContent() != null){   // null이 아니면 수정했단 뜻
            // DB update 해야함
            board.setContent(request.getContent());
        }
        if(request.getTitle() != null){     // null이 아니면 수정했단 뜻
            board.setTitle(request.getTitle());
        }

//        boardRepository.save(board);    // 수정 했으면 저장
    }

    @Transactional
    public void deletePost(int id) {
        // 요청자가, 게시글 작성자와 동일한지 확인할거다.
        int requestUserId = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();

        Optional<Board> boardOptional = boardRepository.findById(id);
        if(boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시물을 못찾았음");
        }
        Board board = boardOptional.get();
        int writerId = board.getAuthor().getId();

        // 요청자 ID와 게시글 작성자 ID가 다르면 삭제를 거절한다.
        if(requestUserId != writerId){
            throw new AuthorizationFailureException("작성자 이외에는 삭제 못한다");
        }

        board.setDeleted(true);
        boardRepository.save(board);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts() {

        List<Board> results = boardRepository.findByAuthorAndIsDeletedFalseOrderByCreatedDatetimeDescIdDesc(
                userRepository.findByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName()
                )
        );

        // 새로운 결과 전용 상자 제작
        List<PostResponse> response = new ArrayList<>();

        // board를 post response 로 변경하는 로직
        for(Board board : results) {
            PostResponse newResult = new PostResponse(
                    board.getId(),
                    board.getTitle(),
                    board.getContent(),
                    board.getAuthor().getName(),
                    board.getHits(),
                    board.getLikeCount(),
                    board.getCreatedDatetime(),
                    board.getUpdatedDatetime()
            );

            response.add(newResult);
        }

        return response;
    }

    // 이번에 새로 눌렀으면 true, 이번 액션으로 좋아요가 취소됐으면 false
    @Transactional
    public boolean toggleLike(int id){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        Board board = boardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if(board.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }

        Optional<Like> existingLike = likeRepository.findByUserIdAndBoardId(user.getId(), board.getId());
        if(existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            board.setLikeCount(board.getLikeCount() - 1);
            return false;
        } else {
            Like like = Like.builder()
                    .board(board)
                    .user(user)
                    .build();
            likeRepository.save(like);
            board.setLikeCount(board.getLikeCount() + 1);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<PostResponse> search(String keyword) {
        // 검색 -> SQL을 실행
        List<Board> results = boardRepository.searchByTitle(keyword);

        // 새로운 결과 전용 상자 제작
        List<PostResponse> response = new ArrayList<>();

        // board를 post response 로 변경하는 로직
        for(Board board : results) {
            PostResponse newResult = new PostResponse(
                    board.getId(),
                    board.getTitle(),
                    board.getContent(),
                    board.getAuthor().getName(),
                    board.getHits(),
                    board.getLikeCount(),
                    board.getCreatedDatetime(),
                    board.getUpdatedDatetime()
            );

            response.add(newResult);
        }
        // 결과를 return
        return response;
    }
}
