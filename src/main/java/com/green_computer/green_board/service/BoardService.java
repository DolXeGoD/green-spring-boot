package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.PostCreateRequest;
import com.green_computer.green_board.dto.PostResponse;
import com.green_computer.green_board.dto.PostUpdateRequest;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.Like;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.exceptions.AuthenticationFailureException;
import com.green_computer.green_board.exceptions.AuthorizationFailureException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.BoardRepository;
import com.green_computer.green_board.repository.LikeRepository;
import com.green_computer.green_board.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

    public List<PostResponse> getAllBoards(Pageable pageable) {
        List<Board> results = boardRepository.findBoardsByIsDeletedFalse(pageable);

        List<PostResponse> response = new ArrayList<>();

        for(Board board : results) {
            User author = board.getAuthor();
            String authorName = author.getName();

            PostResponse newResult = new PostResponse(
                    board.getId(),
                    board.getTitle(),
                    board.getContent(),
                    authorName
            );

            response.add(newResult);
        }

        return response;
    }

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
        boardRepository.save(board); // 변경된 조회수 데이터를 반영해서 다시 저장

        // DTO 만들어서, DTO를 응답해야된다.
        return new PostResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                writerName
        );
    }

    public int createNewPost(PostCreateRequest request) {
        // 지금 로그인한 유저의 정보
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        Board board1 = new Board();
        board1.setTitle(request.getTitle());
        board1.setContent(request.getContent());
        board1.setAuthor(user);

        Board newPost = boardRepository.save(board1);
        return newPost.getId();
    }

    @Transactional
    public void updatePost(int id, PostUpdateRequest request) {
        // 옵셔널은 있을수도 있고, 없을수도 있다는 의미의 타입이다
        // 이유 : DB에는 게시글 5까지밖에 없는데 사용자가 7 입력하면 JPA가 못찾음
        // -> 그럼 Board board에 담을게 없어서 null 담음
        // -> 그럼 그 밑에 board.getTitle() 같은 코드가 NullPointerExcepion 나서 폭발함
        // 이런 실수를 방지하기 위해 "없을 수도 있으니 꺼내 쓰기 전에 확인부터 해라" 를 유도하기 위한 타입
        Optional<Board> boardOptional = boardRepository.findById(id);
        if(boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시물 못찾음");
        }

        // 수정할 대상 게시글을 DB에서 가져옴
        Board board = boardOptional.get();

        // 요청자의 User Id
        int requestUserId = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        // 게시글 원 작성자의 User Id
        int writerId = board.getAuthor().getId();

        // 요청자 ID와 게시글 작성자 ID가 다르면 삭제를 거절한다.
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

        boardRepository.deleteById(id);
    }

    public List<PostResponse> getMyPosts() {

        List<Board> results = boardRepository.findBoardsByAuthor(
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
                    "sdads"
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
}
