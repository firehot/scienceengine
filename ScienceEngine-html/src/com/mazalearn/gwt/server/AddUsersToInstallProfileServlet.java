package com.mazalearn.gwt.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class AddUsersToInstallProfileServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String installId = request.getParameter(InstallProfileServlet.INSTALL_ID);
    String[] userIdsToAdd = request.getParameterValues(InstallProfileServlet.USER_IDS);
    System.out.println("AddUsersToInstallProfile - Received get: " + installId);
    if (userIdsToAdd != null) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      Entity installEntity = InstallProfileServlet.createOrGetInstall(installId, ds, false);
      if (installEntity == null) {
        response.getWriter().append("Installation not found: " + installId);
        return;
      }
      Text value = (Text) installEntity.getProperty(InstallProfileServlet.USER_IDS);
      String[] userArray = value == null ? new String[0] : value.getValue().split(",");
      Set<String> userSet = new HashSet<String>(Arrays.asList(userArray));
      for (String userId: userIdsToAdd) {
        userSet.add(userId);
      }
      String delimiter = "";
      String users = "";
      for (String userId: userSet) {
        users += delimiter + userId;
        delimiter = ",";
      }
      installEntity.setProperty(InstallProfileServlet.USER_IDS, new Text(users));
      long lastUpdated = System.currentTimeMillis();
      installEntity.setProperty(ProfileData.LAST_UPDATED, String.valueOf(lastUpdated));
      ds.put(installEntity);
      response.getWriter().append("Current list of users: " + users);
    }
  }
}
