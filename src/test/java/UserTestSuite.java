import com.aventstack.extentreports.Status;
import com.google.gson.JsonElement;
import io.restassured.response.Response;
import org.example.config.Constants;
import org.example.user.Auth;
import org.example.user.User;
import org.example.user.User1;
import org.inivos.utils.ApiTestSupport;
import org.inivos.utils.BaseTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class UserTestSuite extends BaseTest {

    List<User> userList;
    User1 user1;

    @BeforeTest
    public void testSetup(ITestContext context) {
        extent.setSystemInfo("Application", "ReQres Rest API");
        extent.setSystemInfo("Environment", "Development");
        extent.setSystemInfo("OS", "Windows 10 64 bit");
        extent.setSystemInfo("User Name", "Tharindu Kanchana");
    }

    @BeforeMethod
    public void methodSetup(ITestContext context) {
        String baseUrl = Constants.BaseUrl;
        request.baseUri(baseUrl);
        context.setAttribute("base", baseUrl);
    }

    /**
     * Test of retrieving a list of users as a json array and convert the response into List of User objects.
     * Then saving it to a json file in source folder.
     *
     * @param context
     * @throws IOException
     */
    @Test
    public void getUserList(ITestContext context) throws IOException {
        //Send Get Request
        ApiTestSupport.setQueryParams(request, Map.ofEntries(
                Map.entry("page", 1)
        ));
        Response response = ApiTestSupport.getRequest(request, "users");
        String jsonBody = response.asString();

        //Set Test Context
        ApiTestSupport.setTestContext(context, "get", "users", response);

        Assert.assertEquals(response.getStatusCode(), 200);
        ApiTestSupport.loggerAddNode(logger, "Status code is 200", Status.PASS, "Pass");

        //Convert json array of users to List of User Objects
        userList = (List<User>) (Object) ApiTestSupport.jsonStringToObjectArray(jsonBody, "data", new User());
        Assert.assertTrue(userList.size() > 0);

        ApiTestSupport.loggerAddNode(logger, "Object list is created", Status.PASS, "Pass");

        ApiTestSupport.objectToJsonFile("testdata/userlist.json", userList);
    }

    /**
     * Test of retrieving a single user convert it to a User object and
     * do object comparison between the same user object in user list above.
     *
     * @param context
     */
    @Test(dependsOnMethods = "getUserList")
    public void getSingleUser(ITestContext context) {
        //Send Get Request
        ApiTestSupport.setPathParams(request, Map.ofEntries(
                Map.entry("userid", 2)
        ));
        Response response = ApiTestSupport.getRequest(request, "users/{userid}");
        //Set Test Context
        ApiTestSupport.setTestContext(context, "get", "users/2", response);
        Assert.assertEquals(response.getStatusCode(), 200);
        ApiTestSupport.loggerAddNode(logger, "Status code is 200", Status.PASS, "Pass");

        JsonElement user = ApiTestSupport.stringToJsonObject(response.body().asPrettyString()).get("data");
        User user2 = (User) ApiTestSupport.jsonStringToObject(String.valueOf(user), new User());
        Assert.assertTrue(user2.equals(userList.get(1)));
        ApiTestSupport.loggerAddNode(logger, "User is equals to the 2nd user in list", Status.PASS, "Pass");
    }

    /**
     * Retrieve an unknown user. Test fail not found case.
     *
     * @param context
     */
    @Test
    public void getSingleUserUnknown(ITestContext context) {
        //Send Get Request
        ApiTestSupport.setPathParams(request, Map.ofEntries(
                Map.entry("userid", 44)
        ));
        Response response = ApiTestSupport.getRequest(request, "users/{userid}");

        //Set Test Context
        ApiTestSupport.setTestContext(context, "get", "users/44", response);
        Assert.assertEquals(response.getStatusCode(), 404);
        ApiTestSupport.loggerAddNode(logger, "Status code is 404", Status.PASS, "Pass");
    }

    /**
     * Registration of a user. save token return by the response.
     *
     * @param context
     */
    @Test(priority = 1)
    public void registrationSuccessful(ITestContext context) {
        //Send Post Request
        Response response = ApiTestSupport.postRequest(request, "register", Auth.authCredentials, Auth.contentHeaders);
        String jsonBody = response.asString();

        //Set Test Context
        ApiTestSupport.setTestContext(context, "post", "register", response);
        Assert.assertEquals(response.getStatusCode(), 200);
        ApiTestSupport.loggerAddNode(logger, "Status code is 200", Status.PASS, "Pass");

        String token = (String) ApiTestSupport.getValueFromResponse(response, "token");
        Assert.assertNotNull(token);
        ApiTestSupport.loggerAddNode(logger, "Token is included", Status.PASS, "Pass");
        Auth.token = token;
    }

    /**
     * Registration unsuccessful scenario. Missing password.
     *
     * @param context
     */
    @Test
    public void registrationUnsuccessful(ITestContext context) {
        //Send Post Request
        Response response = ApiTestSupport.postRequest(request, "register", Auth.authFailedCredentials, Auth.contentHeaders);
        String jsonBody = response.asString();

        //Set Test Context
        ApiTestSupport.setTestContext(context, "post", "register", response);
        Assert.assertEquals(response.getStatusCode(), 400);
        ApiTestSupport.loggerAddNode(logger, "Status code is 400", Status.PASS, "Pass");
    }

    /**
     * Creation of user using json file located at source folder.
     * take the response do a schema validation against a user json schema file.
     *
     * @param context
     * @throws FileNotFoundException
     */
    @Test
    public void createUser(ITestContext context) throws FileNotFoundException {
        //Send Post Request
        String body = ApiTestSupport.jsonFileToJsonString("testdata/newUser.json", new User1());
        System.out.println(body);
        ApiTestSupport.setHeaders(request, Auth.contentHeaders);
        Response response = ApiTestSupport.postRequest(request, "users", body);

        String json = response.body().asPrettyString();
        //Set Test Context
        ApiTestSupport.setTestContext(context, "post", "users", response);

        ApiTestSupport.jsonSchemaValidator(response, "validators/userSchema.json");
        user1 = (User1) ApiTestSupport.jsonStringToObject(json, new User1());
        System.out.println(user1.getId());
    }

    /**
     * Update user using put method and test data are taken from json file.
     *
     * @param context
     * @throws FileNotFoundException
     */
    @Test(dependsOnMethods = "createUser")
    public void updateUserPutMethod(ITestContext context) throws FileNotFoundException {
        //Send Put Request
        String body = ApiTestSupport.jsonFileToJsonString("testdata/updatePutUser.json", new User1());
        ApiTestSupport.setHeaders(request, Auth.contentHeaders);
        Response response = ApiTestSupport.putRequest(request, "users/" + user1.getId(), body);
        //Set Test Context
        ApiTestSupport.setTestContext(context, "patch", "users/" + user1.getId(), response);
    }

    /**
     * Update user using patch method and test data are taken from json file.
     *
     * @param context
     * @throws FileNotFoundException
     */
    @Test(dependsOnMethods = "createUser")
    public void updateUserPatchMethod(ITestContext context) throws FileNotFoundException {
        //Send Put Request
        String body = ApiTestSupport.jsonFileToJsonString("testdata/updatePatchUser.json", new User1());
        ApiTestSupport.setHeaders(request, Auth.contentHeaders);
        Response response = ApiTestSupport.patchRequest(request, "users/" + user1.getId(), body);

        //Set Test Context
        ApiTestSupport.setTestContext(context, "patch", "users/" + user1.getId(), response);
    }

    /**
     * Perform a user deletion.
     *
     * @param context
     */
    @Test(priority = 2, dependsOnMethods = "createUser")
    public void deleteUser(ITestContext context) {
        //Send Delete Request
        Response response = ApiTestSupport.deleteRequest(request, "users/{userId}", Map.ofEntries(
                Map.entry("userId", user1.getId())
        ));

        //Set Test Context
        ApiTestSupport.setTestContext(context, "delete", "users/" + user1.getId(), response);
    }
}
