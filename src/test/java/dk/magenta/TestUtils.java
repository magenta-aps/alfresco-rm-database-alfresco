package dk.magenta;

import dk.magenta.model.DatabaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static Map<String, String> originalProps() {
        return new HashMap<String, String>() {
            {
                put(PROP_MOTHER_ETHINICITY, "Svensk");
                put(PROP_DOCTOR1, "Doctor New Name");
                put(PROP_VERDICT_DATE, "2018-08-03T00:00:00.000Z");
                put(PROP_IS_CLOSED, "true");
                put(PROP_PETITION_DATE, "2018-07-20T00:00:00.000Z");
                put(PROP_ENDED_WITHOUT_DECLARATION, "true");
            }
        };
    }

    public static Map<String, String> updatedProps() {
        return new HashMap<String, String>() {
            {
                put(PROP_MOTHER_ETHINICITY, "Dansk");
                put(PROP_DOCTOR1, "Emanuel");
                put(PROP_VERDICT_DATE, "2018-12-03T00:00:00.000Z");
                put(PROP_IS_CLOSED, "false");
                put(PROP_PETITION_DATE, "2019-10-03T00:00:00.000Z");
                put(PROP_ENDED_WITHOUT_DECLARATION, "false");
            }
        };
    }

    private static String PROP_MOTHER_ETHINICITY = "motherEthnicity";
    private static String PROP_DOCTOR1 = "doctor1";
    private static String PROP_VERDICT_DATE = "verdictDate";
    private static String PROP_IS_CLOSED = "isClosed";
    private static String PROP_PETITION_DATE = "petitionDate";
    private static String PROP_ENDED_WITHOUT_DECLARATION = "endedWithoutDeclaration";

    public static JSONObject addEntry(CredentialsProvider provider) throws JSONException, IOException {
        String siteShortName = "retspsyk";
        String type = DatabaseModel.TYPE_PSYC_DEC;

        JSONObject properties = new JSONObject();
        for (Map.Entry<String, String> propEntry : originalProps().entrySet()) {
            String key = propEntry.getKey();
            String value = propEntry.getValue();
            properties.put(key, value);
        }

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
