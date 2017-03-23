CREATE TABLE IF NOT EXISTS Locale (
    id            SERIAL PRIMARY KEY,
    localeId      VARCHAR(128)      NOT NULL,
    name          VARCHAR(191),
    creationDate  DATE              NOT NULL,
    lastChanged   DATE              NOT NULL,
    UNIQUE (localeId)
);

CREATE TABLE IF NOT EXISTS Document (
    id            SERIAL PRIMARY KEY,
    url           TEXT          NOT NULL,
    srcLocaleId      BIGINT         NOT NULL REFERENCES Locale (id),
    targetLocaleId   BIGINT         NOT NULL REFERENCES Locale (id),
    usedCount     INTEGER     DEFAULT 0,
    creationDate  DATE              NOT NULL,
    lastChanged   DATE              NOT NULL,
    urlHash       VARCHAR(128)      NOT NULL,
    UNIQUE (urlHash, srcLocaleId, targetLocaleId)
);

CREATE TABLE IF NOT EXISTS TextFlow (
    id            SERIAL PRIMARY KEY,
    contentHash   VARCHAR(128)      NOT NULL,
    localeId      BIGINT            NOT NULL REFERENCES Locale (id),
    content       TEXT              NOT NULL,
    creationDate  DATE              NOT NULL,
    lastChanged   DATE              NOT NULL,
    UNIQUE (localeId, contentHash)
);

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
    UNIQUE (localeId, textFlowId, backendId)
);
