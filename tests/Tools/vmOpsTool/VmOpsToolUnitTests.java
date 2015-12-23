
import static org.assertj.core.api.Assertions.assertThat;

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
    public void executeActionShouldRestoreSnapshotForRestoreOperation() throws Exception {

        String[] cmdArgs = getCmdArgs("vm1, vm2", Constants.snapshotOps, Constants.restoreSnapshotAction,
                vmSnapshotName);

        vmOpsTool.executeAction(cmdArgs);

        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo(vmSnapshotName);
        assertThat(vmWareImpl.getCurrentSnapshot("vm2", connData)).isEqualTo(vmSnapshotName);
    }

    @Test
    public void executeActionShouldThrowForRestoreSnapshotFailureOnAVM() {
        String[] cmdArgs = getCmdArgs("vm1, vm3", Constants.snapshotOps, Constants.restoreSnapshotAction,
                vmSnapshotName);
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
        String[] cmdArgs = getCmdArgs("vm1, vm2", Constants.snapshotOps, "invalid", vmSnapshotName);
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
        String[] cmdArgs = getCmdArgs("vm1, vm2", "-invalidOps", Constants.restoreSnapshotAction, vmSnapshotName);
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }
        assertThat(exp).isNotNull();
    }

    @Test
    public void executeActionShouldIfRequiredInputIsNotPresent() {
        String[] cmdArgs = new String[] { Constants.vmOpsTool };
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
    }

    private String[] getCmdArgs(String vmList, String actionName, String actionOption, String snapshotName) {
        String[] cmdArgs = new String[] { Constants.vmOpsTool, Constants.vCenterUrl, vCenterUrl,
                Constants.vCenterUserName, "dummyuser", Constants.vCenterPassword, "dummypassword", Constants.vmList,
                vmList, actionName, actionOption, Constants.snapshotName, snapshotName };
        return cmdArgs;
    }
}
