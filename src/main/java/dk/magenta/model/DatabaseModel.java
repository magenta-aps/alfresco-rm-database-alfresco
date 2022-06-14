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
    QName PROP_CLOSED_DATE = QName.createQName(RM_MODEL_URI, "closedDate");
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


    /* Fields from flowchart */
    QName PROP_FLOW_ARREST = QName.createQName(RM_MODEL_URI, "arrest");
    QName PROP_FLOW_PSYKOLOGFOKUS = QName.createQName(RM_MODEL_URI, "psykologfokus");
    QName PROP_FLOW_SAMTYKKEOPL = QName.createQName(RM_MODEL_URI, "samtykkeopl");
    QName PROP_FLOW_TOLKSPROG = QName.createQName(RM_MODEL_URI, "tolksprog");
    QName PROP_FLOW_KOMMENTAR = QName.createQName(RM_MODEL_URI, "kommentar");
    QName PROP_FLOW_OPLYSNINGEREKSTERNT = QName.createQName(RM_MODEL_URI, "oplysningerEksternt");
    QName PROP_FLOW_FRITIDVED = QName.createQName(RM_MODEL_URI, "fritidved");
    QName PROP_FLOW_KVALITETSKONTROL = QName.createQName(RM_MODEL_URI, "kvalitetskontrol");


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
    QName PROP_SUPERVISINGDOCTOR = QName.createQName(RM_MODEL_URI, "supervisingDoctor");
    QName PROP_PSYCHOLOGIST = QName.createQName(RM_MODEL_URI, "psychologist");
    QName PROP_HENVISENDEINSTANS = QName.createQName(RM_MODEL_URI, "referingAgency");

    /* ICD-10 diagnosis */
    QName PROP_MAIN_DIAGNOSIS = QName.createQName(RM_MODEL_URI, "mainDiagnosis");
    QName PROP_BI_DIAGNOSIS = QName.createQName(RM_MODEL_URI, "biDiagnoses");

    /* Waiting time */
    QName PROP_WAITING_PASSIVE = QName.createQName(RM_MODEL_URI, "waiting_passive");
    QName PROP_WAITING_ACTIVE = QName.createQName(RM_MODEL_URI, "waiting_active");
    QName PROP_WAITING_TOTAL = QName.createQName(RM_MODEL_URI, "waiting_total");

    QName PROP_VISITATOR_DATA = QName.createQName(RM_MODEL_URI, "visitator");


    /* signature */

    QName TYPE_SIGNATURE = QName.createQName(RM_MODEL_URI, "signature");
    QName PROP_SIGNATURE = QName.createQName(RM_MODEL_URI, "signatureText");


    /* psycdata*/

