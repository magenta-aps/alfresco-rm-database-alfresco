package dk.magenta.webscripts.entry;

import dk.magenta.Const;
import dk.magenta.TestUtils;
import dk.magenta.model.DatabaseModel;
import dk.magenta.webscripts.users.UserHelperTest;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RunWith(value = AlfrescoTestRunner.class)
public class LockTest extends AbstractAlfrescoIT {

    private static Logger log = Logger.getLogger(LockTest.class);

    private UserHelperTest helper = UserHelperTest.getInstance();
    private JSONObject jsonObject;


    public LockTest() {
        super();
    }

    @After
    public void tearDown() throws Exception {
        // todo: delete the entry just created
    }

    @Test
    public void testLockEntry() throws IOException, JSONException, InterruptedException {

    }

    @Test
    public void testUnLockEntry() throws IOException, JSONException, InterruptedException {

    }

    @Test
    public void testNotAbleToEditLockedEntry() throws IOException, JSONException, InterruptedException {

    }

}