package com.mazalearn.scienceengine.app.services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public class LevelManager {
  private static final float THUMBNAIL_SCALE = 7.5f;
  private Stage stage;
  private FileHandle file;
  private int level = 1;
  private ControlPanel controlPanel;
  private String description;
  private IScience2DModel science2DModel;

  public LevelManager(Stage stage, IScience2DModel science2DModel, ControlPanel controlPanel) {
    this.stage = stage;
    this.science2DModel = science2DModel;
    this.controlPanel = controlPanel;
  }

  public String getName() {
    return controlPanel.getExperimentName();
  }

  public int getLevel() {
    return level;
  }
  
  public void setLevel(int level) {
    this.level = level;
    String fileName = getFileName(controlPanel.getExperimentName(), ".json", level);
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
    controlPanel.refresh();
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
    writeCircuits(jsonWriter);
    writeConfigs(jsonWriter);

    jsonWriter.flush();
    jsonWriter.close();
  }

  private void writeLevelInfo(JsonWriter jsonWriter) throws IOException {
    jsonWriter.set("name", controlPanel.getExperimentName());
    jsonWriter.set("level", level);
    jsonWriter.set("description", description);
  }

  private void writeConfigs(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("configs");
    for (final IModelConfig<?> config : controlPanel.getModelConfigs()) {
      jsonWriter.object()
          .set("name", config.getName())
          .set("permitted", config.isPermitted())
          .set("value", config.getValue())
          .pop();
    }
    jsonWriter.pop();
  }

  private void writeCircuits(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("circuits");
    for (final List<Science2DBody> circuit : science2DModel.getCircuits()) {
      jsonWriter.array();
      for (final Science2DBody science2DBody: circuit) {
          jsonWriter.value(science2DBody.getName());
      }
      jsonWriter.pop();
    }
    jsonWriter.pop();
  }

  private void writeComponents(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("components");
    for (Actor a : stage.getActors()) {
      if (!a.isVisible()) continue; 
      boolean moveAllowed = false, dynamicBody = false;
      if (a instanceof Science2DActor) {
        Science2DActor science2DActor = (Science2DActor) a;
        moveAllowed = science2DActor.isAllowMove();
        dynamicBody = science2DActor.getBody().getType() == BodyType.DynamicBody;
      }
      jsonWriter.object()
          .set("name", a.getName())
          .set("x", a.getX())
          .set("y", a.getY())
          .set("originX", a.getOriginX())
          .set("originY", a.getOriginY())
          .set("width", a.getWidth())
          .set("height", a.getHeight())
          .set("visible", a.isVisible())
          .set("rotation", a.getRotation())
          .set("move", moveAllowed)
          .set("bodytype", dynamicBody)
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
    initializeComponents();
    readComponents((Array<?>) rootElem.get("components"));
    readCircuits((Array<?>) rootElem.get("circuits"));
    readConfigs((Array<?>) rootElem.get("configs"));
  }

  private void readLevelInfo(OrderedMap<String, ?> info) {
    description = (String) nvl(info.get("description"), 
        controlPanel.getExperimentName() + " : Level " + level);
    Label title = (Label) stage.getRoot().findActor("Title");
    title.setText(description);
  }

  private void readCircuits(Array<?> circuits) {
    science2DModel.removeCircuits();

    if (circuits == null) return;
    
    for (int i = 0; i < circuits.size; i++) {
      @SuppressWarnings("unchecked")
      Array<String> circuit = (Array<String>) circuits.get(i);
      readCircuit(circuit);
    }
  }
  
  private void readCircuit(Array<String> circuit) {
    Science2DBody[] circuitElements = new Science2DBody[circuit.size];
    for (int i = 0; i < circuit.size; i++) {
      String name = circuit.get(i);
      Actor actor = stage.getRoot().findActor(name);
      if (actor == null) {
        throw new IllegalArgumentException("Component not found: " + name);
      }
      circuitElements[i] = ((Science2DActor) actor).getBody();      
    }
    science2DModel.addCircuit(circuitElements);
  }

  private void readConfigs(Array<?> configs) {
    if (configs == null) return;
    
    for (int i = 0; i < configs.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> config = (OrderedMap<String, ?>) configs.get(i);
      readConfig(config);
    }
  }

  private void initializeComponents() {
    // Make all actors inactive and invisible.
    for (Actor actor: stage.getActors()) {
      if (!"Title".equals(actor.getName()) && !"Circuit".equals(actor.getName())) {
        actor.setVisible(false);
      }
      if (actor instanceof Science2DActor) {
        ((Science2DActor) actor).setPositionFromViewCoords(false);
      }
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
    if (name == null) return;
    Actor actor = stage.getRoot().findActor(name);
    if (actor == null)
      return;

    actor.setX((Float) nvl(component.get("x"), 0f));
    actor.setY((Float) nvl(component.get("y"), 0f));
    actor.setOriginX((Float) nvl(component.get("originX"), 0f));
    actor.setOriginY((Float) nvl(component.get("originY"), 0f));
    actor.setWidth((Float) nvl(component.get("width"), 20f));
    actor.setHeight((Float) nvl(component.get("height"), 20f));
    actor.setVisible((Boolean) nvl(component.get("visible"), true));
    actor.setRotation((Float) nvl(component.get("rotation"), 0f));
    if (actor instanceof Science2DActor) {
      Science2DActor science2DActor = (Science2DActor) actor;
      science2DActor.setPositionFromViewCoords(false);
      science2DActor.setAllowMove((Boolean) nvl(component.get("move"), false));
      if ((Boolean)nvl(component.get("bodytype"), false)) {
        science2DActor.getBody().setType(BodyType.DynamicBody);
      } else {
        science2DActor.getBody().setType(BodyType.StaticBody);
      }
    }
  }

  private IModelConfig<?> findConfig(String name) {
    if (name == null) return null;
    for (IModelConfig<?> config : controlPanel.getModelConfigs()) {
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
    String fileName = getFileName(controlPanel.getExperimentName(), ".png", level);
    FileHandle screenFile = Gdx.files.external(fileName);
    Pixmap screenShot = ScreenUtils.getScreenshot(0, 0, Gdx.graphics.getWidth(), 
        Gdx.graphics.getHeight(), true);
    Pixmap thumbnail = ScreenUtils.createThumbnail(screenShot, THUMBNAIL_SCALE);
    PixmapIO.writePNG(screenFile, thumbnail);
    screenShot.dispose();
    thumbnail.dispose();
  }
  
  public static Texture getThumbnail(String experimentName, int level) {
    // TODO: internal external confusion for files - different paths on desktop
    FileHandle screenFile;
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      screenFile = Gdx.files.external(getFileName(experimentName, ".png", level));
    } else {
      screenFile = Gdx.files.internal(getFileName(experimentName, ".png", level));      
    }
    Pixmap pixmap;
    try {
      pixmap = new Pixmap(screenFile);
    } catch (GdxRuntimeException e) {
      pixmap = new Pixmap(ScreenUtils.powerOf2Ceiling(Gdx.graphics.getWidth()/7.5f), 
          ScreenUtils.powerOf2Ceiling(Gdx.graphics.getHeight()/7.5f), 
          Format.RGBA8888);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }
}