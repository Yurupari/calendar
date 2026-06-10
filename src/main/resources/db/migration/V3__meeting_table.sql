CREATE TYPE meeting_status AS ENUM ('SCHEDULED', 'CANCELLED');

CREATE TABLE meeting (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    host_id BIGINT,
    title VARCHAR(50) NOT NULL,
    description TEXT,
    status meeting_status NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meeting_user
        FOREIGN KEY (host_id) REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_meeting_host ON meeting (host_id);