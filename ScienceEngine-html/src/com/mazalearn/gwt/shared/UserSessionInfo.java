package com.mazalearn.gwt.shared;

import java.io.Serializable;
import com.google.gwt.core.client.GWT;

@SuppressWarnings("serial")
/**
 * This object holds information about the current session with a user.
 * It includes the user id, user email, and permission for access to demo.
 *
 */
public class UserSessionInfo implements Serializable {

  private String userEmail;
  private String userName;
  private boolean isPermitted;

  /**
   * Return a new object with information about the current session. This
   * constructor can only be invoked on the server.
   * 
   * @param userName
   * @param userEmail
   * @param isPermitted
   */

  public UserSessionInfo(String userName, String userEmail, boolean isPermitted) {
    assert !GWT.isClient();
    this.userName = userName;
    this.userEmail = userEmail;
    this.isPermitted = isPermitted;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public String getUserName() {
    return userName;
  }

  public boolean isPermitted() {
    return isPermitted;
  }

  /**
   * Return a new object. Note that this one is used only for RPC serialization.
   */

  public UserSessionInfo() {
  }

} // end of class UserSessionInfo
