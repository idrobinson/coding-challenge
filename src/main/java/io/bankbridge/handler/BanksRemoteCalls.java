package io.bankbridge.handler;
import java.util.*;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import spark.Request;
import spark.Response;

public class BanksRemoteCalls {

    private static Map<String, String> config = new HashMap<>();

    public static void init() throws Exception {
	
	try {
            config = new ObjectMapper().readValue(Thread.currentThread().getContextClassLoader().getResource("banks-v2.json"), Map.class);
        } catch (Exception e) {
	    throw e;
        }
    }

    public static String handle(Request request, Response response) {
	
        //System.out.println(config);
        //throw new RuntimeException("Not implemented");

	// Using the retrieved file contents stored in the config Map:
	//     Loop through the config, using a lambda.
	//     Open an http connection and get the returned entity.
	//     Read the entity into a BankModel.
	//     If the BIC is valid, put into the result list.
	// Nest try statements to first catch any IOException, then to finally close the http objects & connection.
        List<Map> result = new ArrayList<>();
	CloseableHttpClient httpclient = HttpClients.createDefault();
	
	config.forEach((name, url)->{
            HttpGet httpGet = new HttpGet(url);
            try {
                CloseableHttpResponse response1 = httpclient.execute(httpGet);
                try {
                    //System.out.println(response1.getStatusLine());
                    HttpEntity entity1 = response1.getEntity();
		    BankModel model = new ObjectMapper().readValue(EntityUtils.toString(entity1), BankModel.class);
		    // Include all banks where the BIC is not null.
		    // This could be another action e.g. output but state as "Unknown" or "Not given" or "Missing".
		    // This could throw and exception to draw attention, but that would stop execution at that point.
		    // This could be logged to a file for later examination.
		    // Or a different version (or configuration of this version) could be used just to list the failing or incomplete replies.
		    if (model.bic != null) {
                        Map map = new HashMap<>();
		        map.put("id", model.bic);
		        map.put("name", name);
		        result.add(map);
		    }
                    EntityUtils.consume(entity1);
	        } finally {
                    response1.close();
                }
	    } catch (IOException e) {
                throw new RuntimeException("Error while processing httpclient");
	    }
	});

	// Do as the cache based version does:
	//     Read the result map into a string.
	//     Return the string.
        try {
            String resultAsString = new ObjectMapper().writeValueAsString(result);
            //System.out.println(resultAsString);
            return resultAsString;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while processing request");
        }
		
    }

}
