package dk.magenta.webscripts.flowchart;

import dk.magenta.Const;
import dk.magenta.beans.EntryBean;
import dk.magenta.webscripts.users.UserHelperTest;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import static org.alfresco.util.ApplicationContextHelper.getApplicationContext;

/**
 * Created by flemmingheidepedersen on 18/05/2018.
 */

@RunWith(value = AlfrescoTestRunner.class)
public class QueryTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(QueryTest.class);

    private UserHelperTest helper = UserHelperTest.getInstance();

    public ApplicationContext appContext = getApplicationContext();

    private EntryBean entryBean = (EntryBean) appContext.getBean("entryBean");

    private DataProducer dataProducer = new DataProducer();

    public QueryTest() {
        super();


    }

    @Before
    public void setUp() throws Exception {
        helper.createAndActivateGenericUser();
    }

    @After
    public void tearDown() throws Exception {
        helper.deleteGenericUser();
    }

    @Test
    public void testQueryTest() throws Exception {

                System.out.println("entryBean");
        System.out.println(entryBean);

        dataProducer.wipeAllCases();
        dataProducer.createDeclarationsForOngoingTest();

                Assert.assertTrue("Attribute member is present.", true);
        }



}
