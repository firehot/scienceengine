package com.mazalearn.scienceengine.app.services;


public class InstallData {
  public static final String PNG = "png";
  public static final String USER_IDS = "userids";
  public static final String INSTALL_DATA = "installdata";

  public String installId;
  public String registeredUserId;
  public String installName;
  public String enterpriseId;
  public String pngEnterpriseLogo;
  public String enterpriseName;
  public String expiryDate;
  public long lastUpdated;
  public String[] userIds;

  public InstallData() {
  }
}