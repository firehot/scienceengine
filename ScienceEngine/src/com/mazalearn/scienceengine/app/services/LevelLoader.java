package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
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
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.EnvironmentBody;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.probe.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.probe.Hint;
import com.mazalearn.scienceengine.core.probe.LearningProber;
import com.mazalearn.scienceengine.core.probe.ParameterDirectionProber;
import com.mazalearn.scienceengine.core.probe.ParameterMagnitudeProber;
import com.mazalearn.scienceengine.core.probe.ProbeManager;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;

public class LevelLoader {
    
  private IScience2DController science2DController;
  private IScience2DView science2DView;
  private IScience2DModel science2DModel;
  private ControlPanel controlPanel;
  private int level;
  private OrderedMap<String, ?> rootElem;
  private Map<String, Integer> componentTypeCount = new HashMap<String, Integer>();

  public LevelLoader(IScience2DController science2DController) {
    this.science2DController = science2DController;
    this.level = science2DController.getLevel();
    this.science2DView = science2DController.getView();
    this.science2DModel = science2DController.getModel();
    this.controlPanel = science2DController.getControlPanel();
  }
   
  public void load() {
    rootElem = getJsonFromFile();
    loadFromJson();
  }

  @SuppressWarnings("unchecked")
  public OrderedMap<String, ?> getJsonFromFile() {
    Gdx.app.log(ScienceEngine.LOG, "Opening level json file");
    String experimentName = science2DController.getName();
    int level = science2DController.getLevel();
    FileHandle file = LevelUtil.getLevelFile(experimentName, ".json", level);
    if (file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open level json file");
      return null;
    }
    String str = file.readString();
    rootElem = (OrderedMap<String, ?>) new JsonReader().parse(str);
    return rootElem;
  }
  
  public void loadFromJson() {
    Gdx.app.log(ScienceEngine.LOG, "Loading from json");
    readLevelInfo(rootElem);
    readComponents((Array<?>) rootElem.get("components"), true);
    readGroups((Array<?>) rootElem.get("groups"));
    readCircuits((Array<?>) rootElem.get("circuits"));
    
    science2DModel.prepareModel();
    science2DView.prepareStage();
    controlPanel.refresh();
    readConfigs((Array<?>) rootElem.get("configs"), science2DModel);
    readProbers((Array<?>) rootElem.get("probers"));
  }
  
  public void reload() {
    Gdx.app.log(ScienceEngine.LOG, "Reloading from json");
    science2DModel.reset();
    rootElem = getJsonFromFile();
    readLevelInfo(rootElem);
    componentTypeCount.clear();
    readComponents((Array<?>) rootElem.get("components"), false);
    readConfigs((Array<?>) rootElem.get("configs"), science2DModel);
    controlPanel.refresh();
  }
  
  private void readLevelInfo(OrderedMap<String, ?> info) {
    String description = (String) nvl(info.get("description"), 
        science2DController.getName() + " : Level " + level);
    Label title = (Label) science2DView.findActor("Title");
    title.setText(description);
  }

