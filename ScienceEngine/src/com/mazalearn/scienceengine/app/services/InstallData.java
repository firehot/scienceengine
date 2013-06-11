package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;

import com.mazalearn.scienceengine.JsonSerializable;


public class InstallData  implements JsonSerializable {
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
  public ArrayList<String> availableTopicNames = new ArrayList<String>();
  public boolean isChanged;

  public InstallData() {
  }
}