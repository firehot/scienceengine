package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;

@SuppressWarnings("serial")
public class UserCoachesServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("UserCoaches");
    response.setHeader("ContentType", "application/json");
    response.getWriter().append(retrieveUserCoaches());
  }
  
  private String retrieveUserCoaches() {
    // Get the Datastore Service
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Use class Query to assemble a query
    Query q = new Query("User"); // .setFilter(colorPresentFilter);

    // Use PreparedQuery interface to retrieve results
    PreparedQuery pq = datastore.prepare(q);

    Gson gson = new Gson();
    String jsonStr = "[";
    boolean firstCoach = true;
    for (Entity user : pq.asIterable()) {
      EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(ProfileServlet.PROFILE);
      String userId = user.getKey().getName();
      System.out.println(userId);
      if (profileEntity == null) continue;
      
      Text clientPropsText = (Text) profileEntity.getProperty(ProfileData.CLIENT_PROPS);
      if (clientPropsText == null) continue;
      ClientProps clientProps = gson.fromJson(clientPropsText.getValue(), ClientProps.class);
      if (clientProps.color == null) continue;
      if (profileEntity.getProperty(ProfileData.CURRENT) == null) continue;
      
      Text serverPropsText = (Text) profileEntity.getProperty(ProfileData.SERVER_PROPS);
      if (serverPropsText == null) continue;
      ServerProps serverProps = gson.fromJson(serverPropsText.getValue(), ServerProps.class);
      
      String current = String.format("%2.2f", clientProps.current);
      if (!firstCoach) {
        jsonStr += ",";
      }
      firstCoach = false;
      jsonStr += "{";
      jsonStr += "\"" + ProfileData.USER_ID + "\":\"" + serverProps.userId + "\"";
      jsonStr += ",\"" + ProfileData.COLOR + "\":\"" + clientProps.color + "\"";
      jsonStr += ",\"" + ProfileData.USER_NAME + "\":\"" + serverProps.userName + "\"";
      jsonStr += ",\"" + ProfileData.CURRENT + "\":" + current;
      jsonStr += "}\n";
    }
    
    jsonStr += "]";
    
    return jsonStr;
  }
}
