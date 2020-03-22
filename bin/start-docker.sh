#!/usr/bin/env bash

set -eu
set -o pipefail

open --background -a Docker

while (! docker stats --no-stream &>/dev/null ); 
do 
  echo "Waiting for Docker to launch..." 
  sleep 2
done;