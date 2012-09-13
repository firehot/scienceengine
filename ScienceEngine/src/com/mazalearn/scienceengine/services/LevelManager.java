package com.mazalearn.scienceengine.services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.controller.IModelConfig;

public class LevelManager {
  private List<IModelConfig<?>> modelConfigs;
  private Stage stage;
  private FileHandle file;
  private String experimentName;
  private int level = 1;

  public LevelManager(String experimentName, Stage stage, 
      List<IModelConfig<?>> modelConfigs) {
    this.experimentName = experimentName;
    this.modelConfigs = modelConfigs;
    this.stage = stage;
  }

  /**
   * Loads the content of the provided file and automatically position and size
   * the objects.
   * @param index 
   */
  public void load() {
    try {
      loadFile();
      Gdx.app.log(ScienceEngine.LOG, "[LevelEditor] File successfully loaded!");
    } catch (GdxRuntimeException ex) {
      System.err.println("[LevelEditor] Error happened while loading "
          + file.path());
    }
  }

  public void saveLevel() {
    try {
      writeFile();
      System.out.println("[LevelEditor] File successfully saved!");
    } catch (IOException ex) {
      System.err.println("[LevelEditor] Error happened while writing "
          + file.path());
    }
  }

  private void writeFile() throws IOException {
    FileWriter writer = new FileWriter(file.file());
    JsonWriter jsonWriter = new JsonWriter(writer);

    jsonWriter = jsonWriter.object();

    writeComponents(jsonWriter);
    writeConfigs(jsonWriter);

    jsonWriter.flush();
    jsonWriter.close();
  }

  private void writeConfigs(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("configs");
    for (final IModelConfig<?> config : modelConfigs) {
      jsonWriter.object().set("name", config.getName())
          .set("permitted", config.isPermitted())
          .set("value", config.getValue()).pop();
    }
    jsonWriter.pop();
  }

  private void writeComponents(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("components");
    for (Actor a : stage.getActors()) {
      jsonWriter.object().set("name", a.name).set("x", a.x).set("y", a.y)
          .set("originX", a.originX).set("originY", a.originY)
          .set("width", a.width).set("height", a.height)
          .set("visible", a.visible).set("rotation", a.rotation).pop();
    }
    jsonWriter.pop();
  }

  @SuppressWarnings("unchecked")
  private void loadFile() {
    String str = file.readString();
    OrderedMap<String, ?> rootElem = (OrderedMap<String, ?>) new JsonReader()
        .parse(str);

    readComponents(rootElem);
    readConfigs(rootElem);
  }

  private void readConfigs(OrderedMap<String, ?> rootElem) {
    Array<?> configs = (Array<?>) rootElem.get("configs");
    if (configs != null) {
      for (int i = 0; i < configs.size; i++) {
        @SuppressWarnings("unchecked")
        OrderedMap<String, ?> config = (OrderedMap<String, ?>) configs.get(i);
        readConfig(config);
      }
    }
  }

  private void readComponents(OrderedMap<String, ?> rootElem) {
    Array<?> components = (Array<?>) rootElem.get("components");
    if (components != null) {
      for (int i = 0; i < components.size; i++) {
        @SuppressWarnings("unchecked")
        OrderedMap<String, ?> component = (OrderedMap<String, ?>) components
            .get(i);
        readComponent(component);
      }
    }
  }

  private Object nvl(Object val, Object defaultVal) {
    return val == null ? defaultVal : val;
  }

  private void readComponent(OrderedMap<String, ?> component) {
    String name = (String) component.get("name");
    Actor actor = stage.findActor(name);
    if (actor == null)
      return;

    actor.x = (Float) nvl(component.get("x"), 0f);
    actor.y = (Float) nvl(component.get("y"), 0f);
    actor.originX = (Float) nvl(component.get("originX"), 0f);
    actor.originY = (Float) nvl(component.get("originY"), 0f);
    actor.width = (Float) nvl(component.get("width"), 20f);
    actor.height = (Float) nvl(component.get("height"), 20f);
    actor.visible = (Boolean) nvl(component.get("visible"), true);
    actor.rotation = (Float) nvl(component.get("rotation"), 0f);
    if (actor instanceof Box2DActor) {
      ((Box2DActor) actor).setPositionFromViewCoords();
    }
  }

  private IModelConfig<?> findConfig(String name) {
    if (name == null) return null;
    for (IModelConfig<?> config : modelConfigs) {
      if (config.getName().equals(name)) return config;
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  private void readConfig(OrderedMap<String, ?> configObj) {
    String name = (String) configObj.get("name");
    IModelConfig<?> config = findConfig(name);
    if (config == null) return;
    
    config.setPermitted((Boolean) nvl(configObj.get("permitted"), false));
    if (configObj.get("value") != null) {
      switch (config.getType()) {
      case RANGE:
        ((IModelConfig<Float>) config).setValue((Float) configObj
            .get("value"));
        break;
      case LIST:
        ((IModelConfig<String>) config).setValue((String) configObj
            .get("value"));
        break;
      }
    }
  }

  public String getExperimentName() {
    return experimentName;
  }

  public int getLevel() {
    return level;
  }
  
  public void setLevel(int level) {
    this.level = level;
    Gdx.app.log(ScienceEngine.LOG, "Opening file: " + "data/" + experimentName + "_" + level + ".json");
    this.file = Gdx.files.internal("data/" + experimentName + "_" + level + ".json");
    if (this.file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open file");
    }
  }
}