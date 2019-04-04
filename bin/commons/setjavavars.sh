CLASSPATH=

#CLASSPATH=${PWD}/conf:${PWD}/conf/QueryPattern:${CLASSPATH}

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

if [ -d ${PWD}/static_libs ]; then
    CLASSPATH=${CLASSPATH}:static_libs;
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

# setup 'java.library.path' for native-hadoop code
JAVA_PLATFORM=`CLASSPATH=${CLASSPATH} $JAVA -Xmx32M org.apache.hadoop.util.PlatformName | sed -e "s/ /_/g"`
if [ -z "$JAVA_PLATFORM" ]; then
	echo "Failed to get platform name for hadoop" > /dev/stderr
else
	JAVA_LIBRARY_PATH=${PWD}/lib/hadoopnative/${JAVA_PLATFORM}
	if [ ! -z "$JAVA_LIBRARY_PATH" ]; then
		JAVA_OPTIONS="-Djava.library.path=$JAVA_LIBRARY_PATH $JAVA_OPTIONS"
	fi
fi
