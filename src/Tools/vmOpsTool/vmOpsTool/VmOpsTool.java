package vmOpsTool;

import java.util.*;

public class VmOpsTool {

    public static void main(String[] args) {
        try {
            runMain(args);
        }
        catch (Exception exp) {
            System.exit(1);
        }
    }

    private static void runMain(String[] args) throws Exception {
        
        Map<String, String> argsMap = parseCmdLine(args);
        
        String vCenterUrl = argsMap.get("-vCenterUrl");
        String vCenterUserName = argsMap.get("-vCenterUserName");
        String vCenterPassword = argsMap.get("-vCenterPassword");
        ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword);
        String vmList = argsMap.get("-vmList");
        VMWareImpl vmWareImpl = new VMWareImpl();
        
        if(argsMap.containsKey("-snapShotOps")) {
            String actionName = argsMap.get("-snapshotOps");
            String snapshotName = argsMap.get("-snapshotName");
            if(actionName.equalsIgnoreCase("restore")) {
            	System.out.printf("Initiating restore snapshot operation on vmList[%s]", vmList);
                vmWareImpl.RestoreSnapShot(vmList, snapshotName, connData);
            }
        }
    }

    // Duplicate keys, key missing, value missing all those cases we are not expecting
    // as the command construction is done by typescript layer. Hence parse logic is not
    // handling any of these cases
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
