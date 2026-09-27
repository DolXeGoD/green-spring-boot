package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.ReportCreateRequest;
import com.green_computer.green_board.dto.ReportProcessRequest;
import com.green_computer.green_board.dto.ReportResponse;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.Comment;
import com.green_computer.green_board.entity.Report;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.ReportStatus;
import com.green_computer.green_board.exceptions.InvalidStateException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.BoardRepository;
import com.green_computer.green_board.repository.CommentRepository;
import com.green_computer.green_board.repository.ReportRepository;
import com.green_computer.green_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final DiscordNotificationService discordNotificationService;

    @Transactional
    public void reportBoard(int boardId, ReportCreateRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (board.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User reporter = userRepository.findByUsername(username);

        Report report = new Report();
        report.setReporter(reporter);
        report.setBoard(board);
        report.setReason(request.getReason());
        report.setStatus(ReportStatus.PENDING);
        reportRepository.save(report);

        discordNotificationService.sendReportAlert();
    }

    @Transactional
    public void reportComment(int commentId, ReportCreateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));
        if (comment.isDeleted() || comment.getBoard().isDeleted()) {
            throw new ResourceNotFoundException("삭제된 댓글입니다.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User reporter = userRepository.findByUsername(username);

        Report report = new Report();
        report.setReporter(reporter);
        report.setComment(comment);
        report.setReason(request.getReason());
        report.setStatus(ReportStatus.PENDING);
        reportRepository.save(report);

        discordNotificationService.sendReportAlert();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReports() {
        List<Report> reports = reportRepository.findAllByOrderByIdDesc();
        List<ReportResponse> responses = new ArrayList<>();

        for (Report report : reports) {
            Integer boardId = null;
            Integer commentId = null;
            if (report.getBoard() != null) {
                boardId = report.getBoard().getId();
            }
            if (report.getComment() != null) {
                commentId = report.getComment().getId();
            }
            responses.add(new ReportResponse(
                    report.getId(), boardId, commentId, report.getReporter().getUsername(),
                    report.getReason(), report.getStatus(), report.getCreatedDatetime()
            ));
        }
        return responses;
    }

    @Transactional
    public void processReport(int id, ReportProcessRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("신고를 찾을 수 없습니다."));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new InvalidStateException("이미 처리된 신고입니다.");
        }

        if (request.getAccepted()) {
            if (report.getBoard() != null) {
                Board board = report.getBoard();
                board.setDeleted(true);
                boardRepository.save(board);
            } else {
                Comment comment = report.getComment();
                comment.setDeleted(true);
                commentRepository.save(comment);
            }
            report.setStatus(ReportStatus.ACCEPTED);
        } else {
            report.setStatus(ReportStatus.REJECTED);
        }

        reportRepository.save(report);
    }
}
