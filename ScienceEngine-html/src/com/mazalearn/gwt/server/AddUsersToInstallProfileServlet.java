package com.mazalearn.gwt.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class AddUsersToInstallProfileServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String installId = request.getParameter(ProfileData.INSTALL_ID);
    String[] userIdsToAdd = request.getParameterValues(InstallData.USER_IDS);
    System.out.println("AddUsersToInstallProfile - Received get: " + installId);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    PropertyContainer installEntity = new ProfileUtil().createOrGetInstall(installId, false);
    if (installEntity == null) {
      response.getWriter().append("Installation not found: " + installId);
      return;
    }
    Text value = (Text) installEntity.getProperty(InstallData.INSTALL_DATA);
    InstallData data = null;
    if (value == null) {
      data = new InstallData();
    } else {
      data = new Gson().fromJson(value.getValue(), InstallData.class);
    }
    if (data.userIds == null) {
      data.userIds = new String[0];
    }
      
    if (userIdsToAdd != null) {
      List<String> userIds = new ArrayList<String>(0);
      for (String userId: data.userIds) {
        userIds.add(userId);
      }
      for (String userId: userIdsToAdd) {
        if (!userIds.contains(userId.toLowerCase())) {
          userIds.add(userId);
          data.lastUpdated = System.currentTimeMillis();
        }
      }
      data.userIds = userIds.toArray(new String[0]);
      installEntity.setProperty(InstallData.INSTALL_DATA, new Text(new Gson().toJson(data)));
      ds.put((Entity) installEntity);
    }
    response.getWriter().append("Current list of users: " + Arrays.asList(data.userIds));
  }
}
