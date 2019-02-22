#!/usr/bin/env bash

echo "Reading .pem certificates"
CERTS=$(cat *.pem)
echo "Converting .pem to .der"
echo "${CERTS}" | openssl x509 -inform pem -outform der -out trusted_roots.der
echo "Generating keystore"
keytool -import -alias jeakbot-trusted-roots -keystore trusted_roots.jks -file trusted_roots.der -storepass jeakbot -trustcacerts