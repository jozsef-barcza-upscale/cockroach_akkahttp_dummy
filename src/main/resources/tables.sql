CREATE USER 'testuser'@'127.0.0.1' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON * . * TO 'testuser'@'127.0.0.1';
CREATE DATABASE bank;


CREATE TABLE IF NOT EXISTS bank.accounts (
    id INT PRIMARY KEY,
    balance DECIMAL
);