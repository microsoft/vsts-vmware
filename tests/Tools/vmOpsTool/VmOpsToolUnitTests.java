
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class VmOpsToolUnitTests {

    private InMemoryVMWareImpl vmWareImpl = new InMemoryVMWareImpl();
    private VmOpsTool vmOpsTool = new VmOpsTool(vmWareImpl);
    private String vCenterUrl = "https://localhost:8080/sdk/vimservice";
    private String vCenterUserName = "Administrator";
    private String vCenterPassword = "Password~1";
    private String vmSnapshotName = "Snapshot1";

    private ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword);

    @Test
    public void parseCmdArgsWithAllRequiredInputs() {
        String[] cmdArgs = {Constants.vmOpsTool, Constants.vCenterUrl, vCenterUrl};

        Map<String, String> argsMap = VmOpsTool.parseCmdLine(cmdArgs);

        assertThat(argsMap.size()).isEqualTo(1);
        assertThat(argsMap.containsKey(Constants.vmOpsTool)).isEqualTo(false);
        assertThat(argsMap.containsKey(Constants.vCenterUrl)).isEqualTo(true);
        assertThat(argsMap.get(Constants.vCenterUrl)).isEqualTo(vCenterUrl);
    }

    @Test
    public void parseCmdArgsWithEmptyDescription() {
        String[] cmdArgs = {Constants.vmOpsTool, Constants.description, ""};

        Map<String, String> argsMap = VmOpsTool.parseCmdLine(cmdArgs);

        assertThat(argsMap.size()).isEqualTo(1);
        assertThat(argsMap.containsKey(Constants.vmOpsTool)).isEqualTo(false);
        assertThat(argsMap.containsKey(Constants.description)).isEqualTo(true);
        assertThat(argsMap.get(Constants.description)).isEqualTo("");
    }

    @Test
    public void executeActionShouldSucceedForCloneAndDeleteVMActionWithValidInputs() throws Exception {
        String[] cmdArgs = getCmdArgs("newVM1, newVM2", Constants.cloneTemplate, "dummyTemplate", Constants.targetLocation,
                "dummyLocation", Constants.computeType, "DummyCompute", Constants.computeName, "DummyName", Constants.description, "Dummy description");

        vmOpsTool.executeAction(cmdArgs);

        assertThat(vmWareImpl.vmExists("newVM1", connData)).isEqualTo(true);
        assertThat(vmWareImpl.vmExists("newVM2", connData)).isEqualTo(true);

        // Delete vm validation
        cmdArgs = getCmdArgs("newVM1, newVM2", Constants.deleteVm, Constants.deleteVmAction);

        vmOpsTool.executeAction(cmdArgs);
        assertThat(vmWareImpl.vmExists("newVM1", connData)).isEqualTo(false);
        assertThat(vmWareImpl.vmExists("newVM2", connData)).isEqualTo(false);
    }

    @Test
    public void executeActionShouldThrowForCloneAndDeleteVMFailureOnAVM() throws Exception {
        String[] cmdArgs = getCmdArgs("newVM1, newVM3", Constants.cloneTemplate, "dummyTemplate", Constants.targetLocation,
                "dummyLocation", Constants.computeType, "DummyCompute", Constants.computeName, "DummyName", Constants.description, "Dummy description");

        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
        assertThat(vmWareImpl.vmExists("newVM1", connData)).isEqualTo(true);

        // Delete vm validation
        cmdArgs = getCmdArgs("newVM1, newVM3", Constants.deleteVm, Constants.deleteVmAction);
        exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
        assertThat(vmWareImpl.vmExists("newVM1", connData)).isEqualTo(false);
    }

    @Test
    public void executeActionShouldSucceedForCreateAndDeleteSnapshotOperation() throws Exception {
        // Create snapshot operation validation
        String createSnapshot = "Sample Snapshot";
        String[] cmdArgs = getCmdArgs("vm1, vm2", Constants.snapshotOps, Constants.createSnapshotAction,
                Constants.snapshotName, createSnapshot);

        vmOpsTool.executeAction(cmdArgs);

        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo(createSnapshot);
        assertThat(vmWareImpl.getCurrentSnapshot("vm2", connData)).isEqualTo(createSnapshot);

        // Delete snapshot operation validation
        cmdArgs = getCmdArgs("vm1, vm2", Constants.snapshotOps, Constants.deleteSnapshotAction, Constants.snapshotName,
                createSnapshot);

        vmOpsTool.executeAction(cmdArgs);

        assertThat(vmWareImpl.snapshotExists("vm1", createSnapshot, connData)).isEqualTo(false);
        assertThat(vmWareImpl.snapshotExists("vm2", createSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void executeActionShouldRestoreSnapshotForRestoreOperation() throws Exception {

        String[] cmdArgs = getCmdArgs("vm1, vm2", Constants.snapshotOps, Constants.restoreSnapshotAction,
                Constants.snapshotName, vmSnapshotName);

        vmOpsTool.executeAction(cmdArgs);

        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo(vmSnapshotName);
        assertThat(vmWareImpl.getCurrentSnapshot("vm2", connData)).isEqualTo(vmSnapshotName);
    }

    @Test
    public void executeActionShouldThrowForCreateAndDeleteSnapshotFailureOnAVM() throws Exception {
        // Delete snapshot operation throws on failure validation
        String vmSnapshot = "New Snapshot";
        String[] cmdArgs = getCmdArgs("vm1, vm3", Constants.snapshotOps, Constants.createSnapshotAction,
                Constants.snapshotName, vmSnapshot);
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo(vmSnapshot);

        // Delete snapshot throws on failure validation
        exp = null;
        cmdArgs = getCmdArgs("vm1, vm3", Constants.snapshotOps, Constants.deleteSnapshotAction, Constants.snapshotName,
                vmSnapshot);
        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
        assertThat(vmWareImpl.snapshotExists("vm1", vmSnapshot, connData)).isEqualTo(false);
    }

    @Test
    public void executeActionShouldThrowForRestoreSnapshotFailureOnAVM() throws Exception {
        String[] cmdArgs = getCmdArgs("vm1, vm3", Constants.snapshotOps, Constants.restoreSnapshotAction,
                Constants.snapshotName, vmSnapshotName);
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo(vmSnapshotName);
    }

    @Test
    public void executeActionInvalidSnapshotOperationShouldFail() {
        String[] cmdArgs = getCmdArgs("vm1, vm2", Constants.snapshotOps, "invalid", Constants.snapshotName,
                vmSnapshotName);
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void executeActionForInvalidActionNameShouldFail() {
        String[] cmdArgs = getCmdArgs("vm1, vm2", "-invalidOps", Constants.restoreSnapshotAction,
                Constants.snapshotName, vmSnapshotName);
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void executeActionShouldThrowIfRequiredInputIsNotPresent() {
        String[] cmdArgs = new String[]{Constants.vmOpsTool};
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
    }

    private String[] getCmdArgs(String vmList, String... vaArgs) {
        List<String> cmdArgs = new ArrayList<>();

        cmdArgs.add(Constants.vmOpsTool);
        cmdArgs.add(Constants.vCenterUrl);
        cmdArgs.add(vCenterUrl);
        cmdArgs.add(Constants.vCenterUserName);
        cmdArgs.add("dummyuser");
        cmdArgs.add(Constants.vCenterPassword);
        cmdArgs.add("dummypassword");
        cmdArgs.add(Constants.vmList);
        cmdArgs.add(vmList);

        Collections.addAll(cmdArgs, vaArgs);

        return cmdArgs.toArray(new String[cmdArgs.size()]);

    }
}
