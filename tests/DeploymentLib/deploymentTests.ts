/// <reference path="../../typings/tsd.d.ts" />

import * as Deployment from "../../src/DeploymentLib/deployment";
import MachineGroup = Deployment.MachineGroup;
import Machine = Deployment.Machine;

import chai = require("chai");
import mocha = require("mocha");
import sinon = require("sinon");
import tl = require("vsts-task-lib/task");

var expect = chai.expect;

describe("saveMachineGroup tets", (): void => {
    var sandbox;
    var logErrorSpy;
    var setVariableSpy;

    function AssertThrowsAndLogs(call: Function, errorMessage: string): void {
        expect(call).to.throw(errorMessage);
        logErrorSpy.withArgs(errorMessage).should.have.been.calledOnce;
    }

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        setVariableSpy = sandbox.spy(tl, "setVariable");
        logErrorSpy = sandbox.spy(tl, "error");

        // mock the std and err streams of vsts-task-lib to reduce noise in test output 
        var stdoutmock = {
            write: function(message: string) {}
        };
        tl.setStdStream(stdoutmock);
        tl.setErrStream(stdoutmock);

        // vsts-task-lib calls process.exit in failure test cases
        // we should eat that - test runner exits otherwise 
        sandbox.stub(process, "exit");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("should log and throw if machineGroup is null", (): void => {
        AssertThrowsAndLogs(() => Deployment.saveMachineGroup(null), "Invalid machine group");
    });

    it("should log and throw if name of the machine group is not set", (): void => {
        var machineGroup = new MachineGroup();
        AssertThrowsAndLogs(() => Deployment.saveMachineGroup(machineGroup), "Invalid machine group name");
    });

    it("should log and throw if name of the machine group is null or empty or whitepsace", (): void => {
        var invalidValues = [null, "", "   ", "		" /* \t */];
        invalidValues.forEach(invalidName => {
            var machineGroup = new MachineGroup();
            machineGroup.Name = invalidName;

            sandbox.reset();
            AssertThrowsAndLogs(() => Deployment.saveMachineGroup(machineGroup), "Invalid machine group name");
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

        setVariableSpy.withArgs(machineGroup.Name, JSON.stringify(machineGroup)).should.have.been.calledOnce;
    });
});