/// <reference path="../../../typings/tsd.d.ts" />

import * as vmOperations from "../../../src/Tasks/vmOperations/vmOperations";

import mocha = require("mocha");
import chai = require("chai");
import sinon = require("sinon");
import sinonChai = require("sinon-chai");
import tl = require("vso-task-lib");
import Q = require("q");

var expect = chai.expect;
chai.use(sinonChai);
chai.should();

describe("getCmdCommonArgs", (): void => {
    var sandbox;
    var getInputStub;
    var getEndPointUrlStub;
    var getEndpointAuthorizationStub;
    var logErrorStub;
    var dummyConnectionName = "DummyConnectionName";
    var dummyEndpointUrl = "http://localhost:8080";
    var dummyVmList = "dummyvm1, dummyvm2";

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        getEndPointUrlStub = sandbox.stub(tl, "getEndpointUrl");
        getEndpointAuthorizationStub = sandbox.stub(tl, "getEndpointAuthorization");
        logErrorStub = sandbox.stub(tl, "error");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("Successfully read all the common params (url, username, password, vmList)", (): void => {

        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummyuser", "password" : "dummypassword"}});

        var cmdArgs = vmOperations.VmOperations.getCmdCommonArgs();

        cmdArgs.should.contain("-vCenterUrl \"" + dummyEndpointUrl + "sdk/vimService\"");
        cmdArgs.should.contain("-vCenterUserName \"dummyuser\"");
        cmdArgs.should.contain("-vCenterPassword \"dummypassword\"");
        cmdArgs.should.contain("-vmList \"" + dummyVmList + "\"");
    });

    it("Should throw on failure to get connected service name", (): void => {
        getInputStub.withArgs("vCenterConnection", true).throws();

        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
        getInputStub.should.have.thrown("Error");
    });

    it("Should throw on failure to get end point url", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).throws();

        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getEndPointUrlStub.should.have.been.calledOnce;
        getEndPointUrlStub.should.have.thrown("Error");
    });

    it("Should throw on failure to get end point username or password", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).throws();

        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getEndpointAuthorizationStub.should.have.been.calledOnce;
        getEndpointAuthorizationStub.should.have.thrown("Error");
    });

    it("Should escape inputs with spaces, double quotes, comma, semi colon, uni code characters", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummydomain\\dummyuser", "password" : " dummyp\" assword , ; "}});

        var cmdArgs = vmOperations.VmOperations.getCmdCommonArgs();

        cmdArgs.should.contain(" -vCenterUserName \"dummydomain\\dummyuser\"");
        cmdArgs.should.contain(" -vCenterPassword \" dummyp\\\" assword , ; \"");
    });

    it("Should fail task for invalid vmList input, i.e vmname empty string", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns("vm1, ,vm, vm2, vm3,");
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummydomain\\dummyuser", "password" : " dummyp\" assword , ; "}});
        logErrorStub.withArgs("Invalid input for vmList: vmName cannot be empty string.").returns(1);

        vmOperations.VmOperations.getCmdCommonArgs();

        logErrorStub.withArgs("Invalid input for vmList: vmName cannot be empty string.").should.have.been.calledTwice;

    });
});

