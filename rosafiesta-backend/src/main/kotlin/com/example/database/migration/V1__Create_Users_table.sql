CREATE TABLE Users
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(256),
    firstName   VARCHAR(256),
    lastName    VARCHAR(256),
    email       VARCHAR(256),
    phoneNumber VARCHAR(50),
    bornDate    VARCHAR(256),
    created     VARCHAR(50),
    avatar      VARCHAR(5000) NULL,
    password    VARCHAR(512),
    salt        VARCHAR(512)
);