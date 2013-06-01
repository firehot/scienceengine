package com.mazalearn.gwt.server;

import java.io.UnsupportedEncodingException;

public class MockEmailUtil extends EmailUtil {

  private String mailSentTo;

  void sendConfirmationEmail(String userEmail, String userName, String installId) 
      throws UnsupportedEncodingException {
    this.mailSentTo = userEmail;
  }
  
  String getMailSentTo() {
    return mailSentTo;
  }
}
