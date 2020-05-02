#!/usr/bin/env bash

echo "PAUSING $1"
lsof -i :$1 | grep LISTEN | awk '{print $2}' | xargs kill -STOP
