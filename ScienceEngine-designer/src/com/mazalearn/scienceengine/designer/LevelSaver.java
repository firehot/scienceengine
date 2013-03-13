package com.mazalearn.scienceengine.designer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.JsonWriter;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;

public class LevelSaver {
  
  private Topic level;
  private IScience2DController science2DController;
  private IScience2DView science2DView;
  private IScience2DModel science2DModel;


  public LevelSaver(IScience2DController science2DController) {
    this.science2DController = science2DController;
    this.level = science2DController.getLevel();
    this.science2DView = science2DController.getView();
    this.science2DModel = science2DController.getModel();
  }
  
  public void save() throws IOException {
    FileHandle file = 
        LevelUtil.getLevelFile(science2DController.getTopic(), level, ".json");
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
  }

  private void writeLevelInfo(JsonWriter jsonWriter) throws IOException {
    jsonWriter.set("name", science2DController.getTopic());
    jsonWriter.set("level", level);
    Label title = (Label) science2DView.findActor(ScreenComponent.Title.name());
    jsonWriter.set("description", title.getText());
  }
  
  private void writeConfigs(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("configs");
    for (final IModelConfig<?> config : science2DModel.getAllConfigs().values()) {
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
    for (final List<Actor> locationGroup : science2DView.getLocationGroups()) {
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
        Science2DBody science2DBody = (Science2DBody) circuitElement;
        jsonWriter.value(science2DBody.name());
      }
      jsonWriter.pop();
    }
    jsonWriter.pop();
  }
  
  private void writeComponents(JsonWriter jsonWriter) throws IOException {
    jsonWriter.array("components");
    for (Actor a : science2DView.getActors()) {
      if (!a.isVisible()) continue; 
      String movementMode = "None";
      boolean dynamicBody = false;
      if (a instanceof Science2DActor) {
        Science2DActor science2DActor = (Science2DActor) a;
        movementMode = science2DActor.getMovementMode();
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
          .set("move", movementMode)
          .set("bodytype", dynamicBody)
          .pop();
    }
    jsonWriter.pop();
  }

}