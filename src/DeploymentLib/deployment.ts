/// <reference path="../../typings/vsts-task-lib/vsts-task-lib.d.ts" />

import * as tl from "vsts-task-lib/task";

export class MachineGroup {
    public Name: string;
}


export function saveMachineGroup(machineGroup: MachineGroup): void {
    if (machineGroup.Name == null) {
        var errorMessage = "machine group's name is invalid";
        tl.error(errorMessage);
        throw new Error(errorMessage + "1");
    }
    tl.setVariable(machineGroup.Name, JSON.stringify(machineGroup));
}