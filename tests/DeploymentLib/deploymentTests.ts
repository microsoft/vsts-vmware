/// <reference path="../../typings/tsd.d.ts" />

import * as Deployment from "../../src/DeploymentLib/deployment";
import MachineGroup = Deployment.MachineGroup;
import Machine = Deployment.Machine;

import chai = require("chai");
import mocha = require("mocha");
import sinon = require("sinon");
import tl = require("vsts-task-lib/task");

var expect = chai.expect;

var sandbox;
var setVariableStub;
var logErrorStub;
function AssertLogAndError(call: Function, errorMessage: string): void {
    logErrorStub.withArgs(errorMessage).throws(new Error(errorMessage));

    expect(call).to.throw(errorMessage);
    logErrorStub.withArgs(errorMessage).should.have.been.calledOnce;
}

describe("saveMachineGroup tets", (): void => {
    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        setVariableStub = sandbox.stub(tl, "setVariable");
        logErrorStub = sandbox.stub(tl, "error");
        sandbox.stub(tl, "debug");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("should log and throw if machineGroup is null", (): void => {
       AssertLogAndError(() => Deployment.saveMachineGroup(null), "Invalid machine group");
    });

    it("should log and throw if name of the machine group is not set", (): void => {
        var machineGroup = new MachineGroup();
        AssertLogAndError(() => Deployment.saveMachineGroup(machineGroup), "Invalid machine group name");
    });

    it("should log and throw if name of the machine group is null or empty or whitepsace", (): void => {
        var invalidValues = [null, "", "   ", "		" /* \t */];
        invalidValues.forEach(invalidName => {
            var machineGroup = new MachineGroup();
            machineGroup.Name = invalidName;

            sandbox.reset();
            AssertLogAndError(() => Deployment.saveMachineGroup(machineGroup), "Invalid machine group name");
        });
    });

    it("should serialize and save MachineGroup as a task variable named as machine group's name", (): void => {
        var machineGroup: MachineGroup = {
            Name: "dummyMachineGroupName",
            Machines: [
                {
                    Name: "dummyMachineName1",
                    UserName: "dummyUserName",
                    Password: "dummyPassword",
                    Properties: {
                        ["dummyPropetyKey1"]: "dummyPropertyValue1",
                        ["dummyPropetyKey2"]: "dummyPropertyValue2"
                    }
                },
                new Machine()
            ]
        };

        Deployment.saveMachineGroup(machineGroup);

        setVariableStub.withArgs(machineGroup.Name, JSON.stringify(machineGroup)).should.have.been.calledOnce;
    });
});