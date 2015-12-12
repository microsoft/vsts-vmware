/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../../typings/vso-task-lib/vso-task-lib.d.ts" />

import * as tl from "vso-task-lib";
import * as util from "util";

export class VmOperations {
    public static getCmdCommonArgs(): string {
        var cmdArgs = "";
        var vCenterConnectionName: string = tl.getInput("vCenterConnection", true);
        var vCenterUrl: string = tl.getEndpointUrl(vCenterConnectionName, false);
        var vCenterUserName: string =
            this.escapeDoubleQuotes(tl.getEndpointAuthorization(vCenterConnectionName, false)["parameters"]["username"]);
        var vCenterPassword: string =
            this.escapeDoubleQuotes(tl.getEndpointAuthorization(vCenterConnectionName, false)["parameters"]["password"]);
        var vmList: string = this.escapeDoubleQuotes(tl.getInput("vmList", true));

        tl.debug("vCenterConnectionName = " + vCenterConnectionName);
        tl.debug("vCenterUrl = " + vCenterUrl);
        tl.debug("vCenterUserName = " + vCenterUserName);
        tl.debug("vmList = " + vmList);

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
            case "RestoreSnapshot":
                snapshotName  = this.escapeDoubleQuotes(tl.getInput("snapshotName", true));
                tl.debug("snapshotName  = " + snapshotName );
                cmdArgs += " -snapShotOps restore -snapshotName \"" + snapshotName  + "\"";
                tl.debug(util.format("action args: %s", cmdArgs));
                break;
            default:
                tl.debug("actionName = " + actionName);
                throw "Invalid action name";
        }
        return cmdArgs;
    }

    public static runMain(): Q.Promise<void> {
        var actionName: string = tl.getInput("action", true);
        var commonArgs: string = this.getCmdCommonArgs();
        var cmdArgsForAction: string = this.getCmdArgsForAction(actionName);
        var cmdArgs = "vmOpsTool " + cmdArgsForAction + commonArgs;
        util.log("Invoking command to perform vm operations ...\n");
        return tl.exec("java", cmdArgs)
            .then((code) => {
                tl.debug("Exit code: " + code);
                tl.exit(code);
            })
            .fail( (err) => {
                var splitted = err.split("\n", 2);
                var telemetryData = "";
                var failureMsg;
                if (splitted[1] !== undefined) {
                    telemetryData = splitted[0];
                    failureMsg = splitted[1];
                }
                else {
                    failureMsg = splitted[0];
                }
                tl.debug(telemetryData);
                tl.debug("Failure reason : " + failureMsg);
                tl.exit(1);
            });
    }

    private static escapeDoubleQuotes(str: any): string {
        var strUpdated = str.replace("\"", "\\\"");
        tl.debug(util.format("Input string: %s, Updated string:%s", str, strUpdated));
        return strUpdated;
    }
}