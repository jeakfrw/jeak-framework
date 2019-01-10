#! /usr/bin/env bash
# ./startscript.sh
# ### BEGIN LSB INFORMATION ###
# Provides:             jeakbot
# Required-Start:       $network $named $remote_fs $time
# Required-Stop:        $remote_fs
# Default-Start:        3 4 5
# Default-Stop:         0 1 2 6
# Short-Description:    Manage jeakbot application
# Description:          Start/Stop/Status information about the jeakbot application
# X-Interactive:        false
# X-Start-After:        ts3server
# X-Stop-Before:        ts3server
# ### END LSB INFORMATION   ###

# Which user should we run with?
# Default: "jeakbot"
JEAK_RUN_USER="jeakbot"

# Where is the Bot located?
# Default: "$(eval echo ~${JEAK_RUN_USER})"
# (Which is the home dir of the run user)
JEAK_RUN_DIR="$(eval echo ~${JEAK_RUN_USER})"

# What arguments shall we provide to the JVM?
export JEAK_JVM_ARGS=""

# What arguments shall we provide to the J-Application?
export JEAK_ARGS=""

# Do we run as the desired user? If not, sudo re-run the script
if [[ $(whoami) != ${JEAK_RUN_USER} ]]; then
    sudo -u ${JEAK_RUN_USER} $0 $@
    exit $?
fi

## FUNCTIONS ##

function is_alive() {
    if tmux has-session -t "jeakbot"; then
        return 0
    fi
    return 1
}

function stop() {
    printf "\tAttempting to stop instance\n..."
    if ! is_alive; then
        printf "\t\tInstance died for some reason!\n"
        return 0
    else
        printf "\t\t"
        # Write "\nstop\n" into the console for a more graceful shutdown
        tmux send -t "jeakbot" ENTER 'stop' ENTER
        JEAK_STOP_COUNT=0
        while is_alive; do
            printf "."
            sleep 1s
            ((JEAK_STOP_COUNT++))
            if [[ ${JEAK_STOP_COUNT} -gt 20 ]]; then
               break
            fi
        done
        printf "\n"
        if is_alive; then
            printf "\t\tInstance still alive... killing\n"
            tmux kill-session -C -t "jeakbot"
            sleep 1s
        fi
        # Is STILL alive? Then we were unsuccessful!
        if is_alive; then
            return 1
        fi
        return 0
    fi
}

function start() {
    if [[ -e jeakbot_minimal_runscript.sh ]]; then
        EXECS="$(find *.jar -type f)"
        if [[ 1 -eq "${#EXECS[@]}" ]]; then
            JEAK_EXECUTABLE=${EXECS[0]}
        fi

        tmux new-session -ds "jeakbot" ./jeakbot_minimal_runscript.sh
        return $?
    else
        printf "\tCannot start: runscript missing\n"
        return 1
    fi
}

## ACTUAL EXECUTION ##

cd ${JEAK_RUN_DIR}

printf "jeakbot_Startscript...\n"

case "$1" in
    start)
        if ! stop; then
            printf "\tFailed to stop instance!\n"
            exit 1
        fi
        if [[ -z "$(which java)" ]]; then
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
        if ! stop; then
            printf "\tFailed to stop instance!\n"
            exit 1
        fi
        printf "\tStopped..\n"
        exit 0
    ;;
    status)
        if is_alive; then
            printf "\tIs running\n"
        else
            printf "\tIs not running\n"
        fi
        exit 0
    ;;
    attach)
        if ! is_alive; then
            printf "\tNot alive: can't attach\n"
            exit 1
        else
            tmux attach-session -t "jeakbot"
        fi
esac

printf "\tInvalid argument: $1\n"
exit 1