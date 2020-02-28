ENV_FILE := .env
include ${ENV_FILE}
export $(shell sed 's/=.*//' ${ENV_FILE})
current_dir = $(shell pwd)

.PHONY: oc_login
oc_login:
	@oc login ${OC_URL} -u ${OC_USER} -p ${OC_PASSWORD} --insecure-skip-tls-verify=true
	@oc new-project leaderboard || oc project leaderboard || true

.PHONY:	import_openjdk8_is
import_openjdk8_is:	oc_login
	@oc import-image redhat-openjdk-18/openjdk18-openshift \
	  --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift

.PHONY: create-leaderboard-aggregator-bc
create-leaderboard-aggregator-bc:	oc_login
	@oc new-build --name=leaderboard-aggregator openjdk18-openshift~. \
	  --env=MAVEN_MIRROR_URL=$(MAVEN_MIRROR_URL) \
		--context-dir=leaderboard-aggregator

.PHONY: build-leaderboard-aggregator
build-leaderboard-aggregator:	oc_login
	@oc start-build leaderboard-aggregator \
	  --from-dir="$(current_dir)"
	@oc logs -f bc/leaderboard-aggregator

.PHONY: create-app-leaderboard-aggregator
create-app-leaderboard-aggregator:	oc_login
	@oc new-app --name leaderboard-aggregator \
	  --image-stream=leaderboard-aggregator \
		--labels app.kubernetes.io/part-of=leaderboard
	@oc expose svc/leaderboard-aggregator
		
.PHONY:	deploy-leaderboard
deploy-leaderboard-aggregator:	oc_login	create-leaderboard-aggregator-bc	build-leaderboard-aggregator	create-app-leaderboard-aggregator

.PHONY:	clean-leaderboard-aggregator
clean-leaderboard-aggregator:	 oc_login	
	oc delete bc/leaderboard-aggregator 
	oc delete dc/leaderboard-aggregator 
	oc delete svc/leaderboard-aggregator
	oc delete is/leaderboard-aggregator

# Leaderboard mock producer
.PHONY: create-mock-producer-bc
create-mock-producer-bc:	oc_login
	@oc new-build --name=leaderboard-mock-producer openjdk18-openshift~. \
	  --env=MAVEN_MIRROR_URL=$(MAVEN_MIRROR_URL) \
		--context-dir=leaderboard-mock-producer

.PHONY: build-mock-producer
build-mock-producer:	oc_login
	@oc start-build leaderboard-mock-producer \
	  --from-dir="$(current_dir)"
	@oc logs -f bc/leaderboard-mock-producer

.PHONY: create-app-mock-producer
create-app-mock-producer:	oc_login
	@oc new-app --name leaderboard-mock-producer \
	  --image-stream=leaderboard-mock-producer \
		--labels app.kubernetes.io/part-of=leaderboard
		
.PHONY:	deploy-mock-producer
deploy-mock-producer:	 oc_login	create-mock-producer-bc	build-mock-producer	create-app-mock-producer

.PHONY:	clean-mock-producer
clean-mock-producer:	 oc_login	
	oc delete bc/leaderboard-mock-producer 
	oc delete dc/leaderboard-mock-producer 
	oc delete svc/leaderboard-mock-producer
	oc delete is/leaderboard-mock-producer