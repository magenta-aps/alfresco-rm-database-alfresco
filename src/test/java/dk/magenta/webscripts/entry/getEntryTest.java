package dk.magenta.webscripts.entry;

import dk.magenta.Const;
import dk.magenta.TestUtils;
import dk.magenta.model.DatabaseModel;
import dk.magenta.webscripts.users.UserHelperTest;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RunWith(value = AlfrescoTestRunner.class)
public class getEntryTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(getEntryTest.class);

    private UserHelperTest helper = UserHelperTest.getInstance();
    private JSONObject jsonObject;


    public getEntryTest() {
        super();
    }

    @After
    public void tearDown() throws Exception {
        log.debug("DeleteEntryTest.testDeleteEntry");

        String uuid = jsonObject.getString(DatabaseModel.UUID);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(helper.getProviderForAdmin())
                .build()) {
            HttpDelete http = new HttpDelete("http://localhost:8080/alfresco/service/entry/" + uuid);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);
            System.out.println("deleted - " + jsonObject.getString(DatabaseModel.CASENUMBER));

            Assert.assertTrue("Assert status is present.", returnJSON.has("status"));
            Assert.assertTrue("Assert status equals success", "success".equals(returnJSON.getString("status")));
        }
    }

    @Test
    public void testGetEntry() throws IOException, JSONException, InterruptedException {
        log.debug("DeleteEntryTest.testDeleteEntry");

        jsonObject = TestUtils.addEntry(helper.getProviderForRegularUser());
        String caseNumber = jsonObject.getString(DatabaseModel.CASENUMBER);

        System.out.println("sleep");
        TimeUnit.MINUTES.sleep(1); // make sure the solr indexed the new declaration
        System.out.println("wake up");

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(helper.getProviderForRegularUser())
                .build()) {
            HttpGet http = new HttpGet(Const.HOST + "/alfresco/s/database/retspsyk/entry/" + caseNumber);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);

            Assert.assertTrue("caseNumber is present.", returnJSON.has("caseNumber"));
            Assert.assertTrue("caseNumber equals " + caseNumber, caseNumber.equals(returnJSON.getString("caseNumber")));
        }
    }
}