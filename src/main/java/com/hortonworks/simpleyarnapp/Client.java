package com.hortonworks.simpleyarnapp;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class Client {

  private final Configuration conf = new YarnConfiguration();
  
  public void run(String[] args) throws Exception {
    //用于接受的 command /bin/date
    final String command = args[0];
    final int n = Integer.parseInt(args[1]);
    final Path jarPath = new Path(args[2]);
    boolean useAsync = false;
    if (args.length > 3) {
      useAsync = Boolean.parseBoolean(args[3]);
    }

    // Create yarnClient
    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(conf);
    yarnClient.start();
    
    // Create application via yarnClient
    YarnClientApplication app = yarnClient.createApplication();

    // Set up the container launch context for the application master
    ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
    List<String> list = new ArrayList<>();
    list.add("$JAVA_HOME/bin/java");
    list.add("-Xmx256M");
    list.add(useAsync ? "com.hortonworks.simpleyarnapp.ApplicationMasterAsync" : "com.hortonworks.simpleyarnapp.ApplicationMaster");
    list.add(command);
    list.add(String.valueOf(n));
    list.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
    list.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");
    amContainer.setCommands(list);
    
    // Setup jar for ApplicationMaster
    LocalResource appMasterJar = Records.newRecord(LocalResource.class);
    setupAppMasterJar(jarPath, appMasterJar);
    amContainer.setLocalResources(Collections.singletonMap("simple-app.jar", appMasterJar));

    // Setup CLASSPATH for ApplicationMaster
    Map<String, String> appMasterEnv = new HashMap<String, String>();
    setupAppMasterEnv(appMasterEnv);
    amContainer.setEnvironment(appMasterEnv);
    
    // Set up resource type requirements for ApplicationMaster
    Resource capability = Records.newRecord(Resource.class);
    capability.setMemorySize(256); //in MB
    capability.setVirtualCores(1);

    // Finally, set-up ApplicationSubmissionContext for the application
    ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
    appContext.setApplicationName("simple-yarn-app"); // application name
    appContext.setAMContainerSpec(amContainer);
    appContext.setResource(capability);
    appContext.setQueue("default"); // queue 

    // Submit application
    ApplicationId appId = appContext.getApplicationId();
    System.out.println("Submitting application " + appId);
    yarnClient.submitApplication(appContext);
    
    ApplicationReport appReport = yarnClient.getApplicationReport(appId);
    YarnApplicationState appState = appReport.getYarnApplicationState();
    while (appState != YarnApplicationState.FINISHED && 
           appState != YarnApplicationState.KILLED && 
           appState != YarnApplicationState.FAILED) {
      Thread.sleep(100);
      appReport = yarnClient.getApplicationReport(appId);
      appState = appReport.getYarnApplicationState();
    }
    
    System.out.println(
        "Application " + appId + " finished with" +
    		" state " + appState + 
    		" at " + appReport.getFinishTime());

  }
  
  private void setupAppMasterJar(Path jarPath, LocalResource appMasterJar) throws IOException {
    FileStatus jarStat = FileSystem.get(conf).getFileStatus(jarPath);
    appMasterJar.setResource(URL.fromPath(jarPath));
    appMasterJar.setSize(jarStat.getLen());
    appMasterJar.setTimestamp(jarStat.getModificationTime());
    appMasterJar.setType(LocalResourceType.FILE);
    appMasterJar.setVisibility(LocalResourceVisibility.PUBLIC);
  }
  
  private void setupAppMasterEnv(Map<String, String> appMasterEnv) {
    for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH, YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
      Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), c.trim(), File.pathSeparator);
    }
    Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), Environment.PWD.$() + File.separator + "*", File.pathSeparator);
  }
  
  public static void main(String[] args) throws Exception {
    Client c = new Client();
    c.run(args);
  }
}
