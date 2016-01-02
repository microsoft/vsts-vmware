
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
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
        String[] cmdArgs = { Constants.vmOpsTool, Constants.vCenterUrl, vCenterUrl };

        Map<String, String> argsMap = VmOpsTool.parseCmdLine(cmdArgs);

        assertThat(argsMap.size()).isEqualTo(1);
        assertThat(argsMap.containsKey(Constants.vmOpsTool)).isEqualTo(false);
        assertThat(argsMap.containsKey(Constants.vCenterUrl)).isEqualTo(true);
        assertThat(argsMap.get(Constants.vCenterUrl)).isEqualTo(vCenterUrl);
    }

    @Test
    public void executeActionShouldCreateSnapshotForCreateOperation() throws Exception {
        String createSnapshot = "Sample Snapshot";
        String[] cmdArgs = getCmdArgs("vm1, vm2", Constants.snapshotOps, Constants.createSnapshotAction,
                Constants.snapshotName, createSnapshot);

        vmOpsTool.executeAction(cmdArgs);

        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo(createSnapshot);
        assertThat(vmWareImpl.getCurrentSnapshot("vm2", connData)).isEqualTo(createSnapshot);
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
    public void executeActionShouldThrowForCreateSnapshotFailureOnAVM() throws Exception {
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
        String[] cmdArgs = new String[] { Constants.vmOpsTool };
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
    }

    private String[] getCmdArgs(String vmList, String... vaArgs) {
        List<String> cmdArgs = new ArrayList<String>();

        cmdArgs.add(Constants.vmOpsTool);
        cmdArgs.add(Constants.vCenterUrl);
        cmdArgs.add(vCenterUrl);
        cmdArgs.add(Constants.vCenterUserName);
        cmdArgs.add("dummyuser");
        cmdArgs.add(Constants.vCenterPassword);
        cmdArgs.add("dummypassword");
        cmdArgs.add(Constants.vmList);
        cmdArgs.add(vmList);

        for (String arg : vaArgs) {
            cmdArgs.add(arg);
        }

        return cmdArgs.toArray(new String[cmdArgs.size()]);

    }
}