  private void readCircuits(Array<?> circuits) {
    science2DModel.removeCircuits();

    if (circuits == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading circuits");
    
    for (int i = 0; i < circuits.size; i++) {
      @SuppressWarnings("unchecked")
      Array<String> circuit = (Array<String>) circuits.get(i);
      readCircuit(circuit);
    }
  }
  
  private void readCircuit(Array<String> circuit) {
    Gdx.app.log(ScienceEngine.LOG, "Loading circuit");
    CircuitElement[] circuitElements = new CircuitElement[circuit.size];
    for (int i = 0; i < circuit.size; i++) {
      String name = circuit.get(i);
      Actor actor = science2DView.findActor(name);
      if (actor == null) {
        throw new IllegalArgumentException("Component not found: " + name);
      }
      circuitElements[i] = (CircuitElement) (((Science2DActor) actor).getBody());      
    }
    science2DModel.addCircuit(circuitElements);
  }

  private void readGroups(Array<?> groups) {
    science2DView.removeLocationGroups();

    if (groups == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading groups");
    
    for (int i = 0; i < groups.size; i++) {
      @SuppressWarnings("unchecked")
      Array<String> group = (Array<String>) groups.get(i);
      readGroup(group);
    }
  }
  
  private void readGroup(Array<String> group) {
    Gdx.app.log(ScienceEngine.LOG, "Loading group");
    Actor[] groupElements = new Actor[group.size];
    for (int i = 0; i < group.size; i++) {
      String name = group.get(i);
      Actor actor = science2DView.findActor(name);
      if (actor == null) {
        throw new IllegalArgumentException("Actor not found: " + name);
      }
      groupElements[i] = actor;      
    }
    science2DView.addLocationGroup(groupElements);
  }

  /**
   * Load configs from JSON array
   * @param configs
   * @param science2DModel
   */
  public static void readConfigs(Array<?> configs, IScience2DModel science2DModel) {
    if (configs == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading configs");
    
    for (int i = 0; i < configs.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> config = (OrderedMap<String, ?>) configs.get(i);
      readConfig(config, science2DModel);
    }
  }

  private void readProbers(Array<?> probers) {
    if (probers == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading probers");
    
    for (int i = 0; i < probers.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> prober = (OrderedMap<String, ?>) probers.get(i);
      readProber(prober);
    }
  }

  private void readEnvironment(EnvironmentBody environment, Array<?> environmentParams) {
    Gdx.app.log(ScienceEngine.LOG, "Loading environment");
    for (int i = 0; i < environmentParams.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> parameter = (OrderedMap<String, ?>) environmentParams.get(i);
      String parameterName = (String) parameter.get("name");
      environment.addParameter(parameterName);
    }
  }

  private void readComponents(Array<?> components, boolean create) {
    if (components == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading components");
   
    for (int i = 0; i < components.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> component = (OrderedMap<String, ?>) components.get(i);
      readComponent(component, create);
    }
  }

  private static Object nvl(Object val, Object defaultVal) {
    return val == null ? defaultVal : val;
  }
  
  @SuppressWarnings("unchecked")
  private void readComponent(OrderedMap<String, ?> component, boolean create) {
    String type = (String) component.get("type");
    Gdx.app.log(ScienceEngine.LOG, "Loading component: " + type);
    if (type == null) return;
    float x = (Float) nvl(component.get("x"), 0f);
    float y = (Float) nvl(component.get("y"), 0f);
    float rotation = (Float) nvl(component.get("rotation"), 0f);
    
    Actor actor = create ? createActor(type, x, y, rotation) : findActor(type);
    if (actor == null) {
      Gdx.app.log(ScienceEngine.LOG, "Ignoring - Could not load component: " + type);
      return;
    }
    actor.setX(x);
    actor.setY(y);
    actor.setOriginX((Float) nvl(component.get("originX"), 0f));
    actor.setOriginY((Float) nvl(component.get("originY"), 0f));
    actor.setWidth((Float) nvl(component.get("width"), 20f));
    actor.setHeight((Float) nvl(component.get("height"), 20f));
    actor.setVisible((Boolean) nvl(component.get("visible"), true));
    actor.setRotation(rotation);
    if (actor instanceof Science2DActor) {
      Science2DActor science2DActor = (Science2DActor) actor;
      science2DActor.setPositionFromViewCoords(false);
      science2DActor.setMovementMode((String) nvl(component.get("move"), "None"));
      if ((Boolean)nvl(component.get("bodytype"), false)) {
        science2DActor.getBody().setType(BodyType.DynamicBody);
      } else {
        science2DActor.getBody().setType(BodyType.StaticBody);
      }
      if (ComponentType.Environment.name().equals(type)) {
        readEnvironment((EnvironmentBody) science2DActor.getBody(), 
            (Array<String>) component.get("params"));
      }
    }
  }

  private Actor createActor(String type, float x, float y, float rotation) {
    Science2DBody science2DBody = 
        science2DModel.addBody(type, x / ScienceEngine.PIXELS_PER_M, 
            y / ScienceEngine.PIXELS_PER_M, 
            rotation * MathUtils.degreesToRadians);
    Actor actor = null;
    if (science2DBody != null) {
      actor = science2DView.addScience2DActor(science2DBody);
    } else {
      actor = science2DView.addVisualActor(type);
    }
    if (actor == null && type.equals("ControlPanel")) {
      actor = science2DView.findActor(type);
    }
    return actor;
  }

  private Actor findActor(String type) {
    Actor actor = science2DView.findActor(type);
    // If multiple actors of same type, they have number suffix 1,2,3...
    if (actor == null) {
      Integer count = (Integer) nvl(componentTypeCount.get(type), 0) + 1;
      componentTypeCount.put(type, count);
      actor = science2DView.findActor(type + "." + count);
    }
    return actor;
  }

  @SuppressWarnings("unchecked")
  private static void readConfig(OrderedMap<String, ?> configObj, IScience2DModel science2DModel) {
    String name = (String) configObj.get("name");
    Gdx.app.log(ScienceEngine.LOG, "Loading config: " + name);
    IModelConfig<?> config = science2DModel.getConfig(name);
    if (config == null) return;
    
    config.setPermitted((Boolean) nvl(configObj.get("permitted"), false));
    if (configObj.get("value") != null) {
      switch (config.getType()) {
      case TOGGLE:
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

  @SuppressWarnings("unchecked")
  private void readProber(OrderedMap<String, ?> proberObj) {
    String proberName = (String) proberObj.get("name");
    Gdx.app.log(ScienceEngine.LOG, "Loading prober: " + proberName);
    ProbeManager probeManager = science2DView.getProbeManager();
    AbstractScience2DProber prober = science2DView.createProber(proberName, probeManager);
    probeManager.registerProber(prober);
    String parameterName = (String) proberObj.get("parameter");
    String type = (String) proberObj.get("type");
    IModelConfig<?> parameter = science2DModel.getConfig(parameterName);
    Array<?> configs = (Array<?>) proberObj.get("configs");
    if (parameter != null) {
      ((ParameterMagnitudeProber) prober).setProbeConfig((IModelConfig<Float>) parameter, type, configs);
      return;
    }
    Array<String> depends = (Array<String>) proberObj.get("depends");
    if (depends != null) {
      List<IModelConfig<?>> dependConfigs = new ArrayList<IModelConfig<?>>();
      for (int i = 0; i < depends.size; i++) {
        parameterName = (String) depends.get(i);
        parameter = science2DModel.getConfig(parameterName);
        dependConfigs.add(parameter);
      }
      ((ParameterDirectionProber) prober).setProbeConfig(dependConfigs, type, configs);
      return;
    }
    if (configs != null) {
      Array<?> hintsObj = (Array<?>) proberObj.get("hints");
      List<Hint> hints = readHints(hintsObj);
      ((LearningProber) prober).setProbeConfig(configs, hints);
      return;
    }
  }

  @SuppressWarnings("unchecked")
  private List<Hint> readHints(Array<?> hintsObj) {
    List<Hint> hints = new ArrayList<Hint>();
    for (int i = 0; i < hintsObj.size; i++) {
      try {
        hints.add(readHint((OrderedMap<String, ?>) hintsObj.get(i)));
      } catch (SyntaxException e) {
        e.printStackTrace();
      }
    }
    return hints;
  }

  private Hint readHint(OrderedMap<String, ?> hintObj) throws SyntaxException {
    String hintText = (String) hintObj.get("hint");
    String exprString = (String) hintObj.get("expr");
    Parser parser = new Parser();
    Expr expr = parser.parseString(exprString);
    return new Hint(hintText, expr, parser.getVariables());
  }
}