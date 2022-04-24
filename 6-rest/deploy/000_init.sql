CREATE TABLE users
(
    uid    SERIAL       NOT NULL PRIMARY KEY,
    name   VARCHAR(50)  NOT NULL,
    email  VARCHAR(200) NOT NULL,
    avatar VARCHAR(200) NOT NULL,
    sex    VARCHAR(10)  NOT NULL
);
