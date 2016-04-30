package com.ds.assignment;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

@Path("/rest/checkin")
public class RestServlet {

	// Creating serverside mongoDB which is listening on port 27017
	MongoClient mongoClient = new MongoClient("localhost", 27017);
	@SuppressWarnings("deprecation")
	DB db = mongoClient.getDB("mydb");
	// gets collection user..similar to table
	DBCollection table1 = db.getCollection("user");
	DBCollection table2 = db.getCollection("modelStat");

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(CheckIn checkin) {
		// create a row (document) in collection based on the object sent from
		// post call
		BasicDBObject document = new BasicDBObject();
		document.put("regNo", checkin.getRegistrationNo());
		BasicDBObject details = new BasicDBObject();
		details.put("modelName", checkin.getModelName());
		details.put("modelYear", checkin.getModelYear());
		document.put("details", details);
		table1.insert(document);

		String result = "New Car registered \n";

		// searching the DB to get the document for the given client
		BasicDBObject searchQuery1 = new BasicDBObject();
		searchQuery1.put("modelName", checkin.getModelName());
		BasicDBObject searchQuery2 = new BasicDBObject();
		searchQuery1.put("modelYear", checkin.getModelYear());
		DBCursor cursor = table2.find(searchQuery1,searchQuery2);
		while (cursor.hasNext()) {
			DBObject nextDocument = cursor.next();
			String detailsObj = nextDocument.toString();
			result += detailsObj + "\n";
		}

		// sending the registered client information to the client
		return Response.status(201).entity(result).build();
	}

}