#!/usr/bin/env sh
curl --user "${DIST_USER}:${DIST_PASS}" --upload-file "$1" "$2"
