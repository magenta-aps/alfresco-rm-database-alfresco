package dk.magenta.model;

import org.alfresco.service.namespace.QName;
import org.json.simple.JSONArray;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

public interface DatabaseModel {

    String RM_MODEL_URI = "http://www.rm.dk/model/database/1.0";
    String RM_MODEL_PREFIX = "rm";
    String DATABASE = "database";

    QName PROP_DATABASE_TYPE = QName.createQName(RM_MODEL_URI, "databaseType");

    QName PROP_CASE_NUMBER = QName.createQName(RM_MODEL_URI, "caseNumber");
    QName PROP_CASE_NUMBER_OLD = QName.createQName(RM_MODEL_URI, "oldcaseNumber");
    QName PROP_JOURNALNUMMER = QName.createQName(RM_MODEL_URI, "journalNumber");
    QName PROP_STATUS = QName.createQName(RM_MODEL_URI, "status");
    QName PROP_CLOSED = QName.createQName(RM_MODEL_URI, "closed");
    QName PROP_CPR = QName.createQName(RM_MODEL_URI, "cprNumber");
    QName PROP_FIRST_NAME = QName.createQName(RM_MODEL_URI, "firstName");
    QName PROP_LAST_NAME = QName.createQName(RM_MODEL_URI, "lastName");
    QName PROP_FULL_NAME = QName.createQName(RM_MODEL_URI, "fullName");
    QName PROP_ADDRESS = QName.createQName(RM_MODEL_URI, "address");
    QName PROP_POSTCODE = QName.createQName(RM_MODEL_URI, "postbox");
    QName PROP_CITY = QName.createQName(RM_MODEL_URI, "city");
    QName PROP_ETHNICITY = QName.createQName(RM_MODEL_URI, "ethnicity");
    QName PROP_MOTHER_ETHINICITY = QName.createQName(RM_MODEL_URI, "motherEthnicity");
    QName PROP_FATHER_ETHINICITY = QName.createQName(RM_MODEL_URI, "fatherEthnicity");
    QName PROP_REFERING_AGENCY = QName.createQName(RM_MODEL_URI, "referingAgency");
    QName PROP_MAIN_CHARGE = QName.createQName(RM_MODEL_URI, "mainCharge");
    QName PROP_PLACEMENT = QName.createQName(RM_MODEL_URI, "placement");
    QName PROP_SANCTION_PROPOSAL = QName.createQName(RM_MODEL_URI, "sanctionProposal");

    /* History */
    QName PROP_CREATION_DATE = QName.createQName(RM_MODEL_URI, "creationDate");
    QName PROP_OBSERVATION_DATE = QName.createQName(RM_MODEL_URI, "observationDate");
    QName PROP_DECLARATION_DATE = QName.createQName(RM_MODEL_URI, "declarationDate");
    QName PROP_CLOSED_WITHOUT_DECLARATION = QName.createQName(RM_MODEL_URI, "closedWithoutDeclaration");
    QName PROP_CLOSED_WITHOUT_DECLARATION_REASON = QName.createQName(RM_MODEL_URI, "closedWithoutDeclarationReason");
    QName PROP_CLOSED_WITHOUT_DECLARATION_SENT_TO = QName.createQName(RM_MODEL_URI, "closedWithoutDeclarationSentTo");
    QName PROP_FORENSIC_DOCTOR_COUNCIL = QName.createQName(RM_MODEL_URI, "forensicDoctorCouncil");
    QName PROP_FORENSIC_DOCTOR_COUNCIL_TEXT = QName.createQName(RM_MODEL_URI, "forensicDoctorCouncilText");
    QName PROP_FINAL_VERDICT = QName.createQName(RM_MODEL_URI, "finalVerdict");
    QName PROP_FINAL_VERDICT_NOTE = QName.createQName(RM_MODEL_URI, "finalVerdictNote");
    QName PROP_REMARKS = QName.createQName(RM_MODEL_URI, "remarks");

    /* Declaration made by */
    QName PROP_DOCTOR = QName.createQName(RM_MODEL_URI, "doctor");
    QName PROP_PSYCHOLOGIST = QName.createQName(RM_MODEL_URI, "psychologist");

    /* ICD-10 diagnosis */
    QName PROP_MAIN_DIAGNOSIS = QName.createQName(RM_MODEL_URI, "mainDiagnosis");
    QName PROP_BI_DIAGNOSIS = QName.createQName(RM_MODEL_URI, "biDiagnoses");

    /* Waiting time */
    QName PROP_WAITING_PASSIVE = QName.createQName(RM_MODEL_URI, "waiting_passive");
    QName PROP_WAITING_ACTIVE = QName.createQName(RM_MODEL_URI, "waiting_active");
    QName PROP_WAITING_TOTAL = QName.createQName(RM_MODEL_URI, "waiting_total");

    /* Generic */
    QName PROP_LOCKED_FOR_EDIT = QName.createQName(RM_MODEL_URI, "locked4edit");
    QName PROP_LOCKED_FOR_EDIT_BY = QName.createQName(RM_MODEL_URI, "locked4editBy");


    public static QName PROP_ENTRIES = QName.createQName(RM_MODEL_URI, "maillog_entries");

    public static String TYPE_PSYC_DEC = "forensicPsychiatryDeclaration";
    public static String TYPE_PSYC_DEC_KEY = "caseNumber";
    public static String TYPE_PSYC_SITENAME = "retspsyk";

    /* Aspects */
    QName ASPECT_BUA = QName.createQName(RM_MODEL_URI, "bua");

    // Prop Strings
    String NAME = "name";
    String ENTRY_KEY = "entryKey";
    String UUID = "node-uuid";
    String CASENUMBER = "caseNumber";

    // Containers
    String PROP_VALUES = "propertyValues";
    String PROP_TEMPLATE_LIBRARY = "documentTemplates";

    // template docs
    String PROP_TEMPLATE_DOC_SAMTYKKE = "samtykke.odt";
    String PROP_TEMPLATE_DOC_KENDELSE = "kendelse.odt";


    /**
     * caselink properties
     */
    String CONTENT_MODEL_URI = "http://www.alfresco.org/model/content/1.0";
    QName PROP_LINK = QName.createQName(CONTENT_MODEL_URI, "link");
    QName PROP_LINK_TARGET = QName.createQName(CONTENT_MODEL_URI, "targetproject");
    QName PROP_LINK_TARGET_NODEREF = QName.createQName(CONTENT_MODEL_URI, "targetproject_noderef");

    // custom permissions

    String Permission_SiteTemplateManager = "SiteTemplateManager";



    QName ASPECT_SENDMAILLOGS = QName.createQName(RM_MODEL_URI, "sendMaillogs");



}
