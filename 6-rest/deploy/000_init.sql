CREATE TABLE users
(
    uid    SERIAL       NOT NULL PRIMARY KEY,
    name   VARCHAR(50)  NOT NULL,
    email  VARCHAR(200) NOT NULL,
    avatar VARCHAR(200) NOT NULL,
    sex    VARCHAR(10)  NOT NULL
);

CREATE TABLE stats
(
    uid        INT PRIMARY KEY,
    games      INT NOT NULL DEFAULT 0,
    wins       INT NOT NULL DEFAULT 0,
    loses      INT NOT NULL DEFAULT 0,
    time_spent INT NOT NULL DEFAULT 0
);

CREATE TABLE tasks
(
    task_id SERIAL      NOT NULL PRIMARY KEY,
    uid     INT         NOT NULL,
    status  VARCHAR(10) NOT NULL,
    result  TEXT DEFAULT NULL
);
