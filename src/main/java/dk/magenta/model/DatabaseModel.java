package dk.magenta.model;

import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface DatabaseModel {

    public static String CONTENT_MODEL_URI = "http://www.alfresco.org/model/content/1.0";
    public static String RM_MODEL_URI = "http://www.rm.dk/model/database/1.0";
    public static String RM_MODEL_PREFIX = "rm";

    public static QName PROP_CASE_NUMBER = QName.createQName(RM_MODEL_URI, "caseNumber");

}
