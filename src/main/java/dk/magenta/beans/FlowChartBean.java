package dk.magenta.beans;

import dk.magenta.model.DatabaseModel;
import dk.magenta.utils.QueryUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class FlowChartBean {


    private PersonService personService;

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private SearchService searchService;

    public void setDatabaseBean(DatabaseBean databaseBean) {
        this.databaseBean = databaseBean;
    }

    private DatabaseBean databaseBean;


    public void setEntryBean(EntryBean entryBean) {
        this.entryBean = entryBean;
    }

    private EntryBean entryBean;


    public List<NodeRef> getEntriesbyUser(String user, String siteShortName, String default_query) throws JSONException {

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        query += " AND (" + QueryUtils.getParametersQuery("socialworker", user, false);
        query += " OR " + QueryUtils.getParametersQuery("doctor", user, false);
        query += " OR " + QueryUtils.getParametersQuery("psychologist", user, false);
        query += ")";

        query += " AND " + default_query;

        System.out.println("getEntriesbyUser query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesbyUserStateArrestanter(String user, String siteShortName, String default_query, String sort, boolean desc) throws JSONException {

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        query += " AND (" + QueryUtils.getParametersQuery("socialworker", user, false);
        query += " OR " + QueryUtils.getParametersQuery("doctor", user, false);
        query += " OR " + QueryUtils.getParametersQuery("psychologist", user, false);
        query += ")";

        query += " AND " + default_query;

        String[] status = new String[2];
        status[0] = "Ambulant/arrestant";
        status[1] = "Ambulant/surrogatanbragt";

        String statusQuery = " AND ( ";

        for (int i=0; i<= status.length-1; i++) {

            String state = status[i];
            if (statusQuery.equals(" AND ( ")) {
                statusQuery += QueryUtils.getParameterQuery("status", state, false);
            }
            else {
                statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
            }
        }
        statusQuery += ") ";

        query += statusQuery;


        System.out.println("getEntriesbyUserStateArrestanter query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, sort, desc);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesbyUserStateNOTArrestanter(String user, String siteShortName, String default_query, String sort, boolean desc) throws JSONException {

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        query += " AND (" + QueryUtils.getParametersQuery("socialworker", user, false);
        query += " OR " + QueryUtils.getParametersQuery("doctor", user, false);
        query += " OR " + QueryUtils.getParametersQuery("psychologist", user, false);
        query += ")";

        query += " AND " + default_query;

        String[] status = new String[2];
        status[0] = "Ambulant/arrestant";
        status[1] = "Ambulant/surrogatanbragt";

        String statusQuery = " AND NOT ( ";

        for (int i=0; i<= status.length-1; i++) {

            String state = status[i];
            if (statusQuery.equals(" AND NOT ( ")) {
                statusQuery += QueryUtils.getParameterQuery("status", state, false);
            }
            else {
                statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
            }
        }
        statusQuery += ") ";

        query += statusQuery;


        System.out.println("getEntriesbyUserStateNOTArrestanter query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, sort, desc);

        return nodeRefs;
    }

    public JSONObject getEntriesbyUser(String user, String siteShortName, String default_query, String sort, boolean desc) throws JSONException {

        JSONObject result = new JSONObject();

        result.put("arrestanter",this.nodeRefsTOData(this.getEntriesbyUserStateArrestanter(user, siteShortName, default_query, sort, desc)));
        result.put("andre",this.nodeRefsTOData(this.getEntriesbyUserStateNOTArrestanter(user, siteShortName, default_query, sort, desc)));

        return result;
    }

    public List<NodeRef> getWaitingList(String siteShortName, String sort, boolean desc) {

        JSONObject o = new JSONObject();

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        // no one assigned

        query += " AND (" + QueryUtils.getParametersQueryNullValue("socialworker", false);
        query += " AND " + QueryUtils.getParametersQueryNullValue("doctor", false);
        query += " AND " + QueryUtils.getParametersQueryNullValue("psychologist", false);
        query += ")";

        query += " AND ISUNSET:\"rm:closed\"";

        // not the below states

        String[] status = new String[4];
        status[0] = "Ambulant/arrestant";
        status[1] = "Ambulant/surrogatanbragt";
        status[2] = "Indlagt til observation";
        status[3] = "Gr-*";

        String statusQuery = " AND NOT ( ";

        for (int i=0; i<= status.length-1; i++) {

            String state = status[i];
            if (statusQuery.equals(" AND NOT ( ")) {
                statusQuery += QueryUtils.getParameterQuery("status", state, false);
            }
            else {
                statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
            }
        }
        statusQuery += ") ";

        query += statusQuery;



        System.out.println("getWaitingList query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, sort, desc);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesByIgangvaerende(String siteShortName, String default_query, String sort, boolean desc) {

        JSONObject o = new JSONObject();

        String type = databaseBean.getType(siteShortName);

        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        // atleast one assigned

        query += " AND (" + " (" + QueryUtils.getParametersQuery("socialworker", "*", false) + " AND " + QueryUtils.getParametersQueryNullValue("socialworker", true) + ") ";
        query += " OR " + " (" + QueryUtils.getParametersQuery("doctor", "*", false) + " AND " + QueryUtils.getParametersQueryNullValue("doctor", true) + ") ";;
        query += " OR " + " (" + QueryUtils.getParametersQuery("psychologist", "*", false) + " AND " + QueryUtils.getParametersQueryNullValue("psychologist", true) + ") ";;
        query += ")";


        // not closed

        query += " AND " + default_query;

        String[] status = new String[4];
        status[0] = "Ambulant/arrestant";
        status[1] = "Ambulant/surrogatanbragt";
        status[2] = "Indlagt til observation";
        status[3] = "Gr-*";


        // not the below states

        String statusQuery = " AND NOT ( ";

        for (int i=0; i<= status.length-1; i++) {

            String state = status[i];
            if (statusQuery.equals(" AND NOT ( ")) {
                statusQuery += QueryUtils.getParameterQuery("status", state, false);
            }
            else {
                statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
            }
        }
        statusQuery += ") ";

        query += statusQuery;

        System.out.println("getEntriesByOngoing query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, sort, desc);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesByStateArrestanter(String siteShortName, String default_query, String sort, boolean desc) {

        JSONObject o = new JSONObject();

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        query += " AND " + default_query;

        String[] status = new String[2];
        status[0] = "Ambulant/arrestant";
        status[1] = "Ambulant/surrogatanbragt";

        String statusQuery = " AND ( ";

        for (int i=0; i<= status.length-1; i++) {

            String state = status[i];
            if (statusQuery.equals(" AND ( ")) {
                statusQuery += QueryUtils.getParameterQuery("status", state, false);
            }
            else {
                statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
            }
        }
        statusQuery += ") ";

        query += statusQuery;

        System.out.println("getEntriesByStateArrestanter query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, sort, desc);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesByStateObservation(String siteShortName, String default_query, String sort, boolean desc) {

        System.out.println("doing getEntriesByStateObservation");

        JSONObject o = new JSONObject();

        String type = databaseBean.getType(siteShortName);
        System.out.println("done type");
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        query += " AND " + default_query;

        String state = "Indlagt til observation";


        String statusQuery = " AND ( ";
        statusQuery += QueryUtils.getParameterQuery("status", state, false);
        statusQuery += ") ";

        query += statusQuery;

        System.out.println("getEntriesByStateObservation query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, sort, desc);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesByStateVentedeGR(String siteShortName, String default_query, String sort, boolean desc) {

        System.out.println("doing ventedegr");

        JSONObject o = new JSONObject();

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        query += " AND " + default_query;

        String state = "Gr-*";

        String statusQuery = " AND ( ";
        statusQuery += QueryUtils.getParameterQuery("status", state, false);
        statusQuery += ") ";

        query += statusQuery;

        System.out.println("getEntriesByStateventedegr query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, sort, desc);

        return nodeRefs;
    }




    public JSONArray nodeRefsTOData(List<NodeRef> nodeRefs) throws JSONException {

        Iterator<NodeRef> i = nodeRefs.iterator();
        org.json.JSONArray entries = new org.json.JSONArray();

        while (i.hasNext()) {
            NodeRef nodeRef = i.next();

            JSONObject tmp = entryBean.toJSON(nodeRef);

            JSONObject e = new JSONObject();

            e.put("caseNumber", tmp.get("caseNumber"));

            if (tmp.has("cprNumber")) {
                e.put("cprNumber", tmp.get("cprNumber"));
            }

            if (tmp.has("fullName")) {
                e.put("fullName", tmp.get("fullName"));
            }

            e.put("creationDate", tmp.get("creationDate"));

            if (tmp.has("doctor") && !tmp.get("doctor").equals("null")) {
                e.put("doctor", tmp.get("doctor"));
            }

            if (tmp.has("psychologist") && !tmp.get("psychologist").equals("null")) {
                e.put("psychologist", tmp.get("psychologist"));
            }

            if (tmp.has("socialworker") && !tmp.get("socialworker").equals("null")) {
                e.put("socialworker", tmp.get("socialworker"));
            }


            if (tmp.has("node-uuid")) {
                e.put("node_uuid", tmp.get("node-uuid"));
            }



            if (tmp.has("firstName")) {
                e.put("firstName", tmp.get("firstName"));
            }

            if (tmp.has("lastName")) {
                e.put("lastName", tmp.get("lastName"));
            }

            if (tmp.has("closed")) {
                e.put("closed", tmp.get("closed"));
            }

            if (tmp.has("mainCharge")) {
                e.put("mainCharge", tmp.get("mainCharge"));
            }

            if (tmp.has("status")) {
                e.put("status", tmp.get("status"));
            }


            if (tmp.has("kommentar")) {
                e.put("kommentar", tmp.get("kommentar"));
            }

            if (tmp.has("samtykkeopl")) {
                e.put("samtykkeopl", tmp.get("samtykkeopl"));
            }

            if (tmp.has("tolksprog")) {
                e.put("tolksprog", tmp.get("tolksprog"));
            }

            if (tmp.has("arrest")) {
                e.put("arrest", tmp.get("arrest"));
            }

            if (tmp.has("fritidved")) {
                e.put("fritidved", tmp.get("fritidved"));
            }

            if (tmp.has("kvalitetskontrol")) {
                e.put("kvalitetskontrol", tmp.get("kvalitetskontrol"));
            }

            if (tmp.has("psykologfokus")) {
                e.put("psykologfokus", tmp.get("psykologfokus"));
            }



            if (tmp.has("declarationDate")) {
                e.put("declarationDate", tmp.get("declarationDate"));
            }

            if (tmp.has("locked4edit")) {
                e.put("locked4edit", tmp.get("locked4edit"));
            }

            if (tmp.has("locked4editBy")) {
                e.put("locked4editBy", tmp.get("locked4editBy"));
            }

            if (tmp.has("psychologist") && !tmp.get("psychologist").equals("null") ) {
                e.put("psychologist", tmp.get("psychologist"));
            }

            e.put("show","false");

            entries.put(e);
        }

        return entries;
    }


    public JSONObject getTotals(String siteShortName, String default_query, String user) throws JSONException {

        JSONObject result = new JSONObject();

        result.put("ongoing",this.getEntriesByIgangvaerende(siteShortName, default_query, "@rm:creationDate", true).size());
        result.put("arrestanter",this.getEntriesByStateArrestanter(siteShortName, default_query, "@rm:creationDate", false).size());
        result.put("observation",this.getEntriesByStateObservation(siteShortName, default_query, "@rm:creationDate", false).size());

        if (user != null) {
            result.put("user",this.getEntriesbyUser(user, siteShortName, default_query).size());
        }
        else {
            result.put("user"," -bruger ikke fundet-");
        }


        result.put("waitinglist",this.getWaitingList(siteShortName, "@rm:creationDate", true).size());
        result.put("ventendegr",this.getEntriesByStateVentedeGR(siteShortName,default_query, "@rm:creationDate", true).size());

        return result;


    }

    public JSONObject getAlle(String siteShortName, String default_query, String user) throws JSONException {

        JSONObject result = new JSONObject();

//        result.put("ongoing",this.nodeRefsTOData(this.getEntriesByIgangvaerende(siteShortName, default_query, "@rm:creationDate", true)));
//        result.put("arrestanter",this.nodeRefsTOData(this.getEntriesByStateArrestanter(siteShortName, default_query, "@rm:creationDate", false)));
//        result.put("observation",this.nodeRefsTOData(this.getEntriesByStateObservation(siteShortName, default_query, "@rm:creationDate", false)));
//
//        if (user != null) {
//            result.put("user",this.nodeRefsTOData(this.getEntriesbyUser(user, siteShortName, default_query, "@rm:creationDate", true)));
//        }
//        else {
//            result.put("user"," -bruger ikke fundet-");
//        }
//
//
//        result.put("waitinglist",this.nodeRefsTOData(this.getWaitingList(siteShortName, "@rm:creationDate", true)));
//        result.put("ventendegr",this.nodeRefsTOData(this.getEntriesByStateVentedeGR(siteShortName,default_query, "@rm:creationDate", true)));

        return result;


    }


}