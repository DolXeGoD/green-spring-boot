package com.green_computer.green_board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// JPA가 DB에서 가져온 데이터를 최초로 넣는 곳 (Entity)
@Entity
// JPA가 어느 테이블로 찾아가야 하는지 알려주는 곳
@Table(name = "boards")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Board {
    // DB에서 id라는 필드는 Primary Key라는 것을 알림
    @Id
    // DB에서 id라는 필드는 AUTO_INCREMENT 처리되어 있음을 알림
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 테이블의 title 컬럼에서 가져온 값은 여기 (title 필드)에 세팅하라고 알림
    @Column(name = "title", nullable = false)
    private String title;

    // 테이블의 content 컬럼에서 가져온 값은 여기에 세팅하라고 알림
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "hits", nullable = false)
    private int hits;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author", nullable = false)
    private User author;

    @CreatedDate
    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}
