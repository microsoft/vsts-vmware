import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vmOpsTool.ConnectionData;
import vmOpsTool.IVMWare;

public class InMemoryVMWareImpl implements IVMWare {

    private List<String> knownVmList = Arrays.asList("vm1", "vm2");
    private Map<String, String> activeSnapshotInfo = new HashMap<String, String>();
    
    public void restoreSnapshot(String vmList, String snapshotName, ConnectionData connData) throws Exception {
        String[] vms = vmList.split(",");
        for (String vm : vms) {
            vm = vm.trim();
            if(knownVmList.contains(vm)) {
                activeSnapshotInfo.put(vm, snapshotName);
            }
            else {
                throw new Exception("VM not found.");
            }
        }
    }

    public Boolean snapshotExists(String vmName, String snapshotName) {
        if(activeSnapshotInfo.containsKey(vmName)) {
            return activeSnapshotInfo.get(vmName).equalsIgnoreCase(snapshotName);
        }
        return false;
    }
}