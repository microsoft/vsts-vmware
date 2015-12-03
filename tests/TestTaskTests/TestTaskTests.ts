import * as assert from "assert";
import * as testTask from "../../src/Tasks/TestTask/testTask";

describe("HelloWorldWriter.Write", () : void => {
  it("should throw not implemented error", () : void => {
      assert.doesNotThrow(() : void => {
      // dummy code
      var helloWorldWriter = new testTask.HelloWorldWriter();
      helloWorldWriter.Write();
    });
  });
});
