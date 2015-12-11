/// <reference path="../../typings/tsd.d.ts" />

import vmOpsTask = require("../../src/Tasks/VMOpsTask/VMOpsTask");

import mocha = require("mocha");
import chai = require("chai");
import sinon = require("sinon");
import sinonChai = require("sinon-chai");
import tl = require("vso-task-lib");
import Q = require("q");

var expect = chai.expect;
chai.use(sinonChai);
chai.should();

describe("GetCmdCommonArgs", (): void => {
    var sandbox;
    var getInputStub;
    var getEndPointUrlStub;
    var getEndpointAuthorizationStub;
    var dummyConnectionName = "DummyConnectionName";
    var dummyEndpointUrl = "http://localhost:8080";
    var dummyVmList = "dummyvm1, dummyvm2";

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        getEndPointUrlStub = sandbox.stub(tl, "getEndpointUrl");
        getEndpointAuthorizationStub = sandbox.stub(tl, "getEndpointAuthorization");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("Successfully read all the common params (url, username, password, vmList)", (): void => {

        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummyuser", "password" : "dummypassword"}});

        var cmdArgs = vmOpsTask.GetCmdCommonArgs();

        cmdArgs.should.contain("-vCenterUrl \"" + dummyEndpointUrl + "\"");
        cmdArgs.should.contain("-vCenterUserName \"dummyuser\"");
        cmdArgs.should.contain("-vCenterPassword \"dummypassword\"");
        cmdArgs.should.contain("-vmList \"" + dummyVmList + "\"");
        getInputStub.should.have.been.calledTwice;
        getEndPointUrlStub.should.have.been.calledOnce;
        getEndpointAuthorizationStub.should.have.been.calledTwice;
    });

    it("Should throw on failure to get connected service name", (): void => {
        getInputStub.withArgs("vCenterConnection", true).throws();

        expect(vmOpsTask.GetCmdCommonArgs).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
        getInputStub.should.have.thrown("Error");
    });

    it("Should throw on failure to get end point url", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).throws();

        expect(vmOpsTask.GetCmdCommonArgs).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
        getEndPointUrlStub.should.have.been.calledOnce;
        getEndPointUrlStub.should.have.thrown("Error");
    });

    it("Should throw on failure to get end point username or password", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).throws();

        expect(vmOpsTask.GetCmdCommonArgs).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
        getEndPointUrlStub.should.have.been.calledOnce;
        getEndpointAuthorizationStub.should.have.been.calledOnce;
        getEndpointAuthorizationStub.should.have.thrown("Error");
    });

    it("Should escape inputs with spaces, double quotes, comma, semi colon, uni code characters", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummydomain\\dummyuser", "password" : " dummyp\" assword , ; "}});

        var cmdArgs = vmOpsTask.GetCmdCommonArgs();

        cmdArgs.should.contain(" -vCenterUserName \"dummydomain\\dummyuser\"");
        cmdArgs.should.contain(" -vCenterPassword \" dummyp\\\" assword , ; \"");
        getInputStub.should.have.been.calledTwice;
        getEndPointUrlStub.should.have.been.calledOnce;
        getEndpointAuthorizationStub.should.have.been.calledTwice;
    });
});

describe("GetCmdArgsForAction", (): void => {
    var sandbox;
    var getInputStub;
    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    it("Should read snapshot name for restore snapshot action", (): void => {
        getInputStub.withArgs("snapshotName", true).returns("dummySnap\"shotName");

        var cmdArgs = vmOpsTask.GetCmdArgsForAction("ResoreSnapshot");

        cmdArgs.should.contain("-snapShotOps restore -snapshotName \"dummySnap\\\"shotName\"");
    });

    it("Should throw on failure to read snapshot name for restore action", (): void => {
        getInputStub.withArgs("snapshotName", true).throws();

        expect( (): void => {
             vmOpsTask.GetCmdArgsForAction("ResoreSnapshot");
             }).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
    });

    it("Should throw on failure for invalid action name", (): void => {
        expect( (): void => {
             vmOpsTask.GetCmdArgsForAction("InvalidAction");
             }).to.throw("Invalid action name");
    });
});

describe("RunMain", (): void => {
    var sandbox;
    var getInputStub;
    var getCmdCommonArgsStub;
    var getCmdArgsForActionStub;
    var execCmdStub;
    var exitStub;

    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        execCmdStub = sandbox.stub(tl, "exec");
        exitStub = sandbox.stub(tl, "exit");
        getCmdCommonArgsStub = sandbox.stub(vmOpsTask, "GetCmdCommonArgs");
        getCmdArgsForActionStub = sandbox.stub(vmOpsTask, "GetCmdArgsForAction");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    var commonArgs = " -vCenterUrl \"http://localhost:8080\" -vCenterUserName \"dummydomain\\dummyuser\" -vCenterPassword \"  pas\\\" w,o ;d\" ";
    var cmdArgsForAction = " -snapShotOps restore -snapshotName \"dummysnapshot\"";
    var cmdArgs = "vmOpsTool " + cmdArgsForAction + commonArgs;
    var actionName = "ResoreSnapshot";

    it("Should return 0 on successful exection of the command", (done): void => {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        var promise = Q.Promise<number>((complete, failure) => {
            complete(0);
        });
        execCmdStub.withArgs("java", cmdArgs).returns(promise);
        exitStub.withArgs(0);

        vmOpsTask.RunMain().then((code) => {
            getInputStub.should.have.been.calledOnce;
            getCmdCommonArgsStub.should.have.been.calledOnce;
            getCmdArgsForActionStub.should.have.been.calledOnce;
            execCmdStub.should.have.been.calledOnce;
            exitStub.should.have.been.calledOnce;
        }).done(done);
    });

    it("Should exit with 1 and log telemetry point on exection failure", (done): void => {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        var promise = Q.Promise<number>((complete, failure) => {
            failure("Command execution failed");
        });
        execCmdStub.withArgs("java", cmdArgs).returns(promise);
        exitStub.withArgs(1);

        vmOpsTask.RunMain().then((code) => {
            getInputStub.should.have.been.calledOnce;
            getCmdCommonArgsStub.should.have.been.calledOnce;
            getCmdArgsForActionStub.should.have.been.calledOnce;
            execCmdStub.should.have.been.calledOnce;
            exitStub.should.have.been.calledOnce;
        }).done(done);
    });

    it("Should throw exception on failure to get actionName", (): void => {
        getInputStub.withArgs("action", true).throws();

        expect(vmOpsTask.RunMain).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
    });
});
