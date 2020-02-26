#!/usr/bin/env bash

set -eu
set -o pipefail

basedir() {
  # Default is current directory
  local script=${BASH_SOURCE[0]}

  # Resolve symbolic links
  if [ -L $script ]; then
      if readlink -f $script >/dev/null 2>&1; then
          script=$(readlink -f $script)
      elif readlink $script >/dev/null 2>&1; then
          script=$(readlink $script)
      elif realpath $script >/dev/null 2>&1; then
          script=$(realpath $script)
      else
          echo "ERROR: Cannot resolve symbolic link $script"
          exit 1
      fi
  fi

  local dir=$(dirname "$script")
  local full_dir=$(cd "${dir}" && pwd)
  echo ${full_dir}
}


dbdir() {
  echo "$(basedir)/../db"
}

kafkadir() {
  echo "$(basedir)/../kafka"
}

# Turn colors in this script off by setting the NO_COLOR variable in your
# environment to any value:
#
# $ NO_COLOR=1 test.sh
NO_COLOR=${NO_COLOR:-""}
if [ -z "$NO_COLOR" ]; then
  header=$'\e[1;33m'
  reset=$'\e[0m'
else
  header=''
  reset=''
fi

strimzi_version=`curl https://github.com/strimzi/strimzi-kafka-operator/releases/latest |  awk -F 'tag/' '{print $2}' | awk -F '"' '{print $1}' 2>/dev/null`

EXTRA_CONFIG="apiserver.enable-admission-plugins=\
LimitRanger,\
NamespaceExists,\
NamespaceLifecycle,\
ResourceQuota,\
ServiceAccount,\
DefaultStorageClass,\
MutatingAdmissionWebhook"

function header_text {
  echo "$header$*$reset"
}

header_text "Starting Knative on minikube!"
header_text "Using Strimzi Version:                  ${strimzi_version}"

minikube profile "${MINIKUBE_PROFILE:-rhdemo}"
minikube start -p "${MINIKUBE_PROFILE:-rhdemo}" \
  --container-runtime="${CONTAINER_RUNTIME:-docker}" \
  --memory="${MEMORY:-12G}" \
  --cpus="${CPUS:-4}" \
  --disk-size="${DISKSIZE:-50g}" \
  --extra-config="$EXTRA_CONFIG" \
  --insecure-registry='10.0.0.0/24'

header_text "Waiting for core k8s services to initialize"
sleep 5; while echo && kubectl get pods -n kube-system | grep -v -E "(Running|Completed|STATUS)"; do sleep 5; done

# Kafka
header_text "Strimzi install"
kubectl create ns kafka
curl -L "https://github.com/strimzi/strimzi-kafka-operator/releases/download/${strimzi_version}/strimzi-cluster-operator-${strimzi_version}.yaml" \
  | sed 's/namespace: .*/namespace: kafka/' \
  | kubectl -n kafka apply -f -

header_text "Applying Strimzi Cluster file"
kubectl -n kafka apply -f "$(kafkadir)"

header_text "Waiting for Strimzi to become ready"
sleep 5; while echo && kubectl get pods -n kafka | grep -v -E "(Running|Completed|STATUS)"; do sleep 5; done

header_text "PostgresSQL install"
kubectl create ns db

kubectl apply -n db -f "$(dbdir)"
header_text "Waiting for Postgresql to become ready"
sleep 5; while echo && kubectl get pods -n db | grep -v -E "(Running|Completed|STATUS)"; do sleep 5; done

header_text "Creating project demo namespace"
kubectl create ns leaderboard
kubectl config set-context --current --namespace=leaderboard 
