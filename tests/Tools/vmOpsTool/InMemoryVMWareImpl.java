import java.util.HashMap;
import java.util.Map;

public class InMemoryVMWareImpl implements IVMWare {

    private Map<String, Map<String, Integer>> vmSnapshotInfo = new HashMap<String, Map<String, Integer>>();
    private Map<String, Integer> snapshotMap = new HashMap<String, Integer>();
    
    public InMemoryVMWareImpl() {
        snapshotMap.put("Snapshot1", 0);
        snapshotMap.put("Snapshot2", 1);
        vmSnapshotInfo.put("TestVM1", snapshotMap);
        vmSnapshotInfo.put("PoweredOffVM", snapshotMap);
        vmSnapshotInfo.put("DuplicateVMName", snapshotMap);
        vmSnapshotInfo.put("TemplateVM", snapshotMap);
        vmSnapshotInfo.put("VMTemplate", snapshotMap);
        vmSnapshotInfo.put("VMInDC1", snapshotMap);
        vmSnapshotInfo.put("VMInDC2", snapshotMap);
        vmSnapshotInfo.put("vm1", snapshotMap);
        vmSnapshotInfo.put("vm2", snapshotMap);
    }

    public void restoreSnapshot(String vmList, String snapshotName, ConnectionData connData) throws Exception {
        String[] vms = vmList.split(",");
        Map<String, Integer> cpMap = null;

        for (String vm : vms) {
            vm = vm.trim();
            
            if(vmSnapshotInfo.containsKey(vm)) {
                cpMap = vmSnapshotInfo.get(vm);
                if(!cpMap.containsKey(snapshotName)) {
                    System.out.println("Snapshot does not exist: " + snapshotName);
                    throw new Exception("Snapshot does not exist: " + snapshotName);
                }
                
                for (Map.Entry<String, Integer> mapEntry : cpMap.entrySet()) {
                    if(mapEntry.getKey().equalsIgnoreCase(snapshotName)) {
                        mapEntry.setValue(1);
                        System.out.println("Restored snapshot " + snapshotName);
                    }
                    else {
                        mapEntry.setValue(0);
                    }
                }
            }
            else {
                throw new Exception("VM not found.");
            }
        }
        return;
    }

    public void connect(ConnectionData connData) throws Exception {
        if(connData.password.equals("InvalidPassword")) {
            throw new Exception();
        }
    }

    public String getCurrentSnapshot(String vmName, ConnectionData connData) {

        Map<String, Integer> cpMap = null;
        String currentSnapshotName = null;
        cpMap = vmSnapshotInfo.get(vmName);

        for (Map.Entry<String, Integer> mapEntry : cpMap.entrySet()) {
            if(mapEntry.getValue().equals(1)) {
                currentSnapshotName = mapEntry.getKey();
            }
        }

        return currentSnapshotName;
    }
}
