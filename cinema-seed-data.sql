-- Cinema project demo seed data
-- Purpose: lets a brand-new, empty database show content on the home page / movie list / seat
-- selection page, for demo purposes.
-- How to run: mysql -u cinema_app -p cinema < cinema-seed-data.sql
-- Note: run this file once, straight through in order (the showtimes section below references
-- the auto-generated IDs 1, 2, 3... from the inserts above it).

-- ---- Cinemas ----
INSERT INTO cinemas (name, location) VALUES
('Xinyi Vieshow Cinemas', 'No. 20, Songshou Rd., Xinyi Dist., Taipei City'),
('Banqiao Global Mall Vieshow Cinemas', 'No. 28, Xinzhan Rd., Banqiao Dist., New Taipei City');

-- ---- Halls (price, hall type, hall number) ----
INSERT INTO halls (price, hall_type, hall_number) VALUES
(280, '2D Hall', 1),
(320, 'IMAX Hall', 2),
(280, '2D Hall', 3);

-- ---- Showdates (today, tomorrow, day after tomorrow) ----
INSERT INTO showdates (show_date) VALUES
(CURDATE()),
(CURDATE() + INTERVAL 1 DAY),
(CURDATE() + INTERVAL 2 DAY);

-- ---- Movies ----
-- The img column points to a static/img file bundled with the project; Spring Boot
-- automatically serves it at http://localhost:8443/movie/img/<filename>.
-- status is a TRUE/FALSE enum string, not a boolean.
INSERT INTO movies (title, description, duration, released_date, img, genre, director, actor, `status`, created_time) VALUES
('Deadpool & Wolverine', 'Deadpool and Wolverine team up to fight a multiversal threat.', 128, '2024-07-24', 'http://localhost:8443/movie/img/DEADPOOL.jpg', 'Action/Comedy', 'Shawn Levy', 'Ryan Reynolds, Hugh Jackman', 'TRUE', NOW()),
('Alien: Romulus', 'A group of young space colonizers encounter the Alien aboard a derelict space station.', 119, '2024-08-14', 'http://localhost:8443/movie/img/alien-romulus.jpg', 'Sci-Fi/Horror', 'Fede Alvarez', 'Cailee Spaeny', 'TRUE', NOW()),
('Gatao: Dajiaotou', 'The latest chapter in the Gatao series, as old rivalries in the underworld resurface.', 122, '2024-01-01', 'http://localhost:8443/movie/img/gatao-dajiaotou.jpeg', 'Drama/Action', 'Masa', 'Zheng Renshuo, Wang Shixian', 'TRUE', NOW()),
('Look Back', 'The story of two girls who meet through manga and grow up together.', 58, '2024-09-06', 'http://localhost:8443/movie/img/look-back.jpg', 'Animation/Drama', 'Kiyotaka Oshiyama', 'Voice Cast', 'TRUE', NOW()),
('Howl''s Moving Castle', 'A fantastical adventure and love story that transcends time and space.', 119, '2004-11-20', 'http://localhost:8443/movie/img/howls-moving-castle.avif', 'Animation/Fantasy', 'Hayao Miyazaki', 'Chieko Baisho, Takuya Kimura', 'FALSE', NOW());

-- ---- News/Announcements ----
INSERT INTO news (img, `text`, created_time) VALUES
('http://localhost:8443/movie/img/popcorn.jpg', 'Mid-Autumn Festival long weekend special: buy one get one free popcorn, now through Sep 20.', NOW()),
('http://localhost:8443/movie/img/BTS.webp', 'Special screening announcement for the BTS concert film -- stay tuned.', NOW());

-- ---- Showtimes ----
-- References the rows created above in order: movie_id 1-5, cinema_id 1-2, hall_id 1-3, showdate_id 1-3
INSERT INTO showtimes (fk_movie_id, fk_cinema_id, fk_hall_id, fk_show_date_id, show_time) VALUES
(1, 1, 1, 1, '13:00:00'),  -- Deadpool & Wolverine @ Xinyi Vieshow, 2D Hall, today 13:00
(1, 1, 2, 1, '19:30:00'),  -- Deadpool & Wolverine @ Xinyi Vieshow, IMAX Hall, today 19:30
(2, 2, 3, 1, '15:00:00'),  -- Alien: Romulus @ Banqiao Global Mall, 2D Hall, today 15:00
(3, 1, 1, 2, '20:00:00'),  -- Gatao: Dajiaotou @ Xinyi Vieshow, 2D Hall, tomorrow 20:00
(4, 2, 2, 2, '12:30:00');  -- Look Back @ Banqiao Global Mall, IMAX Hall, tomorrow 12:30
