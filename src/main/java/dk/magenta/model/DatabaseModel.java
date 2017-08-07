package dk.magenta.model;

import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;

import java.util.*;
import java.util.stream.Stream;

public interface DatabaseModel {

    public static String RM_MODEL_URI = "http://www.rm.dk/model/database/1.0";
    public static String RM_MODEL_PREFIX = "rm";
    public static String DATABASE = "database";

    public static QName PROP_DATABASE_TYPE = QName.createQName(RM_MODEL_URI, "databaseType");
    public static QName PROP_CREATION_DATE = QName.createQName(RM_MODEL_URI, "creationDate");

    public static String TYPE_PSYC_DEC = "forensicPsychiatryDeclaration";
    public static String TYPE_PSYC_DEC_KEY = "caseNumber";
    public static String TYPE_PSYC_SITENAME = "retspsyk";

    // Prop Strings
    public static String NAME = "name";
    public static String ENTRY_KEY = "entryKey";
    public static String UUID = "node-uuid";

    // Containers
    public static String PROP_VALUES = "propertyValues";


    /**
     * caselink properties
     */
    public static String CONTENT_MODEL_URI = "http://www.alfresco.org/model/content/1.0";
    public static QName PROP_LINK = QName.createQName(CONTENT_MODEL_URI, "link");
    public static QName PROP_LINK_TARGET = QName.createQName(CONTENT_MODEL_URI, "targetproject");
    public static QName PROP_LINK_TARGET_NODEREF = QName.createQName(CONTENT_MODEL_URI, "targetproject_noderef");

}
