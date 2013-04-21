package com.mazalearn.gwt.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class EmailCertificateServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID).toLowerCase();
    System.out.println("EmailCertificate - User: " + userId);

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity user = ProfileServlet.retrieveUser(userId, ds);
    if (user == null) { 
      response.getWriter().append("Could not find user");
      return;
    }

    EmbeddedEntity profile = ProfileServlet.createOrGetUserProfile(user, true);
    
    String userEmail = userId;
    if (userEmail == null || !userEmail.contains("@")) {
      response.getWriter().append("Improper email address. Cannot send certificate");
      return;
    }
    String userName = (String) profile.getProperty(ProfileData.USER_NAME);
    if (userName == null || userName.length() < 2) {
      response.getWriter().append("Cannot create certificate - Improper Name" + userName);
      return;
    }
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
    String dateStr = dateFormat.format(date);

    //response.getWriter().append("Certificate sent to: " + userEmail);
    sendUserEmail(userEmail, userName, userId, dateStr, response.getOutputStream());
  }

  private void sendUserEmail(String userEmail, String userName, String userId, String dateStr, OutputStream outputStream) {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);

    String msgBody = "Congratulations! \n\n" +
        "Your ceriticate for Electromagnetism is attached" + 
        "\n\n-MazaLearn";
    ByteArrayOutputStream op = new ByteArrayOutputStream();
    PdfCertificateMaker.makeCertificate(getServletContext(), userName, dateStr, op);
    byte[] pdfBytes = op.toByteArray();
    try {
        outputStream.write(pdfBytes);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("admin@mazalearn.com", "Mazalearn Admin"));
        msg.addRecipient(Message.RecipientType.TO,
                         new InternetAddress(userEmail, "User"));
        msg.setSubject("Science Engine - Electromagnetism Certificate");
        msg.setText(msgBody);
        
        Multipart mp = new MimeMultipart();
        
        MimeBodyPart body = new MimeBodyPart();
        DataSource src1 = new ByteArrayDataSource(msgBody.getBytes(), "text/plain");
        body.setDataHandler(new DataHandler(src1)); 
        mp.addBodyPart(body);
        
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setFileName("Certificate.pdf");
        DataSource src = new ByteArrayDataSource(pdfBytes, "application/pdf");
        attachment.setDataHandler(new DataHandler(src)); 
        mp.addBodyPart(attachment);

        msg.setContent(mp);
        Transport.send(msg);

    } catch (AddressException e) {
       e.printStackTrace();
    } catch (MessagingException e) {
       e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }
}

