-- Adminer 4.7.6 PostgreSQL dump

\connect "gamedb";

DROP TABLE IF EXISTS "player";
CREATE TABLE "public"."player" (
    "player_id" character varying(255) NOT NULL,
    "player_avatar" character varying(255),
    "cluster_source" character varying(255),
    "player_name" character varying(255),
    "guess_right" integer,
    "guess_score" integer,
    "guess_wrong" integer,
    "game_id" character varying(255),
    CONSTRAINT "player_pkey" PRIMARY KEY ("player_id")
) WITH (oids = false);

CREATE INDEX "idx_playerid" ON "public"."player" USING btree ("player_id");

TRUNCATE "player";

-- 2020-02-28 04:05:10.13827+00