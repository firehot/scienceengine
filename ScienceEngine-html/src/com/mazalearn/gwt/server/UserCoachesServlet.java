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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.mazalearn.scienceengine.app.services.ProfileData;

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

    Filter colorPresentFilter =
      new FilterPredicate(ProfileData.COLOR, FilterOperator.NOT_EQUAL, null);

    // Use class Query to assemble a query
    Query q = new Query("User"); // .setFilter(colorPresentFilter);

    // Use PreparedQuery interface to retrieve results
    PreparedQuery pq = datastore.prepare(q);


    String jsonStr = "[";
    boolean firstCoach = true;
    for (Entity user : pq.asIterable()) {
      EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(ProfileServlet.PROFILE);
      String userId = user.getKey().getName();
      System.out.println(userId);
      if (profileEntity == null) continue;
      if (profileEntity.getProperty(ProfileData.COLOR) == null) continue;
      if (profileEntity.getProperty(ProfileData.CURRENT) == null) continue;
      String color = (String) profileEntity.getProperty(ProfileData.COLOR);
      String userName = (String) profileEntity.getProperty(ProfileData.USER_NAME);
      float currentValue = Float.parseFloat((String) profileEntity.getProperty(ProfileData.CURRENT));
      String current = String.format("%2.2f", currentValue);
      if (!firstCoach) {
        jsonStr += ",";
      }
      firstCoach = false;
      jsonStr += "{";
      jsonStr += "\"" + ProfileData.USER_ID + "\":\"" + userId + "\"";
      jsonStr += ",\"" + ProfileData.COLOR + "\":\"" + color + "\"";
      jsonStr += ",\"" + ProfileData.USER_NAME + "\":\"" + userName + "\"";
      jsonStr += ",\"" + ProfileData.CURRENT + "\":" + current;
      jsonStr += "}\n";
    }
    
    jsonStr += "]";
    
    return jsonStr;
  }
}
