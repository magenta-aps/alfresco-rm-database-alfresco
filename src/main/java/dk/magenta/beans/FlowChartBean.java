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

        query += " AND ISUNSET:\"rm:closed\"";

        System.out.println("getEntriesbyUser query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true);

        return nodeRefs;
    }

    public List<NodeRef> getWaitingList(String siteShortName) {

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

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesByOngoing(String siteShortName, String default_query) {

        JSONObject o = new JSONObject();

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        // atleast one assigned

        query += " AND (" + QueryUtils.getParametersQuery("socialworker", "*", false);
        query += " OR " + QueryUtils.getParametersQuery("doctor", "*", false);
        query += " OR " + QueryUtils.getParametersQuery("psychologist", "*", false);
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

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true);

        return nodeRefs;
    }

    public List<NodeRef> getEntriesByStatus(String siteShortName, String[] status, boolean bua) {

        JSONObject o = new JSONObject();

        String type = databaseBean.getType(siteShortName);
        String query = QueryUtils.getSiteQuery(siteShortName) + " AND " + QueryUtils.getTypeQuery(type);

        query += " AND (" + QueryUtils.getParametersQuery("socialworker", "*", false);
        query += " OR " + QueryUtils.getParametersQuery("doctor", "*", false);
        query += " OR " + QueryUtils.getParametersQuery("psychologist", "*", false);
        query += ")";

        query += " AND ISUNSET:\"rm:closed\"";

        String statusQuery = "AND ( ";

        for (int i=0; i<= status.length-1; i++) {

            String state = status[i];
            if (statusQuery.equals("AND ( ")) {
                statusQuery += QueryUtils.getParameterQuery("status", state, false);
            }
            else {
                statusQuery += " OR " + QueryUtils.getParameterQuery("status", state, false);
            }
        }
        statusQuery += ") ";

        query += statusQuery;

        System.out.println("getEntriesByStatus query");
        System.out.println(query);

        List<NodeRef> nodeRefs = entryBean.getEntries(query, 0, 1000, "@rm:creationDate", true);

        System.out.println("resultat fra getentriesbyuser");

        for (int i=0; i <= nodeRefs.size()-1;i++) {
            System.out.println(nodeRefs.get(0));
        }

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

            if (tmp.has("closed")) {
                e.put("closed", tmp.get("closed"));
            }

            if (tmp.has("declarationDate")) {
                e.put("declarationDate", tmp.get("declarationDate"));
            }

            if (tmp.has("psychologist") && !tmp.get("psychologist").equals("null") ) {
                e.put("psychologist", tmp.get("psychologist"));
            }

            entries.put(e);
        }

        return entries;
    }


}