DO $$
DECLARE
    user_idx INT;
    calendar_idx BIGINT;
    current_day DATE;
    start_date DATE := '2026-07-01';
    end_date DATE := '2026-07-31';
    target_hour INT;
    generated_user_id BIGINT;
BEGIN
    FOR user_idx IN 1..100 LOOP

        INSERT INTO users (name, last_name, email, status)
        VALUES (
            'User' || user_idx,
            'LastName' || user_idx,
            'user' || user_idx || '_' || floor(random() * 1000000)::text || '@example.com',
            'ACTIVE'
        )
        RETURNING id INTO generated_user_id;

        INSERT INTO calendar (timezone, user_id, status)
        VALUES (
            CASE (user_idx % 3)
                WHEN 0 THEN 'UTC'
                WHEN 1 THEN 'America/New_York'
                ELSE 'Europe/Paris'
            END,
            generated_user_id,
            'ACTIVE'
        )
        RETURNING id INTO calendar_idx;

        current_day := start_date;
        WHILE current_day <= end_date LOOP

            FOR target_hour IN 8..17 LOOP

                INSERT INTO slot (calendar_id, start_time, end_time)
                VALUES (
                    calendar_idx,
                    (current_day + make_time(target_hour, 0, 0.0)) AT TIME ZONE 'UTC',
                    (current_day + make_time(target_hour + 1, 0, 0.0)) AT TIME ZONE 'UTC'
                );

            END LOOP;

            current_day := current_day + 1;
        END LOOP;

    END LOOP;

    RAISE NOTICE 'Successfully seeded 100 users, 100 calendars, and associated month-long daytime slots.';
END $$;