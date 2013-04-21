package com.mazalearn.gwt.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.utils.Crypter;

@SuppressWarnings("serial")
public class InstallProfileServlet extends HttpServlet {

  private static final String INSTALL_PROFILE = "InstallProfile";
  public static final String INSTALL_ID = ProfileData.INSTALL_ID;
  public static final String USER_IDS = "userids";

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String installId = request.getParameter(INSTALL_ID);
    String lastUpdatedStr = request.getParameter(ProfileData.LAST_UPDATED);
    long lastUpdated = 0;
    try {
      lastUpdated = Long.parseLong(lastUpdatedStr);
    } catch (NumberFormatException ignore) {}
    
    System.out.println("Received get: " + installId + " " + lastUpdated);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    writeProfileResponse(response, installId, lastUpdated, ds);
  }

  private void writeProfileResponse(HttpServletResponse response, String installId, 
      long lastUpdatedClient, DatastoreService ds)
      throws IOException {
    Entity installEntity = createOrGetInstall(installId, ds, true);
    String lastUpdatedServerStr = (String) installEntity.getProperty(ProfileData.LAST_UPDATED);
    long lastUpdatedServer = 0;
    try {
      lastUpdatedServer = Long.parseLong(lastUpdatedServerStr);
    } catch (NumberFormatException ignore) {}
    
    // If client is already up to date, no need to send install profile
    if (lastUpdatedClient >= lastUpdatedServer) {
      response.getWriter().close();
      return;
    }
    
    // Send install profile
    String responseStr = getInstallProfileAsBase64(installId, installEntity, ds);
    if (responseStr.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      response.getWriter().append(responseStr);
    }
    response.getWriter().close();
  }
  
  static class InstallProfile {
    Map<String, String> properties;
    String[] userids;
  }

  public static Entity createOrGetInstall(String installId, DatastoreService ds, boolean create) {
    if (installId == null || installId.length() == 0) return null;
    Key key = KeyFactory.createKey(INSTALL_PROFILE, installId.toLowerCase());
    Entity install = null;
    try {
      install = ds.get(key);
    } catch (EntityNotFoundException e) {
      if (create && install == null) {
        install = new Entity(INSTALL_PROFILE, installId.toLowerCase());
        install.setProperty(INSTALL_ID, installId);
        install.setProperty(ProfileData.LAST_UPDATED, 
            String.valueOf(System.currentTimeMillis()));
        ds.put(install);
      }
    }
    return install;
  }
  
  public String getInstallProfileAsBase64(String installId, Entity installEntity, DatastoreService ds) 
      throws IllegalStateException {
    System.out.println(installEntity);
    StringBuilder properties = new StringBuilder("{");
    String propDelimiter = "";
    String userids = "";
    for (Map.Entry<String, Object> property: installEntity.getProperties().entrySet()) {
      Object value = property.getValue();
      if (value instanceof Text) {
        String s = ((Text) value).getValue();
        if (property.getKey().startsWith(ProfileData.PNG)) {
          properties.append(propDelimiter + property.getKey() + ":\"" + s + "\"");
          propDelimiter = ",";
        } else if (property.getKey().equals(USER_IDS)) {
          userids = ",userids:[";
          String delim = "";
          for (String email: s.split(",")) {
            userids += delim + "\"" + email + "\"";
            delim = ",";
          }
          userids += "]";
        }
      } else {
        properties.append(propDelimiter + property.getKey() + ":\"" + value + "\"");
        propDelimiter = ",";
      }
    }
    properties.append("}");
    String json = "{ properties:" + properties + userids + "}";
    System.out.println(json);
    return packageProfile(json, installId);
  }
  
  private String packageProfile(String json, String installId) {
    String profileBase64 = Base64.encode(json);
    String hash = Crypter.saltedSha1Hash(profileBase64, installId);
    return profileBase64 + hash;
  }
}
