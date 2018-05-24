package dk.magenta.webscripts.users;

import dk.magenta.Const;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
public class CreateUserTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(CreateUserTest.class);


    private HelperTest helper = HelperTest.getInstance();

    public CreateUserTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        helper.createAndActivateGenericUser();
    }

    @After
    public void tearDown() throws Exception {
        helper.deleteGenericUser();
    }

    @Test
    public void isGenericUserActivated() throws Exception {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(helper.getProviderForAdmin()).build()) {

                HttpGet http = new HttpGet(Const.HOST + "/alfresco/s/isActivated?userName=" + Const.GENERIC_USERNAME);

                HttpResponse httpResponse = httpclient.execute(http);
                String s = EntityUtils.toString(httpResponse.getEntity());

                JSONObject returnJSON = new JSONObject(s);

                Assert.assertTrue("Attribute member is present.", returnJSON.has("member"));
                Assert.assertTrue("Member  equals true", "true".equals(returnJSON.getString("member")));
        }
    }


}
