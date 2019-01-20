#!/usr/bin/env bash
curl -v --user "${DIST_USER}:${DIST_PASS}" --upload-file "$1" "$2"
