#!/usr/bin/env bash

CLASSPATH=${PWD}/conf:${PWD}/conf/QueryPattern:${CLASSPATH}

if [ -d ${PWD}/build/classes/main ]; then
    CLASSPATH=${CLASSPATH}:${PWD}/build/classes/main;
fi

if [ -d ${PWD}/build/classes/java/main ]; then
    CLASSPATH=${CLASSPATH}:${PWD}/build/classes/java/main;
fi

if [ -d ${PWD}/build/resources/main ]; then
    CLASSPATH=${CLASSPATH}:${PWD}/build/resources/main;
fi

if [ -d ${PWD}/build/libs ]; then
    for f in ${PWD}/build/libs/*.jar; do
        CLASSPATH=${CLASSPATH}:$f;
    done
fi

if [ -d ${PWD}/lib ]; then
    for f in ${PWD}/lib/*.jar; do
        CLASSPATH=${CLASSPATH}:$f;
    done
fi

if [ "$JAVA_HOME" = "" ]; then
    JAVA_HOME=`readlink -f \`which java\` | sed "s/^\(.*\)\/bin\/java/\\1/"`
    JAVA=$JAVA_HOME/bin/java
    if [ ! -x $JAVA_HOME/bin/java ]; then
        echo "Error: No suitable jvm found. Using default one." > /dev/stderr
        JAVA_HOME=""
        JAVA=java
    fi
else
    JAVA=$JAVA_HOME/bin/java
fi

export CLASSPATH
export JAVA_HOME

exec $JAVA quannk.test.excel.Excel "$@"