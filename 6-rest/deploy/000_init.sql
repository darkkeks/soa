CREATE TABLE users
(
    uid    SERIAL       NOT NULL PRIMARY KEY,
    name   VARCHAR(50)  NOT NULL,
    email  VARCHAR(200) NOT NULL,
    avatar VARCHAR(200) NOT NULL,
    sex    VARCHAR(10)  NOT NULL
);

CREATE TABLE user_stats
(
    uid        INT PRIMARY KEY,
    games      INT NOT NULL DEFAULT 0,
    wins       INT NOT NULL DEFAULT 0,
    loses      INT NOT NULL DEFAULT 0,
    time_spent INT NOT NULL DEFAULT 0
);

CREATE TABLE games
(
    id     SERIAL PRIMARY KEY,
    status VARCHAR(10) NOT NULL
);

ALTER TABLE games
    ALTER COLUMN status TYPE VARCHAR(15);

CREATE TABLE game_players
(
    id      SERIAL PRIMARY KEY,
    game_id INT         NOT NULL,
    uid     INT         NOT NULL,
    role    VARCHAR(10) NOT NULL,
    score   INT         NOT NULL
);

CREATE TABLE game_comments
(
    id          SERIAL PRIMARY KEY,
    game_id     INT         NOT NULL,
    author      VARCHAR(50) NOT NULL,
    content     TEXT        NOT NULL,
    create_time TIMESTAMP   NOT NULL
);

CREATE TABLE tasks
(
    task_id SERIAL      NOT NULL PRIMARY KEY,
    uid     INT         NOT NULL,
    status  VARCHAR(10) NOT NULL,
    result  TEXT DEFAULT NULL
);
