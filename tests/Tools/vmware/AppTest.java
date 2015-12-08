import static org.junit.Assert.assertEquals;
import org.junit.Test;

import app1.App1;

public class AppTest {
  @Test
  public void TestAddApp() {
    app1.App1 app = new app1.App1();
    String addedStr = app.addStrings("a","bc");
    assertEquals("abc", addedStr);
  }
}