#!/usr/bin/env bash

cat *.pem > trusted_cas.pem_
openssl x509 -outform der -in trusted_cas.pem_ -out trusted_cas.der
keytool -import -alias jeakbot-trusted -keystore trustedcerts.jks -file trusted_cas.der
