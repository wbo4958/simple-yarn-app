simple-yarn-app
===============

Simple YARN application to run n copies of a unix command - deliberately kept simple (with minimal error handling etc.)

Build:
======
$ mvn clean package

Usage:
======

$ export SIMPLE_APP_JAR=simple-yarn-app-1.1.0.jar

### Unmanaged mode (Not working)
$ yarn jar $HADOOP_YARN_HOME/share/hadoop/yarn/hadoop-yarn-applications-unmanaged-am-launcher-2.1.1-SNAPSHOT.jar Client -classpath target/$SIMPLE_APP_JAR -cmd "java com.hortonworks.simpleyarnapp.ApplicationMaster /bin/date 2"

* If you want to use ApplicationMasterAsync, then use it instead of ApplicationMaster. 

### Managed mode
$ hadoop fs -mkdir -p /apps/simple/
$ hadoop fs -copyFromLocal -f $SIMPLE_APP_JAR /apps/simple/$SIMPLE_APP_JAR
$ yarn jar target/$SIMPLE_APP_JAR com.hortonworks.simpleyarnapp.Client /bin/date 2 /apps/simple/$SIMPLE_APP_JAR
