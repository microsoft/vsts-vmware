import java.util.*;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import vmOpsTool.VMWareImpl;
import vmOpsTool.VmOpsTool;
import vmOpsTool.ConnectionData;
import vmOpsTool.IVMWare;

public class VmOpsToolUnitTests {

    @Test
    public void parseCmdArgsWithAllRequiredInputs() {
        String[] cmdArgs = {"vmOpsTool", "-vCenterUrl", "http://localhost:8080"}; 

        Map<String, String> argsMap = VmOpsTool.parseCmdLine(cmdArgs);

        assertThat(argsMap.size()).isEqualTo(1);
        assertThat(argsMap.containsKey("vmOpsTool")).isEqualTo(false);
        assertThat(argsMap.containsKey("-vCenterUrl")).isEqualTo(true);
        assertThat(argsMap.get("-vCenterUrl")).isEqualTo("http://localhost:8080");
    }

    // Restore snapshot operation for one vm successful
    // Restore snapshot operation for one vm failed
    // Restore snapshot operation for multiple vms successful
    // Restore snapshot operation for multiple vms for one vm failed with vm does not exist
    // Restore snapshot operation for multiple vms for one vm failed with vm snapshot does not exist
    @Test
    public void restoreSnapshotforOneVMSuccess(){
        String vmList = "dummyvm";
        String snapshotName = "dummySnapshot";
        String vCenterUrl = "http://localhost:8080";
        String vCenterUserName = "dummyuser";
        String vCenterPassword = "dummypassword";
        ConnectionData connData = new ConnectionData(vCenterUrl, vCenterUserName, vCenterPassword);        
        IVMWare vmwareInterface = new VMWareImpl();
    }
}
