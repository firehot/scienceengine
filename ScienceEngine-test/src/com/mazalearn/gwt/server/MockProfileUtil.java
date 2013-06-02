package com.mazalearn.gwt.server;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.PropertyContainer;

public class MockProfileUtil extends ProfileUtil {
  
  private PropertyContainer installEntity, savedEntity;
  private Map<String, PropertyContainer> userEntities = new HashMap<String, PropertyContainer>();

  @Override
  public PropertyContainer createOrGetInstall(String installId, boolean create) {
    return installEntity;
  }
  
  void setInstallEntity(PropertyContainer installEntity) {
    this.installEntity = installEntity;
  }

  public void setUserEntity(String userId, PropertyContainer userEntity) {
    this.userEntities.put(userId, userEntity);
    ((EmbeddedEntity) userEntity).setProperty("KEY", userId);
  }

  @Override
  public PropertyContainer createOrGetUser(String userId, boolean create) {
    PropertyContainer user = userEntities.get(userId);
    if (user == null  && create) {
      user = new EmbeddedEntity();
      user.setProperty("KEY", userId);
    }
    return user;
  }
  
  @Override
  public void saveEntity(PropertyContainer entity) {
    savedEntity = entity;
  }
  
  public PropertyContainer getSavedEntity() {
    return savedEntity;
  }
};
