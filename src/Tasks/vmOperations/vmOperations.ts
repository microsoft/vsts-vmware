/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../../typings/vso-task-lib/vso-task-lib.d.ts" />

import * as tl from "vso-task-lib";
import * as util from "util";

export class VmOperations {
    // tl.getInput will exit the task if required input is null or empty, we were not able to have
    // a platform test for this behavior as process is exiting if you don't stub
    public static getCmdCommonArgs(): string {
        var cmdArgs = "";
        var vCenterConnectionName: string = tl.getInput("vCenterConnection", true);
        var vCenterUrl: string = tl.getEndpointUrl(vCenterConnectionName, false) + "sdk/vimService";
        var endPointAuthCreds = tl.getEndpointAuthorization(vCenterConnectionName, false)["parameters"];
        var vCenterUserName: string = this.escapeDoubleQuotes(endPointAuthCreds["username"]);
        var vCenterPassword: string = this.escapeDoubleQuotes(endPointAuthCreds["password"]);
        var vmList: string = this.escapeDoubleQuotes(tl.getInput("vmList", true));
        this.validateVmListInput(vmList);

        cmdArgs += " -vCenterUrl \"" + vCenterUrl  + "\" -vCenterUserName \"" + vCenterUserName  + "\" -vCenterPassword \"" +
                 vCenterPassword + "\" -vmList \"" + vmList + "\"";
        tl.debug(util.format("common args: -vCenterUrl \"%s\" -vCenterUserName \"%s\" -vCenterPassword \"%s\" -vmList \"%s\"",
                vCenterUrl, vCenterUserName, "**********", vmList));
        return cmdArgs;
    }

    public static getCmdArgsForAction(actionName: string): string {
        var cmdArgs = "";
        var snapshotName  = null;
        switch (actionName) {
            case "Apply Snapshot to Virtual Machines":
                snapshotName  = this.escapeDoubleQuotes(tl.getInput("snapshotName", true));
                cmdArgs += " -snapshotOps restore -snapshotName \"" + snapshotName  + "\"";
                tl.debug(util.format("action args: %s", cmdArgs));
                break;
            default:
                tl.error("Invalid action name : " + actionName);
                tl.exit(1);
        }
        return cmdArgs;
    }

    public static runMain(): Q.Promise<void> {
        var actionName: string = tl.getInput("action", true);
        var commonArgs: string = this.getCmdCommonArgs();
        var cmdArgsForAction: string = this.getCmdArgsForAction(actionName);
        var systemClassPath: string = tl.getVariable("classpath");
        var cmdArgs = "-classpath vmOpsTool-1.0.jar;" + systemClassPath + " VmOpsTool " + cmdArgsForAction + commonArgs;
        util.log("Invoking command to perform vm operations ...\n");
        return tl.exec("java", cmdArgs)
            .then((code) => {
                tl.debug("Exit code: " + code);
                tl.exit(code);
            })
            .fail( (err) => {
                tl.error("Failure reason : " + err);
                tl.exit(1);
            });
    }

    private static escapeDoubleQuotes(str: any): string {
        var strUpdated = str.replace("\"", "\\\"");
        tl.debug(util.format("Input string: %s, Updated string:%s", str, strUpdated));
        return strUpdated;
    }

    // tl.exit will exit the task and hence not exiting from here, we were not able to have
    // a platform test for this behavior as process is exiting if you don't stub
    private static validateVmListInput(vmList: any): void {
        var vms = vmList.split(",");
        vms.forEach(vm => {
            if (!vm.trim()) {
                tl.error("Invalid input for vmList: vmName cannot be empty string.");
            }
        });
    }
}