package com.mazalearn.gwt.server;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Text;
import com.mazalearn.scienceengine.app.services.ProfileData;

public class EntityMapConverter {
  /* convert entity to map */
  public static Map<String, Object> entityToMap(EmbeddedEntity entity) {
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
  public static void mapToEntity(EmbeddedEntity entity,
      Map<String, Object> entityMap) {

    for (Map.Entry<String, Object> property : entityMap.entrySet()) {
      String key = property.getKey();
      Object value = property.getValue();
      if (value instanceof Map) {
        mapToEntity((EmbeddedEntity) entity.getProperty(key), (Map<String, Object>) value);
      } else if (value instanceof String) {
        entity.setProperty(key, new Text((String) value));
      } else {
        entity.setProperty(key, value);
      }
    }

  }
}