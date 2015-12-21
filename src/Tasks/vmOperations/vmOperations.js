/// <reference path="../../../typings/tsd.d.ts" />
/// <reference path="../../../typings/vso-task-lib/vso-task-lib.d.ts" />
var tl = require("vso-task-lib");
var util = require("util");
var VmOperations = (function () {
    function VmOperations() {
    }
    VmOperations.getCmdCommonArgs = function () {
        var cmdArgs = "";
        var vCenterConnectionName = tl.getInput("vCenterConnection", true);
        var vCenterUrl = tl.getEndpointUrl(vCenterConnectionName, false);
        var vCenterUserName = this.escapeDoubleQuotes(tl.getEndpointAuthorization(vCenterConnectionName, false)["parameters"]["username"]);
        var vCenterPassword = this.escapeDoubleQuotes(tl.getEndpointAuthorization(vCenterConnectionName, false)["parameters"]["password"]);
        var vmList = this.escapeDoubleQuotes(tl.getInput("vmList", true));
        tl.debug("vCenterConnectionName = " + vCenterConnectionName);
        tl.debug("vCenterUrl = " + vCenterUrl);
        tl.debug("vCenterUserName = " + vCenterUserName);
        tl.debug("vmList = " + vmList);
        cmdArgs += " -vCenterUrl \"" + vCenterUrl + "\" -vCenterUserName \"" + vCenterUserName + "\" -vCenterPassword \"" +
            vCenterPassword + "\" -vmList \"" + vmList + "\"";
        tl.debug(util.format("common args: -vCenterUrl \"%s\" -vCenterUserName \"%s\" -vCenterPassword \"%s\" -vmList \"%s\"", vCenterUrl, vCenterUserName, "**********", vmList));
        return cmdArgs;
    };
    VmOperations.getCmdArgsForAction = function (actionName) {
        var cmdArgs = "";
        var snapshotName = null;
        switch (actionName) {
            case "RestoreSnapshot":
                snapshotName = this.escapeDoubleQuotes(tl.getInput("snapshotName", true));
                tl.debug("snapshotName  = " + snapshotName);
                cmdArgs += " -snapShotOps restore -snapshotName \"" + snapshotName + "\"";
                tl.debug(util.format("action args: %s", cmdArgs));
                break;
            default:
                tl.debug("actionName = " + actionName);
                throw "Invalid action name";
        }
        return cmdArgs;
    };
    VmOperations.runMain = function () {
        var actionName = tl.getInput("action", true);
        var commonArgs = this.getCmdCommonArgs();
        var cmdArgsForAction = this.getCmdArgsForAction(actionName);
        var cmdArgs = "vmOpsTool " + cmdArgsForAction + commonArgs;
        util.log("Invoking command to perform vm operations ...\n");
        return tl.exec("java", cmdArgs)
            .then(function (code) {
            tl.debug("Exit code: " + code);
            tl.exit(code);
        })
            .fail(function (err) {
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
    };
    VmOperations.escapeDoubleQuotes = function (str) {
        var strUpdated = str.replace("\"", "\\\"");
        tl.debug(util.format("Input string: %s, Updated string:%s", str, strUpdated));
        return strUpdated;
    };
    return VmOperations;
})();
exports.VmOperations = VmOperations;
