package dk.magenta.webscripts.users;

import dk.magenta.Const;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */

@RunWith(value = AlfrescoTestRunner.class)
public class CreateUserFromBSKTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(CreateUserFromBSKTest.class);


    private UserHelperTest helper = UserHelperTest.getInstance();

    public CreateUserFromBSKTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
         UserHelperTest userHelperTest = UserHelperTest.getInstance();
         userHelperTest.deleteUser("createdByBSK");

    }

    @Test
    public void createUserFromBSKValidateNotActivated() throws Exception {


        String query = "?userName=createdByBSK&firstName=user_from_bsk&lastName=sen&email=createdByBS@magenta.dk&password=createdByBSK";


        // create the user
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(helper.getProviderForBSK()).build()) {

                HttpGet http = new HttpGet(Const.HOST + "/alfresco/s/createUser" + query);

                HttpResponse httpResponse = httpclient.execute(http);
                String s = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("hvad er return" + s);

                JSONObject returnJSON = new JSONObject(s);


                Assert.assertTrue("Attribute status is present.", returnJSON.has("status"));
                Assert.assertTrue("Status  equals success", "success".equals(returnJSON.getString("status")));
        }

        // verify that the user is not activated
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(helper.getProviderForBSK()).build()) {

            HttpGet http = new HttpGet(Const.HOST + "/alfresco/s/isActivated?userName=createdByBSK");


            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());

            JSONObject returnJSON = new JSONObject(s);

            Assert.assertTrue("Attribute member is present.", returnJSON.has("member"));
            Assert.assertTrue("member  equals false", "false".equals(returnJSON.getString("member")));
        }

    }


}
