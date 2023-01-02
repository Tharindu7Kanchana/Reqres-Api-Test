import org.inivos.utils.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSuit extends BaseTest {

//    @BeforeMethod(alwaysRun = true)
//    public void beforeMethodTest(Method testMethod) {
//        repoSpec = new RequestSpecBuilder().setBaseUri(APIConstants.baseUrl)
//                .setConfig(newConfig().logConfig(logConfig().blacklistHeader("Authorization")))
//                .addHeader("Authorization", APIConstants.token).setAccept("application/json")
//                .setContentType("application/json").build().log().all();
//    }

    @Test
    public void testPass() {
        Assert.assertTrue(true);
    }

    @Test
    public void testFail() {
        Assert.assertTrue(false);
    }
}