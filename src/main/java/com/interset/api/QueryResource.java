package com.interset.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Path("/api/query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryResource {
    private static final Logger LOG = LoggerFactory.getLogger(QueryResource.class);
    private final String ELASTICBASE = "http://es01:9200/movies/_search";

    /*
    * Send GET request and return the response
    * */
    private JSONObject sendGET(String url) {
        JSONArray toBeReturned = null;
        JSONObject toBeReturnedObj = null;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONArray jsonArray = new JSONObject(response.toString()).getJSONObject("hits").getJSONArray("hits");
                toBeReturnedObj = new JSONObject();
                toBeReturned = new JSONArray();
                if (jsonArray != null) {
                    Iterator it = jsonArray.iterator();
                    while (it.hasNext()) {
                        JSONObject obj_t = (JSONObject) it.next();
                        JSONObject n = obj_t.getJSONObject("_source");
                        toBeReturned.put(n);
                    }
                }
                toBeReturnedObj.put("results", toBeReturned);
            } else {
                System.out.println("GET request not worked");
                return new JSONObject();
            }
        }catch (IOException e){
            System.out.println(e.toString());
        }
        return toBeReturnedObj;
    }

    /*
     * Send POST request and return the response
     * */
    private JSONArray postGET(String url, String body) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        JSONArray result = new JSONArray();
        try {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(body);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONArray jsonArray = new JSONObject(responseString).getJSONObject("hits").getJSONArray("hits");
            result = new JSONArray();
            if (jsonArray != null) {
                Iterator it = jsonArray.iterator();
                while (it.hasNext()) {
                    JSONObject obj = (JSONObject) it.next();
                    JSONObject n = obj.getJSONObject("_source");
                    result.put(n);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } finally {
            httpClient.close();
        }
        return result;
    }

    /*
    * Encode a string to URL format
    * */
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    /*
        Used for Task 4.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(
            @QueryParam("q") String q,
            @QueryParam("docCount") @DefaultValue("10") int docCount
    ) {
        int count = docCount>10?10:docCount;
        String URL = ELASTICBASE+"?size="+count+"&q="+encodeValue(q);
        JSONObject toBeReturnedObj= sendGET(URL);
        return Response.status(Response.Status.OK).entity(toBeReturnedObj.toString()).build();
    }

    /*
        Used for Task 5.
     */
    @GET
    @Path("/topRated")
    @Produces(MediaType.APPLICATION_JSON)
    public Response topRated(
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("minVotes") @DefaultValue("1000") int minVotes
    ) {
        JSONArray result = null;
        try {
            String JString = "{\n" +
                    "\"aggs\" : {\n" +
                    "\"max_price\" : { \"max\" : { \"field\" : \"Vote Average\" } }" +
                    "},\n" +
                    "\"query\": {\n" +
                    "\"bool\": {\n" +
                    "   \"must\": [\n" +
                    "    {\n" +
                    "        \"range\" : {\n" +
                    "        \"Release Date\" : {\n" +
                    "            \"gte\" : \"" + startDate + "\",\n" +
                    "                    \"lte\" : \"" + endDate + "\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "    },\n" +
                    "    {\"range\":{\n" +
                    "        \"Vote Count\": {\n" +
                    "            \"gte\": " + minVotes + "\n" +
                    "        }\n" +
                    "    }}\n" +
                    "]\n" +
                    "}\n" +
                    "}\n" +
                    "}";
            result = postGET(ELASTICBASE + "?size=5", JString);
        }catch(IOException e){
            System.out.println(e.toString());
        }
        return Response.status(Response.Status.OK).entity(result.toString()).build();
    }
}