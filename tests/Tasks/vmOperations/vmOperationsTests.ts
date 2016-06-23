/// <reference path="../../../typings/tsd.d.ts" />

import * as util from "util";
import * as path from "path";
import * as vmOperations from "../../../src/Tasks/vmOperations/vmOperations";

import mocha = require("mocha");
import chai = require("chai");
import sinon = require("sinon");
import sinonChai = require("sinon-chai");
import tl = require("vsts-task-lib/task");
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
    var exitStub;
    var dummyConnectionName = "DummyConnectionName";
    var dummyEndpointUrl = "http://localhost:8080";
    var dummyVmList = "dummyvm1, dummyvm2";

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        getEndPointUrlStub = sandbox.stub(tl, "getEndpointUrl");
        getEndpointAuthorizationStub = sandbox.stub(tl, "getEndpointAuthorization");
        logErrorStub = sandbox.stub(tl, "error");
        exitStub = sandbox.stub(tl, "exit");
        sandbox.stub(tl, "debug");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("Successfully read all the common params (url, username, password, vmList, skipca, datacenter)", (): void => {

        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummyuser", "password" : "dummypassword"}});
        getInputStub.withArgs("skipca", false).returns("true");
        getInputStub.withArgs("targetdc", true).returns("dummydc");

        var cmdArgs = vmOperations.VmOperations.getCmdCommonArgs();

        cmdArgs.should.contain("-vCenterUrl \"" + dummyEndpointUrl + "sdk/vimService\"");
        cmdArgs.should.contain("-vCenterUserName \"dummyuser\"");
        cmdArgs.should.contain("-vCenterPassword \"dummypassword\"");
        cmdArgs.should.contain("-vmList \"" + dummyVmList + "\"");
        cmdArgs.should.contain("-skipca true");
        cmdArgs.should.contain("-targetdc \"dummydc\"");
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

    it("Should throw on failure read skipca check", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummyuser", "password" : "dummypassword"}});
        getInputStub.withArgs("vmList", true).returns("vm1");
        getInputStub.withArgs("skipca", false).throws();

        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getInputStub.withArgs("skipca", false).should.have.been.calledOnce;
        getInputStub.withArgs("skipca", false).should.have.thrown("Error");
    });

    it("Should throw on failure read datastore name", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummyuser", "password" : "dummypassword"}});
        getInputStub.withArgs("vmList", true).returns("vm1");
        getInputStub.withArgs("skipca", false).returns("true");
        getInputStub.withArgs("targetdc", true).throws();

        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getInputStub.withArgs("targetdc", true).should.have.been.calledOnce;
        getInputStub.withArgs("targetdc", true).should.have.thrown("Error");
    });

    it("Should fail task for invalid vmList input, i.e vmname empty string", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns("vm1, ,vm, vm2, vm3,");
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummydomain\\dummyuser", "password" : " dummypassword , ; "}});
        logErrorStub.withArgs("Invalid input for vmList: vmName cannot be empty string.").returns(1);

        vmOperations.VmOperations.getCmdCommonArgs();

        logErrorStub.withArgs("Invalid input for vmList: vmName cannot be empty string.").should.have.been.calledTwice;
        exitStub.withArgs(1).should.have.been.calledTwice;
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

    it("Should read snapshot name, snapshot vm memory, quiesce file system,description and timeout for create snapshot", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnapshotName");
        getInputStub.withArgs("snapshotMemory", true).returns("true");
        getInputStub.withArgs("description", false).returns("Sample description");
        getInputStub.withArgs("timeout", false).returns("1200");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Take Snapshot of Virtual Machines");

        cmdArgs.should.contain("-snapshotOps create -snapshotName \"dummySnapshotName\" -snapshotVMMemory true -quiesceGuestFileSystem false -description \"Sample description\" -timeout 1200");
        debugStub.should.have.been.calledOnce;
    });

    it("Should read snapshot name, snapshot vm memory, quiesce file system,description and timeout for create snapshot", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnapshotName");
        getInputStub.withArgs("snapshotMemory", true).returns("false");
        getInputStub.withArgs("description", false).returns("Sample description");
        getInputStub.withArgs("timeout", false).returns("1200");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Take Snapshot of Virtual Machines");

        cmdArgs.should.contain("-snapshotOps create -snapshotName \"dummySnapshotName\" -snapshotVMMemory false -quiesceGuestFileSystem false -description \"Sample description\" -timeout 1200");
        debugStub.should.have.been.calledOnce;
    });

    it("Should not throw on failure to read description for create snapshot action", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnapshotName");
        getInputStub.withArgs("snapshotMemory", true).returns("true");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Take Snapshot of Virtual Machines");

        cmdArgs.should.contain("-snapshotOps create -snapshotName \"dummySnapshotName\" -snapshotVMMemory true -quiesceGuestFileSystem false -description \"undefined\"");
        getInputStub.should.have.callCount(4);
    });

    it("Should read snapshot name for restore snapshot action and timeout", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnapshotName");
        getInputStub.withArgs("timeout", false).returns("1200");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Revert Snapshot of Virtual Machines");

        cmdArgs.should.contain("-snapshotOps restore -snapshotName \"dummySnapshotName\" -timeout 1200");
    });

    it("Should read snapshot name for delete snapshot action", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnapshotName");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Delete Snapshot of Virtual Machines");

        cmdArgs.should.contain("-snapshotOps delete -snapshotName \"dummySnapshotName\"");
    });

    it("Should read template, computeType, hostname, datastore, customization spec,description and timeout for clone template", (): void => {
        getInputStub.withArgs("template", true).returns("dummyTemplate");
        getInputStub.withArgs("computeType", true).returns("ESXi Host");
        getInputStub.withArgs("hostname", true).returns("Dummy Host");
        getInputStub.withArgs("datastore", true).returns("Dummy Datastore");
        getInputStub.withArgs("description", false).returns("Dummy description");
        getInputStub.withArgs("customizationspec", false).returns("Dummy Customization Spec");
        getInputStub.withArgs("timeout", false).returns("1200");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Deploy Virtual Machines using Template");

        cmdArgs.should.contain("-clonetemplate \"dummyTemplate\" -computetype \"ESXi Host\" -computename \"Dummy Host\" -datastore \"Dummy Datastore\" -customizationspec \"Dummy Customization Spec\" -description \"Dummy description\" -timeout 1200");
    });

    it("Should read cluster name if compute is cluster and read empty description", (): void => {
        getInputStub.withArgs("computeType", true).returns("Cluster");
        getInputStub.withArgs("clustername", true).returns("Dummy Cluster");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Deploy Virtual Machines using Template");

        cmdArgs.should.contain("-computetype \"Cluster\" -computename \"Dummy Cluster\"");
    });

    it("Should read resource pool name if compute is resource pool", (): void => {
        getInputStub.withArgs("computeType", true).returns("Resource Pool");
        getInputStub.withArgs("resourcepoolname", true).returns("Dummy Resource Pool");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Deploy Virtual Machines using Template");

        cmdArgs.should.contain("-computetype \"Resource Pool\" -computename \"Dummy Resource Pool\"");
    });

    it("Should log error and exit for invalid compute type", (): void => {
        getInputStub.withArgs("computeType", true).returns("Invalid Compute");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Deploy Virtual Machines using Template");

        logErrorStub.should.have.been.calledOnce;
        exitStub.withArgs(1).should.have.been.calledOnce;
    });

    it("Should construct command action for delete vm action", (): void => {

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Delete Virtual Machines");

        cmdArgs.should.contain("-deletevm delete");
    });

    it("Should construct command action for power on vm operation and read timeout", (): void => {
        getInputStub.withArgs("timeout", false).returns("1200");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Power On Virtual Machines");

        cmdArgs.should.contain("-powerops poweron -timeout 1200");
    });

    it("Should construct command action for shutdown vm operation and read timeout", (): void => {
        getInputStub.withArgs("timeout", false).returns("1200");

        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Shutdown Virtual Machines");

        cmdArgs.should.contain("-powerops shutdown -timeout 1200");
    });

    it("Should construct command action for power off vm operation", (): void => {
        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("Power off Virtual Machines");

        cmdArgs.should.contain("-powerops poweroff");
    });

    it("Should throw on failure to read snapshot name for restore/create/delete snapshot action", (): void => {
        getInputStub.withArgs("snapshotName", true).throws();

        expect( (): void => {
             vmOperations.VmOperations.getCmdArgsForAction("Revert Snapshot of Virtual Machines");
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
    var errorStub;
    var getVariableStub;
    var changeCwdStub;
    var pathResolveStub;

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        execCmdStub = sandbox.stub(tl, "exec");
        exitStub = sandbox.stub(tl, "exit");
        errorStub = sandbox.stub(tl, "error");
        getVariableStub = sandbox.stub(tl, "getVariable");
        getCmdCommonArgsStub = sandbox.stub(vmOperations.VmOperations, "getCmdCommonArgs");
        getCmdArgsForActionStub = sandbox.stub(vmOperations.VmOperations, "getCmdArgsForAction");
        changeCwdStub = sandbox.stub(tl, "cd");
        pathResolveStub = sandbox.stub(path, "resolve");
        sandbox.stub(tl, "debug");
        sandbox.stub(util, "log");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    var commonArgs = " -vCenterUrl \"http://localhost:8080\" -vCenterUserName \"dummydomain\\dummyuser\" -vCenterPassword \"  pas\\\" w,o ;d\" ";
    var cmdArgsForAction = " -snapshotOps restore -snapshotName \"dummysnapshot\"";
    var cmdArgs = "-classpath vmOpsTool-1.0.jar;c:\\Windows VmOpsTool " + cmdArgsForAction + commonArgs;
    var actionName = "RestoreSnapshot";
    var taskWorkingFolder = "c:\\agent\\tasks\\VMwareTask\\0.2.0";

    it("Should return 0 on successful exection of the command", (done): void => {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        pathResolveStub.returns(taskWorkingFolder);
        getVariableStub.withArgs("CLASSPATH").returns("c:\\Windows");
        var promise = Q.Promise<number>((complete, failure) => {
            complete(0);
        });
        execCmdStub.withArgs("java", cmdArgs, {failOnStdErr: true}).returns(promise);

        vmOperations.VmOperations.runMain().then((code) => {
            getInputStub.should.have.been.calledOnce;
            getCmdCommonArgsStub.should.have.been.calledOnce;
            getCmdArgsForActionStub.should.have.been.calledOnce;
            execCmdStub.should.have.been.calledOnce;
            changeCwdStub.withArgs(taskWorkingFolder).should.have.been.calledOnce;
            exitStub.withArgs(0).should.have.been.calledOnce;
        }).done(done);
    });

    it("Should exit with 1 and log failure message", (done): void => {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        getVariableStub.withArgs("CLASSPATH").returns("c:\\Windows");
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
