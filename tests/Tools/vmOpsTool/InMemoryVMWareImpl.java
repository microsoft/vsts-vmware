import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryVMWareImpl implements IVMWare {

    private Map<String, List<String>> vmSnapshotInfo = new HashMap<>();
    private Map<String, String> vmActiveSnapshot = new HashMap<>();

    public InMemoryVMWareImpl() {
        List<String> snapshotList = new ArrayList<>();
        snapshotList.add("Snapshot1");
        snapshotList.add("Snapshot2");

        vmSnapshotInfo.put("win2012r2", snapshotList);
        vmSnapshotInfo.put("poweredoffvm", snapshotList);
        vmSnapshotInfo.put("win10", snapshotList);
        vmSnapshotInfo.put("ubuntuvm", snapshotList);
        vmSnapshotInfo.put("win8", snapshotList);
        vmSnapshotInfo.put("win7", snapshotList);
        vmSnapshotInfo.put("vm1", snapshotList);
        vmSnapshotInfo.put("vm2", snapshotList);

        String activeSnapshot = "Snapshot2";
        vmActiveSnapshot.put("win2012r2", activeSnapshot);
        vmActiveSnapshot.put("poweredoffvm", activeSnapshot);
        vmActiveSnapshot.put("win10", activeSnapshot);
        vmActiveSnapshot.put("ubuntuvm", activeSnapshot);
        vmActiveSnapshot.put("win8", activeSnapshot);
        vmActiveSnapshot.put("win7", activeSnapshot);
        vmActiveSnapshot.put("vm1", activeSnapshot);
        vmActiveSnapshot.put("vm2", activeSnapshot);
    }

    public synchronized void createSnapshot(String vmName, String snapshotName, boolean saveVMMemory, boolean quiesceFs,
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

    public synchronized void restoreSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception {
        List<String> cpList;

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
    }

    public synchronized void deleteSnapshot(String vmName, String snapshotName, ConnectionData connData) throws Exception {
        vmName = vmName.toLowerCase();
        if (vmSnapshotInfo.containsKey(vmName)) {
            List<String> vmCpList = vmSnapshotInfo.get(vmName);
            if (vmCpList.contains(snapshotName)) {
                vmCpList.remove(snapshotName);
                vmSnapshotInfo.put(vmName, vmCpList);
                vmActiveSnapshot.put(vmName, vmCpList.get(vmCpList.size() - 1));
            } else {
                throw new Exception("Snapshot not found !!");
            }
        } else {
            throw new Exception("VM not found.");
        }
    }

    public void connect(ConnectionData connData) throws Exception {
        if (connData.getPassword().equals("InvalidPassword") || !connData.isSkipCACheck()) {
            throw new Exception();
        }
    }

    public void startVM(String vmName, ConnectionData connData) throws Exception {
        if (vmName.equals("VmThatFailsInStart")) {
            throw new Exception("start vm operation failed for VmThatFailsInStart");
        }
    }

    public String getCurrentSnapshot(String vmName, ConnectionData connData) throws Exception {

        String currentSnapshotName;

        vmName = vmName.toLowerCase();
        if (vmSnapshotInfo.containsKey(vmName)) {
            currentSnapshotName = vmActiveSnapshot.get(vmName);
        } else {
            throw new Exception("VM not found.");
        }

        return currentSnapshotName;
    }

    public boolean isSnapshotExists(String vmName, String snapshotName, ConnectionData connData) throws Exception {

        vmName = vmName.toLowerCase();
        if (vmSnapshotInfo.containsKey(vmName)) {
            return vmSnapshotInfo.get(vmName).contains(snapshotName);
        } else {
            throw new Exception("VM not found.");
        }
    }

    public boolean isVmExists(String vmName, ConnectionData connData) throws Exception {
        return vmSnapshotInfo.containsKey(vmName);
    }

    public boolean isVmPoweredOn(String vmName, ConnectionData connData) throws Exception {
        return true;
    }

    public synchronized void cloneVMFromTemplate(String templateName, String vmName, String computeType, String computeName,
                                                 String datastore, String description, ConnectionData connData) throws Exception {
        if (vmName.equals("VMNameThatFailsInClone")) {
            throw new Exception("Clone VM from template operation failed for VMNameThatFailsInClone");
        }

        if (templateName.equals("InvalidTemplate")) {
            throw new Exception("Template does not exists");
        }

        if (connData.getTargetDC().equals("InvalidDc")) {
            throw new Exception("Target datacenter does not exists");
        }

        if (computeName.equals("InvalidHost") || computeName.equals("InvalidCluster") || computeName.equals("InvalidRp")) {
            throw new Exception("Invalid compute name");
        }

        if (computeType.equals("Invalid Compute")) {
            throw new Exception("Invalid compute resource");
        }


        vmSnapshotInfo.put(vmName, new ArrayList<>());
        vmActiveSnapshot.put(vmName, "");
    }

    public synchronized void deleteVM(String vmName, ConnectionData connData) throws Exception {
        if (vmName.equals("VMNameThatFailsInDelete")) {
            throw new Exception("delete vm operation failed for VMNameThatFailsInDelete");
        }
        vmSnapshotInfo.remove(vmName);
        vmActiveSnapshot.remove(vmName);
    }
}
