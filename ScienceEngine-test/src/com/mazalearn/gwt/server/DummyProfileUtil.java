package com.mazalearn.gwt.server;

import com.google.appengine.api.datastore.Entity;

public class DummyProfileUtil extends ProfileUtil {
  
  private Entity installEntity;

  @Override
  public Entity createOrGetInstall(String installId, boolean create) {
    return installEntity;
  }
  
  void setInstallEntity(Entity entity) {
    installEntity = entity;
  }

};
