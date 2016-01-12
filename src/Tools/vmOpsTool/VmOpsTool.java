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

            String[] vmNames = vmList.split(",");
            String failedVmList = "";
            for (String vmName : vmNames) {
                vmName = vmName.trim();
                try {
                    switch (actionName) {
                        case Constants.restoreSnapshotAction:
                            vmWareImpl.restoreSnapshot(vmName, snapshotName, connData);
                            break;
                        case Constants.createSnapshotAction:
                            String description = argsMap.get(Constants.description);
                            boolean saveVmMemory = Boolean.parseBoolean(argsMap.get(Constants.saveVmMemory));
                            boolean quiesceVmFs = Boolean.parseBoolean(argsMap.get(Constants.quiesceVmFs));

                            vmWareImpl.createSnapshot(vmName, snapshotName, saveVmMemory, quiesceVmFs, description,
                                    connData);
                            break;
                        case Constants.deleteSnapshotAction:
                            vmWareImpl.deleteSnapshot(vmName, snapshotName, connData);
                            break;
                        default:
                            System.out.printf(
                                    "##vso[task.logissue type=error;code=INFRA_InvalidSnapshotOperation;TaskId=%s;]\n",
                                    Constants.taskId);
                            throw new Exception("Invalid action name ( " + actionName + " ) for snapshot operation");
                    }
                } catch (Exception exp) {
                    System.out.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occured.");
                    failedVmList += vmName + " ";
                }
            }

            if (!failedVmList.isEmpty()) {
                throw new Exception(String.format("Failed to [%s] snapshot [%s] on virtual machines [%s].", actionName,
                        snapshotName, failedVmList));
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
        Map<String, String> argsMap = new HashMap<>();
        String key = null;
        String value;

        System.out.println("Parsing input parameters...");
        for (String arg : cmdArgs) {
            if (!arg.equals("") && arg.charAt(0) == '-') {
                key = arg;
            } else if (!arg.equals(Constants.vmOpsTool)){
                value = arg;
                argsMap.put(key, value);
            }
        }
        return argsMap;
    }
}
