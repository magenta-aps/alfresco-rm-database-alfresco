package dk.magenta.webscripts.flowchart;

import dk.magenta.Const;
import dk.magenta.beans.EntryBean;
import dk.magenta.beans.FlowChartBean;
import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.TypeUtils;
import dk.magenta.webscripts.users.UserHelperTest;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.namespace.QName;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.alfresco.util.ApplicationContextHelper.getApplicationContext;

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */

@RunWith(value = AlfrescoTestRunner.class)
public class QueryTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(QueryTest.class);

    private UserHelperTest helper = UserHelperTest.getInstance();

    private CredentialsProvider provider = new BasicCredentialsProvider();
    public ApplicationContext appContext = getApplicationContext();

    private EntryBean entryBean = (EntryBean) appContext.getBean("entryBean");

    private DataProducer dataProducer = new DataProducer();

    public QueryTest() throws JSONException {
        super();


    }

    @Before
    public void setUp() throws Exception {

        // Login credentials for Alfresco Repo
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);

    }

    @After
    public void tearDown() throws Exception {
    }

//    @Test
//    public void OngoingTest() throws Exception {
//
//        dataProducer.wipeAllCases();
//        dataProducer.createDeclarationsForOngoingTest();
//
//        JSONObject properties = new JSONObject();
//        properties.put("method", "ongoing");
//
//        JSONObject data = new JSONObject();
//
//        data.put("properties", properties);
//
//        // Execute Web Script call
//        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
//                .setDefaultCredentialsProvider(provider)
//                .build()) {
//
//            HttpPost http = new HttpPost("http://localhost:8080/alfresco/service/database/retspsyk/flowchart");
//
//            StringEntity se = new StringEntity(data.toString());
//            se.setContentType(new BasicHeader("Content-type", "application/json"));
//            http.setEntity(se);
//
//            HttpResponse httpResponse = httpclient.execute(http);
//
//            String s = EntityUtils.toString(httpResponse.getEntity());
//
//            JSONObject result = new JSONObject(s);
//
//            Assert.assertTrue("Assert total is equal to 60" + " result was : " + result.getString("total"), result.getString("total").equals("60"));
//        }
//    }

    @Test
    public void ArrestantStateTest() throws Exception {

        dataProducer.wipeAllCases();
        dataProducer.createDeclarationsForStateArrestanterTest();

        JSONObject properties = new JSONObject();
        properties.put("method", "arrestanter");

        JSONObject data = new JSONObject();

        data.put("properties", properties);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpPost http = new HttpPost("http://localhost:8080/alfresco/service/database/retspsyk/flowchart");

            StringEntity se = new StringEntity(data.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);

            String s = EntityUtils.toString(httpResponse.getEntity());

            JSONObject result = new JSONObject(s);

            Assert.assertTrue("Assert total is equal to 20" + " result was : " + result.getString("total"), result.getString("total").equals("20"));
        }
    }
}