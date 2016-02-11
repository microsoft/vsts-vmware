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
            System.err.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occurred");
            System.exit(1);
        }
    }

    /**
     * parse the command line arguments and performs the vm operation
     *
     * @param args command line arguments
     * @throws Exception on failure
     */
    public void executeAction(String[] args) throws Exception {

        Map<String, String> argsMap = parseCmdLine(args);

        String vCenterUrl = argsMap.get(Constants.V_CENTER_URL);
        String vCenterUserName = argsMap.get(Constants.V_CENTER_USER_NAME);
        String vCenterPassword = argsMap.get(Constants.V_CENTER_PASSWORD);
        String vmList = argsMap.get(Constants.VM_LIST);
        boolean skipCACheck = Boolean.parseBoolean(argsMap.get(Constants.SKIP_CA_CHECK));

        ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword, skipCACheck);
        String[] vmNames = vmList.split(",");
        String failedVmList = "";
        String errorMessage = "";

        for (String vmName : vmNames) {
            vmName = vmName.trim();

            if (argsMap.containsKey(Constants.SNAPSHOT_OPS)) {

                String actionName = argsMap.get(Constants.SNAPSHOT_OPS);
                String snapshotName = argsMap.get(Constants.SNAPSHOT_NAME);
                errorMessage = String.format("Failed to [%s] snapshot [%s] on virtual machines ", actionName, snapshotName);
                failedVmList += executeSnapshotAction(argsMap, vmName, snapshotName, actionName, connData);

            } else if (argsMap.containsKey(Constants.CLONE_TEMPLATE)) {
                errorMessage = "Create vm from template operation failed for virtual machines ";
                failedVmList += executeCloneVmAction(argsMap, vmName, connData);
            } else if (argsMap.containsKey(Constants.DELETE_VM)) {
                errorMessage = "delete vm operation failed for virtual machines ";
                failedVmList += executeDeleteVmAction(argsMap, vmName, connData);
            } else {
                System.out.printf("##vso[task.logissue type=error;code=INFRA_InvalidOperation;TaskId=%s;]\n",
                        Constants.TASK_ID);
                throw new Exception("Invalid action input for the operation.");
            }
        }

        if (!failedVmList.isEmpty()) {
            throw new Exception(String.format("%s [%s].", errorMessage, failedVmList));
        }
    }

    private String executeDeleteVmAction(Map<String, String> argsMap, String vmName, ConnectionData connData) {
        String failedVm = "";
        try {
            vmWareImpl.deleteVM(vmName, connData);
        } catch (Exception exp) {
            System.out.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occurred.");
            failedVm = vmName + " ";
        }
        return failedVm;
    }

    /**
     * @param argsMap  map of command line arguments
     * @param vmName   name of the virtual machine
     * @param connData vCenter connection information
     * @return vmName if operation fails
     */
    private String executeCloneVmAction(Map<String, String> argsMap, String vmName, ConnectionData connData) {
        String failedVm = "";
        String templateName = argsMap.get(Constants.CLONE_TEMPLATE);
        String targetLocation = argsMap.get(Constants.TARGET_LOCATION);
        String computeType = argsMap.get(Constants.COMPUTE_TYPE);
        String computeName = argsMap.get(Constants.COMPUTE_NAME);
        String datastore = argsMap.get(Constants.DATASTORE);
        String description = argsMap.get(Constants.DESCRIPTION);

        try {
            vmWareImpl.cloneVMFromTemplate(templateName, vmName, targetLocation, computeType, computeName, datastore, description, connData);
        } catch (Exception exp) {
            System.out.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occurred.");
            failedVm = vmName + " ";
        }
        return failedVm;
    }

    /**
     * @param argsMap      map of command line arguments
     * @param vmName       name of the virtual machine
     * @param snapshotName name of the virtual machine snapshot
     * @param actionName   type of snapshot action
     * @param connData     vCenter connection information
     * @return vmName if operation fails
     */
    private String executeSnapshotAction(Map<String, String> argsMap, String vmName, String snapshotName, String actionName, ConnectionData connData) {
        String failedVm = "";
        try {
            switch (actionName) {
                case Constants.RESTORE_SNAPSHOT_ACTION:
                    vmWareImpl.restoreSnapshot(vmName, snapshotName, connData);
                    break;
                case Constants.CREATE_SNAPSHOT_ACTION:
                    String description = argsMap.get(Constants.DESCRIPTION);
                    boolean saveVmMemory = Boolean.parseBoolean(argsMap.get(Constants.SAVE_VM_MEMORY));
                    boolean quiesceVmFs = Boolean.parseBoolean(argsMap.get(Constants.QUIESCE_VM_FS));

                    vmWareImpl.createSnapshot(vmName, snapshotName, saveVmMemory, quiesceVmFs, description,
                            connData);
                    break;
                case Constants.DELETE_SNAPSHOT_ACTION:
                    vmWareImpl.deleteSnapshot(vmName, snapshotName, connData);
                    break;
                default:
                    System.out.printf(
                            "##vso[task.logissue type=error;code=INFRA_InvalidSnapshotOperation;TaskId=%s;]\n",
                            Constants.TASK_ID);
                    throw new Exception("Invalid action name ( " + actionName + " ) for snapshot operation");
            }
        } catch (Exception exp) {
            System.out.println(exp.getMessage() != null ? exp.getMessage() : "Unknown error occurred.");
            failedVm = vmName + " ";
        }
        return failedVm;
    }

    /**
     * Duplicate keys, key missing, value missing all those cases we are not
     * expecting as the command construction is done by type script layer. Hence
     * parse logic is not handling any of these cases
     *
     * @param cmdArgs array of input command line options
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
            } else if (!arg.equals(Constants.VM_OPS_TOOL)) {
                value = arg;
                argsMap.put(key, value);
            }
        }
        return argsMap;
    }
}
