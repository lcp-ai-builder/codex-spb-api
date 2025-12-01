-- Rename and convert status fields to is_open flags (0=off, 1=on)

-- Operators table
ALTER TABLE operators
    ADD COLUMN is_open TINYINT(1) NOT NULL DEFAULT 0 AFTER role_id;

UPDATE operators
SET is_open = CASE
    WHEN LOWER(status) IN ('enabled', 'enable', 'open', '1') THEN 1
    ELSE 0
END;

ALTER TABLE operators
    DROP COLUMN status;

-- Rules table
ALTER TABLE rules
    ADD COLUMN is_open TINYINT(1) NOT NULL DEFAULT 0 AFTER description;

UPDATE rules
SET is_open = CASE
    WHEN LOWER(status) IN ('enabled', 'enable', 'open', '1') THEN 1
    ELSE 0
END;

ALTER TABLE rules
    DROP COLUMN status;
