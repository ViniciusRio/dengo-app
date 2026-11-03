-- Revisão global: toda escrita bloqueia esta linha até o commit.
CREATE TABLE sync_counter (
    id    SMALLINT PRIMARY KEY CHECK (id = 1),
    value BIGINT NOT NULL
);
INSERT INTO sync_counter (id, value) VALUES (1, 0);

CREATE TABLE device_session (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person     TEXT NOT NULL CHECK (person IN ('LIDIANNE', 'VINICIUS')),
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX one_active_session_per_person ON device_session (person) WHERE revoked_at IS NULL;

CREATE TABLE pairing_code (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person         TEXT NOT NULL CHECK (person IN ('LIDIANNE', 'VINICIUS')),
    code_hash      TEXT NOT NULL UNIQUE,
    expires_at     TIMESTAMPTZ NOT NULL,
    used_at        TIMESTAMPTZ,
    invalidated_at TIMESTAMPTZ
);

-- Contador único: serializa as tentativas de vínculo e sobrevive a reinícios.
CREATE TABLE pairing_throttle (
    id              SMALLINT PRIMARY KEY CHECK (id = 1),
    failed_attempts INT NOT NULL,
    locked_until    TIMESTAMPTZ
);
INSERT INTO pairing_throttle (id, failed_attempts) VALUES (1, 0);

CREATE TABLE care_request (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type         TEXT NOT NULL,
    requester_id TEXT NOT NULL,
    recipient_id TEXT NOT NULL,
    status       TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    message      TEXT,
    revision     BIGINT NOT NULL
);
CREATE INDEX care_request_revision ON care_request (revision);

CREATE TABLE history_event (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type        TEXT NOT NULL,
    actor_id    TEXT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    request_id  BIGINT REFERENCES care_request (id),
    mood_option TEXT,
    revision    BIGINT NOT NULL
);
CREATE INDEX history_event_revision ON history_event (revision);

CREATE TABLE mood (
    partner_id TEXT PRIMARY KEY,
    option     TEXT NOT NULL,
    mood_date  DATE NOT NULL,
    revision   BIGINT NOT NULL
);

-- Linha única; revisão 0 significa estado inicial, que a sincronização não precisa entregar.
CREATE TABLE personal_space (
    id           SMALLINT PRIMARY KEY CHECK (id = 1),
    status       TEXT NOT NULL,
    activated_at TIMESTAMPTZ,
    revision     BIGINT NOT NULL
);
INSERT INTO personal_space (id, status, revision) VALUES (1, 'INACTIVE', 0);

-- Segunda defesa da regra de duplicidade, além da serialização das escritas.
CREATE UNIQUE INDEX one_active_request_per_predefined_type ON care_request (type)
    WHERE type <> 'OTHER' AND status IN ('PENDING', 'ACCEPTED');

-- Chaves sem prazo: uma tentativa incerta pode ficar pendente por tempo indeterminado.
-- A linha só fica visível no commit da escrita, e a unicidade faz outro envio esperar por ele.
CREATE TABLE operation (
    person        TEXT NOT NULL,
    op_key        TEXT NOT NULL,
    state         TEXT NOT NULL CHECK (state IN ('COMPLETED', 'CANCELLED')),
    fingerprint   TEXT,
    http_status   INT,
    response_body TEXT,
    revision      BIGINT,
    PRIMARY KEY (person, op_key)
);
