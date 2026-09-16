-- Cinema 專案 Demo 種子資料
-- 用途：讓全新、空白的資料庫也能在首頁/電影列表/劃位頁面看到內容，方便展示。
-- 執行方式：mysql -u cinema_app -p cinema < cinema-seed-data.sql
-- 注意：請整份從頭到尾照順序執行一次（後面 showtimes 直接用 1,2,3... 對應前面新增資料的自動編號 ID）。

-- ---- 影城 ----
INSERT INTO cinemas (name, location) VALUES
('信義威秀影城', '台北市信義區松壽路20號'),
('板橋大遠百威秀影城', '新北市板橋區新站路28號');

-- ---- 廳別（價格、廳型、廳號）----
INSERT INTO halls (price, hall_type, hall_number) VALUES
(280, '2D 廳', 1),
(320, 'IMAX 廳', 2),
(280, '2D 廳', 3);

-- ---- 場次日期（今天、明天、後天）----
INSERT INTO showdates (show_date) VALUES
(CURDATE()),
(CURDATE() + INTERVAL 1 DAY),
(CURDATE() + INTERVAL 2 DAY);

-- ---- 電影 ----
-- img 欄位指向專案內建的 static/img 圖檔，Spring Boot 會自動用
-- http://localhost:8443/movie/img/<檔名> 這個網址提供出去。
-- status 是 TRUE/FALSE 的列舉字串，不是布林值。
INSERT INTO movies (title, description, duration, released_date, img, genre, director, actor, `status`, created_time) VALUES
('死侍與金鋼狼', '死侍與金鋼狼聯手對抗多重宇宙的威脅。', 128, '2024-07-24', 'http://localhost:8443/movie/img/DEADPOOL.jpg', '動作/喜劇', 'Shawn Levy', '雷恩·雷諾斯, 休·傑克曼', 'TRUE', NOW()),
('異形：羅穆路斯', '一群年輕拓荒者在廢棄太空站遭遇異形。', 119, '2024-08-14', 'http://localhost:8443/movie/img/異形.jpg', '科幻/恐怖', 'Fede Alvarez', '凱莉·史派妮', 'TRUE', NOW()),
('角頭：大橋頭', '角頭系列最新篇章，江湖恩怨再起。', 122, '2024-01-01', 'http://localhost:8443/movie/img/角頭.jpeg', '劇情/動作', '瑪莎', '鄭人碩, 王識賢', 'TRUE', NOW()),
('驀然回首', '兩位少女因漫畫而相遇、成長的故事。', 58, '2024-09-06', 'http://localhost:8443/movie/img/驀然回首.jpg', '動畫/劇情', '押山清高', '配音陣容', 'TRUE', NOW()),
('霍爾的移動城堡', '一段跨越時空的奇幻冒險與愛情故事。', 119, '2004-11-20', 'http://localhost:8443/movie/img/移動城堡.avif', '動畫/奇幻', '宮崎駿', '倍賞千惠子, 木村拓哉', 'FALSE', NOW());

-- ---- 新聞/公告 ----
INSERT INTO news (img, `text`, created_time) VALUES
('http://localhost:8443/movie/img/爆米花.jpg', '中秋連假限定套餐：爆米花買一送一，即日起至9/20。', NOW()),
('http://localhost:8443/movie/img/BTS.webp', 'BTS 演唱會電影特別上映場次公告，敬請期待。', NOW());

-- ---- 場次 ----
-- 對應上面依序建立的資料：movie_id 1~5、cinema_id 1~2、hall_id 1~3、showdate_id 1~3
INSERT INTO showtimes (fk_movie_id, fk_cinema_id, fk_hall_id, fk_show_date_id, show_time) VALUES
(1, 1, 1, 1, '13:00:00'),  -- 死侍與金鋼狼 @ 信義威秀 2D廳 今天 13:00
(1, 1, 2, 1, '19:30:00'),  -- 死侍與金鋼狼 @ 信義威秀 IMAX廳 今天 19:30
(2, 2, 3, 1, '15:00:00'),  -- 異形 @ 板橋大遠百 2D廳 今天 15:00
(3, 1, 1, 2, '20:00:00'),  -- 角頭 @ 信義威秀 2D廳 明天 20:00
(4, 2, 2, 2, '12:30:00');  -- 驀然回首 @ 板橋大遠百 IMAX廳 明天 12:30
