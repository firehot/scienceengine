package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.utils.Crypter;

@SuppressWarnings("serial")
public class InstallProfileServlet extends HttpServlet {

  static final String INSTALL_PROFILE = "InstallProfile";

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String installId = request.getParameter(ProfileData.INSTALL_ID);
    String lastUpdatedStr = request.getParameter(ProfileData.LAST_UPDATED);
    long lastUpdatedClient = 0;
    try {
      lastUpdatedClient = Long.parseLong(lastUpdatedStr);
    } catch (NumberFormatException ignore) {}
    
    System.out.println("Received get: " + installId + " " + lastUpdatedClient);
    writeProfileResponse(response, installId, lastUpdatedClient);
  }

  private void writeProfileResponse(HttpServletResponse response, String installId, 
      long lastUpdatedClient)
      throws IOException {
    Entity installEntity = new ProfileUtil().createOrGetInstall(installId, true);
    Text installDataText = (Text) installEntity.getProperty(InstallData.INSTALL_DATA);
    InstallData installData = new Gson().fromJson(installDataText.getValue(), InstallData.class);

    
    // If client is already up to date, no need to send install profile
    if (lastUpdatedClient >= installData.lastUpdated) {
      response.getWriter().close();
      return;
    }
    
    // Send install profile
    String responseStr = getInstallProfileAsBase64(installId, installEntity);
    if (responseStr.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      response.getWriter().append(responseStr);
    }
    response.getWriter().close();
  }
  
  public String getInstallProfileAsBase64(String installId, Entity installEntity) 
      throws IllegalStateException {
    System.out.println(installEntity);
    Text data = (Text) installEntity.getProperty(InstallData.INSTALL_DATA);
    String json = data.getValue();
    System.out.println(json);
    String profileBase64 = Base64.encode(json);
    String hash = Crypter.saltedSha1Hash(profileBase64, installId);
    return profileBase64 + hash;
  }
}
