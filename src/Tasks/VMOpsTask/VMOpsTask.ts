/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../../typings/vso-task-lib/vso-task-lib.d.ts" />

import * as tl from "vso-task-lib";

export function GetCmdCommonArgs(): string {
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

export function GetCmdArgsForAction(actionName: string): string {
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

export function RunCommand(exeName: string, cmdArgs: string): number {
    return 0;
}