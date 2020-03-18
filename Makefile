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
	  --from=registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift --confirm

.PHONY: create-leaderboard-api-bc
create-leaderboard-api-bc:	oc_login
	@oc new-build --name=leaderboard-api openjdk18-openshift~. \
	  --env=MAVEN_MIRROR_URL=$(MAVEN_MIRROR_URL) \
		--context-dir=leaderboard-api

.PHONY: build-leaderboard-api
build-leaderboard-api:	oc_login
	@oc start-build leaderboard-api \
	  --from-dir="$(current_dir)"
	@oc logs -f bc/leaderboard-api

.PHONY: create-app-leaderboard-api
create-app-leaderboard-api:	oc_login
	@oc new-app --name leaderboard-api \
	  --image-stream=leaderboard-api \
		--labels app.kubernetes.io/part-of=leaderboard
	@oc expose svc/leaderboard-api
		
.PHONY:	deploy-leaderboard
deploy-leaderboard-api:	oc_login	create-leaderboard-api-bc	build-leaderboard-api	create-app-leaderboard-api

.PHONY:	clean-leaderboard-api
clean-leaderboard-api:	 oc_login	
	oc delete bc/leaderboard-api 
	oc delete dc/leaderboard-api 
	oc delete svc/leaderboard-api
	oc delete is/leaderboard-api
