/// <reference path="../../typings/vsts-task-lib/vsts-task-lib.d.ts" />

import * as tl from "vsts-task-lib/task";

export class Machine {
    public Name: string;
    public UserName: string;
    public Password: string;
}

export class MachineGroup {
    public Name: string;
    public Machines: Machine[];
}

export function saveMachineGroup(machineGroup: MachineGroup): void {
    if (machineGroup == null) {
        var errorMessage = "invalid machineGroup";
        tl.error(errorMessage);
        throw new Error(errorMessage);
    }
    if (machineGroup.Name == null || machineGroup.Name.replace(/\s/g, "").length < 1) {
        var errorMessage = "machine group's name is invalid";
        tl.error(errorMessage);
        throw new Error(errorMessage);
    }
    tl.setVariable(machineGroup.Name, JSON.stringify(machineGroup));
}