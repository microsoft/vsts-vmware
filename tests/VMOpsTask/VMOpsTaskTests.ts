/// <reference path="../../typings/tsd.d.ts" />

import vmOps = require("../../src/Tasks/VMOpsTask/VMOpsTask");

import mocha = require("mocha");
import chai = require("chai");
import sinon = require("sinon");
import assert = require("assert");
import tl = require("vso-task-lib");

var expect = chai.expect;
var should = chai.should;

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

        var cmdArgs = vmOps.GetCmdCommonArgs();

        expect(cmdArgs).to.contain("-vCenterUrl \"" + dummyEndpointUrl + "\"");
        expect(cmdArgs).to.contain("-vCenterUserName \"dummyuser\"");
        expect(cmdArgs).to.contain("-vCenterPassword \"dummypassword\"");
        expect(cmdArgs).to.contain("-vmList \"" + dummyVmList + "\"");
        assert(getInputStub.calledTwice);
        assert(getEndPointUrlStub.calledOnce);
        assert(getEndpointAuthorizationStub.calledTwice);
    });

    it("Should throw on failure to get connected service name", (): void => {
        getInputStub.withArgs("vCenterConnection", true).throws();

        expect(vmOps.GetCmdCommonArgs).to.throw("Error");
        assert(getInputStub.calledOnce);
        assert(getInputStub.threw("Error"));
    });

    it("Should throw on failure to get end point url", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).throws();

        expect(vmOps.GetCmdCommonArgs).to.throw("Error");
        assert(getInputStub.calledOnce);
        assert(getEndPointUrlStub.calledOnce);
        assert(getEndPointUrlStub.threw("Error"));
    });

    it("Should throw on failure to get end point username or password", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).throws();

        expect(vmOps.GetCmdCommonArgs).to.throw("Error");
        assert(getInputStub.calledOnce);
        assert(getEndPointUrlStub.calledOnce);
        assert(getEndpointAuthorizationStub.calledOnce);
        assert(getEndpointAuthorizationStub.threw("Error"));
    });

    it("Should escape inputs with spaces, double quotes, comma, semi colon, uni code characters", (): void => {
        getInputStub.withArgs("vCenterConnection", true).returns(dummyConnectionName);
        getInputStub.withArgs("vmList", true).returns(dummyVmList);
        getEndPointUrlStub.withArgs(dummyConnectionName, false).returns(dummyEndpointUrl);
        getEndpointAuthorizationStub.withArgs(dummyConnectionName, false).returns( { "parameters": { "username" : "dummydomain\\dummyuser", "password" : " dummyp\" assword , ; "}});

        var cmdArgs = vmOps.GetCmdCommonArgs();

        expect(cmdArgs).to.contain(" -vCenterUserName \"dummydomain\\dummyuser\"");
        expect(cmdArgs).to.contain(" -vCenterPassword \" dummyp\\\" assword , ; \"");
        assert(getInputStub.calledTwice);
        assert(getEndPointUrlStub.calledOnce);
        assert(getEndpointAuthorizationStub.calledTwice);
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

        var cmdArgs = vmOps.GetCmdArgsForAction("ResoreSnapshot");

        expect(cmdArgs).to.contain("-snapShotOps restore -snapshotName \"dummySnap\\\"shotName\"");
    });

    it("Should throw on failure to read snapshot name for restore action", (): void => {
        getInputStub.withArgs("snapshotName", true).throws();

        expect( (): void => {
             vmOps.GetCmdArgsForAction("ResoreSnapshot");
             }).to.throw("Error");
        assert(getInputStub.calledOnce);
    });

    it("Should throw on failure for invalid action name", (): void => {
        expect( (): void => {
             vmOps.GetCmdArgsForAction("InvalidAction");
             }).to.throw("Invalid action name");
    });
});

describe("RunCommand", (): void => {
    var sandbox;
    var getInputStub;
    beforeEach((): void => {
        sandbox = sinon.sandbox.create();
        getInputStub = sandbox.stub(tl, "getInput");
    });

    afterEach((): void => {
        sandbox.restore();
    });

    // Should log telemetry data and throw on failure to find java
    // Should return 0 on successful exection of command
    // Should log telemetry data and return 1 on failure of command execution
});
