-- Adminer 4.7.6 PostgreSQL dump

INSERT INTO "games" ("game_id", "game_config", "game_date", "game_state") VALUES
('3ca8dda4-723a-4771-920e-c8fd0432b6dc',	NULL,	'2020-02-25 13:19:20+00',	'active'),
('3350f95c-2221-4173-8c67-d50f388f0196',	NULL,	'2020-02-21 17:00:59+00',	'active'),
('ce69f40c-9ae8-4028-9e46-4668bf628424',	NULL,	'2020-02-25 13:25:27+00',	'stopped'),
('7e45a986-af25-4e82-aa35-ec43bf6f7150',	NULL,	'2020-02-21 08:12:32+00',	'stopped'),
('ffa93cca-571b-4f07-bb3f-4a3a871997f7',	NULL,	'2020-02-21 08:12:53+00',	'lobby'),
('dc5d0888-f6d5-4c83-bb03-778cedc44284',	NULL,	'2020-02-20 11:54:11+00',	'lobby'),
('d645bbdc-7426-4e9c-850b-141e9ad99500',	NULL,	'2020-02-21 08:15:14+00',	'lobby'),
('7d088731-e277-48b7-ae20-6de7295319c1',	NULL,	'2020-02-23 02:25:31+00',	'active'),
('b7a0a4dd-53f8-4f65-8765-396dcbcba79c',	NULL,	'2020-02-25 18:54:53+00',	'bonus'),
('7dd63503-d666-4db3-a15e-9efd12222cde',	NULL,	'2020-02-23 02:46:49+00',	'active'),
('f6889b6d-dc6d-4865-b615-3f04251a76e9',	NULL,	'2020-02-23 12:24:06+00',	'paused'),
('985ce22b-e150-4951-981f-453781d8d8f9',	NULL,	'2020-02-25 19:01:06+00',	'bonus');

INSERT INTO "players" ("player_id", "player_name", "guess_right", "guess_wrong", "guess_score", "creation_server", "game_server", "scoring_server", "player_avatar", "game_id") VALUES
('New York - Well Shriek',	'Well Shriek',	0,	3,	0,	'New York',	'New York',	'NY',	'"{\"body\":1,\"color\":8,\"ears\":2,\"eyes\":1,\"mouth\":0,\"nose\":0}"',	'dc5d0888-f6d5-4c83-bb03-778cedc44284'),
('London - Cord Crow',	'Cord Crow',	17,	55,	295,	'London',	'London',	'LDN',	'"{\"body\":3,\"color\":4,\"ears\":2,\"eyes\":0,\"mouth\":3,\"nose\":2}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Amber Rabbit',	'Amber Rabbit',	7,	7,	180,	'London',	'London',	'LDN',	'"{\"body\":0,\"color\":1,\"ears\":0,\"eyes\":2,\"mouth\":2,\"nose\":3}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Maze Weasel',	'Maze Weasel',	6,	27,	85,	'London',	'London',	'LDN',	'"{\"body\":2,\"color\":7,\"ears\":2,\"eyes\":2,\"mouth\":2,\"nose\":1}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Hail Lantern',	'Hail Lantern',	18,	14,	455,	'London',	'London',	'LDN',	'"{\"body\":2,\"color\":1,\"ears\":1,\"eyes\":0,\"mouth\":2,\"nose\":0}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Dull Tongue',	'Dull Tongue',	16,	20,	370,	'London',	'London',	'LDN',	'"{\"body\":0,\"color\":5,\"ears\":0,\"eyes\":3,\"mouth\":0,\"nose\":1}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Shell Ox',	'Shell Ox',	4,	8,	65,	'London',	'London',	'LDN',	'"{\"body\":1,\"color\":1,\"ears\":3,\"eyes\":1,\"mouth\":1,\"nose\":3}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Shade Raccoon',	'Shade Raccoon',	2,	4,	0,	'London',	'London',	'LDN',	'"{\"body\":3,\"color\":4,\"ears\":2,\"eyes\":2,\"mouth\":3,\"nose\":0}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Tabby Puppy',	'Tabby Puppy',	13,	8,	360,	'London',	'London',	'LDN',	'"{\"body\":1,\"color\":2,\"ears\":0,\"eyes\":2,\"mouth\":1,\"nose\":0}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Rose Leg',	'Rose Leg',	18,	19,	440,	'London',	'London',	'LDN',	'"{\"body\":2,\"color\":4,\"ears\":2,\"eyes\":1,\"mouth\":0,\"nose\":1}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Legend Wyrm',	'Legend Wyrm',	8,	1,	100,	'London',	'London',	'LDN',	'"{\"body\":1,\"color\":6,\"ears\":2,\"eyes\":0,\"mouth\":2,\"nose\":0}"',	'dc5d0888-f6d5-4c83-bb03-778cedc44284'),
('London - Mica Friend',	'Mica Friend',	0,	2,	0,	'London',	'London',	'LDN',	'"{\"body\":3,\"color\":0,\"ears\":3,\"eyes\":2,\"mouth\":1,\"nose\":2}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Rain Dive',	'Rain Dive',	52,	3,	1395,	'London',	'London',	'LDN',	'"{\"body\":3,\"color\":4,\"ears\":1,\"eyes\":3,\"mouth\":2,\"nose\":0}"',	'dc5d0888-f6d5-4c83-bb03-778cedc44284'),
('London - Dent Dive',	'Dent Dive',	169,	21,	4900,	'London',	'London',	'LDN',	'"{\"body\":1,\"color\":2,\"ears\":0,\"eyes\":1,\"mouth\":1,\"nose\":2}"',	'dc5d0888-f6d5-4c83-bb03-778cedc44284'),
('New York - West Dive',	'West Dive',	0,	7,	0,	'New York',	'New York',	'NY',	'"{\"body\":3,\"color\":4,\"ears\":0,\"eyes\":0,\"mouth\":0,\"nose\":2}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('New York - Wood Dive',	'Wood Dive',	0,	6,	0,	'New York',	'New York',	'NY',	'"{\"body\":3,\"color\":6,\"ears\":2,\"eyes\":2,\"mouth\":2,\"nose\":3}"',	'dc5d0888-f6d5-4c83-bb03-778cedc44284'),
('London - Silk Seer',	'Silk Seer',	6,	2,	195,	'London',	'London',	'LDN',	'"{\"body\":3,\"color\":4,\"ears\":2,\"eyes\":1,\"mouth\":2,\"nose\":3}"',	'3350f95c-2221-4173-8c67-d50f388f0196'),
('London - Shard Tracker',	'Shard Tracker',	3,	0,	100,	'London',	'London',	'LDN',	'"{\"body\":3,\"color\":7,\"ears\":0,\"eyes\":2,\"mouth\":0,\"nose\":1}"',	'3350f95c-2221-4173-8c67-d50f388f0196');

-- 2020-03-26 01:16:56.098588+00