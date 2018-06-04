package dk.magenta.webscripts.entry;

import dk.magenta.TestUtils;
import dk.magenta.model.DatabaseModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.service.namespace.QName;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

@RunWith(value = AlfrescoTestRunner.class)
public class UpdateEntryTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(UpdateEntryTest.class);
    private CredentialsProvider provider = new BasicCredentialsProvider();


    public UpdateEntryTest() {
        super();

        // Login credentials for Alfresco Repo
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);
    }

    @Test
    public void testUpdateEntry() throws IOException, JSONException {
        log.debug("UpdateEntryTest.testUpdateEntry");

        JSONObject jsonObject = TestUtils.addEntry(provider, TestUtils.originalProps());
        String uuid = jsonObject.getString(DatabaseModel.UUID);

        JSONObject properties = new JSONObject();
        for (Map.Entry<QName, String> propEntry : TestUtils.updatedProps().entrySet()) {
            String key = propEntry.getKey().getLocalName();
            String value = propEntry.getValue();
            properties.put(key, value);
        }

        JSONObject data = new JSONObject();
        data.put("properties", properties);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPut http = new HttpPut("http://localhost:8080/alfresco/service/entry/" + uuid);

            StringEntity se = new StringEntity( data.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);

            for (Map.Entry<QName, String> propEntry : TestUtils.updatedProps().entrySet()) {
                String key = propEntry.getKey().getLocalName();
                String value = propEntry.getValue();
                Assert.assertTrue("Assert " + key + " is present.", returnJSON.has(key));
                Assert.assertTrue("Assert " + key + " equals success", value.equals(returnJSON.getString(key)));
            }
        }

    }
}