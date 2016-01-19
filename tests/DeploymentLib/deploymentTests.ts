/// <reference path="../../typings/tsd.d.ts" />

import * as Deployment from "../../src/DeploymentLib/deployment";
import MachineGroup = Deployment.MachineGroup;

import mocha = require("mocha");
import chai = require("chai");

var expect = chai.expect;

describe("saveMachineGroup tets", (): void => {
    it("should throw if name of the machine group is not set", (): void => {
        var machineGroup = new MachineGroup();

        expect(() => {
            Deployment.saveMachineGroup(machineGroup);
        }).to.throw();
    });

    it("should save the JSON string as a task variable with name MachineGroup:Name", (): void => {
        var machineGroup = new MachineGroup();
        machineGroup.Name = "dummy Name";

        Deployment.saveMachineGroup(machineGroup);

        // TODO: assert
    });
});