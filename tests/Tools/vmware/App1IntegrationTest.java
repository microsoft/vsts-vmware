import static org.junit.Assert.assertEquals;
import org.junit.Test;

import app1.App1;

public class App1IntegrationTest {

	@Test	
	public void AppIT() throws Exception {
		app1.App1 app = new app1.App1();
        String ret = app.anyOtherMethod("a");
        assertEquals("a", ret);
	}
}