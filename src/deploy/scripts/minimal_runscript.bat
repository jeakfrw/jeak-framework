@echo off
setlocal enabledelayedexpansion

IF "!JEAK_JVM_ARGS!" == "" set JEAK_JVM_ARGS="-Xmx1G -Xms1G"
IF "!JEAK_ARGS!" == "" set JEAK_ARGS=""

set JEAK_CP="libraries/*;jeakbot.jar"

java "!JEAK_JVM_ARGS!" -cp "!JEAK_CP!" de.fearnixx.jeak.Main !JEAK_ARGS!