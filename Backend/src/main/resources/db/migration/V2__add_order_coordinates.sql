ALTER TABLE delivery_requests
    ADD COLUMN pickup_latitude NUMERIC(10,7),
    ADD COLUMN pickup_longitude NUMERIC(10,7),
    ADD COLUMN delivery_latitude NUMERIC(10,7),
    ADD COLUMN delivery_longitude NUMERIC(10,7);

-- Backfill an existing demo database before applying NOT NULL constraints.
UPDATE delivery_requests
SET pickup_latitude = 10.7768890,
    pickup_longitude = 106.7008060,
    delivery_latitude = 10.7826810,
    delivery_longitude = 106.6957540
WHERE pickup_latitude IS NULL;

ALTER TABLE delivery_requests
    ALTER COLUMN pickup_latitude SET NOT NULL,
    ALTER COLUMN pickup_longitude SET NOT NULL,
    ALTER COLUMN delivery_latitude SET NOT NULL,
    ALTER COLUMN delivery_longitude SET NOT NULL,
    ADD CONSTRAINT chk_pickup_latitude CHECK (pickup_latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_pickup_longitude CHECK (pickup_longitude BETWEEN -180 AND 180),
    ADD CONSTRAINT chk_delivery_latitude CHECK (delivery_latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_delivery_longitude CHECK (delivery_longitude BETWEEN -180 AND 180);
