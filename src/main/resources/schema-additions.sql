ALTER TABLE boards
    ADD COLUMN board_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL';

ALTER TABLE users
    MODIFY COLUMN unblock_datetime DATETIME(6) NULL;

CREATE TABLE comments (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    author_id INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    created_datetime DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_comments_board FOREIGN KEY (board_id) REFERENCES boards(id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE reports (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reporter_id INT NOT NULL,
    board_id INT,
    comment_id INT,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_datetime DATETIME(6),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_reports_board FOREIGN KEY (board_id) REFERENCES boards(id),
    CONSTRAINT fk_reports_comment FOREIGN KEY (comment_id) REFERENCES comments(id)
);
