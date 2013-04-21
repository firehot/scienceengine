package com.mazalearn.gwt.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.utils.Crypter;

@SuppressWarnings("serial")
public class RegistrationServlet extends HttpServlet {

  public static final long EXPIRY_TIME_MS = 7 * 24 * 3600 * 1000; // 7 days

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.INSTALL_ID).toLowerCase();
    System.out.println("Register User: " + userId);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity user = ProfileServlet.retrieveUser(userId, ds);
    if (user == null) { 
      response.getWriter().append("Already registered? Could not find installation");
      return;
    }

    EmbeddedEntity profile = ProfileServlet.createOrGetUserProfile(user, true);
    
    String userEmail = request.getParameter(ProfileData.USER_EMAIL).toLowerCase();
    if (userEmail == null || !userEmail.contains("@")) {
      response.getWriter().append("Improper email. Cannot register");
      return;
    }
    String userName = request.getParameter(ProfileData.USER_NAME);
    if (userName == null || userName.length() < 2) {
      response.getWriter().append("Improper name. Cannot register");
      return;
    }
    profile.setProperty(ProfileData.SEX, request.getParameter(ProfileData.SEX));
    profile.setProperty(ProfileData.GRADE, request.getParameter(ProfileData.GRADE));
    profile.setProperty(ProfileData.SCHOOL, request.getParameter(ProfileData.SCHOOL));
    profile.setProperty(ProfileData.CITY, request.getParameter(ProfileData.CITY));
    profile.setProperty(ProfileData.COMMENTS, request.getParameter(ProfileData.COMMENTS));
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    profile.setProperty(ProfileData.REGN_DATE, dateFormat.format(date));
    ds.put(user);

   sendUserEmail(userEmail, userName, userId);
    response.getWriter().append("<html><body><br><br>");
    response.getWriter().append("Registration in progress: " + userEmail);
    response.getWriter().append("<br><br>Email has been sent. <br>Please click on URL in email to complete registration.</body></html>");
  }

  private void sendUserEmail(String userEmail, String userName, String installId) {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);

    long timeEmailSent = System.currentTimeMillis();
    String msgBody = "Welcome to Science Engine\nTo complete registration please click on link below: \n" +
        "http://www.mazalearn.com/re" + 
        "?i=" + installId + 
        "&e=" + userEmail +
        "&n=" + userName +
        "&t=" + timeEmailSent + 
        "&h=" + Crypter.saltedSha1Hash(installId + userEmail + userName + timeEmailSent, installId) + 
        "\n\n-MazaLearn";

    try {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("admin@mazalearn.com", "Mazalearn Admin"));
        msg.addRecipient(Message.RecipientType.TO,
                         new InternetAddress(userEmail, "User"));
        msg.setSubject("Science Engine - Mazalearn.com account registration");
        msg.setText(msgBody);
        Transport.send(msg);

    } catch (AddressException e) {
        // ...
    } catch (MessagingException e) {
        // ...
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }    
  }
}

