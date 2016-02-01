/// <reference path="../../typings/tsd.d.ts" />

import * as Deployment from "../../src/DeploymentLib/deployment";
import MachineGroup = Deployment.MachineGroup;
import Machine = Deployment.Machine;

import assert = require("assert");
import chai = require("chai");
import mocha = require("mocha");
import sinon = require("sinon");
import tl = require("vsts-task-lib/task");

var expect = chai.expect;

describe("saveMachineGroup tests", (): void => {
    var sandbox;
    var logErrorSpy;

    function assertThrowsAndLogs(call: Function, errorMessage: string): void {
        expect(call).to.throw(errorMessage);
        logErrorSpy.withArgs(errorMessage).should.have.been.calledOnce;
    }

    function getMachineGroup(machineGroupName: string): MachineGroup {
        var machineGroupOutoutVariableName = tl.getVariable(machineGroupName);
        var machineGroupOutputVariableJson = tl.getVariable(machineGroupOutoutVariableName);
        return JSON.parse(machineGroupOutputVariableJson);
    }

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
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
        assertThrowsAndLogs(() => Deployment.saveMachineGroup(null), "Invalid machine group");
    });

    it("should log and throw if name of the machine group is not set", (): void => {
        var machineGroup = new MachineGroup();
        assertThrowsAndLogs(() => Deployment.saveMachineGroup(machineGroup), "Invalid machine group name");
    });

    it("should log and throw if name of the machine group is null or empty or whitepsace", (): void => {
        var invalidValues = [null, "", "   ", "		" /* \t */];
        invalidValues.forEach(invalidName => {
            var machineGroup = new MachineGroup();
            machineGroup.Name = invalidName;

            sandbox.reset();
            assertThrowsAndLogs(() => Deployment.saveMachineGroup(machineGroup), "Invalid machine group name");
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

        var outputMahcineGroup = getMachineGroup(machineGroup.Name);
        assert.deepEqual(machineGroup, outputMahcineGroup);
    });

    it("should work with localized characters", (): void => {
       var machineGroup: MachineGroup = {
           Name: "مجموعة آلة",
           Machines: [
               {
                   Name: "機器",
                   UserName: "Μηχάνημα",
                   Password: "מכונת",
                   Properties: {
                       ["Arabic"]: "الملكية",
                       ["Bulgarian"]: "Собственост",
                       ["Chinese Simplified"]: "属性",
                       ["Chinese Traditional"]: "屬性",
                       ["English"]: "Property",
                       ["French"]: "Propriété",
                       ["Greek"]: "Το κατάλυμα",
                       ["Hebrew"]: "המאפיין",
                       ["Hindi"]: "संपत्ति",
                       ["Italian"]: "Proprietà",
                       ["Japanese"]: "プロパティ",
                       ["Klignon (plqaD)"]: "",
                       ["Korean"]: "속성",
                       ["Latvian"]: "Īpašuma",
                       ["Persian"]: "ملک",
                       ["Polish"]: "Właściwość",
                       ["Queretaro Otomi"]: "Ha̲i",
                       ["Russian"]: "Недвижимость",
                       ["Serbian"]: "Својство",
                       ["Thai"]: "คุณสมบัติ",
                       ["Turkish"]: "Özelliği",
                       ["Urdu"]: "خاصیت",
                       ["Vietnamese"]: "Bất động sản"
                   }
               }
           ]
       };

       Deployment.saveMachineGroup(machineGroup);

       var outputMahcineGroup = getMachineGroup(machineGroup.Name);
       assert.deepEqual(machineGroup, outputMahcineGroup);
    });
});