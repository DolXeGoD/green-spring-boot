CREATE DATABASE IF NOT EXISTS green_board
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE green_board;

CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40) NOT NULL,
    password VARCHAR(500) NOT NULL,
    name VARCHAR(30),
    email VARCHAR(100) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    status ENUM('QUITTED', 'BANNED', 'BLOCKED', 'ACTIVE', 'PENDING') NOT NULL DEFAULT 'ACTIVE',
    created_datetime DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_datetime DATETIME(6),
    unblock_datetime DATETIME(6),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE boards (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    hits INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    author INT NOT NULL,
    board_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    created_datetime DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_datetime DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_boards_author FOREIGN KEY (author) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FULLTEXT KEY ft_boards_title (title) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE likes (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    board_id INT NOT NULL,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_likes_board FOREIGN KEY (board_id) REFERENCES boards(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    author_id INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    created_datetime DATETIME(6),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_comments_board FOREIGN KEY (board_id) REFERENCES boards(id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_token (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(1024) NOT NULL,
    expiration_datetime DATETIME(6) NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE access_token_blacklist (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(1024) NOT NULL,
    expiration_datetime DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE verification_code (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    code VARCHAR(10) NOT NULL,
    expiration_datetime DATETIME(6) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_verification_code_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
