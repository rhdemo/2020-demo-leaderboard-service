DROP TABLE IF EXISTS "player";
CREATE TABLE "public"."player" (
    "player_id" character varying(255) NOT NULL,
    "player_avatar" json NOT NULL,
    "cluster_source" character varying(255) NOT NULL,
    "player_name" character varying(255) NOT NULL,
    "guess_right" integer NOT NULL,
    "guess_score" integer NOT NULL,
    "guess_wrong" integer NOT NULL,
    "game_id" character varying(255) NOT NULL,
    CONSTRAINT "player_pkey" PRIMARY KEY ("player_id")
) WITH (oids = false);

DROP INDEX IF EXISTS "idx_player_id";
CREATE INDEX "idx_player_id" ON "public"."player" USING btree ("player_id");
DROP INDEX IF EXISTS "idx_cluster_source";
CREATE INDEX "idx_cluster_source" ON "public"."player" USING btree ("cluster_source");
DROP INDEX IF EXISTS "idx_game_id";
CREATE INDEX "idx_game_id" ON "public"."player" USING btree ("game_id");