#!/usr/bin/env bash
# ./minimal_runscript.sh

# Check if java exists
if [[ -z "$(command -v java)" ]]; then
    printf "Missing dependencies: Java\n"
    exit 1
fi

# Passed from the startscript
if [[ -z "$JEAK_JVM_ARGS" ]]; then
    JEAK_JVM_ARGS="-Xmx1G -Xms1G"
fi
# Passed from the startscript
if [[ -z "$JEAK_ARGS" ]]; then
    JEAK_ARGS=""
fi

# Note: If you're on a Windows environment (w/ bash/shell),
# please be aware that the cp separator should be ; instead of :
JEAK_CP="libraries/*:jeakbot.jar"

printf '[JVM-Arguments] %s \n' "${JEAK_JVM_ARGS}"
printf '[Jeak-Arguments] %s \n' "${JEAK_ARGS}"
printf '[Classpath] %s \n' "${JEAK_CP}"

java ${JEAK_JVM_ARGS} -cp "${JEAK_CP}" de.fearnixx.jeak.Main ${JEAK_ARGS}
exit $?
