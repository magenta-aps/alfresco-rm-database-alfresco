package dk.magenta.webscripts.users;

import dk.magenta.Const;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */

@RunWith(value = AlfrescoTestRunner.class)
public class ActivatedTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(ActivatedTest.class);
    private CredentialsProvider provider = new BasicCredentialsProvider();

    public ActivatedTest() {
        super();

        // Login credentials for Alfresco Repo
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);
    }



    @Test
    public void isUserActivated() throws IOException, JSONException {

//
//    // Execute Web Script call
//    try (CloseableHttpClient httpclient = HttpClientBuilder.create()
//            .setDefaultCredentialsProvider(provider)
//            .build()) {
//        HttpPost http = new HttpPost(Const.HOST + "/alfresco/s/isActivated?userName=\" + user");
//
//        StringEntity se = new StringEntity( o.toString());
//        se.setContentType(new BasicHeader("Content-type", "application/json"));
//        http.setEntity(se);
//
//        HttpResponse httpResponse = httpclient.execute(http);
//        String s = EntityUtils.toString(httpResponse.getEntity());
//    }
    }





}
