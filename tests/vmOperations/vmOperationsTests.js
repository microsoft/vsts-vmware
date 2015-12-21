/// <reference path="../../typings/tsd.d.ts" />
var vmOperations = require("../../src/Tasks/vmOperations/vmOperations");
var chai = require("chai");
var sinon = require("sinon");
var sinonChai = require("sinon-chai");
var tl = require("vso-task-lib");
var Q = require("q");
var expect = chai.expect;
chai.use(sinonChai);
chai.should();
describe("getCmdCommonArgs", function () {
    var sandbox;
    var getInputStub;
    var getEndPointUrlStub;
    var getEndpointAuthorizationStub;
    var dummyConnectionName = "DummyConnectionName";
    var dummyEndpointUrl = "http://localhost:8080";
    var dummyVmList = "dummyvm1, dummyvm2";
    beforeEach(function () {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        getEndPointUrlStub = sandbox.stub(tl, "getEndpointUrl");
        getEndpointAuthorizationStub = sandbox.stub(tl, "getEndpointAuthorization");
    });
    afterEach(function () {
        sandbox.restore();
    });
    it("Successfully read all the common params (url, username, password, vmList)", function () {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns({ "parameters": { "username": "dummyuser", "password": "dummypassword" } });
        var cmdArgs = vmOperations.VmOperations.getCmdCommonArgs();
        cmdArgs.should.contain("-vCenterUrl \"" + dummyEndpointUrl + "\"");
        cmdArgs.should.contain("-vCenterUserName \"dummyuser\"");
        cmdArgs.should.contain("-vCenterPassword \"dummypassword\"");
        cmdArgs.should.contain("-vmList \"" + dummyVmList + "\"");
    });
    it("Should throw on failure to get connected service name", function () {
        getInputStub.withArgs("vCenterConnection", true).throws();
        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
        getInputStub.should.have.thrown("Error");
    });
    it("Should throw on failure to get end point url", function () {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).throws();
        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getEndPointUrlStub.should.have.been.calledOnce;
        getEndPointUrlStub.should.have.thrown("Error");
    });
    it("Should throw on failure to get end point username or password", function () {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).throws();
        expect(vmOperations.VmOperations.getCmdCommonArgs).to.throw("Error");
        getEndpointAuthorizationStub.should.have.been.calledOnce;
        getEndpointAuthorizationStub.should.have.thrown("Error");
    });
    it("Should escape inputs with spaces, double quotes, comma, semi colon, uni code characters", function () {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns({ "parameters": { "username": "dummydomain\\dummyuser", "password": " dummyp\" assword , ; " } });
        var cmdArgs = vmOperations.VmOperations.getCmdCommonArgs();
        cmdArgs.should.contain(" -vCenterUserName \"dummydomain\\dummyuser\"");
        cmdArgs.should.contain(" -vCenterPassword \" dummyp\\\" assword , ; \"");
    });
});
describe("getCmdArgsForAction", function () {
    var sandbox;
    var getInputStub;
    beforeEach(function () {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
    });
    afterEach(function () {
        sandbox.restore();
    });
    it("Should read snapshot name for restore snapshot action", function () {
        getInputStub.withArgs("snapshotName", true).returns("dummySnap\"shotName");
        var cmdArgs = vmOperations.VmOperations.getCmdArgsForAction("RestoreSnapshot");
        cmdArgs.should.contain("-snapShotOps restore -snapshotName \"dummySnap\\\"shotName\"");
    });
    it("Should throw on failure to read snapshot name for restore action", function () {
        getInputStub.withArgs("snapshotName", true).throws();
        expect(function () {
            vmOperations.VmOperations.getCmdArgsForAction("RestoreSnapshot");
        }).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
    });
    it("Should throw on failure for invalid action name", function () {
        expect(function () {
            vmOperations.VmOperations.getCmdArgsForAction("InvalidAction");
        }).to.throw("Invalid action name");
    });
});
describe("runMain", function () {
    var sandbox;
    var getInputStub;
    var getCmdCommonArgsStub;
    var getCmdArgsForActionStub;
    var execCmdStub;
    var exitStub;
    var debugStub;
    beforeEach(function () {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
        execCmdStub = sandbox.stub(tl, "exec");
        exitStub = sandbox.stub(tl, "exit");
        debugStub = sandbox.stub(tl, "debug");
        getCmdCommonArgsStub = sandbox.stub(vmOperations.VmOperations, "getCmdCommonArgs");
        getCmdArgsForActionStub = sandbox.stub(vmOperations.VmOperations, "getCmdArgsForAction");
    });
    afterEach(function () {
        sandbox.restore();
    });
    var commonArgs = " -vCenterUrl \"http://localhost:8080\" -vCenterUserName \"dummydomain\\dummyuser\" -vCenterPassword \"  pas\\\" w,o ;d\" ";
    var cmdArgsForAction = " -snapShotOps restore -snapshotName \"dummysnapshot\"";
    var cmdArgs = "vmOpsTool " + cmdArgsForAction + commonArgs;
    var actionName = "RestoreSnapshot";
    it("Should return 0 on successful exection of the command", function (done) {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        var promise = Q.Promise(function (complete, failure) {
            complete(0);
        });
        execCmdStub.withArgs("java", cmdArgs).returns(promise);
        vmOperations.VmOperations.runMain().then(function (code) {
            getInputStub.should.have.been.calledOnce;
            getCmdCommonArgsStub.should.have.been.calledOnce;
            getCmdArgsForActionStub.should.have.been.calledOnce;
            execCmdStub.should.have.been.calledOnce;
            exitStub.withArgs(0).should.have.been.calledOnce;
        }).done(done);
    });
    it("Should exit with 1 and log telemetry point for expected failure in command line tool", function (done) {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        var promise = Q.Promise(function (complete, failure) {
            failure("##vso[task.logissue type=error;code=deployment_dummyerror;taskid=dummytaskid\nCommand execution failed");
        });
        execCmdStub.withArgs("java", cmdArgs).returns(promise);
        vmOperations.VmOperations.runMain().then(function (code) {
            exitStub.withArgs(1).should.have.been.calledOnce;
            debugStub.withArgs("Failure reason : Command execution failed").should.have.been.calledOnce;
            debugStub.withArgs("##vso[task.logissue type=error;code=deployment_dummyerror;taskid=dummytaskid").should.have.been.calledOnce;
        }).done(done);
    });
    it("Should exit with 1 and does not log telemetry point for unexpected command line tool termination", function (done) {
        getInputStub.withArgs("action", true).returns(actionName);
        getCmdCommonArgsStub.returns(commonArgs);
        getCmdArgsForActionStub.withArgs(actionName).returns(cmdArgsForAction);
        var promise = Q.Promise(function (complete, failure) {
            failure("Command execution failed");
        });
        execCmdStub.withArgs("java", cmdArgs).returns(promise);
        vmOperations.VmOperations.runMain().then(function (code) {
            exitStub.withArgs(1).should.have.been.calledOnce;
            debugStub.withArgs("Failure reason : Command execution failed").should.have.been.calledOnce;
            debugStub.withArgs("##vso[task.logissue type=error;code=deployment_dummyerror;taskid=dummytaskid").should.not.have.been.called;
        }).done(done);
    });
    it("Should throw exception on failure to get actionName", function () {
        getInputStub.withArgs("action", true).throws();
        expect(vmOperations.VmOperations.runMain).to.throw("Error");
        getInputStub.should.have.been.calledOnce;
    });
});
