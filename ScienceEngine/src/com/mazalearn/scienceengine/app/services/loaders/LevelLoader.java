package com.mazalearn.scienceengine.app.services.loaders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.guru.AbstractTutor;

public class LevelLoader {
    
  private IScience2DController science2DController;
  private IScience2DView science2DView;
  private IScience2DModel science2DModel;
  private ControlPanel controlPanel;
  private int level;
  private OrderedMap<String, ?> rootElem;
  private ComponentLoader componentLoader;
  private TutorLoader tutorLoader;

  public LevelLoader(IScience2DController science2DController) {
    this.science2DController = science2DController;
    this.level = science2DController.getLevel();
    this.science2DView = science2DController.getView();
    this.science2DModel = science2DController.getModel();
    this.controlPanel = science2DController.getControlPanel();
    componentLoader = new ComponentLoader(science2DController);
    tutorLoader = new TutorLoader(science2DController);
  }
   
  public void load() {
    rootElem = getJsonFromFile();
    loadFromJson();
  }

  @SuppressWarnings("unchecked")
  public OrderedMap<String, ?> getJsonFromFile() {
    Gdx.app.log(ScienceEngine.LOG, "Opening level json file");
    String domain = science2DController.getDomain();
    int level = science2DController.getLevel();
    FileHandle file = LevelUtil.getLevelFile(domain, ".json", level);
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
    componentLoader.loadComponents((Array<?>) rootElem.get("components"), true);
    GroupLoader.loadGroups((Array<?>) rootElem.get("groups"), science2DView);
    CircuitLoader.loadCircuits((Array<?>) rootElem.get("circuits"), science2DModel);
    
    science2DModel.prepareModel();
    science2DView.prepareView();
    controlPanel.refresh();
    ConfigLoader.loadConfigs((Array<?>) rootElem.get("configs"), science2DModel);
    loadPlan((Array<?>) rootElem.get("plan"));
  }
  
  public void reload() {
    Gdx.app.log(ScienceEngine.LOG, "Reloading from json");
    science2DModel.reset();
    rootElem = getJsonFromFile();
    readLevelInfo(rootElem);
    componentLoader.loadComponents((Array<?>) rootElem.get("components"), false);
    ConfigLoader.loadConfigs((Array<?>) rootElem.get("configs"), science2DModel);
    controlPanel.refresh();
  }
  
  private void readLevelInfo(OrderedMap<String, ?> info) {
    String description = (String) nvl(info.get("description"), 
        science2DController.getDomain() + " : Level " + level);
    Label title = (Label) science2DView.findActor("Title");
    title.setText(description);
  }

  private void loadPlan(Array<?> tutors) {
    if (tutors == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading tutors");
    
    for (int i = 0; i < tutors.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> tutorObj = (OrderedMap<String, ?>) tutors.get(i);
      AbstractTutor tutor = tutorLoader.loadTutor(tutorObj);
      science2DController.getGuru().registerTutor(tutor);
    }
  }

  static Object nvl(Object val, Object defaultVal) {
    return val == null ? defaultVal : val;
  }
}