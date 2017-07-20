package dk.magenta.model;

import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;

import java.util.*;
import java.util.stream.Stream;

public interface DatabaseModel {

    public static String CONTENT_MODEL_URI = "http://www.alfresco.org/model/content/1.0";
    public static String RM_MODEL_URI = "http://www.rm.dk/model/database/1.0";
    public static String RM_MODEL_PREFIX = "rm";

    public static QName PROP_LINK = QName.createQName(CONTENT_MODEL_URI, "link");

    public static String TYPE_PSYC_DEC = "forensicPsychiatryDeclaration";
    public static String TYPE_PSYC_DEC_KEY = "caseNumber";
    public static String TYPE_PSYC_DEC_SITE = "retspsyk";

    // Type Strings
    public static String NAME = "name";
    public static String ENTRY_KEY = "entryKey";
    public static String UUID = "node-uuid";



    /**
     * caselink properties
     */
    public static QName PROP_LINK_TARGET = QName.createQName(CONTENT_MODEL_URI, "targetproject");
    public static QName PROP_LINK_TARGET_NODEREF = QName.createQName(CONTENT_MODEL_URI, "targetproject_noderef");

    // Containers
    public static String DOC_LIBRARY = "documentLibrary";

    //Folder paths

    public static List<String> PROP_VALUES_PATH = new ArrayList<>(
            Arrays.asList("Data Dictionary", "Database Extension", "Property Values")
    );

}
