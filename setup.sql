CREATE DATABASE watchlist_db;

USE watchlist_db;

CREATE TABLE shows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL UNIQUE,
    episode INT DEFAULT 0
);
