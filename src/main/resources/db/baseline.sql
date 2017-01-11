CREATE TABLE IF NOT EXISTS Locale (
    id            SERIAL PRIMARY KEY,
    localeId      VARCHAR(255)      NOT NULL,
    name          VARCHAR(255),
    creationDate  DATE              NOT NULL,
    lastChanged   DATE              NOT NULL,
    UNIQUE (localeId)
);

CREATE TABLE IF NOT EXISTS Document (
    id            SERIAL PRIMARY KEY,
    url           TEXT              NOT NULL,
    srcLocaleId      BIGINT         NOT NULL REFERENCES Locale (id),
    targetLocaleId   BIGINT         NOT NULL REFERENCES Locale (id),
    usedCount     INTEGER     DEFAULT 0,
    creationDate  DATE              NOT NULL,
    lastChanged   DATE              NOT NULL,
    UNIQUE (url, srcLocaleId, targetLocaleId)
);

CREATE TABLE IF NOT EXISTS TextFlow (
    id            SERIAL PRIMARY KEY,
    hash          VARCHAR(255)      NOT NULL,
    localeId      BIGINT            NOT NULL REFERENCES Locale (id),
    content       TEXT              NOT NULL,
    creationDate  DATE              NOT NULL,
    lastChanged   DATE              NOT NULL,
    UNIQUE (hash, localeId)
);

CREATE INDEX ON TextFlow (hash);

CREATE TABLE IF NOT EXISTS TextFlowTarget (
    id            SERIAL PRIMARY KEY,
    textFlowId    BIGINT      NOT NULL REFERENCES TextFlow (id),
    localeId      BIGINT      NOT NULL REFERENCES Locale (id),
    content       TEXT        NOT NULL,
    rawContent    TEXT        NOT NULL,
    backendId      VARCHAR(20) NOT NULL,
    usedCount     INTEGER     DEFAULT 0,
    creationDate  DATE        NOT NULL,
    lastChanged  DATE         NOT NULL,
    UNIQUE (textFlowId, localeId, provider)
);

INSERT INTO Locale VALUES (1, 'de', 'German', now(), now());
INSERT INTO Locale VALUES (2, 'en-us', 'English (United States)', now(), now());
INSERT INTO Locale VALUES (3, 'en', 'English', now(), now());
INSERT INTO Locale VALUES (4, 'es', 'Spanish', now(), now());
INSERT INTO Locale VALUES (5, 'fr', 'French', now(), now());
INSERT INTO Locale VALUES (6, 'it', 'Italian', now(), now());
INSERT INTO Locale VALUES (7, 'ja', 'Japanese', now(), now());
INSERT INTO Locale VALUES (8, 'ko', 'Korean', now(), now());
INSERT INTO Locale VALUES (9, 'pt', 'Portuguese', now(), now());
INSERT INTO Locale VALUES (10, 'ru', 'Russian', now(), now());
INSERT INTO Locale VALUES (11, 'zh-cn', 'Chinese (China)', now(), now());
