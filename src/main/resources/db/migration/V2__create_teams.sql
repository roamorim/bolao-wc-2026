CREATE TABLE teams (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    code       VARCHAR(3)   NOT NULL UNIQUE,
    group_name VARCHAR(1),
    flag_url   VARCHAR(500)
);
