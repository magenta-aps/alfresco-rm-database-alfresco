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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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

    private CredentialsProvider provider_regularUser = new BasicCredentialsProvider();

    private CredentialsProvider provider_admin = new BasicCredentialsProvider();


    public CredentialsProvider getProviderForRegularUser() {
        return provider_regularUser;
    }

    public CredentialsProvider getProviderForAdmin() {
        return provider_admin;
    }

    private static HelperTest instance;

    static {
        try {
            instance = new HelperTest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static HelperTest getInstance() {
        return instance;
    }

    private HelperTest() throws IOException, JSONException {
        super();


        // Login credentials for Alfresco Repo as Admin
        UsernamePasswordCredentials credentials_regularuser = new UsernamePasswordCredentials(Const.REGULAR_USER_USERNAME, Const.REGULAR_USER_PASSWORD);
        provider_regularUser.setCredentials(AuthScope.ANY, credentials_regularuser);

        // Login credentials for Alfresco Repo as RegularUser
        UsernamePasswordCredentials credentials_admin = new UsernamePasswordCredentials(Const.ADMIN_USERNAME, Const.ADMIN_PASSWORD);
        provider_admin.setCredentials(AuthScope.ANY, credentials_admin);


        // setup required users
        this.createUser(Const.REGULAR_USER_USERNAME, Const.REGULAR_USER_PASSWORD);
        this.createUser(Const.BSK_USERNAME, Const.BSK_PASSWORD);

        this.activateUser(Const.BSK_USERNAME);
        this.activateUser(Const.REGULAR_USER_USERNAME);

        this.setupRequiredRolesForAdminAndRegularUser();
    }


    public void deleteGenericUser() throws IOException, JSONException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider_admin)
                .build()) {
            HttpDelete http = new HttpDelete(Const.HOST + "/alfresco/s/api/people/" + Const.GENERIC_USERNAME);

            HttpResponse httpResponse = httpclient.execute(http);
            System.out.println("the response:");
            System.out.println(httpResponse.getStatusLine());
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);
            System.out.println("returnJSON");
            System.out.println(returnJSON);
        }
    }

    private void createUser(String username, String password) throws IOException, JSONException {

        JSONObject o = new JSONObject();
        o.put("userName", username);
        o.put("firstName", username);
        o.put("lastName", "Son");
        o.put("email", username + "@magenta.dk");
        o.put("password", password);
        o.put("enabled", true);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider_admin)
                .build()) {
            HttpPost http = new HttpPost(Const.HOST + "/alfresco/s/api/people");

            StringEntity se = new StringEntity( o.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());

            JSONObject returnJSON = new JSONObject(s);

            if (returnJSON.has("status")) {
                JSONObject status = returnJSON.getJSONObject("status");

                Assert.assertEquals("Status code equals 409", status.get("code"), 409);
                Assert.assertTrue("Assert username equals Const.REGULAR_USER_USERNAME", returnJSON.get("message").equals("User name already exists: " + username));

            }
            else {
                Assert.assertTrue("Assert userName returned", returnJSON.has("userName"));
                Assert.assertTrue("Assert username equals Const.GENERIC_USERNAME", returnJSON.get("userName").equals(username));
            }
        }
    }

    private void setupRequiredRolesForAdminAndRegularUser() throws IOException, JSONException {



        JSONObject o = new JSONObject();
        JSONArray addGroups = new JSONArray();
        addGroups.put("GROUP_site_retspsyk_SiteRoleManager");
        JSONArray removeGroups = new JSONArray();
        o.put("addGroups" , addGroups);
        o.put("removeGroups" , removeGroups);

        String input = "{\"type\":\"forensicPsychiatryDeclaration\",\"properties\":" + o.toString() + "}";


        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider_admin)
                .build()) {
            HttpPut http = new HttpPut(Const.HOST + "/alfresco/s/database/retspsyk/user/" + Const.ADMIN_USERNAME);

            StringEntity se = new StringEntity( input.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);

            Assert.assertEquals("success", returnJSON.getString("status"));
        }

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider_admin)
                .build()) {
            HttpPut http = new HttpPut(Const.HOST + "/alfresco/s/database/retspsyk/user/" + Const.REGULAR_USER_USERNAME);

            StringEntity se = new StringEntity( input.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());

            JSONObject returnJSON = new JSONObject(s);

            Assert.assertEquals("success", returnJSON.getString("status"));
        }
    }


    public void createAndActivateGenericUser() throws IOException, JSONException {
//        log.debug("Helper.createGenericUser");
//
//        JSONObject o = new JSONObject();
//        o.put("userName", Const.GENERIC_USERNAME);
//        o.put("firstName", "Generic");
//        o.put("lastName", "Son");
//        o.put("email", "generic@magenta.dk");
//        o.put("password", Const.GENERIC_PASSWORD);
//        o.put("enabled", true);
//
//        // Execute Web Script call
//        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
//                .setDefaultCredentialsProvider(provider_admin)
//                .build()) {
//            HttpPost http = new HttpPost(Const.HOST + "/alfresco/s/api/people");
//
//            StringEntity se = new StringEntity( o.toString());
//            se.setContentType(new BasicHeader("Content-type", "application/json"));
//            http.setEntity(se);
//
//            HttpResponse httpResponse = httpclient.execute(http);
//            String s = EntityUtils.toString(httpResponse.getEntity());
//            System.out.println("svar fra createGenericUser" + s);
//
//            JSONObject returnJSON = new JSONObject(s);
//
//            Assert.assertTrue("Assert userName returned", returnJSON.has("userName"));
//            Assert.assertTrue("Assert username equals Const.GENERIC_USERNAME", returnJSON.get("userName").equals(Const.GENERIC_USERNAME));
//        }

        this.createUser(Const.GENERIC_USERNAME, Const.GENERIC_PASSWORD);
        this.activateUser(Const.GENERIC_USERNAME);
    }

    public void activateUser(String username) throws IOException, JSONException {
        log.debug("Helper.createGenericUser");


        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider_admin)
                .build()) {
            HttpGet http = new HttpGet(Const.HOST + "/alfresco/s/activateUser?userName=" + username);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);


            Assert.assertTrue("Assert status is present.", returnJSON.has("status"));
            Assert.assertTrue("Assert status equals success", "success".equals(returnJSON.getString("status")));
        }
    }

    public void activateRegularUser() throws IOException, JSONException {
        log.debug("Helper.acticateRegularUser");


        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider_admin)
                .build()) {
            HttpGet http = new HttpGet(Const.HOST + "/alfresco/s/activateUser?userName=" + Const.REGULAR_USER_USERNAME);

            HttpResponse httpResponse = httpclient.execute(http);
            String s = EntityUtils.toString(httpResponse.getEntity());
            JSONObject returnJSON = new JSONObject(s);

            Assert.assertTrue("Assert status is present.", returnJSON.has("status"));
            Assert.assertTrue("Assert status equals success", "success".equals(returnJSON.getString("status")));
        }
    }


    @Test
    public static void main(String[] args) throws IOException, JSONException {
        HelperTest h = HelperTest.getInstance();
//        h.createUser(Const.GENERIC_USERNAME, Const.GENERIC_PASSWORD);
        h.createAndActivateGenericUser();
//        h.deleteGenericUser();
//        h.createRegularUser();

        h.setupRequiredRolesForAdminAndRegularUser();

    }
}


