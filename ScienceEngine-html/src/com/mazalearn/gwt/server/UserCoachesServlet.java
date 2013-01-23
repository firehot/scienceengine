package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@SuppressWarnings("serial")
public class UserCoachesServlet extends HttpServlet {

  private static final String NAME = "name";
  private static final String COLOR = "color";
  private static final String CURRENT = "current";

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("UserCoaches");
    response.setHeader("ContentType", "applicatn/json");
    response.getWriter().append(retrieveUserCoaches());
  }
  
  private String retrieveUserCoaches() {
    // Get the Datastore Service
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Filter colorPresentFilter =
      new FilterPredicate(COLOR, FilterOperator.NOT_EQUAL, null);

    // Use class Query to assemble a query
    Query q = new Query("User").setFilter(colorPresentFilter);

    // Use PreparedQuery interface to retrieve results
    PreparedQuery pq = datastore.prepare(q);


    String jsonStr = "[";
    boolean firstCoach = true;
    for (Entity user : pq.asIterable()) {
      String color = (String) user.getProperty(COLOR);
      String name = (String) user.getProperty(NAME);
      String current = String.format("%2.2f", (Double) user.getProperty(CURRENT));
      String id = user.getKey().getName();
      if (!firstCoach) {
        jsonStr += ",";
      }
      firstCoach = false;
      jsonStr += "{";
      jsonStr += "\"id\":\"" + id + "\"";
      jsonStr += ",\"color\":\"#" + color.substring(0, 6) + "\"";
      jsonStr += ",\"name\":\"" + name + "\"";
      jsonStr += ",\"current\":" + current;
      jsonStr += "}\n";
    }
    
    jsonStr += "]";
    
    return jsonStr;
  }
}
