package com.mazalearn.gwt.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
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

@SuppressWarnings("serial")
public class RegistrationServlet extends HttpServlet {

  public static final long EXPIRY_TIME_MS = 7 * 24 * 3600 * 1000; // 7 days
  private static final String SALT = "imazalearne";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileServlet.INSTALL_ID);
    System.out.println("Register User: " + userId);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity user = ProfileServlet.retrieveUser(userId, ds);
    if (user == null) { 
      response.getWriter().append("Already registered? Could not find installation");
      return;
    }

    EmbeddedEntity profile = ProfileServlet.createOrGetUserProfile(user, true);
    
    String userEmail = request.getParameter(ProfileServlet.USER_EMAIL);
    profile.setProperty(ProfileServlet.SEX, request.getParameter(ProfileServlet.SEX));
    profile.setProperty(ProfileServlet.GRADE, request.getParameter(ProfileServlet.GRADE));
    profile.setProperty(ProfileServlet.SCHOOL, request.getParameter(ProfileServlet.SCHOOL));
    profile.setProperty(ProfileServlet.CITY, request.getParameter(ProfileServlet.CITY));
    profile.setProperty(ProfileServlet.COMMENTS, request.getParameter(ProfileServlet.COMMENTS));
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    profile.setProperty(ProfileServlet.REGN_DATE, dateFormat.format(date));
    ds.put(user);

    response.getWriter().append("Registered: " + userEmail);
    sendUserEmail(userEmail, request.getParameter(ProfileServlet.USER_NAME), userId);
    response.getWriter().append("\n\nEmail has been sent. Please click on URL in email to complete registration.");
  }

  private void sendUserEmail(String userEmail, String userName, String installId) {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);

    long timeEmailSent = System.currentTimeMillis();
    String msgBody = "Welcome to Science Engine\nTo complete registration please visit: \n" +
        "http://www.mazalearn.com/re" + 
        "?i=" + installId + 
        "&e=" + userEmail +
        "&n=" + userName +
        "&t=" + timeEmailSent + 
        "&h=" + getHash(installId, userEmail, userName, timeEmailSent) + 
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

  public static String getHash(String installId, String userEmail, String userName, long timeEmailSent) {
    try { 
      MessageDigest md = MessageDigest.getInstance("MD5");
      String msg = installId + SALT + userEmail + SALT + userName + SALT + timeEmailSent;
      System.out.println(msg);
      return Base64.encode(md.digest(msg.getBytes("US-ASCII")));
    } catch (Exception ex) { 
    }
    return null;
  }
}

