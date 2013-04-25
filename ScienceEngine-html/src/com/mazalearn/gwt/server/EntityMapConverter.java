package com.mazalearn.gwt.server;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.ProfileData;

public class EntityMapConverter {
  private Gson gson = new Gson();
  /* convert entity to map */
  public Map<String, Object> entityToMap(EmbeddedEntity entity) {
    Map<String, Object> entityMap = new HashMap<String, Object>();

    for (Map.Entry<String, Object> property : entity.getProperties().entrySet()) {
      String key = property.getKey();
      Object value = property.getValue();
      if (value instanceof EmbeddedEntity) {
        entityMap.put(key, entityToMap((EmbeddedEntity) value));
      } else if (value instanceof Text) {
        String s = ((Text) value).getValue();
        if (key.startsWith(ProfileData.PNG)) {
          entityMap.put(key, s);
        } else {
          entityMap.put(key, value);
        }
      } else {
        entityMap.put(key, value);
      }
    }

    return entityMap;
  }

  /* convert map to entity in the same entity sent in */
  @SuppressWarnings("unchecked")
  public void mapToEntity(EmbeddedEntity entity,
      Map<String, Object> entityMap) {

    for (Map.Entry<String, Object> property : entityMap.entrySet()) {
      String key = property.getKey();
      Object value = property.getValue();
      if (entity.getProperty(key) instanceof EmbeddedEntity) {
        mapToEntity((EmbeddedEntity) entity.getProperty(key), (Map<String, Object>) value);
      } else if (value instanceof String) {
        entity.setProperty(key, new Text((String) value));
      } else if (value instanceof Text || value instanceof Long) {
        entity.setProperty(key, value);
      } else {
        entity.setProperty(key, new Text(gson.toJson(value)));
      }
    }

  }
}