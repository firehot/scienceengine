package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IExperimentController;
import com.mazalearn.scienceengine.core.view.IExperimentView;

/**
 * Experiment Home screen - shows all levels for that experiment.
 */
public class ExperimentHomeScreen extends AbstractScreen {

  IExperimentController experimentController;
  private Image[] experimentThumbs;
  private LevelManager levelManager;
  private Array<?> levels;
  private Array<?> resources;
  private LabelStyle smallLabelStyle;

  public ExperimentHomeScreen(ScienceEngine scienceEngine, 
      IExperimentController experimentController) {
    super(scienceEngine);
    this.experimentController = experimentController;
    setBackgroundColor(Color.DARK_GRAY);
    readExperimentInfo();
    getFont().setScale(0.8f);
    smallLabelStyle = new LabelStyle(getFont(), Color.WHITE);
  }

  @Override
  public void show() {
    super.show();
    
    Table table = super.getTable();
    
    table.defaults().fill().center();
    table.add(experimentController.getName()).colspan(100);
    table.row();
    
    Label levels = new Label("L\nE\nV\nE\nL\nS", getSkin());
    table.add(levels).width(20).pad(10, 0, 5, 0).center();
    table.add(createExperimentLevelPane()).fill();    
    table.row();
    Label content = new Label("R\nE\nS\nO\nU\nR\nC\nE\nS", smallLabelStyle);
    table.add(content).width(20).pad(10, 0, 5, 0).center();
    table.add(createResourcePane()).fill();
    table.row();
    
    // register the back button
    TextButton backButton = 
        new TextButton("Back to Experiments", scienceEngine.getSkin());
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(new ExperimentMenuScreen(scienceEngine));
      }
    });
    table.row();
    table.add(backButton).fill().colspan(100);
  }

  private Actor createExperimentLevelPane() {
    final IExperimentView experimentView = experimentController.getView();
    Profile profile = scienceEngine.getProfileManager().retrieveProfile();
    int level = Math.max(profile.getCurrentLevelId(), 1);
    levelManager = experimentView.getLevelManager();
    levelManager.setLevel(level);
    
    Table experimentLevels = super.getTable().newTable();
    FlickScrollPane experimentLevelPane = 
        new FlickScrollPane(experimentLevels, "Levels");
    experimentThumbs = new Image[levels.size];
    
    for (int i = 0; i < levels.size; i++) {
      OrderedMap<String, ?> levelInfo = (OrderedMap<String, ?>) levels.get(i);
      Label label = new Label((String) levelInfo.get("name"), smallLabelStyle);
      label.setWrap(true);
      experimentLevels.add(label).width(128).left().pad(5);
    }
    experimentLevels.row();
    
    for (int i = 0; i < levels.size; i++) {
      final int iLevel = i + 1;
      Image experimentThumb = 
          new Image(LevelManager.getThumbnail(experimentController.getName(), iLevel));
      experimentThumb.setClickListener(new ClickListener() {
        @Override
        public void click(Actor actor, float x, float y) {
          Screen experimentLevelScreen = 
              new ExperimentScreen(scienceEngine, levelManager, 
                  iLevel, experimentController);
          scienceEngine.setScreen(experimentLevelScreen);
        }
      });
      experimentThumbs[i] = experimentThumb;
      experimentLevels.add(experimentThumb);
    }
    experimentLevels.row();

    for (int i = 0; i < levels.size; i++) {
      OrderedMap<String, ?> levelInfo = (OrderedMap<String, ?>) levels.get(i);
      String description = (String) levelInfo.get("description");
      Label label = new Label(description.substring(0,80), smallLabelStyle);
      label.setWrap(true);
      experimentLevels.add(label).width(128).left().pad(5);
    }
    experimentLevels.row();
    return experimentLevelPane;
  }

  private Actor createResourcePane() {
    Table resourceTable = super.getTable().newTable();
    resourceTable.defaults().fill();
    FlickScrollPane resourcePane = new FlickScrollPane(resourceTable, "Resource");
    
    Table buttonTable = resourceTable.newTable();
    for (int i = 0; i < resources.size; i++) {
      OrderedMap<String, ?> resourceInfo = (OrderedMap<String, ?>) resources.get(i);
      final String type = (String) resourceInfo.get("type");
      final String url = (String) resourceInfo.get("url");
      buttonTable.defaults().fill().left();

      TextButton button = new TextButton(getSkin());
      button.setText(type.equals("video") ? "Play" : "Browse");
      button.setClickListener(new ClickListener() {
        @Override
        public void click(Actor actor, float x, float y) {
          if (type.equals("video")) {
            scienceEngine.fetchURL(url);
          } else if (type.equals("web")) {
            scienceEngine.fetchURL(url);
          }
        }
      });
      buttonTable.add(button).width(128).pad(5);
    }
    resourceTable.add(buttonTable).left();
    resourceTable.row();
    
    Table infoTable = resourceTable.newTable();
    for (int i = 0; i < resources.size; i++) {
      OrderedMap<String, ?> resourceInfo = (OrderedMap<String, ?>) resources.get(i);
      String attribution = (String) resourceInfo.get("attribution");
      String description = (String) resourceInfo.get("description");
      
      Label attributionLabel = new Label("From: " + attribution + "\n" + description.substring(0, 40), smallLabelStyle);
      attributionLabel.setWrap(true);
      infoTable.add(attributionLabel).width(128).pad(5);
    }
    infoTable.row();
    
    resourceTable.add(infoTable).left();
    resourceTable.row();
    return resourcePane;   
  }

  @SuppressWarnings("unchecked")
  public void readExperimentInfo() {
    FileHandle file;
    String fileName = "data/" + experimentController.getName() + ".json";
    Gdx.app.log(ScienceEngine.LOG, "Opening file: " + fileName);
    file = Gdx.files.internal(fileName);
    if (file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open file: " + fileName);
    }
    String fileContents = file.readString();
    OrderedMap<String, ?> rootElem = 
        (OrderedMap<String, ?>) new JsonReader().parse(fileContents);
    this.levels = (Array<?>) rootElem.get("Levels");
    this.resources = (Array<?>) rootElem.get("Resources");  
  }

}
