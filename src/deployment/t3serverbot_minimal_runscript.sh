#!/usr/bin/env bash
# ./t3serverbot_minimal_runscript.sh

if [ -z "$(which java)" ]; then
    exit 1
fi

if [ -z "$T3SB_JVM_ARGS" ]; then
    T3SB_JVM_ARGS="-Xmx1G -Xms1G"
fi
if [ -z "$T3SB_ARGS" ]; then
    T3SB_ARGS=""
fi

if [ -d "libraries" ]; then
    LIBS="$(find libraries -type f)"
fi

LIBSTR=""
for LIB in LIBS; do
    LIBSTR="$LIBSTR:$LIB"
done
LIBSTR="$LIBSTR:t3serverbot.jar"

java -cp ${LIBSTR} ${T3SB_JVM_ARGS} -jar ${T3SB_ARGS}
exit $?
