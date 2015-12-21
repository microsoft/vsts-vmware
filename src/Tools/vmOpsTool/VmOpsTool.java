

import java.util.*;

public class VmOpsTool {

    private IVMWare vmWareImpl;
    private String taskId = "735d144e-55fe-44d6-b687-db9031b6e70b";

    public VmOpsTool(IVMWare vmWareImpl) {
        this.vmWareImpl = vmWareImpl;
    }
    
    public static void main(String[] args) {
        try {
            new VmOpsTool(new VMWareImpl()).executeAction(args);
        }
        catch (Exception exp) {
            System.exit(1);
        }
    }

    /**
     * parse the command line arguments and performs the vm operation 
     * @param args command line arguments
     * @throws Exception on failure
     */
    public void executeAction(String[] args) throws Exception {
        
        Map<String, String> argsMap = parseCmdLine(args);

        String vCenterUrl = argsMap.get("-vCenterUrl");
        String vCenterUserName = argsMap.get("-vCenterUserName");
        String vCenterPassword = argsMap.get("-vCenterPassword");
        String vmList = argsMap.get("-vmList");

        ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword);        

        if(argsMap.containsKey("-snapshotOps")) {
            String actionName = argsMap.get("-snapshotOps");
            String snapshotName = argsMap.get("-snapshotName");
            if(actionName.equalsIgnoreCase("restore")) {
                System.out.printf("Initiating restore snapshot operation on vmList[%s]\n", vmList);
                vmWareImpl.restoreSnapshot(vmList, snapshotName, connData);
            }
            else {
                System.out.printf("##vso[task.logissue type=error;code=INFRA_InvalidSnapshotOperation;TaskId=%s;]\n", taskId );
                System.err.printf("Invalid action name(%s) for snapshot operation\n", actionName);
                throw new Exception("Invalid action name(" + actionName + ") for snapshot operation");
            }
        }
        else {
            System.out.printf("##vso[task.logissue type=error;code=INFRA_InvalidOperation;TaskId=%s;]\n", taskId );
            System.err.printf("Invalid action input for the operation.\n");
            throw new Exception("Invalid action input for the operation.");
        }
    }

    /**
     * Duplicate keys, key missing, value missing all those cases we are not expecting
     * as the command construction is done by type script layer. Hence parse logic is not
     * handling any of these cases
     * @param cmdArgs array of input command line options
     * @return map of key value pairs of input parameters
     */
    public static Map<String, String> parseCmdLine(String[] cmdArgs) {
        Map<String, String> argsMap = new HashMap<String, String>();
        String key = null;
        String value = null;

        System.out.println("Parsing input parameters...");
        for (String arg : cmdArgs) {
            if(arg.equals("vmOpsTool")) {
                continue;
            }
            else if (arg.charAt(0) == '-') {
                key = arg;
            }
            else {
                value = arg;
                argsMap.put(key, value);
            }
        }
        return argsMap;
    }
}
