simple-yarn-app
===============

Simple YARN application to run n copies of a unix command - deliberately kept simple (with minimal error handling etc.)

Usage:
======

### Unmanaged mode

$ hadoop jar $HADOOP_YARN_HOME/share/hadoop/yarn/hadoop-yarn-applications-unmanaged-am-launcher-2.1.1-SNAPSHOT.jar Client -classpath simple-yarn-app-1.0-SNAPSHOT.jar -cmd "java com.hortonworks.simpleyarnapp.ApplicationMaster /bin/date 2"

### Managed mode
```bash
export SIMPLE_APP_NAME=simple-yarn-app-1.1.0.jar
hadoop fs -mkdir -p /apps/simple/
hadoop fs -copyFromLocal $SIMPLE_APP_NAME /apps/simple/$SIMPLE_APP_NAME
hadoop jar $SIMPLE_APP_NAME com.hortonworks.simpleyarnapp.Client /bin/date 2 /apps/simple/$SIMPLE_APP_NAME
```
