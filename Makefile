ENV_FILE := .env
include ${ENV_FILE}
export $(shell sed 's/=.*//' ${ENV_FILE})
current_dir = $(shell pwd)

.PHONY: start_docker
start_docker:
	@./bin/start-docker.sh

format:	
	@mvn -Pall editorconfig:format

install_jars:
	@mvn -Pall -DskipTests -Dquarkus.package.type=jar clean install

test:
	@mvn -P$(PROFILES) -DskipTests=false -Dquarkus.package.type=jar clean test $(TEST_FILTER)

leaderboard_common:	start_docker
	@mvn -Pcommon -Dquarkus.package.type=jar clean install

leaderboard_aggregator_bp:	start_docker
	@mvn -Paggregator -Dquarkus.package.type=jar clean install
	$(eval _VERSION=$(shell bash -c "xml sel -N ns='http://maven.apache.org/POM/4.0.0' -t -v '//ns:project/ns:version/text()' $(current_dir)/pom.xml"))
	@docker build --build-arg $(MAVEN_MIRROR_URL) -t quay.io/redhatdemo/2020-leaderboard-aggregator:"$(_VERSION)-jar" -f leaderboard-aggregator/Dockerfile.jvm leaderboard-aggregator

leaderboard_aggregator_native_bp:	start_docker
	@mvn -Paggregator -Dquarkus.package.type=native clean install
	$(eval _VERSION=$(shell bash -c "xml sel -N ns='http://maven.apache.org/POM/4.0.0' -t -v '//ns:project/ns:version/text()' $(current_dir)/pom.xml"))
	@docker build --build-arg $(MAVEN_MIRROR_URL) --t quay.io/redhatdemo/2020-leaderboard-aggregator:"$(_VERSION)-native" -f leaderboard-aggregator/Dockerfile.native leaderboard-aggregator
	@docker tag quay.io/redhatdemo/2020-leaderboard-aggregator:"$(_VERSION)-native" quay.io/redhatdemo/2020-leaderboard-aggregator

start_kafka:	start_docker
	docker-compose -f $(current_dir)/kafka/docker-compose.yaml up -d 

start_postgres:	start_docker
	docker-compose -f $(current_dir)/db/docker-compose.yaml up -d 

create_db:
	psql -f $(current_dir)/db/schema.sql gamedb