describe("getCmdArgsForAction", (): void => {
    var sandbox;
    var getInputStub;
    var logErrorStub;
    var exitStub;
    var debugStub;
    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        logErrorStub = sandbox.stub(tl, "error");
        exitStub = sandbox.stub(tl, "exit");
        debugStub = sandbox.stub(tl, "debug");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("Should read snapshot name, snapshot vm memory, quiesce file system and description", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnapshotName");
        getInputStub.withArgs("snapshotVMMemory", false).returns("true");
        getInputStub.withArgs("quiesceGuestFileSystem", false).returns("false");
        getInputStub.withArgs("description", false).returns("Sample description");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Take Snapshot on Virtual Machines");

        cmdArgs.should.contain("-snapshotOps create -snapshotName \"dummySnapshotName\" -snapshotVMMemory true -quiesceGuestFileSystem false -description \"Sample description\"");
        debugStub.should.have.been.calledOnce;
    });

    it("Should not throw on failure to read description for create snapshot action", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnapshotName");
        getInputStub.withArgs("snapshotVMMemory", false).returns("true");
        getInputStub.withArgs("quiesceGuestFileSystem", false).returns("false");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Take Snapshot on Virtual Machines");

        cmdArgs.should.contain("-snapshotOps create -snapshotName \"dummySnapshotName\" -snapshotVMMemory true -quiesceGuestFileSystem false -description \"undefined\"");
        getInputStub.should.have.callCount(4);
    });

    it("Should read snapshot name for restore snapshot action", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnap\"shotName");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Revert Snapshot on Virtual Machines");

        cmdArgs.should.contain("-snapshotOps restore -snapshotName \"dummySnap\\\"shotName\"");
    });

    it("Should throw on failure to read snapshot name for restore action", (): void => {
        getInputStub.withArgs("snapshotName", true).throws();

        expect( (): void => {
             vmOperations.VmOperations.getCmdArgsForAction("Revert Snapshot on Virtual Machines");
             }).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
    });

    it("Should throw on failure for invalid action name", (): void => {
        vmOperations.VmOperations.getCmdArgsForAction("InvalidAction");

        logErrorStub.withArgs(("Invalid action name : InvalidAction")).should.have.been.calledOnce;
        exitStub.withArgs(1).should.have.been.calledOnce;
    });
});

describe("runMain", (): void => {
    var sandbox;
    var getInputStub;
    var getCmdCommonArgsStub;
    var getCmdArgsForActionStub;
    var execCmdStub;
    var exitStub;
    var debugStub;
    var errorStub;
    var getVariableStub;

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        execCmdStub = sandbox.stub(tl, "exec");
        exitStub = sandbox.stub(tl, "exit");
        debugStub = sandbox.stub(tl, "debug");
        errorStub = sandbox.stub(tl, "error");
        getVariableStub = sandbox.stub(tl, "getVariable");
        getCmdCommonArgsStub = sandbox.stub(vmOperations.VmOperations, "getCmdCommonArgs");
        getCmdArgsForActionStub = sandbox.stub(vmOperations.VmOperations, "getCmdArgsForAction");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    // var systemClassPath = getVariableStub.withArgs("classpath").returns("c:\Windows");
    var commonArgs = " -vCenterUrl \"http://localhost:8080\" -vCenterUserName \"dummydomain\\dummyuser\" -vCenterPassword \"  pas\\\" w,o ;d\" ";
    var cmdArgsForAction = " -snapshotOps restore -snapshotName \"dummysnapshot\"";
    var cmdArgs = "-classpath vmOpsTool-1.0.jar;c:\\Windows VmOpsTool " + cmdArgsForAction + commonArgs;
    var actionName = "RestoreSnapshot";

    it("Should return 0 on successful exection of the command", (done): void => {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        getVariableStub.withArgs("classpath").returns("c:\\Windows");
        var promise = Q.Promise<number>((complete, failure) => {
            complete(0);
        });
        execCmdStub.withArgs("java", cmdArgs, {failOnStdErr: true}).returns(promise);

        vmOperations.VmOperations.runMain().then((code) => {
            getInputStub.should.have.been.calledOnce;
            getCmdCommonArgsStub.should.have.been.calledOnce;
            getCmdArgsForActionStub.should.have.been.calledOnce;
            execCmdStub.should.have.been.calledOnce;
            exitStub.withArgs(0).should.have.been.calledOnce;
        }).done(done);
    });

    it("Should exit with 1 and log failure message", (done): void => {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        getVariableStub.withArgs("classpath").returns("c:\\Windows");
        var promise = Q.Promise<number>((complete, failure) => {
            failure("Command execution failed");
        });
        execCmdStub.withArgs("java", cmdArgs, {failOnStdErr: true}).returns(promise);

        vmOperations.VmOperations.runMain().then((code) => {
            exitStub.withArgs(1).should.have.been.calledOnce;
            errorStub.withArgs("Failure reason : Command execution failed").should.have.been.calledOnce;
        }).done(done);
    });

    it("Should throw exception on failure to get actionName", (): void => {
        getInputStub.withArgs("action", true).throws();

        expect(vmOperations.VmOperations.runMain).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
    });
});
