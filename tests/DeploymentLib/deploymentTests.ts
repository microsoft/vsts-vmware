/// <reference path="../../typings/tsd.d.ts" />

import * as Deployment from "../../src/DeploymentLib/deployment";
import MachineGroup = Deployment.MachineGroup;

import chai = require("chai");
import mocha = require("mocha");
import sinon = require("sinon");
import tl = require("vsts-task-lib/task");

var expect = chai.expect;

describe("saveMachineGroup tets", (): void => {
    var sandbox;
    var setVariableStub;
    var logErrorStub;

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        setVariableStub = sandbox.stub(tl, "setVariable");
        logErrorStub = sandbox.stub(tl, "error");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("should log and throw if name of the machine group is not set", (): void => {
        var machineGroup = new MachineGroup();

        try {
            Deployment.saveMachineGroup(machineGroup);
        }
        catch (error) {
            var errorMessage = "machine group's name is invalid";
            error.message.should.equal(errorMessage);
            logErrorStub.withArgs(errorMessage).should.have.been.calledOnce;
        }
    });

    it("should serialize and save MachineGroup as a task variable named as machine group's name", (): void => {
        var machineGroup = new MachineGroup();
        machineGroup.Name = "dummyMachineGroupName";

        Deployment.saveMachineGroup(machineGroup);

        setVariableStub.withArgs(machineGroup.Name, JSON.stringify(machineGroup)).should.have.been.calledOnce;
    });
});