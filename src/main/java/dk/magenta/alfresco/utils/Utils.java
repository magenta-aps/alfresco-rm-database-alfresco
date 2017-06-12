package dk.magenta.alfresco.utils;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Alfresco's (or Java's) query string parsing doesn't handle UTF-8
     * encoded values. We parse the query string ourselves here.
     * @param url
     * @return
     */
    public static Map<String, String> parseParameters(String url) {
        // Do our own parsing to get the query string since java.net.URI can't
        // handle some URIs
        int queryStringStart = url.indexOf('?');
        String queryString = "";
        if (queryStringStart != -1) {
            queryString = url.substring(queryStringStart+1);
        }
        Map<String, String> parameters = URLEncodedUtils
                .parse(queryString, Charset.forName("UTF-8"))
                .stream()
                .collect(
                        Collectors.groupingBy(
                                NameValuePair::getName,
                                Collectors.collectingAndThen(Collectors.toList(), Utils::paramValuesToString)));
        return parameters;
    }

    private static String paramValuesToString(List<NameValuePair> paramValues) {
        if (paramValues.size() == 1) {
            return paramValues.get(0).getValue();
        }
        List<String> values = paramValues.stream().map(NameValuePair::getValue).collect(Collectors.toList());
        return "[" + StringUtils.join(values, ",") + "]";
    }

    public static JSONArray getJSONError (Exception e) {
        Map<String, Serializable> map = new HashMap<>();
        map.put("error", e.getStackTrace()[0].toString());
        return getJSONReturnArray(map);
    }

    public static JSONArray getJSONReturnArray(Map<?, Serializable> map) {
        JSONObject return_json = new JSONObject();
        JSONArray result = new JSONArray();
        try {
            for (Map.Entry<?, Serializable> pair : map.entrySet())
                return_json.put(pair.getKey().toString(), pair.getValue().toString());
            result.add(return_json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void writeJSONArray (Writer writer, JSONArray result) {
        try {
            result.writeJSONString(writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONArray convertEntryToJSON (NodeService nodeService, NodeRef nodeRef) throws JSONException {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        return getJSONReturnArray(properties);
    }
}
