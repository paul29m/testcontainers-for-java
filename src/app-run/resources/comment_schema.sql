CREATE TABLE IF NOT EXISTS comments (
    ticketId INT,
    commentText VARCHAR(255),
    userId INT
);

CREATE TABLE IF NOT EXISTS users (
    id INT,
    name VARCHAR(255),
    role VARCHAR(255)
);

INSERT
INTO users (id, name, role)
VALUES (1,'User from script', 'customer')
ON CONFLICT do nothing;