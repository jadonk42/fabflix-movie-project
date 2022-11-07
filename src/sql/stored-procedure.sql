DROP PROCEDURE IF EXISTS add_star;
DELIMITER $$

CREATE PROCEDURE add_star(
    IN name VARCHAR(100),
    IN birthYear INT
)
BEGIN
    DECLARE newID VARCHAR(10);

    SET newID = (select concat(left(max(id),2), cast((cast(right(max(id),7) AS UNSIGNED)+1) as char(7)))
    FROM stars);

INSERT INTO stars VALUES(newID, name, birthYear);

END$$
DELIMITER ;




DROP PROCEDURE IF EXISTS add_movie;
DELIMITER $$

CREATE PROCEDURE add_movie(
    IN inTitle VARCHAR(100),
    IN inYear INT,
    IN inDirector VARCHAR(100),
    IN inGenre VARCHAR(100),
    IN inStar VARCHAR(100),
    OUT outResult int
        /*
        0 = nothing existed,
        1 = star and genre existed already,
        2 = genre existed already,
        3 = star existed already
        4 = movie existed already
        */
)
    func: BEGIN
    DECLARE movie_exists TINYINT;
    DECLARE star_exists TINYINT;
    DECLARE genre_exists TINYINT;
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id VARCHAR(10);
    DECLARE newID VARCHAR(10);

SELECT EXISTS(SELECT 1 FROM movies
              WHERE director = inDirector AND year = inYear AND title = inTitle)
INTO movie_exists;
SELECT EXISTS(SELECT 1 FROM stars WHERE name = inStar) INTO star_exists;
SELECT EXISTS(SELECT 1 FROM genres WHERE name = inGenre) INTO genre_exists;

IF movie_exists = 1 THEN
        SET outResult = 4;
SELECT outResult;
LEAVE func;
    ELSEIF star_exists & genre_exists THEN
            SET outResult = 1;

SELECT id INTO star_id FROM stars WHERE name = inStar LIMIT 1;
SELECT id INTO genre_id FROM genres WHERE name = inGenre LIMIT 1;
ELSEIF star_exists THEN
            SET outResult = 3;
INSERT INTO genres(name) VALUES(inGenre);

SELECT max(id) INTO genre_id FROM genres;
SELECT id INTO star_id FROM stars WHERE name = inStar LIMIT 1;
ELSEIF genre_exists THEN
            SET outResult = 2;
CALL add_star(inStar, null);

SELECT max(id) INTO star_id FROM stars;
SELECT id INTO genre_id FROM genres WHERE name = inGenre LIMIT 1;
ELSE
            SET outResult = 0;
CALL add_star(inStar, null);
INSERT INTO genres(name) VALUES(inGenre);

SELECT max(id) INTO star_id FROM stars;
SELECT max(id) INTO genre_id FROM genres;
END IF;

    SET newID = (select concat(left(max(id),3), cast((cast(right(max(id),6) AS UNSIGNED)+1) as char(6)))
    FROM movies);

INSERT INTO movies VALUES(newID, inTitle, inYear, inDirector);
INSERT INTO stars_in_movies VALUES(star_id, newID);
INSERT INTO genres_in_movies VALUES(genre_id, newID);

SELECT outResult;

END$$
DELIMITER ;