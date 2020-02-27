CREATE TABLE public.player (
    playerid character varying(255) NOT NULL,
    avatar character varying(255),
    clustersource character varying(255),
    playername character varying(255),
    guess_right integer,
    guess_score integer,
    guess_wrong integer,
    game_id character varying(255)
);

ALTER TABLE ONLY public.player
    ADD CONSTRAINT player_pkey PRIMARY KEY (playerid);

-- Index: idx_playerid

DROP INDEX public.idx_playerid;

CREATE INDEX idx_playerid
    ON public.player USING btree
    (playerid COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;