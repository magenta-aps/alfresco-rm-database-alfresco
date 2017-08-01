package dk.magenta.webscripts.entry;

import dk.magenta.TestUtils;
import dk.magenta.model.DatabaseModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(value = AlfrescoTestRunner.class)
public class DeleteEntryTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(DeleteEntryTest.class);
    private CredentialsProvider provider = new BasicCredentialsProvider();


    public DeleteEntryTest() {
        super();

        // Login credentials for Alfresco Repo
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);
    }

    @Test
    public void testDeleteEntry() throws IOException, JSONException {
        log.debug("DeleteEntryTest.testDeleteEntry");

        JSONObject jsonObject = TestUtils.addEntry(provider);
        String uuid = jsonObject.getString(DatabaseModel.UUID);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpDelete http = new HttpDelete("http://localhost:8080/alfresco/service/entry?uuid=" + uuid);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);

            Assert.assertTrue("Assert status is present.", returnJSON.has("status"));
            Assert.assertTrue("Assert status equals success", "success".equals(returnJSON.getString("status")));
        }

    }
}