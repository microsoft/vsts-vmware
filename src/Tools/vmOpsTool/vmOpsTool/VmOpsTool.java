package vmOpsTool;

import java.util.HashMap;

public class VmOpsTool {

    public static void main(String[] args) {
        HashMap<String, String> argsMap = parseCmdLine(args);
        // check for snapshot operations option
        // perform snapshot related operation
    }

    // Duplicate keys, key missing, value missing all those cases we are not expecting
    // as the command construction is done by typescript layer. Hence parse logic is not
    // handling any of these cases
    public static HashMap<String, String> parseCmdLine(String[] cmdArgs) {
        HashMap<String, String> argsMap = new HashMap<String, String>();
        String key = null;
        String value = null;

        for (String arg : cmdArgs) {
            if(arg.equals("vmOpsTool")) {
                continue;
            }
            else if (arg.charAt(0) == '-') {
                key = arg;
            }
            else {
                value = arg;
                argsMap.put(key, value);
            }
        }
        return argsMap;
    }
}
