simple-yarn-app
===============

Simple YARN application to run n copies of a unix command - deliberately kept simple (with minimal error handling etc.)

Usage:
======

$ export SIMPLE_APP_JAR=simple-yarn-app-1.1.0.jar

### Unmanaged mode

$ hadoop jar $HADOOP_YARN_HOME/share/hadoop/yarn/hadoop-yarn-applications-unmanaged-am-launcher-2.1.1-SNAPSHOT.jar Client -classpath $SIMPLE_APP_JAR -cmd "java com.hortonworks.simpleyarnapp.ApplicationMaster /bin/date 2"

### Managed mode
$ hadoop fs -mkdir -p /apps/simple/

$ hadoop fs -copyFromLocal $SIMPLE_APP_JAR /apps/simple/$SIMPLE_APP_JAR

$ hadoop jar $SIMPLE_APP_JAR com.hortonworks.simpleyarnapp.Client /bin/date 2 /apps/simple/$SIMPLE_APP_JAR
  
    
