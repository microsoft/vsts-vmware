import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryVMWareImpl implements IVMWare {

    private Map<String, List<String>> vmSnapshotInfo = new HashMap<String, List<String>>();
    private Map<String, String> vmActiveSnapshot = new HashMap<String, String>();
    private List<String> snapshotList = new ArrayList<String>();
    private String activeSnapshot = "Snapshot2";

    public InMemoryVMWareImpl() {
        snapshotList.add("Snapshot1");
        snapshotList.add("Snapshot2");

        vmSnapshotInfo.put("testvm1", snapshotList);
        vmSnapshotInfo.put("poweredoffvm", snapshotList);
        vmSnapshotInfo.put("duplicatevmname", snapshotList);
        vmSnapshotInfo.put("templatevm", snapshotList);
        vmSnapshotInfo.put("vmtemplate", snapshotList);
        vmSnapshotInfo.put("vmindc1", snapshotList);
        vmSnapshotInfo.put("vmindc2", snapshotList);
        vmSnapshotInfo.put("vm1", snapshotList);
        vmSnapshotInfo.put("vm2", snapshotList);

        vmActiveSnapshot.put("testvm1", activeSnapshot);
        vmActiveSnapshot.put("poweredoffvm", activeSnapshot);
        vmActiveSnapshot.put("duplicatevmname", activeSnapshot);
        vmActiveSnapshot.put("templatevm", activeSnapshot);
        vmActiveSnapshot.put("vmtemplate", activeSnapshot);
        vmActiveSnapshot.put("vmindc1", activeSnapshot);
        vmActiveSnapshot.put("vmindc2", activeSnapshot);
        vmActiveSnapshot.put("vm1", activeSnapshot);
        vmActiveSnapshot.put("vm2", activeSnapshot);
    }

    public void createSnapshot(String vmName, String snapshotName, boolean saveVMMemory, boolean quiesceFs,
            String description, ConnectionData connData) throws Exception {
        vmName = vmName.toLowerCase();
        if (vmSnapshotInfo.containsKey(vmName)) {
            List<String> vmCpList = vmSnapshotInfo.get(vmName);
            vmCpList.add(snapshotName);
            vmSnapshotInfo.put(vmName, vmCpList);
            vmActiveSnapshot.put(vmName, snapshotName);
        } else {
            throw new Exception("VM not found.");
        }
    }

    public void restoreSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception {
        List<String> cpList = null;

        vmName = vmName.toLowerCase();
        if (vmSnapshotInfo.containsKey(vmName)) {
            cpList = vmSnapshotInfo.get(vmName);
            if (!cpList.contains(snapshotName)) {
                System.out.println("Snapshot does not exist: " + snapshotName);
                throw new Exception("Snapshot does not exist: " + snapshotName);
            }
            vmActiveSnapshot.put(vmName, snapshotName);

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

    public String getCurrentSnapshot(String vmName, ConnectionData connData) throws Exception {

        String currentSnapshotName = null;

        vmName = vmName.toLowerCase();
        if (vmSnapshotInfo.containsKey(vmName)) {
            currentSnapshotName = vmActiveSnapshot.get(vmName);
        } else {
            throw new Exception("VM not found.");
        }

        return currentSnapshotName;
    }
}
