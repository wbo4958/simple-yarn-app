#! /bin/bash

#${HADOOP_HOME}/bin/hadoop jar $HADOOP_HOME/share/hadoop/yarn/hadoop-yarn-applications-unmanaged-am-launcher-3.2.1.jar Client -classpath target/simple-yarn-app-1.0-SNAPSHOT.jar -cmd "java com.hortonworks.simpleyarnapp.ApplicationMaster /bin/date 2"


HADOOP_HOME_PATH=/home/bobwang/github/mytools/spark.home/hadoop-3.2.1
SIMPLE_APP_JAR=simple-yarn-app-1.1.0.jar
#hadoop fs -mkdir -p /apps/simple/
#hadoop fs -copyFromLocal -f $SIMPLE_APP_JAR /apps/simple/$SIMPLE_APP_JAR
$HADOOP_HOME_PATH/bin/yarn jar target/$SIMPLE_APP_JAR com.hortonworks.simpleyarnapp.Client /bin/date 2 /apps/simple/$SIMPLE_APP_JAR