//    QName ASPECT_PSYCDATA = QName.createQName(RM_MODEL_URI, "psyc");
//    QName PROP_PSYCDATA_INTERVIEWRATINGSCALES = QName.createQName(RM_MODEL_URI, "interviewRatingScales");




    QName PROP_MARKEDBY = QName.createQName(RM_MODEL_URI, "markedBy");

    QName PROP_NEXT_NEWCASES_X = QName.createQName(RM_MODEL_URI, "next_newCases_x");
    QName PROP_NEXT_NEWCASES_Y = QName.createQName(RM_MODEL_URI, "next_newCases_y");

    int INIT_NEXT_NEWCASES_X = 1;
    int INIT_NEXT_NEWCASES_Y = 2;

    QName PROP_NEXT_CLOSEDCASES_X = QName.createQName(RM_MODEL_URI, "next_closedCases_x");
    QName PROP_NEXT_CLOSEDCASES_Y = QName.createQName(RM_MODEL_URI, "next_closedCases_y");

    int INIT_NEXT_CLOSEDCASES_X = 4;
    int INIT_NEXT_CLOSEDCASES_Y = 2;


    QName PROP_BUA_COUNTER = QName.createQName(RM_MODEL_URI, "buaCounter");
    QName PROP_FREE_CASENUMBERS = QName.createQName(RM_MODEL_URI, "freeCaseNumbers");


    /* Generic */
    QName PROP_LOCKED_FOR_EDIT = QName.createQName(RM_MODEL_URI, "locked4edit");
    QName PROP_LOCKED_FOR_EDIT_BY = QName.createQName(RM_MODEL_URI, "locked4editBy");

    QName PROP_FLOWCHART_FLAG = QName.createQName(RM_MODEL_URI, "flowflag");
    QName PROP_CLOSECASEBUTTONPRESSED = QName.createQName(RM_MODEL_URI, "closeCaseButtonPressed");


    public static QName PROP_ENTRIES = QName.createQName(RM_MODEL_URI, "maillog_entries");

    public static String TYPE_PSYC_DEC = "forensicPsychiatryDeclaration";
    public static String TYPE_PSYC_DEC_KEY = "caseNumber";
    public static String TYPE_PSYC_SITENAME = "retspsyk";

    /* Aspects */
    QName ASPECT_BUA = QName.createQName(RM_MODEL_URI, "bua");
    QName ASPECT_SUPOPL = QName.createQName(RM_MODEL_URI, "supopl");
    QName ASPECT_OPENEDIT = QName.createQName(RM_MODEL_URI, "openedit");

    QName ASPECT_RETURNDATEFORDECLARATION = QName.createQName(RM_MODEL_URI, "returnDateForDeclaration");
    QName PROP_RETURNOFDECLARATIONDATE = QName.createQName(RM_MODEL_URI, "returnOfDeclarationDate");

    QName ASPECT_STAT = QName.createQName(RM_MODEL_URI, "stat");
    QName ASPECT_FLOWCHART = QName.createQName(RM_MODEL_URI, "flowchart");
    QName ASPECT_AKTIVVENTETID = QName.createQName(RM_MODEL_URI, "aktivventetidBruger");

    QName ASPECT_ADDSIGNATURE = QName.createQName(RM_MODEL_URI, "addSignature");

    QName ASPECT_SIGNATUREADDEDTOUSER = QName.createQName(RM_MODEL_URI, "signatureAdded");
    QName ASPECT_TMP = QName.createQName(RM_MODEL_URI, "tmp");



    QName PROP_PRIMARYSIGNATURE = QName.createQName(RM_MODEL_URI, "primarySignature");
    QName PlockROP_SECONDARYSIGNATURE = QName.createQName(RM_MODEL_URI, "secondarySignature");



    // Prop Strings
    String NAME = "name";
    String ENTRY_KEY = "entryKey";
    String UUID = "node-uuid";
    String CASENUMBER = "caseNumber";

    // Containers
    String PROP_VALUES = "propertyValues";
    String PROP_TEMPLATE_LIBRARY = "documentTemplates";
    String PROP_FOLDER_TEMPLATE_LIBRARY = "folderTemplates";
    String PROP_SIGNATURE_LIBRARY = "signatureLibrary";
    String PROP_SHAREDFOLDER_BUA = "sharedFolderBua";
    String PROP_PSYC_LIBRARY = "psycPropertiesValues";


    String PROP_PSYC_LIBRARY_PSYCH_TYPE = "psykologisk_undersoegelsestype";

    // the six types of used instruments
    String PROP_PSYC_LIBRARY_INTERVIEWRATING = "psykiatriske_interviews_og_ratingscales";
    String PROP_PSYC_LIBRARY_KOGNITIV = "kognitive_og_neuropsykologiske_praestationstests";
    String PROP_PSYC_LIBRARY_IMPLECITE = "implicitte_projektive_tests";
    String PROP_PSYC_LIBRARY_EXPLICIT = "eksplicitte_spoergeskema_tests";
    String PROP_PSYC_LIBRARY_MALERING = "instrumenter_for_indikation_på_malingering";
    String PROP_PSYC_LIBRARY_RISIKO = "risikovurderingsinstrumenter";

    String PROP_PSYC_LIBRARY_PSYCH_MALERING = "psykologisk_vurdering_af_forekomst_af_malingering";
    String PROP_PSYC_LIBRARY_KONKLUSION_TAGS = "konklusion_tags";

    String PROP_WEEKLYSTAT = "weeklyStat";

    String ATTR_DEFAULT_DECLARATION_FOLDER = "Erklæring og psykologisk undersøgelse";
    String ATTR_DEFAULT_KORRESPONDANCE_FOLDER = "Korrespondance";

    String PROP_TMP = "tmp";

    String PROP_PSYCOLOGICALDOCUMENT = "Psyk test.odt";
    String PROP_PSYCOLOGICALDOCUMENT_BUA = "Psyk test_bua.odt";

    String PROP_SAMTYKKE_TDL_KONTAKT = "samtykketidkontakt.odt";
    String PROP_BERIGTIGELSE = "berigtigelse.odt";
    String PROP_SUPPLERENDEUDTALELSE_FOLDER = "Supplerende udtalelse";
    String PROP_SUPPLERENDEUDTALELSE = "supplerendeudtalelse.odt";
    String PROP_FLETTEBREV = "flettebrev.odt";

    // template docs
    String PROP_TEMPLATE_DOC_SAMTYKKE = "samtykke";
    String PROP_TEMPLATE_DOC_KENDELSE = "kendelse";

    String PROP_TEMPLATE_DOC_SAMTYKKE_BUA = "samtykke_bua";
    String PROP_TEMPLATE_DOC_KENDELSE_BUA = "kendelse_bua";

    String PROP_TEMPLATE_DOC_SAMTYKKE_FILENAME = "samtykke.odt";
    String PROP_TEMPLATE_DOC_KENDELSE_FILENAME = "kendelse.odt";

    String PROP_LOGFORMAILS = "mail_kvitteringer.odt";
    String PROP_DEFAULTFOLDER_MAILRECEIPTS = ATTR_DEFAULT_KORRESPONDANCE_FOLDER;

    String PROP_DEFAULTFOLDER_SIGNATUREIMAGE = PROP_TEMPLATE_LIBRARY;
    String PROP_SIGNATUREIMAGE_FILENAME = "sig.png";

    String PROP_TEMPLATE_DOC_SAMTYKKE_FILENAME_BUA = "samtykke_bua.odt";
    String PROP_TEMPLATE_DOC_KENDELSE_FILENAME_BUA = "kendelse_bua.odt";

    QName PROP_EXPIRYDATE = QName.createQName(RM_MODEL_URI, "expirydate");

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
    QName ASPECT_SKIPFLOW = QName.createQName(RM_MODEL_URI, "skip_flowchart");
    QName ASPECT_BUA_USER = QName.createQName(RM_MODEL_URI, "bua_user");
    QName ASPECT_DECLARATIONMARKEDFOREDIT = QName.createQName(RM_MODEL_URI, "documentMarkedForEdit");
    QName ASPECT_REDFLAG = QName.createQName(RM_MODEL_URI, "redFlag");
    QName ASPECT_EXPIRYUSER = QName.createQName(RM_MODEL_URI, "expiryUser");

    String USER_ALL = "Alle";
    String USER_ONLY_BUA = "BUA";
    String USER_CURRENT = "CURRENT_USER";
    String USER_ONLY_PS = "PS";

    // groups
    String GROUP_ALLOWEDTODELETE = "GROUP_site_retspsyk_ALLOWEDTODELETE";
    String GROUP_TEMPLATEFOLDERVALUEMANAGER = "GROUP_site_retspsyk_TemplateFolderValueManager";
    String GROUP_SITEROLEMANAGER = "GROUP_site_retspsyk_SiteRoleManager";
    String GROUP_SITEPROPERTYVALUEMANAGER = "GROUP_site_retspsyk_SitePropertyValueManager";
    String GROUP_SITEENTRYLOCKMANAGER = "GROUP_site_retspsyk_SiteEntryLockManager";

    String MONTHLY_REPORT_SPREADSHEET_NAME = "rapport.ods";
    String AKTIV_REPORT_SPREADSHEET_NAME = "aktikrapport.ods";
    String WEEKLY_REPORT_SPREADSHEET_A_NAME = "uge.ods";
    String WEEKLY_REPORT_SPREADSHEET_B_NAME = "aar.ods";
    String DEFAULT_MAIL_TEXT_NAME = "standardtext.odt";
    String DEFAULT_POST_ACTIVE_REPORT_TEXT = "_aktivventetid.ods";

    String statusCriteriaAmbulant = "2";
    String statusCriteriaIndlagt = "3";

    String DEFAULT_MAIL_TEXT_RETURN = "standardtextReturnering.odt";

    String DEFAULT_MAIL_TEXT_SEND_VALUE = "send";
    String DEFAULT_MAIL_TEXT_RETURN_VALUE  = "returnering";


    QName ASSOC_VERSION_PREVIEW = QName.createQName(RM_MODEL_URI, "version_preview");

    // unlock modes
    String PROP_UNLOCK_FOR_SUPPOPL = "reopenSupopl"; // include in flowchart, add the new aspect
    String PROP_UNLOCK_FOR_EDIT = "reopenEdit"; // skip in flowchart


    // weeklystat

    QName PROP_WEEKLY_TYPE = QName.createQName(RM_MODEL_URI, "weeklystat");

    QName PROP_WEEK = QName.createQName(RM_MODEL_URI, "week");
    QName PROP_YEAR = QName.createQName(RM_MODEL_URI, "year");
    QName PROP_RECEIVED = QName.createQName(RM_MODEL_URI, "received");
    QName PROP_SENT = QName.createQName(RM_MODEL_URI, "sent");



    /* rmpsy */

    String RMPSY_MODEL_URI = "http://www.rm.dk/model/psycdatabase/1.0";
    String RMPSY_MODEL_PREFIX = "rmpsy";

    QName ASPECT_PSYCDATA = QName.createQName(RMPSY_MODEL_URI, "psyc");

    QName PROPQNAME_PSYCDATA_PSYCH_TYPE = QName.createQName(RMPSY_MODEL_URI, "psykologisk_undersoegelsestype");

    QName PROPQNAME_PSYCDATA_INTERVIEWRATING = QName.createQName(RMPSY_MODEL_URI, "psykiatriske_interviews_og_ratingscales");
    QName PROPQNAME_PSYCDATA_KOGNITIV = QName.createQName(RMPSY_MODEL_URI, "kognitive_og_neuropsykologiske_praestationstests");
    QName PROPQNAME_PSYCDATA_IMPLECITE = QName.createQName(RMPSY_MODEL_URI, "implicitte_projektive_tests");
    QName PROPQNAME_PSYCDATA_EXPLICIT = QName.createQName(RMPSY_MODEL_URI, "eksplicitte_spoergeskema_tests");
    QName PROPQNAME_PSYCDATA_MALERING = QName.createQName(RMPSY_MODEL_URI, "instrumenter_for_indikation_på_malingering");
    QName PROPQNAME_PSYCDATA_RISIKO = QName.createQName(RMPSY_MODEL_URI, "risikovurderingsinstrumenter");

    QName PROPQNAME_PSYCDATA_PSYCH_MALERING = QName.createQName(RMPSY_MODEL_URI, "psykologisk_vurdering_af_forekomst_af_malingering");
    QName PROPQNAME_PSYCDATA_KONKLUSION_TAGS = QName.createQName(RMPSY_MODEL_URI, "konklusion_tags");
    QName PROPQNAME_PSYCDATA_KONKLUSION_FREETEXT = QName.createQName(RMPSY_MODEL_URI, "konklusion_freetext");

    // types

    QName TYPE_ANVENDTUNDERSOEGELSESINST = QName.createQName(RMPSY_MODEL_URI, "anvendtUndersoegelsesinst");
    QName PROP_ANVENDTUNDERSOEGELSESINST_ID = QName.createQName(RMPSY_MODEL_URI, "id_anvendtUndersoegelsesinst");
    QName PROP_ANVENDTUNDERSOEGELSESINST_NAME = QName.createQName(RMPSY_MODEL_URI, "name_anvendtUndersoegelsesinst");
}
