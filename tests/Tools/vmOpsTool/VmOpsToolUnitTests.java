
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import vmOpsTool.ConnectionData;
import vmOpsTool.VmOpsTool;

public class VmOpsToolUnitTests {
  
    private InMemoryVMWareImpl vmWareImpl = new InMemoryVMWareImpl();
    private VmOpsTool vmOpsTool = new VmOpsTool(vmWareImpl);
    private String vCenterUrl = "https://localhost:8080/sdk/vimservice";
    private String vCenterUserName = "Administrator";
    private String vCenterPassword = "Password~1";
    
    private ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword);

    @Test
    public void parseCmdArgsWithAllRequiredInputs() {
        String[] cmdArgs = {"vmOpsTool", "-vCenterUrl", "http://localhost:8080"}; 

        Map<String, String> argsMap = VmOpsTool.parseCmdLine(cmdArgs);

        assertThat(argsMap.size()).isEqualTo(1);
        assertThat(argsMap.containsKey("vmOpsTool")).isEqualTo(false);
        assertThat(argsMap.containsKey("-vCenterUrl")).isEqualTo(true);
        assertThat(argsMap.get("-vCenterUrl")).isEqualTo("http://localhost:8080");
    }

    @Test
    public void executeActionShouldRestoreSnapshotForRestoreOperation() throws Exception {

        String[] cmdArgs = getCmdArgs("vm1, vm2", "-snapshotOps", "restore", "Snapshot1"); 

        vmOpsTool.executeAction(cmdArgs);

        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo("Snapshot1");
        assertThat(vmWareImpl.getCurrentSnapshot("vm2", connData)).isEqualTo("Snapshot1");
    }

    @Test
    public void executeActionShouldThrowForRestoreSnapshotFailureOnAVM() {
        String[] cmdArgs = getCmdArgs("vm1, vm3", "-snapshotOps", "restore", "Snapshot1");
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
        assertThat(vmWareImpl.getCurrentSnapshot("vm1", connData)).isEqualTo("Snapshot1");
    }

    @Test
    public void executeActionInvalidSnapshotOperationShouldFail() {
        String[] cmdArgs = getCmdArgs("vm1, vm2", "-snapshotOps", "invalid", "Snapshot1");
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
        String[] cmdArgs = getCmdArgs("vm1, vm2", "-invalidOps", "restore", "Snapshot1");
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
        String[] cmdArgs = new String[] {"vmOpsTool"};
        Exception exp = null;

        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }

        assertThat(exp).isNotNull();
    }
    
    private String[] getCmdArgs(String vmList, String actionName , String actionOption, String snapshotName) {
        String[] cmdArgs = new String[] {"vmOpsTool", "-vCenterUrl", "http://localhost:8080",
                "-vCenterUserName", "dummyuser", "-vCenterPassword", "dummypassword", "-vmList",
                vmList, actionName, actionOption, "-snapshotName", snapshotName};
        return cmdArgs;
    }
}
