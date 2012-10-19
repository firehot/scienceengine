package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public class LevelLoader {
    
  private IScience2DController science2DController;
  private IScience2DStage science2DStage;
  private IScience2DModel science2DModel;
  private ControlPanel controlPanel;
  private int level;

  public LevelLoader(IScience2DController science2DController) {
    this.science2DController = science2DController;
    this.level = science2DController.getLevel();
    this.science2DStage = science2DController.getView();
    this.science2DModel = science2DController.getModel();
    this.controlPanel = science2DController.getControlPanel();
  }
   
  @SuppressWarnings("unchecked")
  public void load() {
    Gdx.app.log(ScienceEngine.LOG, "Opening level json file");
    FileHandle file = 
        LevelUtil.getLevelFile(science2DController.getName(), ".json", level);
    if (file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open level json file");
      return;
    }
    String str = file.readString();
    OrderedMap<String, ?> rootElem = (OrderedMap<String, ?>) new JsonReader()
        .parse(str);

    readLevelInfo(rootElem);
    //initializeComponents();
    readComponents((Array<?>) rootElem.get("components"));
    readGroups((Array<?>) rootElem.get("groups"));
    readCircuits((Array<?>) rootElem.get("circuits"));
    
    science2DModel.prepareModel();
    science2DStage.prepareStage();
    controlPanel.refresh();
    readConfigs((Array<?>) rootElem.get("configs"));    
  }

  private void readLevelInfo(OrderedMap<String, ?> info) {
    String description = (String) nvl(info.get("description"), 
        science2DController.getName() + " : Level " + level);
    Label title = (Label) science2DStage.findActor("Title");
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
    CircuitElement[] circuitElements = new CircuitElement[circuit.size];
    for (int i = 0; i < circuit.size; i++) {
      String name = circuit.get(i);
      Actor actor = science2DStage.findActor(name);
      if (actor == null) {
        throw new IllegalArgumentException("Component not found: " + name);
      }
      circuitElements[i] = (CircuitElement) (((Science2DActor) actor).getBody());      
    }
    science2DModel.addCircuit(circuitElements);
  }

  private void readGroups(Array<?> groups) {
    science2DStage.removeLocationGroups();

    if (groups == null) return;
    
    for (int i = 0; i < groups.size; i++) {
      @SuppressWarnings("unchecked")
      Array<String> group = (Array<String>) groups.get(i);
      readGroup(group);
    }
  }
  
  private void readGroup(Array<String> group) {
    Actor[] groupElements = new Actor[group.size];
    for (int i = 0; i < group.size; i++) {
      String name = group.get(i);
      Actor actor = science2DStage.findActor(name);
      if (actor == null) {
        throw new IllegalArgumentException("Actor not found: " + name);
      }
      groupElements[i] = actor;      
    }
    science2DStage.addLocationGroup(groupElements);
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
    for (Actor actor: science2DStage.getActors()) {
      if (!"Title".equals(actor.getName()) && !"CircuitElement".equals(actor.getName()) &&
          !"Help".equals(actor.getName())) {
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
    String type = (String) component.get("type");
    if (type == null) return;
    
    Science2DBody science2DBody = science2DModel.addBody(type, 0, 0, 0);
    Actor actor = null;
    if (science2DBody != null) {
      actor = science2DStage.addScience2DActor(science2DBody);
    } else {
      actor = science2DStage.addVisualActor(type);
    }
    if (actor == null) {
      if (!type.equals("ControlPanel")) return;
      actor = controlPanel;
    }

    actor.setName((String) nvl(component.get("name"), type));
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
}