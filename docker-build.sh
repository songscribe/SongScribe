#!/bin/bash
docker build --platform linux/amd64 -t kavai77/songscribe .
docker push kavai77/songscribe