/// <reference path="../../typings/vsts-task-lib/vsts-task-lib.d.ts" />

import * as tl from "vsts-task-lib/task";

export class Machine {
    public Name: string;
    public UserName: string;
    public Password: string;
    public Properties: { [key: string]: string };
}

export class MachineGroup {
    public Name: string;
    public Machines: Machine[];
}

export function saveMachineGroup(machineGroup: MachineGroup): void {
    if (machineGroup == null) {
        throwAndLog("Invalid machine group");
    }
    if (machineGroup.Name == null || machineGroup.Name.trim() == "") {
        throwAndLog("Invalid machine group name");
    }

    tl.setVariable(machineGroup.Name, JSON.stringify(machineGroup));
    tl.debug("Saved machine group with name '" + machineGroup.Name + "'");
}

function throwAndLog(errorMessage: string) {
    tl.error(errorMessage);
    throw new Error(errorMessage);
}