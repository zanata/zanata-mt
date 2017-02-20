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
    UNIQUE (localeId, hash)
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
