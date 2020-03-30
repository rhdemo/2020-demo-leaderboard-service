ENV_FILE := .env
include ${ENV_FILE}
export $(shell sed 's/=.*//' ${ENV_FILE})
CURRENT_DIR = $(shell pwd)
IMAGE_REPO := 'quay.io/rhdevelopers'
APP_VERSION := $(shell bash -c "xml sel -N ns='http://maven.apache.org/POM/4.0.0' -t -v '//ns:project/ns:version/text()' $(CURRENT_DIR)/pom.xml")
NATIVE_BULID_ARGS = -DskipTests -Dquarkus.native.native-image-xmx=6G -Dquarkus.native.container-build=true -Dquarkus.package.type=native package

.PHONY: start_docker
start_docker:
	@./bin/start-docker.sh

format:	
	@mvn -Pall editorconfig:format

install_jars:
	@mvn -Pall -DskipTests -Dquarkus.package.type=jar clean install

test:
	mvn -P$(PROFILES) -DfailIfNoTests=false -DskipTests=false -Dquarkus.package.type=jar clean test -Dtest=$(TEST_FILTER)

native_test:
	mvn -P$(PROFILES) -DfailIfNoTests=false -Dquarkus.native.native-image-xmx=6G  -DskipTests=false -Dquarkus.package.type=native verify -Dtest=$(TEST_FILTER)

clean:
	@mvn -Pall clean 

common:	start_docker
	@mvn -Pcommon -Dquarkus.package.type=jar clean install

leaderboard-aggregator/target/*-runner.jar:	
	@mvn -Paggregator -DskipTests clean install

aggregator_build:	leaderboard-aggregator/target/*-runner.jar

leaderboard-aggregator/target/*-runner:	aggregator_build
	@mvn -Paggregator,native $(NATIVE_BULID_ARGS)

aggregator_jvm_image_build_push:	start_docker	aggregator_build	
	@docker build -t $(IMAGE_REPO)/2020-leaderboard-aggregator:"$(APP_VERSION)-jar" -f leaderboard-aggregator/Dockerfile.jvm leaderboard-aggregator
	@docker push $(IMAGE_REPO)/2020-leaderboard-aggregator:"$(APP_VERSION)-jar"

aggregator-native:	leaderboard-aggregator/target/*-runner

aggregator_native_image_build_push:	start_docker	aggregator-native	
	@docker build -t $(IMAGE_REPO)/2020-leaderboard-aggregator:"$(APP_VERSION)-native" -f leaderboard-aggregator/Dockerfile.native leaderboard-aggregator
	@docker tag $(IMAGE_REPO)/2020-leaderboard-aggregator:"$(APP_VERSION)-native" $(IMAGE_REPO)/2020-leaderboard-aggregator
	@docker push $(IMAGE_REPO)/2020-leaderboard-aggregator:"$(APP_VERSION)-native"	
	@docker push $(IMAGE_REPO)/2020-leaderboard-aggregator

aggregator_build_all:	aggregator_build	aggregator_native_image_build_push	aggregator_jvm_image_build_push

start_kafka:	start_docker
	docker-compose -f $(CURRENT_DIR)/kafka/docker-compose.yaml up -d 

start_postgres:	start_docker
	docker-compose -f $(CURRENT_DIR)/db/docker-compose.yaml up -d 

create_db:
	psql -f $(CURRENT_DIR)/db/schema.sql gamedb