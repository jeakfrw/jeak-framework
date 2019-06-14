#!/usr/bin/env sh
echo "Uploading '$1' TO '$2'"
curl --user "${DIST_USER}:${DIST_PASS}" --upload-file "$1" "$2"
