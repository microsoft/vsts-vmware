import java.util.HashMap;
import java.util.Map;

public class InMemoryVMWareImpl implements IVMWare {

    private Map<String, Map<String, Integer>> vmSnapshotInfo = new HashMap<String, Map<String, Integer>>();
    private Map<String, Integer> snapshotMap = new HashMap<String, Integer>();

    public InMemoryVMWareImpl() {
        snapshotMap.put("Snapshot1", 0);
        snapshotMap.put("Snapshot2", 1);
        vmSnapshotInfo.put("testvm1", snapshotMap);
        vmSnapshotInfo.put("poweredoffvm", snapshotMap);
        vmSnapshotInfo.put("duplicatevmname", snapshotMap);
        vmSnapshotInfo.put("templatevm", snapshotMap);
        vmSnapshotInfo.put("vmtemplate", snapshotMap);
        vmSnapshotInfo.put("vmindc1", snapshotMap);
        vmSnapshotInfo.put("vmindc2", snapshotMap);
        vmSnapshotInfo.put("vm1", snapshotMap);
        vmSnapshotInfo.put("vm2", snapshotMap);
    }

    public void restoreSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception {
        Map<String, Integer> cpMap = null;

        vmName = vmName.toLowerCase();
        if (vmSnapshotInfo.containsKey(vmName)) {
            cpMap = vmSnapshotInfo.get(vmName);
            if (!cpMap.containsKey(snapshotName)) {
                System.out.println("Snapshot does not exist: " + snapshotName);
                throw new Exception("Snapshot does not exist: " + snapshotName);
            }

            for (Map.Entry<String, Integer> mapEntry : cpMap.entrySet()) {
                if (mapEntry.getKey().equalsIgnoreCase(snapshotName)) {
                    mapEntry.setValue(1);
                    System.out.println("Restored snapshot " + snapshotName);
                } else {
                    mapEntry.setValue(0);
                }
            }
        } else {
            throw new Exception("VM not found.");
        }
        return;
    }

    public void connect(ConnectionData connData) throws Exception {
        if (connData.password.equals("InvalidPassword")) {
            throw new Exception();
        }
    }

    public String getCurrentSnapshot(String vmName, ConnectionData connData) {

        Map<String, Integer> cpMap = null;
        String currentSnapshotName = null;
        vmName = vmName.toLowerCase();
        cpMap = vmSnapshotInfo.get(vmName);

        for (Map.Entry<String, Integer> mapEntry : cpMap.entrySet()) {
            if (mapEntry.getValue().equals(1)) {
                currentSnapshotName = mapEntry.getKey();
            }
        }

        return currentSnapshotName;
    }
}
