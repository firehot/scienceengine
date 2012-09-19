package com.mazalearn.scienceengine.services;

import java.io.FileWriter;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.controller.Configurator;
import com.mazalearn.scienceengine.controller.IModelConfig;
import com.mazalearn.scienceengine.utils.ScreenUtils;

public class LevelManager {
  private Stage stage;
  private FileHandle file;
  private int level = 1;
  private Configurator configurator;
  private String description;

  public LevelManager(Stage stage, Configurator configurator) {
    this.stage = stage;
    this.configurator = configurator;
  }

  public String getName() {
    return configurator.getExperimentName();
  }

  public int getLevel() {
    return level;
  }
  
  public void setLevel(int level) {
    this.level = level;
    String fileName = getFileName(configurator.getExperimentName(), ".json", level);
    Gdx.app.log(ScienceEngine.LOG, "Opening file: " + fileName);
    this.file = Gdx.files.internal(fileName);
    if (this.file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open file");
    }
  }

  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * Loads the content of the provided file and automatically position and size
   * the objects.
   * @param index 
   */
  public void load() {
    try {
      loadLevelConfiguration();
      Gdx.app.log(ScienceEngine.LOG, "[LevelEditor] File successfully loaded!");
    } catch (GdxRuntimeException ex) {
      System.err.println("[LevelEditor] Error happened while loading "
          + file.path());
    }
    configurator.refresh();
  }

  public void save() {
    try {
      saveLevelConfiguration();
      saveLevelThumbnail();
      System.out.println("[LevelEditor] File successfully saved!");
    } catch (IOException ex) {
      System.err.println("[LevelEditor] Error happened while writing "
          + file.path());
    }
  }

  private void saveLevelConfiguration() throws IOException {
    FileWriter writer = new FileWriter(file.file());
    JsonWriter jsonWriter = new JsonWriter(writer);

    jsonWriter = jsonWriter.object();
    writeLevelInfo(jsonWriter);
    writeComponents(jsonWriter);
    writeConfigs(jsonWriter);

    jsonWriter.flush();
    jsonWriter.close();
  }

  private void writeLevelInfo(JsonWriter jsonWriter) throws IOException {
    jsonWriter.set("name", configurator.getExperimentName());
    jsonWriter.set("level", level);
    jsonWriter.set("description", description);
  }

  private void writeConfigs(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("configs");
    for (final IModelConfig<?> config : configurator.getModelConfigs()) {
      jsonWriter.object()
          .set("name", config.getName())
          .set("permitted", config.isPermitted())
          .set("value", config.getValue())
          .pop();
    }
    jsonWriter.pop();
  }

  private void writeComponents(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("components");
    for (Actor a : stage.getActors()) {
      jsonWriter.object()
          .set("name", a.name)
          .set("x", a.x)
          .set("y", a.y)
          .set("originX", a.originX)
          .set("originY", a.originY)
          .set("width", a.width)
          .set("height", a.height)
          .set("visible", a.visible)
          .set("rotation", a.rotation)
          .pop();
    }
    jsonWriter.pop();
  }

  @SuppressWarnings("unchecked")
  private void loadLevelConfiguration() {
    String str = file.readString();
    OrderedMap<String, ?> rootElem = (OrderedMap<String, ?>) new JsonReader()
        .parse(str);

    readLevelInfo(rootElem);
    readComponents((Array<?>) rootElem.get("components"));
    readConfigs((Array<?>) rootElem.get("configs"));
  }

  private void readLevelInfo(OrderedMap<String, ?> info) {
    description = (String) nvl(info.get("description"), 
        configurator.getExperimentName() + " : Level " + level);
  }

  private void readConfigs(Array<?> configs) {
    if (configs == null) return;
    
    for (int i = 0; i < configs.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> config = (OrderedMap<String, ?>) configs.get(i);
      readConfig(config);
    }
  }

  private void readComponents(Array<?> components) {
    if (components == null) return;
    
    for (int i = 0; i < components.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> component = (OrderedMap<String, ?>) components
          .get(i);
      readComponent(component);
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
    for (IModelConfig<?> config : configurator.getModelConfigs()) {
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
      case ONOFF:
        ((IModelConfig<Boolean>) config).setValue((Boolean) configObj
            .get("value"));
        break;
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

  private static String getFileName(String experimentName, String extension, int level) {
    return "data/" + experimentName + "/" + level + extension;
  }

  /**
   * Take screenshot, convert to a thumbnail and save to the level file as png.
   */
  private void saveLevelThumbnail() {
    String fileName = getFileName(configurator.getExperimentName(), ".png", level);
    FileHandle screenFile = Gdx.files.external(fileName);
    Pixmap screenShot = ScreenUtils.getScreenshot(0, 0, Gdx.graphics.getWidth(), 
        Gdx.graphics.getHeight(), true);
    Pixmap thumbnail = ScreenUtils.createThumbnail(screenShot, 5);
    PixmapIO.writePNG(screenFile, thumbnail);
    screenShot.dispose();
    thumbnail.dispose();
  }
  
  public static Texture getThumbnail(String experimentName, int level) {
    // TODO: internal external confusion for files - different paths on desktop
    FileHandle screenFile = Gdx.files.external(getFileName(experimentName, ".png", level));
    Pixmap pixmap;
    try {
      pixmap = new Pixmap(screenFile);
    } catch (GdxRuntimeException e) {
      pixmap = new Pixmap(Gdx.graphics.getWidth()/5, Gdx.graphics.getHeight()/5, 
          Format.RGBA8888);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }
}