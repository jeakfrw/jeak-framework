#!/usr/bin/env bash
# ./t3serverbot_minimal_runscript.sh

# Check if java exists
if [ -z "$(which java)" ]; then
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
if [ -z "$T3SB_EXECUTABLE" ]; then
    T3SB_EXECUTABLE="t3serverbot.jar"
fi

# Find libraries and insert them into the start command
if [ -d "libraries" ]; then
    LIBS="$(find libraries -type f)"
fi
LIBSTR=""
for LIB in ${LIBS}; do
    LIBSTR="${LIBSTR}:${LIB}"
done
LIBSTR="$LIBSTR:${T3SB_EXECUTABLE}"

printf "[DLIBS] ${LIBSTR}\n"
printf "[DJVMARGS] ${T3SB_JVM_ARGS}\n"
printf "[DARGS] ${T3SB_ARGS}\n"

java -cp ${LIBSTR} ${T3SB_JVM_ARGS} de.fearnixx.t3.Main ${T3SB_ARGS}
exit $?
