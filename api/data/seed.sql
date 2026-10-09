-- DevConnect — demo seed data
--
-- Fills the database with 17 users, 18 posts, 16 friendships, likes and
-- comments, enough for the feed and the friends list to span more than one
-- page.
--
-- WARNING: the script DELETES all existing data before inserting. It can be
-- run again at any time and always reproduces the same scenario.
--
-- Prerequisite: the schema must already exist. Start the API once so Flyway
-- applies the migrations.
--
-- How to run (from the repository root):
--   docker compose -f api/data/docker-compose.yml exec -T postgres psql -U devconnect -d devconnect < api/data/seed.sql
--
-- Every account uses the password: password123
-- Log in as marilio@devconnect.com to see the full scenario.

SET client_encoding TO 'UTF8';

BEGIN;

TRUNCATE TABLE post_likes, comments, posts, friendships, user_roles, users RESTART IDENTITY CASCADE;

INSERT INTO users (id, full_name, email, nickname, birth_date, password, profile_image, active) VALUES
(1,  'Marilio Almeida',  'marilio@devconnect.com',          'marilio', '1993-06-12', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', 'https://i.pravatar.cc/150?img=12', TRUE),
(2,  'Ana Souza',        'ana.souza@devconnect.com',        'ana',     '1995-03-15', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', 'https://i.pravatar.cc/150?img=45', TRUE),
(3,  'Bruno Lima',       'bruno.lima@devconnect.com',       'bruno',   '1991-11-02', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', 'https://i.pravatar.cc/150?img=33', TRUE),
(4,  'Carla Dias',       'carla.dias@devconnect.com',       NULL,      '1998-07-28', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(5,  'Diego Martins',    'diego.martins@devconnect.com',    'diego',   '1994-01-09', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(6,  'Elisa Ramos',      'elisa.ramos@devconnect.com',      'lisa',    '1996-09-21', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(7,  'Felipe Nunes',     'felipe.nunes@devconnect.com',     NULL,      '1990-04-30', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(8,  'Gabriela Torres',  'gabriela.torres@devconnect.com',  'gabi',    '1997-12-05', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(9,  'Henrique Barros',  'henrique.barros@devconnect.com',  NULL,      '1992-08-17', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(10, 'Isabela Freitas',  'isabela.freitas@devconnect.com',  'isa',     '1999-02-14', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(11, 'João Pedro Rocha', 'joao.rocha@devconnect.com',       'jp',      '1993-10-23', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(12, 'Karina Mendes',    'karina.mendes@devconnect.com',    NULL,      '1995-05-08', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(13, 'Lucas Ferreira',   'lucas.ferreira@devconnect.com',   'lucas',   '1994-03-19', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(14, 'Mariana Castro',   'mariana.castro@devconnect.com',   NULL,      '1996-11-27', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(15, 'Nathalia Prado',   'nathalia.prado@devconnect.com',   'nath',    '1998-01-31', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(16, 'Otávio Campos',    'otavio.campos@devconnect.com',    NULL,      '1991-06-04', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, TRUE),
(17, 'Paula Ribeiro',    'paula.ribeiro@devconnect.com',    'paula',   '1997-09-13', '$2a$10$HSaywiEHmygsKrK5DDNug.Y6whZX231J.7eQ7Wp2cNwUsr1If39Ka', NULL, FALSE);

INSERT INTO user_roles (name, user_id)
SELECT 'USER', id FROM users;

INSERT INTO friendships (id, requester_id, recipient_id, status, requested_at, responded_at) VALUES
(1,  1,  2,  'ACCEPTED', NOW() - INTERVAL '40 days', NOW() - INTERVAL '39 days'),
(2,  3,  1,  'ACCEPTED', NOW() - INTERVAL '38 days', NOW() - INTERVAL '37 days'),
(3,  1,  4,  'ACCEPTED', NOW() - INTERVAL '35 days', NOW() - INTERVAL '34 days'),
(4,  5,  1,  'ACCEPTED', NOW() - INTERVAL '33 days', NOW() - INTERVAL '30 days'),
(5,  1,  6,  'ACCEPTED', NOW() - INTERVAL '28 days', NOW() - INTERVAL '27 days'),
(6,  7,  1,  'ACCEPTED', NOW() - INTERVAL '26 days', NOW() - INTERVAL '25 days'),
(7,  1,  8,  'ACCEPTED', NOW() - INTERVAL '24 days', NOW() - INTERVAL '22 days'),
(8,  9,  1,  'ACCEPTED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '19 days'),
(9,  1,  10, 'ACCEPTED', NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days'),
(10, 11, 1,  'ACCEPTED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days'),
(11, 1,  12, 'ACCEPTED', NOW() - INTERVAL '12 days', NOW() - INTERVAL '11 days'),
(12, 2,  3,  'ACCEPTED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '29 days'),
(13, 4,  5,  'ACCEPTED', NOW() - INTERVAL '21 days', NOW() - INTERVAL '20 days'),
(14, 13, 1,  'PENDING',  NOW() - INTERVAL '3 days',  NULL),
(15, 14, 1,  'PENDING',  NOW() - INTERVAL '1 day',   NULL),
(16, 1,  15, 'PENDING',  NOW() - INTERVAL '2 days',  NULL);

INSERT INTO posts (id, author_id, content, created_at, visibility) VALUES
(1,  1,  'Started a side project today: a social network for developers, with Spring Boot on the API and Angular on the app. Let''s go.', NOW() - INTERVAL '12 days', 'PUBLIC'),
(2,  2,  'Found out today that chaining Optional.filter makes business rules way more readable than a stack of nested ifs:

```java
return userRepository.findByEmail(email)
    .filter(user -> encoder.matches(password, user.getPassword()))
    .filter(User::isActive)
    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
```

Each filter is one rule. No else, no flag, no temporary variable.', NOW() - INTERVAL '11 days', 'PUBLIC'),
(3,  3,  'Anyone else ever fought time zones with a LocalDate coming from the browser? Lost the whole afternoon on it.', NOW() - INTERVAL '10 days', 'PUBLIC'),
(4,  4,  'JPA study notes: be careful with EAGER fetch on collections, the N+1 shows up when you least expect it.', NOW() - INTERVAL '9 days', 'PRIVATE'),
(5,  5,  'Moved my tests to Testcontainers and finally stopped depending on a local database being up.', NOW() - INTERVAL '8 days', 'PUBLIC'),
(6,  1,  'Wireframes for the app: login, feed, people search, friends and profile. Still going to change a lot.', NOW() - INTERVAL '7 days', 'PRIVATE'),
(7,  6,  'Silly tip that saves time: run the formatter before committing and your diffs stop getting noisy.', NOW() - INTERVAL '6 days', 'PUBLIC'),
(8,  2,  'Studying expression indexes in Postgres. LEAST and GREATEST in a unique index solve the mirrored pair problem:

```sql
CREATE UNIQUE INDEX uk_friendships_pair ON friendships (
    LEAST(requester_id, recipient_id),
    GREATEST(requester_id, recipient_id)
);
```

That way (A, B) and (B, A) count as the same friendship and the database rejects the duplicate on its own.', NOW() - INTERVAL '5 days', 'PRIVATE'),
(9,  7,  'Wrapped up my first week studying Spring Security. The resource server flow finally clicked.', NOW() - INTERVAL '4 days', 'PUBLIC'),
(10, 8,  'Does anyone have good material on Angular signals? I''m migrating a project and want to understand them before changing everything.', NOW() - INTERVAL '3 days', 'PUBLIC'),
(11, 10, 'First deploy of my life is done. It broke twice, went up on the third try, and it feels great.', NOW() - INTERVAL '2 days 12 hours', 'PUBLIC'),
(12, 12, 'This week''s study notebook: pagination in Spring Data, interface projections and native queries.', NOW() - INTERVAL '2 days', 'PRIVATE'),
(13, 3,  'Time zone issue solved: it was an implicit conversion to UTC when building the date. Thanks to everyone who replied.', NOW() - INTERVAL '1 day 8 hours', 'PUBLIC'),
(14, 1,  'API is done: every core feature in place and 214 tests passing, 31 of them integration tests running against a real Postgres.', NOW() - INTERVAL '1 day', 'PUBLIC'),
(15, 4,  'First time using Flyway on a personal project. Wrote `V1__create_users_and_roles.sql`, started the app and the schema showed up versioned. Not going back to loose scripts in a folder.', NOW() - INTERVAL '20 hours', 'PUBLIC'),
(16, 13, 'Looking for a study buddy for algorithms twice a week. Anyone in?', NOW() - INTERVAL '16 hours', 'PUBLIC'),
(17, 14, 'Started learning Java this month coming from Python. Static typing is scary at first and helpful later.', NOW() - INTERVAL '10 hours', 'PUBLIC'),
(18, 16, 'Posting here just to try out the network. Hi, everyone.', NOW() - INTERVAL '4 hours', 'PUBLIC');

INSERT INTO post_likes (post_id, user_id, created_at) VALUES
(1, 2, NOW() - INTERVAL '11 days 20 hours'),
(1, 3, NOW() - INTERVAL '11 days 18 hours'),
(1, 4, NOW() - INTERVAL '11 days 10 hours'),
(1, 5, NOW() - INTERVAL '10 days'),
(2, 1, NOW() - INTERVAL '10 days 22 hours'),
(2, 3, NOW() - INTERVAL '10 days 20 hours'),
(3, 1, NOW() - INTERVAL '9 days 20 hours'),
(3, 2, NOW() - INTERVAL '9 days 18 hours'),
(3, 4, NOW() - INTERVAL '9 days'),
(4, 1, NOW() - INTERVAL '8 days 20 hours'),
(4, 5, NOW() - INTERVAL '8 days 12 hours'),
(5, 1, NOW() - INTERVAL '7 days 18 hours'),
(6, 2, NOW() - INTERVAL '6 days 20 hours'),
(6, 3, NOW() - INTERVAL '6 days 15 hours'),
(7, 1, NOW() - INTERVAL '5 days 20 hours'),
(9, 1, NOW() - INTERVAL '3 days 18 hours'),
(9, 8, NOW() - INTERVAL '3 days 10 hours'),
(10, 1, NOW() - INTERVAL '2 days 20 hours'),
(11, 1, NOW() - INTERVAL '2 days'),
(11, 2, NOW() - INTERVAL '1 day 20 hours'),
(13, 1, NOW() - INTERVAL '1 day 2 hours'),
(14, 2, NOW() - INTERVAL '22 hours'),
(14, 6, NOW() - INTERVAL '18 hours'),
(14, 10, NOW() - INTERVAL '12 hours');

INSERT INTO comments (post_id, author_id, content, created_at) VALUES
(1, 2,  'Nice, Marilio! Ping me if you have any Angular questions.', NOW() - INTERVAL '11 days 19 hours'),
(1, 3,  'Great idea for a project. Good luck!', NOW() - INTERVAL '11 days 15 hours'),
(1, 4,  'You''ve got this. Just don''t leave the tests for the end.', NOW() - INTERVAL '11 days'),
(2, 1,  'Agreed. Once I understood Optional, my services shrank by half.', NOW() - INTERVAL '10 days 21 hours'),
(2, 3,  'Just be careful not to chain too much and make it unreadable.', NOW() - INTERVAL '10 days 18 hours'),
(3, 1,  'I lost a day on that once. Check whether the browser is sending the date in UTC.', NOW() - INTERVAL '9 days 22 hours'),
(3, 2,  'Build the date with getFullYear, getMonth and getDate and it goes away.', NOW() - INTERVAL '9 days 16 hours'),
(4, 1,  'That N+1 got me too. BatchSize helped a lot here.', NOW() - INTERVAL '8 days 18 hours'),
(6, 2,  'I like the flow. Keeping people search separate from friends makes it clear.', NOW() - INTERVAL '6 days 18 hours'),
(6, 3,  'Suggestion: make the friend''s name clickable to open their profile.', NOW() - INTERVAL '6 days 10 hours'),
(9, 1,  'What confused me the most was the token scope. After that it all made sense.', NOW() - INTERVAL '3 days 16 hours'),
(11, 1, 'Congrats! The first deploy is always the hardest.', NOW() - INTERVAL '2 days 6 hours'),
(11, 2, 'Nice, Isabela! Tell us how it went.', NOW() - INTERVAL '1 day 22 hours'),
(14, 2, 'Thirty-one integration tests is a lot. Great job.', NOW() - INTERVAL '20 hours'),
(14, 10, 'I''m going to look at this project for ideas for mine.', NOW() - INTERVAL '10 hours');

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('user_roles', 'id'), (SELECT MAX(id) FROM user_roles));
SELECT setval(pg_get_serial_sequence('posts', 'id'), (SELECT MAX(id) FROM posts));
SELECT setval(pg_get_serial_sequence('comments', 'id'), (SELECT MAX(id) FROM comments));
SELECT setval(pg_get_serial_sequence('post_likes', 'id'), (SELECT MAX(id) FROM post_likes));
SELECT setval(pg_get_serial_sequence('friendships', 'id'), (SELECT MAX(id) FROM friendships));

COMMIT;
