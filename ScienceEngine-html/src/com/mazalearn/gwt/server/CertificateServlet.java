package com.mazalearn.gwt.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;

@SuppressWarnings("serial")
public class CertificateServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID).toLowerCase();
    String topic = request.getParameter("topic");
    System.out.println("Certificate - User: " + userId + " ," + topic);

    ProfileUtil profileUtil = new ProfileUtil();
    PropertyContainer user = profileUtil.retrieveUser(userId);
    if (user == null) { 
      response.getWriter().append("Could not find user");
      return;
    }

    if (topic == null) { 
      response.getWriter().append("Could not find topic");
      return;
    }
    
    EmbeddedEntity profile = ProfileUtil.createOrGetUserProfile(user, true);
    
    String userEmail = userId;
    if (userEmail == null || !userEmail.contains("@")) {
      response.getWriter().append("Improper userid. Is User registered?");
      return;
    }
    ClientProps clientProps = new JsonEntityUtil().getFromJsonTextProperty(profile, ProfileData.CLIENT_PROPS, ClientProps.class);
    int pos = clientProps.certificates.indexOf(topic);
    if (pos == -1) {
      response.getWriter().append("Sorry! Certificate not found.");
      return;
    }
    
    ServerProps serverProps = new JsonEntityUtil().getFromJsonTextProperty(profile, ProfileData.SERVER_PROPS, ServerProps.class);
    String userName = serverProps.userName;
    if (userName == null || userName.length() < 2) {
      response.getWriter().append("Cannot create certificate - Improper Name" + userName);
      return;
    }
    Date date = new Date(clientProps.certificateTimes.get(pos));
    DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
    String dateStr = dateFormat.format(date);

    byte[] pdfBytes = PdfCertificateMaker.makeCertificate(getServletContext(), topic, userName, dateStr);
    response.getOutputStream().write(pdfBytes);
    // Email sending of certificate seems unnecessary
    // new EmailUtil().sendCertificateEmail(topic, userEmail, userName, userId, dateStr, pdfBytes);
  }
}

