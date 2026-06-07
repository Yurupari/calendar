CREATE TYPE slot_status AS ENUM ('FREE', 'BUSY');
CREATE TYPE participant_role AS ENUM ('HOST', 'INVITEE');

CREATE TABLE slot (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    calendar_id BIGINT,
    meeting_id BIGINT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status slot_status NOT NULL DEFAULT 'FREE',
    role participant_role,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_slot_calendar
        FOREIGN KEY (calendar_id) REFERENCES calendar (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_slot_meeting
        FOREIGN KEY (meeting_id) REFERENCES meeting (id)
        ON DELETE SET NULL,
    CONSTRAINT chk_slot_duration
        CHECK (end_time > start_time)
);

CREATE INDEX idx_slots_calendar_range
    ON slot (calendar_id, start_time, end_time);
CREATE INDEX idx_slot_meeting ON slots (meeting_id);