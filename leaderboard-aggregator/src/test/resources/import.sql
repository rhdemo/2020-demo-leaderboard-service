DROP TABLE IF EXISTS "game";
CREATE TABLE "public"."game" (
    "game_id" character varying(255) NOT NULL,
    "game_config" json NOT NULL,
    "game_date" character varying(255),
    "game_state" character varying(10) NOT NULL,
    CONSTRAINT "game_pkey" PRIMARY KEY ("game_id")
) WITH (oids = false);

DROP INDEX IF EXISTS "idx_game_state";
CREATE INDEX "idx_game_state" ON "public"."game" USING btree ("game_state");

DROP TABLE IF EXISTS "player";
-- TODO add Foreign Key on game.game_id
CREATE TABLE "public"."player" (
    "player_id" character varying(255) NOT NULL,
    "player_name" character varying(255) NOT NULL,
    "guess_right" integer NOT NULL,
    "guess_wrong" integer NOT NULL,
    "guess_score" integer NOT NULL,
    "creation_server" character varying(255) NOT NULL,
    "game_server" character varying(255) NOT NULL,
    "scoring_server" character varying(255) NOT NULL,
    "player_avatar" json NOT NULL,
    "game_id" character varying(255) NOT NULL,
    CONSTRAINT "player_pkey" PRIMARY KEY ("player_id")
) WITH (oids = false);

DROP INDEX IF EXISTS "idx_player_id";
CREATE INDEX "idx_player_id" ON "public"."player" USING btree ("player_id");
DROP INDEX IF EXISTS "idx_creation_server";
CREATE INDEX "idx_creation_server" ON "public"."player" USING btree ("creation_server");
DROP INDEX IF EXISTS "idx_game_server";
CREATE INDEX "idx_game_server" ON "public"."player" USING btree ("game_server");
DROP INDEX IF EXISTS "idx_scoring_server";
CREATE INDEX "idx_scoring_server" ON "public"."player" USING btree ("scoring_server");
DROP INDEX IF EXISTS "idx_game_id";
CREATE INDEX "idx_game_id" ON "public"."player" USING btree ("game_id");