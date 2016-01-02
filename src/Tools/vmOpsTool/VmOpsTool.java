import java.util.HashMap;
import java.util.Map;

public class VmOpsTool {

    private IVMWare vmWareImpl;

    public VmOpsTool(IVMWare vmWareImpl) {
        this.vmWareImpl = vmWareImpl;
    }

    public static void main(String[] args) {
        try {
            new VmOpsTool(new VMWareImpl()).executeAction(args);
        } catch (Exception exp) {
            System.err.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occured");
            System.exit(1);
        }
    }

    /**
     * parse the command line arguments and performs the vm operation
     * 
     * @param args
     *            command line arguments
     * @throws Exception
     *             on failure
     */
    public void executeAction(String[] args) throws Exception {

        Map<String, String> argsMap = parseCmdLine(args);

        String vCenterUrl = argsMap.get(Constants.vCenterUrl);
        String vCenterUserName = argsMap.get(Constants.vCenterUserName);
        String vCenterPassword = argsMap.get(Constants.vCenterPassword);
        String vmList = argsMap.get(Constants.vmList);

        ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword);

        if (argsMap.containsKey(Constants.snapshotOps)) {
            String actionName = argsMap.get(Constants.snapshotOps);
            String snapshotName = argsMap.get(Constants.snapshotName);

            if (actionName.equals(Constants.restoreSnapshotAction)) {
                System.out.printf("Initiating restore snapshot operation on vmList[%s].\n", vmList);

                String[] vmNames = vmList.split(",");
                String failedVmList = "";

                for (String vmName : vmNames) {
                    vmName = vmName.trim();
                    try {
                        vmWareImpl.restoreSnapshot(vmName, snapshotName, connData);
                    } catch (Exception exp) {
                        System.out.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occured.");
                        failedVmList += vmName + " ";
                        continue;
                    }
                }

                if (!failedVmList.isEmpty()) {
                    throw new Exception(String.format("Failed to revert snapshot [%s] on virtual machines [%s].",
                            snapshotName, failedVmList));
                }
            } else if (actionName.equals(Constants.createSnapshotAction)) {
                String description = argsMap.get(Constants.description);
                boolean saveVmMemory = Boolean.parseBoolean(argsMap.get(Constants.saveVmMemory));
                boolean quiesceVmFs = Boolean.parseBoolean(argsMap.get(Constants.quiesceVmFs));

                System.out.printf("Initiating create snapshot operation on vmList[%s].\n", vmList);

                String[] vmNames = vmList.split(",");
                String failedVmList = "";

                for (String vmName : vmNames) {
                    vmName = vmName.trim();
                    try {
                        vmWareImpl.createSnapshot(vmName, snapshotName, saveVmMemory, quiesceVmFs, description,
                                connData);
                    } catch (Exception exp) {
                        System.out.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occured.");
                        failedVmList += vmName + " ";
                        continue;
                    }
                }

                if (!failedVmList.isEmpty()) {
                    throw new Exception(String.format("Failed to revert snapshot [%s] on virtual machines [%s].",
                            snapshotName, failedVmList));
                }
            } else {
                System.out.printf("##vso[task.logissue type=error;code=INFRA_InvalidSnapshotOperation;TaskId=%s;]\n",
                        Constants.taskId);
                throw new Exception("Invalid action name ( " + actionName + " ) for snapshot operation");
            }
        } else {
            System.out.printf("##vso[task.logissue type=error;code=INFRA_InvalidOperation;TaskId=%s;]\n",
                    Constants.taskId);
            throw new Exception("Invalid action input for the operation.");
        }
    }

    /**
     * Duplicate keys, key missing, value missing all those cases we are not
     * expecting as the command construction is done by type script layer. Hence
     * parse logic is not handling any of these cases
     * 
     * @param cmdArgs
     *            array of input command line options
     * @return map of key value pairs of input parameters
     */
    public static Map<String, String> parseCmdLine(String[] cmdArgs) {
        Map<String, String> argsMap = new HashMap<String, String>();
        String key = null;
        String value = null;

        System.out.println("Parsing input parameters...");
        for (String arg : cmdArgs) {
            if (arg.equals(Constants.vmOpsTool)) {
                continue;
            } else if (arg.charAt(0) == '-') {
                key = arg;
            } else {
                value = arg;
                argsMap.put(key, value);
            }
        }
        return argsMap;
    }
}
