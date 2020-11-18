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
# Optionally passed from the startscript
if [[ -z "$JEAK_EXECUTABLE" ]]; then
    if [[ -e "jeakbot.jar" ]]; then
        JEAK_EXECUTABLE="jeakbot.jar"
    else
        CANDS=($(ls jeakbot*.jar))
        if [[ 0 -lt "${#CANDS[@]}" ]]; then
            JEAK_EXECUTABLE=${CANDS[0]}
        fi
    fi
fi
if [[ -z "$JEAK_EXECUTABLE" ]]; then
    printf "Cannot find JEAK_EXECUTABLE!\n"
    exit 1
fi

# Note: If you're on a Windows environment (w/ bash/shell),
# please be aware that the cp separator should be ; instead of :
JEAK_CP="libraries/*"

printf '[JVM-Arguments] %s \n' "${JEAK_JVM_ARGS}"
printf '[Jeak-Arguments] %s \n' "${JEAK_ARGS}"
printf '[Classpath] %s \n' "${JEAK_CP}"

java -cp "${JEAK_CP}" ${JEAK_JVM_ARGS} -jar "${JEAK_EXECUTABLE}" ${JEAK_ARGS}
exit $?
