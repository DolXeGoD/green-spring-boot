ALTER TABLE boards ADD COLUMN updated_datetime DATETIME(6) NULL;
ALTER TABLE users ADD COLUMN updated_datetime DATETIME(6) NULL;
ALTER TABLE refresh_token MODIFY COLUMN token VARCHAR(1024) NOT NULL;
ALTER TABLE access_token_blacklist MODIFY COLUMN token VARCHAR(1024) NOT NULL;

UPDATE boards SET updated_datetime = created_datetime WHERE updated_datetime IS NULL;
UPDATE users SET updated_datetime = created_datetime WHERE updated_datetime IS NULL;

ALTER TABLE boards ADD FULLTEXT INDEX ft_boards_title (title);
