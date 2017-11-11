#! /usr/bin/env bash
# ./t3serverbot_startscript.sh
# ### BEGIN LSB INFORMATION ###
# Provides:             t3serverbot
# Required-Start:       $network $named $remote_fs $time
# Required-Stop:        $remote_fs
# Default-Start:        3 4 5
# Default-Stop:         0 1 2 6
# Short-Description:    Manage T3ServerBot application
# Description:          Start/Stop/Status information about the T3ServerBot application
# X-Interactive:        false
# X-Start-After:        ts3server
# X-Stop-Before:        ts3server
# ### END LSB INFORMATION   ###

#!/usr/bin/env bash
# Which user should we run with?
# Default: "t3serverbot"
T3SB_RUN_USER="t3serverbot"

# Where is the Bot located?
# Default: "$(eval echo ~${T3SB_RUN_USER})"
# (Which is the home dir of the run user)
T3SB_RUN_DIR="$(eval echo ~${T3SB_RUN_USER})"

# What arguments shall we provide to the JVM?
T3SB_JVM_ARGS=""

# What arguments shall we provide to the J-Application?
T3SB_ARGS=""

# Do we run as the desired user? If not, sudo re-run the script
if [ $(whoami) != ${T3SB_RUN_USER} ]; then
    sudo -u ${T3SB_RUN_USER} $0 $@
    exit $?
fi

## FUNCTIONS ##

function is_alive() {
    if kill -0 "$1" > /dev/null 2> /dev/null; then
        return 0
    fi
    return 1
}

function stop() {
    if [ ! -z "$1" ]; then
        printf "\tAttempting to stop instance\n..."
        if ! is_alive $1; then
            printf "\t\tInstance died for some reason!\n"
            return 0
        else
            printf "\t\t"
            # Write "\nstop\n" into the console for a more graceful shutdown
            printf "\nstop\n" > /proc/$1/fd/0
            T3SB_STOP_COUNT=0
            while is_alive $1; do
                printf "."
                sleep 1s
                ((T3SB_STOP_COUNT++))
                if [ ${T3SB_STOP_COUNT} -gt 20 ]; then
                    break
                fi
            done
            printf "\n"
            if is_alive $1; then
                printf "\t\tInstance still alive... killing\n"
                kill $1
                sleep 1s
            fi
            # Is STILL alive? Then we were unsuccessful!
            if is_alive $1; then
                return 1
            fi
            return 0
        fi
    fi
}

function start() {
    if [ -e t3serverbot_minimal_runscript.sh ]; then
        EXECS="$(find *.jar -type f)"
        if [ 1 -eq "${#EXECS[@]}" ]; then
            T3SB_EXECUTABLE=${EXECS[0]}
        fi

        ./t3serverbot_minimal_runscript.sh > startscript.log &
        EC=$?
        echo "$!" > t3serverbot.pid
        return ${EC}
    else
        printf "\tCannot start: runscript missing\n"
        return 1
    fi
}

## ACTUAL EXECUTION ##

cd ${T3SB_RUN_DIR}

T3SB_LAST_PID=""
if [ -e "t3serverbot.pid" ]; then
    T3SB_LAST_PID="$(cat t3serverbot.pid)"
fi

printf "T3ServerBot_Startscript...\n"
printf "\tLast pid: $T3SB_LAST_PID\n"

case "$1" in
    start)
        if ! stop ${T3SB_LAST_PID}; then
            printf "\tFailed to stop instance!\n"
            exit 1
        fi
        if [ -z "$(which java)" ]; then
            printf "\tERROR - It appears java is missing!\n"
            exit 1
        fi
        if ! start; then
            printf "\tFailed to start instance!\n"
            exit 1
        fi
        printf "\tStarted..\n"
        exit 0
    ;;
    stop)
        if ! stop ${T3SB_LAST_PID}; then
            printf "\tFailed to stop instance!\n"
            exit 1
        fi
        printf "\tStopped..\n"
        exit 0
    ;;
    status)
        if is_alive ${T3SB_LAST_PID}; then
            printf "\tIs running\n"
        else
            printf "\tIs not running\n"
        fi
        exit 0
esac

printf "\tInvalid argument: $1\n"
exit 1