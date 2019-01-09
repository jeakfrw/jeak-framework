#!/usr/bin/env bash
# ./t3serverbot_minimal_runscript.sh

# Check if java exists
if [[ -z "$(which java)" ]]; then
    printf "Missing dependencies: Java\n"
    exit 1
fi

# Passed from the startscript
if [ -z "$T3SB_JVM_ARGS" ]; then
    T3SB_JVM_ARGS="-Xmx1G -Xms1G"
fi
# Passed from the startscript
if [ -z "$T3SB_ARGS" ]; then
    T3SB_ARGS=""
fi
# Optionally passed from the startscript
if [[ -z "$T3SB_EXECUTABLE" ]]; then
    if [[ -e "t3serverbot.jar" ]]; then
        T3SB_EXECUTABLE="t3serverbot.jar"
    else
        CANDS=($(ls t3serverbot*.jar))
        if [[ 1 -lt "${#CANDS[@]}" ]]; then
            T3SB_EXECUTABLE=${CANDS[0]}
        fi
    fi
fi
if [[ -z "$T3SB_EXECUTABLE" ]]; then
    printf "Cannot find T3SB_EXECUTABLE!\n"
    exit 1
fi

printf "[DJVMARGS] ${T3SB_JVM_ARGS}\n"
printf "[DARGS] ${T3SB_ARGS}\n"

java -cp .:libraries ${T3SB_JVM_ARGS} de.fearnixx.t3.Main ${T3SB_ARGS}
exit $?
