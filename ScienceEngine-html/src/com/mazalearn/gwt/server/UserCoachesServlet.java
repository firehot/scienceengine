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
      new FilterPredicate(ProfileServlet.COLOR, FilterOperator.NOT_EQUAL, null);

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
      if (profileEntity.getProperty(ProfileServlet.COLOR) == null) continue;
      if (profileEntity.getProperty(ProfileServlet.CURRENT) == null) continue;
      String color = (String) profileEntity.getProperty(ProfileServlet.COLOR);
      String userName = (String) profileEntity.getProperty(ProfileServlet.USER_NAME);
      float currentValue = Float.parseFloat((String) profileEntity.getProperty(ProfileServlet.CURRENT));
      String current = String.format("%2.2f", currentValue);
      if (!firstCoach) {
        jsonStr += ",";
      }
      firstCoach = false;
      jsonStr += "{";
      jsonStr += "\"" + ProfileServlet.USER_ID + "\":\"" + userId + "\"";
      jsonStr += ",\"" + ProfileServlet.COLOR + "\":\"" + color + "\"";
      jsonStr += ",\"" + ProfileServlet.USER_NAME + "\":\"" + userName + "\"";
      jsonStr += ",\"" + ProfileServlet.CURRENT + "\":" + current;
      jsonStr += "}\n";
    }
    
    jsonStr += "]";
    
    return jsonStr;
  }
}
