package com.hortonworks.simpleyarnapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.Records;

/**
 * This class implements a simple async app master.
 * In real usages, the callbacks should execute in a separate thread or thread pool
 */
public class ApplicationMasterAsync extends AMRMClientAsync.AbstractCallbackHandler {
    private final Configuration configuration;
    private final NMClient nmClient;
    private final String command;
    private final int numContainersToWaitFor;
    private final CountDownLatch completedLatch;

    public ApplicationMasterAsync(String command, int numContainersToWaitFor) {
        this.command = command;
        configuration = new YarnConfiguration();
        this.numContainersToWaitFor = numContainersToWaitFor;
        completedLatch = new CountDownLatch(numContainersToWaitFor);
        nmClient = NMClient.createNMClient();
        nmClient.init(configuration);
        nmClient.start();
    }

    public void onContainersAllocated(List<Container> containers) {
        for (Container container : containers) {
            try {
                // Launch container by create ContainerLaunchContext
                ContainerLaunchContext ctx = Records.newRecord(ContainerLaunchContext.class);
                List<String> list = new ArrayList<>();
                list.add(command);
                list.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
                list.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");
                ctx.setCommands(list);
                System.out.println("[AM] Launching container " + container.getId());
                nmClient.startContainer(container, ctx);
            } catch (Exception ex) {
                System.err.println("[AM] Error launching container " + container.getId() + " " + ex);
            }
        }
    }

    @Override
    public void onContainersUpdated(List<UpdatedContainer> containers) {
    }

    public void onContainersCompleted(List<ContainerStatus> statuses) {
        for (ContainerStatus status : statuses) {
            System.out.println("[AM] Completed container " + status.getContainerId());
            completedLatch.countDown();
        }
    }

    public void onNodesUpdated(List<NodeReport> updated) {
    }

    public void onShutdownRequest() {
    }

    public void onError(Throwable t) {
    }

    public float getProgress() {
        return 0;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public static void main(String[] args) throws Exception {
        final String command = args[0];
        final int n = Integer.parseInt(args[1]);

        ApplicationMasterAsync master = new ApplicationMasterAsync(command, n);
        master.runMainLoop();

    }

    public void runMainLoop() throws Exception {

        AMRMClientAsync<ContainerRequest> rmClient = AMRMClientAsync.createAMRMClientAsync(100, this);
        rmClient.init(getConfiguration());
        rmClient.start();

        // Register with ResourceManager
        System.out.println("[AM] registerApplicationMaster 0");
        rmClient.registerApplicationMaster("", 0, "");
        System.out.println("[AM] registerApplicationMaster 1");

        // Priority for worker containers - priorities are intra-application
        Priority priority = Records.newRecord(Priority.class);
        priority.setPriority(0);

        // Resource requirements for worker containers
        Resource capability = Records.newRecord(Resource.class);
        capability.setMemorySize(128);
        capability.setVirtualCores(1);

        // Make container requests to ResourceManager
        for (int i = 0; i < numContainersToWaitFor; ++i) {
            ContainerRequest containerAsk = new ContainerRequest(capability, null, null, priority);
            System.out.println("[AM] Making ContainerRequest " + i);
            rmClient.addContainerRequest(containerAsk);
        }

        System.out.println("[AM] waiting for containers to finish");
        completedLatch.await();

        System.out.println("[AM] unregisterApplicationMaster 0");
        // Un-register with ResourceManager
        rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
        System.out.println("[AM] unregisterApplicationMaster 1");
    }
}
