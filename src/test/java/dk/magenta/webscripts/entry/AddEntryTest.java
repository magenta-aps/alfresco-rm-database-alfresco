package dk.magenta.webscripts.entry;

import dk.magenta.TestUtils;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.TypeUtils;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

@RunWith(value = AlfrescoTestRunner.class)
public class AddEntryTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(AddEntryTest.class);
    private CredentialsProvider provider = new BasicCredentialsProvider();

    public AddEntryTest() {
        super();

        // Login credentials for Alfresco Repo
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);
    }

    @Test
    public void testAddEntry() throws IOException, JSONException {
        log.debug("AddEntryTest.testAddEntry");

        JSONObject returnJSON = TestUtils.addEntry(provider);
        String type = DatabaseModel.TYPE_PSYC_DEC;
        Assert.assertTrue("Assert entryKey is present.", returnJSON.has(TypeUtils.getEntryKey(type)));
        Assert.assertTrue("Assert uuid is present.", returnJSON.has(DatabaseModel.UUID));
        for (Map.Entry<String, String> propEntry : TestUtils.originalProps().entrySet()) {
            String key = propEntry.getKey();
            String value = propEntry.getValue();
            Assert.assertTrue("Assert " + key + " is present.", returnJSON.has(key));
            Assert.assertTrue("Assert " + key + " equals success", value.equals(returnJSON.getString(key)));
        }
    }
}