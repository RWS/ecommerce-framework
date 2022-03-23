#!/usr/bin/env bash

# Java options and system properties to pass to the JVM when starting the service. For example:
# JVM_OPTIONS="-Xrs -Xms256m -Xmx512m -Dmy.system.property=/var/share"
JVM_OPTIONS="-Xrs -Xms256m -Xmx512m -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"
SERVER_PORT=--server.port=8097

# set max size of request header to 64Kb
MAX_HTTP_HEADER_SIZE=--server.tomcat.max-http-header-size=65536

BASEDIR=$(dirname $0)
CLASS_PATH=.:config:bin:lib/*
CLASS_NAME="com.sdl.ecommerce.service.ServiceContainer"
PID_FILE="sdl-service-container.pid"

cd $BASEDIR/..
if [ -f $PID_FILE ]
  then
    if ps -p $(cat $PID_FILE) > /dev/null
        then
          echo "The service already started."
          echo "To start service again, run stop.sh first."
          exit 0
    fi
fi

ARGUMENTS=()
for ARG in $@
do
    if [[ $ARG == --server\.port=* ]]
    then
        SERVER_PORT=$ARG
    elif [[ $ARG =~ -D.+ ]]; then
    	JVM_OPTIONS=$JVM_OPTIONS" "$ARG
    else
        ARGUMENTS+=($ARG)
    fi
done
ARGUMENTS+=($SERVER_PORT)
ARGUMENTS+=($MAX_HTTP_HEADER_SIZE)

echo "Starting service."

java -cp $CLASS_PATH $JVM_OPTIONS $CLASS_NAME ${ARGUMENTS[@]} & echo $! > $PID_FILE
