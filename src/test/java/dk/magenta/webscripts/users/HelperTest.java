package dk.magenta.webscripts.users;

import dk.magenta.Const;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
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
import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */



@RunWith(value = AlfrescoTestRunner.class)
public class HelperTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(HelperTest.class);
    private CredentialsProvider provider = new BasicCredentialsProvider();

    private static HelperTest instance;

    static {
        instance = new HelperTest();
    }

    public static HelperTest getInstance() {
        return instance;
    }

    private HelperTest() {
        super();

        // Login credentials for Alfresco Repo
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);
    }


    public void deleteGenericUser() throws IOException, JSONException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpDelete http = new HttpDelete(Const.HOST + "/alfresco/s/api/people/" + Const.GENERIC_USERNAME);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);
        }
    }

    @Test
    public void createGenericUser() throws IOException, JSONException {
        log.debug("Helper.createGenericUser");

        JSONObject o = new JSONObject();
        o.put("userName", Const.GENERIC_USERNAME);
        o.put("firstName", "Generic");
        o.put("lastName", "Son");
        o.put("email", "generic@magenta.dk");
        o.put("password", Const.GENERIC_PASSWORD);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPost http = new HttpPost(Const.HOST + "/alfresco/s/api/people");

            StringEntity se = new StringEntity( o.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());

            JSONObject returnJSON = new JSONObject(s);

            Assert.assertTrue("Assert userName returned", returnJSON.has("userName"));
            Assert.assertTrue("Assert username equals Const.GENERIC_USERNAME", returnJSON.get("userName").equals(Const.GENERIC_USERNAME));


        }
    }

    @Test
    public void activateGenericUser() throws IOException, JSONException {
        log.debug("Helper.createGenericUser");


        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpGet http = new HttpGet(Const.HOST + "/alfresco/s/activateUser?userName=" + Const.GENERIC_USERNAME);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);


            Assert.assertTrue("Assert status is present.", returnJSON.has("status"));
            Assert.assertTrue("Assert status equals success", "success".equals(returnJSON.getString("status")));
        }
    }



    public static void main(String[] args) throws IOException, JSONException {
        HelperTest h = HelperTest.getInstance();
        h.createGenericUser();
        h.activateGenericUser();
        h.deleteGenericUser();

    }
}
