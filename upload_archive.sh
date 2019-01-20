#!/usr/bin/env sh
curl -v --user "${DIST_USER}:${DIST_PASS}" --upload-file "$1" "$2"
