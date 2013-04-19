package com.mazalearn.scienceengine.app.services.loaders;


import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;
import com.mazalearn.scienceengine.tutor.ITutor;

public class LevelLoader {
    
  private IScience2DController science2DController;
  private IScience2DView science2DView;
  private IScience2DModel science2DModel;
  private ModelControls modelControls;
  private ViewControls viewControls;
  private Topic level;
  private OrderedMap<String, ?> rootElem;
  private ComponentLoader componentLoader;
  private TutorLoader tutorLoader;

  public LevelLoader(IScience2DController science2DController) {
    this.science2DController = science2DController;
    this.level = science2DController.getLevel();
    this.science2DView = science2DController.getView();
    this.science2DModel = science2DController.getModel();
    this.modelControls = science2DController.getModelControls();
    this.viewControls = science2DController.getViewControls();
    componentLoader = new ComponentLoader(science2DController);
    tutorLoader = new TutorLoader(science2DController);
  }
   
  public void load() {
    rootElem = getJsonFromFile();
    loadFromJson();
  }

  public OrderedMap<String, ?> getJsonFromFile() {
    Gdx.app.log(ScienceEngine.LOG, "Opening level json file");
    Topic topic = science2DController.getTopic();
    Topic level = science2DController.getLevel();
    rootElem = getJsonFromFile(topic, level);
    return rootElem;
  }

  @SuppressWarnings("unchecked")
  public static OrderedMap<String, ?> getJsonFromFile(Topic topic, Topic level) {
    FileHandle file = LevelUtil.getLevelFile(topic, level, ".json");
    if (file == null) {
      Gdx.app.error(ScienceEngine.LOG, "Could not open level json file");
      return null;
    }
    String str = file.readString();
    OrderedMap<String, ?> rootElem = (OrderedMap<String, ?>) new JsonReader().parse(str);
    return rootElem;
  }
  
  public void loadFromJson() {
    Gdx.app.log(ScienceEngine.LOG, "Loading from json");
    // readLevelInfo(rootElem);
    componentLoader.loadComponents((Array<?>) rootElem.get("components"), true);
    GroupLoader.loadGroups((Array<?>) rootElem.get("groups"), science2DView);
    CircuitLoader.loadCircuits((Array<?>) rootElem.get("circuits"), science2DModel);
    
    science2DModel.prepareModel();
    science2DView.prepareView();
    ConfigLoader.loadConfigs((Array<?>) rootElem.get("configs"), science2DModel);
    modelControls.refresh();
    loadTutors((Array<?>) rootElem.get("tutors"));
  }
  
  public void reload() {
    Gdx.app.log(ScienceEngine.LOG, "Reloading from json");
    science2DModel.reset();
    rootElem = getJsonFromFile();
    readLevelInfo(rootElem);
    componentLoader.loadComponents((Array<?>) rootElem.get("components"), false);
    ConfigLoader.loadConfigs((Array<?>) rootElem.get("configs"), science2DModel);
    modelControls.enableControls(true);
    viewControls.enableControls(true);
    modelControls.refresh();
  }
  
  private void readLevelInfo(OrderedMap<String, ?> info) {
    String description = (String) nvl(info.get("description"), 
        science2DController.getTopic() + " : Level " + level);
    Label title = (Label) science2DView.findActor(ScreenComponent.Title.name());
    title.setText(description);
  }

  private void loadTutors(Array<?> tutors) {
    if (tutors == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading tutors");
    ITutor rootTutor = science2DController.getGuru().getRootTutor();
    List<ITutor> childTutors = tutorLoader.loadChildTutors(
        science2DController.getLevel(), rootTutor, tutors);
    science2DController.getGuru().initialize(childTutors);
  }

  static Object nvl(Object val, Object defaultVal) {
    return val == null ? defaultVal : val;
  }
}