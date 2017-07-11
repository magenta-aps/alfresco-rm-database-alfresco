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

    public static String TYPE_FORENSIC_PSYC_DEC = "forensicPsychiatryDeclaration";

    public static QName PROP_CASE_NUMBER = QName.createQName(RM_MODEL_URI, "caseNumber");
    public static QName PROP_LINK = QName.createQName(CONTENT_MODEL_URI, "link");

    /**
     * caselink properties
     */
    public static QName PROP_LINK_TARGET = QName.createQName(CONTENT_MODEL_URI, "targetproject");
    public static QName PROP_LINK_TARGET_NODEREF = QName.createQName(CONTENT_MODEL_URI, "targetproject_noderef");

    // Containers
    public static String DOC_LIBRARY = "documentLibrary";

}
