package dk.magenta.webscripts.entries;

import dk.magenta.TestUtils;
import dk.magenta.model.DatabaseModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(value = AlfrescoTestRunner.class)
public class AddEntryTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(AddEntryTest.class);

    private SiteService siteService = getServiceRegistry().getSiteService();
    private AuthorityService authorityService = getServiceRegistry().getAuthorityService();

    private Map<String, SiteInfo> sites = new HashMap<>();
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

        String siteShortName = "retspsyk";
        String type = DatabaseModel.TYPE_FORENSIC_PSYC_DEC;

        JSONObject properties = new JSONObject();
        properties.put("motherEthnicity", "Svensk");
        properties.put("doctor1", "Doctor New Name");
        properties.put("verdictDate", "2018-08-3T00:00:00.000Z");
        properties.put("isClosed", "true");
        properties.put("petitionDate", "2018-07-20T00:00:00.000Z");
        properties.put("endedWithoutDeclaration", "true");

        assertWebScript(siteShortName, type, properties);
    }

    private JSONObject assertWebScript (String siteShortName, String type, JSONObject properties) throws IOException, JSONException {
        JSONObject returnJSON = executeWebScript(siteShortName, type, properties);
        Assert.assertTrue("Assert caseNumber is present.", returnJSON.has("caseNumber"));
        Assert.assertTrue("Assert status is present.", returnJSON.has("status"));
        Assert.assertTrue("Assert status equals success", "success".equals(returnJSON.getString("status")));
        return returnJSON;
    }

    private JSONObject executeWebScript (String siteShortName, String type, JSONObject properties) throws IOException, JSONException {
        JSONObject data = new JSONObject();
        data.put("siteShortName", siteShortName);
        data.put("type", type);
        data.put("properties", properties);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPost http = new HttpPost("http://localhost:8080/alfresco/service/entry");

            StringEntity se = new StringEntity( data.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            return new JSONObject(s);
        }
    }
}