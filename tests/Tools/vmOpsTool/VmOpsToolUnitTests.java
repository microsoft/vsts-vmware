import java.util.*;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import vmOpsTool.VMWareImpl;
import vmOpsTool.VmOpsTool;
import vmOpsTool.ConnectionData;
import vmOpsTool.IVMWare;

public class VmOpsToolUnitTests {
  
    private InMemoryVMWareImpl vmWareImpl = new InMemoryVMWareImpl();
    private VmOpsTool vmOpsTool = new VmOpsTool(vmWareImpl);

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

        String[] cmdArgs = new String[] {"vmOpsTool", "-vCenterUrl", "http://localhost:8080", "-vCenterUserName", "dummyuser",
                "-vCenterPassword", "dummypassword", "-vmList", "vm1, vm2", "-snapshotOps", "restore",
                "-snapshotName", "dummySnapshot"};
        vmOpsTool.executeAction(cmdArgs);

    	assertThat(vmWareImpl.snapshotExists("vm1", "dummySnapshot")).isEqualTo(true);
    	assertThat(vmWareImpl.snapshotExists("vm2", "dummySnapshot")).isEqualTo(true);
    }

    @Test
    public void executeActionShouldThrowForRestoreSnapshotFailureOnAVM() {
        Exception exp = null;
        String[] cmdArgs = new String[] {"vmOpsTool", "-vCenterUrl", "http://localhost:8080", "-vCenterUserName", "dummyuser",
                "-vCenterPassword", "dummypassword", "-vmList", "vm1, vm3", "-snapshotOps", "restore",
                "-snapshotName", "dummySnapshot"};
        try {
            vmOpsTool.executeAction(cmdArgs);
        } catch (Exception e) {
            exp = e;
        }
        
        assertThat(exp).isNotNull();
        assertThat(vmWareImpl.snapshotExists("vm1", "dummySnapshot")).isEqualTo(true);
        assertThat(vmWareImpl.snapshotExists("vm3", "dummySnapshot")).isEqualTo(false);
    }

    @Test
    public void executeActionInvalidSnapshotOperationShouldFail() {
        String[] cmdArgs = new String[] {"vmOpsTool", "-vCenterUrl", "http://localhost:8080", "-vCenterUserName", "dummyuser",
                "-vCenterPassword", "dummypassword", "-vmList", "vm1, vm2", "-snapshotOps", "invalid",
                "-snapshotName", "dummySnapshot"};
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
        String[] cmdArgs = new String[] {"vmOpsTool", "-vCenterUrl", "http://localhost:8080", "-vCenterUserName", "dummyuser",
                "-vCenterPassword", "dummypassword", "-vmList", "vm1, vm2", "-invalidOps", "restore",
                "-snapshotName", "dummySnapshot"};
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
}
