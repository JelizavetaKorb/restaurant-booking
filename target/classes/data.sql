CREATE TABLE IF NOT EXISTS restaurant_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    zone VARCHAR(50) NOT NULL,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    width DOUBLE NOT NULL DEFAULT 8.0,
    height DOUBLE NOT NULL DEFAULT 6.0
);

CREATE TABLE IF NOT EXISTS table_tags (
    table_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (table_id, tag),
    FOREIGN KEY (table_id) REFERENCES restaurant_table(id)
);

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL,
    guest_name VARCHAR(255) NOT NULL,
    party_size INTEGER NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    group_id VARCHAR(255),
    FOREIGN KEY (table_id) REFERENCES restaurant_table(id)
);

CREATE TABLE IF NOT EXISTS working_hours (
    id BIGINT PRIMARY KEY,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL
);

INSERT INTO restaurant_table (name, capacity, zone, x, y, width, height) VALUES
  ('T1', 2, 'INDOOR', 15.0, 20.0, 6.0, 4.0),
  ('T2', 4, 'INDOOR', 35.0, 20.0, 8.0, 6.0),
  ('T3', 6, 'INDOOR', 60.0, 20.0, 10.0, 6.0),
  ('T4', 8, 'INDOOR', 10.0, 35.0, 12.0, 8.0),
  ('T5', 2, 'INDOOR', 15.0, 50.0, 6.0, 4.0),
  ('T6', 4, 'INDOOR', 35.0, 50.0, 8.0, 6.0),
  ('T7', 4, 'TERRACE', 20.0, 66.0, 8.0, 6.0),
  ('T8', 6, 'TERRACE', 45.0, 66.0, 10.0, 6.0),
  ('T9', 2, 'TERRACE', 70.0, 66.0, 6.0, 4.0),
  ('T10', 8, 'PRIVATE', 80.0, 50.0, 12.0, 10.0),
  ('T11', 6, 'PRIVATE', 80.0, 30.0, 10.0, 8.0),
  ('T12', 4, 'INDOOR', 60.0, 50.0, 8.0, 6.0);

INSERT INTO table_tags (table_id, tag) VALUES
  (1, 'WINDOW'),
  (2, 'QUIET'),
  (3, 'WINDOW'),
  (4, 'KIDS'),
  (5, 'QUIET'),
  (6, 'KIDS'),
  (7, 'WINDOW'),
  (8, 'WINDOW'),
  (9, 'QUIET'),
  (10, 'QUIET'),
  (11, 'WINDOW'),
  (12, 'KIDS');

INSERT INTO working_hours (id, open_time, close_time) VALUES (1, '10:00:00', '22:00:00');
