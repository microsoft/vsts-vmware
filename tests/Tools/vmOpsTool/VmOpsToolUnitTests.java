import java.util.HashMap;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import vmOpsTool.VmOpsTool;

public class VmOpsToolUnitTests {

    @Test
    public void parseCmdArgsWithAllRequiredInputs() {
        String[] cmdArgs = {"vmOpsTool", "-vCenterUrl", "http://localhost:8080"}; 

        HashMap<String, String> argsMap = VmOpsTool.parseCmdLine(cmdArgs);

        assertThat(argsMap.size()).isEqualTo(1);
        assertThat(argsMap.containsKey("vmOpsTool")).isEqualTo(false);
        assertThat(argsMap.containsKey("-vCenterUrl")).isEqualTo(true);
    }
}
