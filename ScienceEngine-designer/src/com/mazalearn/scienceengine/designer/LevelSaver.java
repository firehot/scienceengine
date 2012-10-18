package com.mazalearn.scienceengine.designer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.JsonWriter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public class LevelSaver {
  
  private static final float THUMBNAIL_SCALE = 7.5f;

  private int level;
  private IScience2DStage science2DStage;
  private IScience2DModel science2DModel;
  private ControlPanel controlPanel;

  public LevelSaver(int level,
      ControlPanel controlPanel, IScience2DStage science2DStage,
      IScience2DModel science2DModel) {
    this.level = level;
    this.controlPanel = controlPanel;
    this.science2DStage = science2DStage;
    this.science2DModel = science2DModel;
  }
  
public void save() throws IOException {
  FileHandle file = LevelUtil.getLevelFile(controlPanel.getExperimentName(), ".json", level);
  file = Gdx.files.external(file.path());
  FileWriter writer = new FileWriter(file.file());
  JsonWriter jsonWriter = new JsonWriter(writer);

  jsonWriter = jsonWriter.object();
  writeLevelInfo(jsonWriter);
  writeComponents(jsonWriter);
  writeGroups(jsonWriter);
  writeCircuits(jsonWriter);
  writeConfigs(jsonWriter);

  jsonWriter.flush();
  jsonWriter.close();
  saveLevelThumbnail();
}

/**
 * Take screenshot, convert to a thumbnail and save to the level file as png.
 */
private void saveLevelThumbnail() {
  FileHandle screenFile = 
      LevelUtil.getLevelFile(controlPanel.getExperimentName(), ".png", level);
  screenFile = Gdx.files.external(screenFile.path());
  Pixmap screenShot = ScreenUtils.getScreenshot(0, 0, Gdx.graphics.getWidth(), 
      Gdx.graphics.getHeight(), true);
  Pixmap thumbnail = ScreenUtils.createThumbnail(screenShot, THUMBNAIL_SCALE);
  PixmapIO.writePNG(screenFile, thumbnail);
  screenShot.dispose();
  thumbnail.dispose();
}

private void writeLevelInfo(JsonWriter jsonWriter) throws IOException {
  jsonWriter.set("name", controlPanel.getExperimentName());
  jsonWriter.set("level", level);
  Label title = (Label) science2DStage.findActor("Title");
  jsonWriter.set("description", title.getText());
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

private void writeGroups(JsonWriter jsonWriter) throws IOException {
  jsonWriter.array("groups");
  for (final List<Actor> locationGroup : science2DStage.getLocationGroups()) {
    jsonWriter.array();
    for (final Actor actor: locationGroup) {
        jsonWriter.value(actor.getName());
    }
    jsonWriter.pop();
  }
  jsonWriter.pop();
}

private void writeCircuits(JsonWriter jsonWriter) throws IOException {
  jsonWriter.array("circuits");
  for (final List<CircuitElement> circuit : science2DModel.getCircuits()) {
    jsonWriter.array();
    for (final CircuitElement circuitElement: circuit) {
        jsonWriter.value(((Science2DBody) circuitElement).getName());
    }
    jsonWriter.pop();
  }
  jsonWriter.pop();
}

private void writeComponents(JsonWriter jsonWriter) throws IOException {
  jsonWriter.array("components");
  for (Actor a : science2DStage.getActors()) {
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

}