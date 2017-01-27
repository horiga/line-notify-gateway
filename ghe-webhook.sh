#!/bin/bash
FILE=$1
curl -iks -X POST -H 'Content-Type:application/json' \
  -H 'X-GitHub-Event: pull_request' \
  -H 'X-Hub-Signature: hogehoge' \
  -H 'X-GitHub-Delivery: fugafuga' \
  'http://localhost:18081/v1/ghe/payload?notify_token=abc' --data "@$FILE"
