ENV_FILE := .env
include ${ENV_FILE}
export $(shell sed 's/=.*//' ${ENV_FILE})
current_dir = $(shell pwd)

.PHONY: start_docker
start_docker:
	./bin/start-docker.sh

format:	
	@mvn -Pall editorconfig:format

install_jars:
	@mvn -Pall -Dquarkus.package.type=jar clean install

run_it:	start_docker	install_jars	
	@mvn -Pit -Dquarkus.package.type=jar verify

start_db:	start_docker
	docker-compose -f leaderboard-test/src/test/resources/postgresql/docker-compose.yaml up -d

start_kafka:	start_docker
	docker-compose -f leaderboard-test/src/test/resources/kafka/docker-compose.yaml -d up -d 

create_db:
	psql -U username -f leaderboard-test/src/test/resources/schema.sql database