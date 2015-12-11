/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../../typings/vso-task-lib/vso-task-lib.d.ts" />

import * as tl from "vso-task-lib";

export class VmOperations {
    public static GetCmdCommonArgs(): string {
        var cmdArgs = "";
        var vCenterConnectionName = tl.getInput("vCenterConnection", true);
        var vCenterUrl: any = tl.getEndpointUrl(vCenterConnectionName, false);
        var vCenterUserName: any = tl.getEndpointAuthorization(vCenterConnectionName, false)["parameters"]["username"];
        var vCenterPassword: any = tl.getEndpointAuthorization(vCenterConnectionName, false)["parameters"]["password"];
        var vmList: any = tl.getInput("vmList", true);

        vCenterUserName = vCenterUserName.replace("\"", "\\\"");
        vCenterPassword = vCenterPassword.replace("\"", "\\\"");
        vmList = vmList.replace("\"", "\\\"");

        tl.debug("vCenterConnectionName = " + vCenterConnectionName);
        tl.debug("vCenterUrl = " + vCenterUrl);
        tl.debug("vCenterUserName = " + vCenterUserName);
        tl.debug("vmList = " + vmList);
        cmdArgs += " -vCenterUrl \"" + vCenterUrl  + "\" -vCenterUserName \"" + vCenterUserName  + "\" -vCenterPassword \"" + vCenterPassword + "\" -vmList \"" + vmList + "\"";
        return cmdArgs;
    }

    public static GetCmdArgsForAction(actionName: string): string {
        var cmdArgs = "";
        var snapShotName = null;
        switch (actionName) {
            case "ResoreSnapshot":
                snapShotName = tl.getInput("snapshotName", true);
                snapShotName = snapShotName.replace("\"", "\\\"");
                tl.debug("snapShotName = " + snapShotName);
                cmdArgs += " -snapShotOps restore -snapshotName \"" + snapShotName + "\"";
                break;
            default:
                tl.debug("actionName = " + actionName);
                throw "Invalid action name";
        }
        return cmdArgs;
    }

    public static RunMain(): Q.Promise<void> {
        var actionName: string = tl.getInput("action", true);
        var commonArgs: string = this.GetCmdCommonArgs();
        var cmdArgsForAction: string = this.GetCmdArgsForAction(actionName);
        var cmdArgs = "vmOpsTool " + cmdArgsForAction + commonArgs;
        return tl.exec("java", cmdArgs)
            .then((code) => {
                tl.debug("Exit code: " + code);
                tl.exit(code);
            })
            .fail( (err) => {
                tl.debug("Failure reason :" + err);
                tl.exit(1);
            });
    }
}