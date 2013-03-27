package com.mazalearn.gwt.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest; 
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

@SuppressWarnings("serial")
public class RegistrationEmailServlet extends HttpServlet {

  static final String USER_EMAIL = "useremail";
  static final String INSTALL_ID = "installid";
  private static final String SALT = "imazalearne";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Register - Received post: " + request.getContentLength());
    response.getWriter().append("Post received");
    String userEmail = request.getHeader(USER_EMAIL);
    String installId = request.getHeader(INSTALL_ID);
    System.out.println("User: " + userEmail + " id: " + installId);
    sendUserEmail(userEmail, installId);
  }

  private void sendUserEmail(String userEmail, String installId) {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);

    String msgBody = "Welcome to Science Engine\nTo complete registration please visit: \n" +
        "http://www.mazalearn.com/vu.jsp" + 
        "?i=" + installId + 
        "&e=" + userEmail + 
        "&h=" + getHash(installId, userEmail) + 
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

  public static String getHash(String installId, String userEmail) {
    try { 
      MessageDigest md = MessageDigest.getInstance("MD5");
      String msg = installId + SALT + userEmail;
      return Base64.encode(md.digest(msg.getBytes("US-ASCII")));
    } catch (Exception ex) { 
    }
    return null;
  }
}

