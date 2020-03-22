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