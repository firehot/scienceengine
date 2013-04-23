package com.mazalearn.gwt.server;

import java.lang.reflect.Constructor;

import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.ProfileData;

public class JsonEntityUtil {

  private Gson gson = new Gson();

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> T getFromJsonTextProperty(PropertyContainer entity, String name, Class<T> clz) {
    Text objectJson = (Text) entity.getProperty(name);
    try {
      if (objectJson != null) {
        return gson.fromJson(objectJson.getValue(), clz);
      }
      Constructor c = clz.getConstructor(new Class[0]);
      return (T) c.newInstance(new Object[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void setAsJsonTextProperty(
      PropertyContainer entity, String name, Object javaObj) {
    entity.setProperty(name, new Text(gson.toJson(javaObj)));
  }

  public ProfileData profileFromJson(String clientProfileJson) {
    return gson.fromJson(clientProfileJson, ProfileData.class);
  }

}
