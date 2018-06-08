package dk.magenta;

import dk.magenta.model.DatabaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
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

    public static Map<QName, String> originalProps() {
        return new HashMap<QName, String>() {
            {
                put(DatabaseModel.PROP_MOTHER_ETHINICITY, "Svensk");
                put(DatabaseModel.PROP_DOCTOR, "Doctor New Name");
                put(DatabaseModel.PROP_DECLARATION_DATE, "2018-08-03T00:00:00.000Z");
                put(DatabaseModel.PROP_CLOSED, "true");
                put(DatabaseModel.PROP_OBSERVATION_DATE, "2018-07-20T00:00:00.000Z");
                put(DatabaseModel.PROP_CREATION_DATE, "2018-02-08T00:00:00.000Z");
                put(DatabaseModel.PROP_CLOSED_WITHOUT_DECLARATION, "true");
            }
        };
    }

    public static Map<QName, String> updatedProps() {
        return new HashMap<QName, String>() {
            {
                put(DatabaseModel.PROP_MOTHER_ETHINICITY, "Dansk");
                put(DatabaseModel.PROP_DOCTOR, "Emanuel");
                put(DatabaseModel.PROP_DECLARATION_DATE, "2018-12-03T00:00:00.000Z");
                put(DatabaseModel.PROP_CLOSED, "false");
                put(DatabaseModel.PROP_OBSERVATION_DATE, "2019-10-03T00:00:00.000Z");
                put(DatabaseModel.PROP_CLOSED_WITHOUT_DECLARATION, "false");
            }
        };
    }

    public static JSONObject addEntry(CredentialsProvider provider) throws JSONException, IOException {
        String type = DatabaseModel.TYPE_PSYC_DEC;

        JSONObject properties = new JSONObject();
        for (Map.Entry<QName, String> propEntry : originalProps().entrySet()) {
            String key = propEntry.getKey().getLocalName();
            String value = propEntry.getValue();
            properties.put(key, value);
        }

        JSONObject data = new JSONObject();
        data.put("type", type);
        data.put("properties", properties);

        // Execute Web Script call
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPost http = new HttpPost("http://localhost:8080/alfresco/service/database/retspsyk/entry");

            StringEntity se = new StringEntity( data.toString());
            se.setContentType(new BasicHeader("Content-type", "application/json"));
            http.setEntity(se);

            HttpResponse httpResponse = httpclient.execute(http);

            String s = EntityUtils.toString(httpResponse.getEntity());

            return new JSONObject(s);
        }
    }

}
