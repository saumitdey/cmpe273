/**
 * 
 */
package com.ds.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.json.*;

/**
 * @author saumit
 *
 */
public class CheckInsJerseyClient {
	public static String newCheckIn = null;
	public static Client client = Client.create();
	public static WebResource webResource = client.resource("http://localhost:8080/DSAssignment1/api/rest/checkin");
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// to read input from console
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		ClientInfo clientInfo = new ClientInfo();
		String regNo = null;
		String modelName = null;
		String modelYear = null;
		String serverId = null;
		while (true) {
			System.out.println("Enter the car details interested in registering \n");
			try {
				System.out.println("Registration No : \n");
				String userInput = in.readLine();
				if (userInput.equals("!")) {
					System.exit(0);
				}

				if (!userInput.equals("")) {
					regNo = userInput.toString().trim();
					clientInfo.setRegistrationNo(Integer.parseInt(regNo));
				}
				System.out.println("Model Name : \n");
				userInput = in.readLine();
				if (!userInput.equals("")) {
					modelName = userInput.toString().trim();
					clientInfo.setModelName(modelName);
				}
				System.out.println("Model Year : \n");
				userInput = in.readLine();
				if (!userInput.equals("")) {
					modelYear = userInput.toString().trim();
					clientInfo.setModelYear(Integer.parseInt(modelYear));
				}
				System.out.println("Server ID : \n");
				userInput = in.readLine();
				if (!userInput.equals("")) {
					serverId = userInput.toString().trim();
					clientInfo.setServerId(Integer.parseInt(serverId));
				}
				
				
				//Creating client table entry
				bootstrap(clientInfo);
				
				// constructing JSON string to send to server
				newCheckIn = "{\"regNo\":\"" + regNo + "\",\"modelName\":\"" + modelName + "\", \"modelYear\":\"" + modelYear + "\", \"serverId\":\"" + serverId + "\"}";
								
				
				//Creating server table entry
				register(clientInfo);				
				
								

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void bootstrap(ClientInfo newClient) {
		// Creating client-side mongoDB which is listening on port 12345
		MongoClient mongoClient = new MongoClient("localhost", 12345);
		@SuppressWarnings("deprecation")
		DB db = mongoClient.getDB("clientdb");
		// gets collection user..similar to table
		DBCollection table = db.getCollection("clientInfo");
		java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
				
		// create a row (document) in collection based on the object sent from
		// post call
		BasicDBObject document = new BasicDBObject();
		document.put("regNo", newClient.getRegistrationNo());
		document.put("modelName", newClient.getModelName());
		document.put("modelYear", newClient.getModelYear());
		document.put("serverId", newClient.getServerId());
		table.insert(document);
		System.out.println("Bootstrap successfully completed. New Client Info is stored in the client DB");
		mongoClient.close();
	}

	
	
	
	
	public static void register(ClientInfo newClient) {
		try {
			// making the POST call to RestServlet for register operation -
			// newCheckIn includes device ID, name, location
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, newCheckIn);

			// reading response. checking for 201 status code
			if (response.getStatus() != 201) {
				throw new RuntimeException("Register Failed : HTTP error code : " + response.getStatus());
			}
			// Printing client information from server after successful
			// registration
			String output = response.getEntity(String.class);
			clientWriteAttributes(newClient,output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void clientWriteAttributes(ClientInfo newClient,String output) {
		try {
			// Creating client-side mongoDB which is listening on port 12345
			MongoClient mongoClient = new MongoClient("localhost", 12345);
			@SuppressWarnings("deprecation")
			DB db = mongoClient.getDB("clientdb");
			// gets collection user..similar to table
			DBCollection table = db.getCollection("clientInfo");
			java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
			
			JSONObject root = new JSONObject(output.substring(output.indexOf('{')));
			String length = root.getString("length");
			String breadth = root.getString("breadth");
			
			BasicDBObject document = new BasicDBObject();	
			document.append("$set",new BasicDBObject().append("length",length).append("breadth",breadth));
			BasicDBObject updateQuery = new BasicDBObject().append("regNo", newClient.getRegistrationNo());
			
			table.update(updateQuery, document);
			
			System.out.println("\nThe attributes are being written successfully\n");
			mongoClient.close();		
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}	
		
}
