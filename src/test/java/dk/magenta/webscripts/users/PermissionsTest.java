package dk.magenta.webscripts.users;

import dk.magenta.Const;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
public class PermissionsTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(PermissionsTest.class);

    private String username = "validateNonAdminAbleToSetRole";
    private String password = "validateNonAdminAbleToSetRole";


    private UserHelperTest helper = UserHelperTest.getInstance();

    public PermissionsTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
        helper.deleteUser(username);

    }

    @Test
    public void validateNonAdminAbleToSetRole() throws Exception {
        helper.createUser(username, password);
        helper.activateUser(username);
        helper.setRoleForUser(helper.getProviderForRegularUser(), "GROUP_site_retspsyk_SiteRoleManager", username);
    }

    @Test
    public void validateNonAdminNotAbleToSetRole() throws Exception {

        CredentialsProvider provider = new BasicCredentialsProvider();

        helper.createUser(username, password);
        helper.activateUser(username);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);


        JSONObject o = new JSONObject();
        JSONArray addGroups = new JSONArray();
        addGroups.put("GROUP_site_retspsyk_SiteRoleManager");
        JSONArray removeGroups = new JSONArray();
        o.put("addGroups" , addGroups);
        o.put("removeGroups" , removeGroups);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPut http = new HttpPut(Const.HOST + "/alfresco/s/database/retspsyk/user/" + username);

            StringEntity se = new StringEntity( o.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());

            JSONObject returnJSON = new JSONObject(s);

            Assert.assertTrue(!returnJSON.has("status"));
        }

    }






}
