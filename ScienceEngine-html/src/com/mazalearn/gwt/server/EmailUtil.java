package com.mazalearn.gwt.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import javax.servlet.ServletContext;

import com.mazalearn.scienceengine.app.utils.Crypter;

public class EmailUtil {

  void sendUserInvite(String fromEmail, String toEmail) {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);
  
    String msgBody = 
    		"Your friend " + fromEmail + " has sent you a Science gift." +
        "\nTo use the gift, you have to install Science Engine." +
        "\n\n-MazaLearn";
  
    try {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("admin@mazalearn.com", "Mazalearn Admin"));
        msg.addRecipient(MimeMessage.RecipientType.TO,
                         new InternetAddress(toEmail, "User"));
        msg.setSubject("Collect Science Engine Gift sent by " + fromEmail);
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

  void sendCertificateEmail(String userEmail, String userName, String userId, String dateStr, OutputStream outputStream, ServletContext servletContext) {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);
  
    String msgBody = "Congratulations! \n\n" +
        "Your ceriticate for Electromagnetism is attached" + 
        "\n\n-MazaLearn";
    ByteArrayOutputStream op = new ByteArrayOutputStream();
    PdfCertificateMaker.makeCertificate(servletContext, userName, dateStr, op);
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

  void sendConfirmationEmail(String userEmail, String userName, String installId) 
      throws UnsupportedEncodingException {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);
  
    long timeEmailSent = System.currentTimeMillis();
    String msgBody = "Welcome to Science Engine\nTo complete registration please click on link below: \n" +
        "http://www.mazalearn.com/re" + 
        "?i=" + installId + 
        "&e=" + userEmail +
        "&n=" + URLEncoder.encode(userName, "UTF-8") +
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
