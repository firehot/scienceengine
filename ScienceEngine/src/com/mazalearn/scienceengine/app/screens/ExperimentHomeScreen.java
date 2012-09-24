package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.FlickScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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

  private static final int RESOURCE_WIDTH = 120;
  private static final int THUMBNAIL_WIDTH = 200;
  private static final int THUMBNAIL_HEIGHT = 150;
  private static final int INFO_HEIGHT = 50;
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
    table.add(experimentController.getName() + ": " + "Levels");
    table.row();
    
    table.add(createExperimentLevelPane()).fill();    
    table.row();
    table.add(experimentController.getName() + ": " + "Resources on the Internet").colspan(100);
    table.row();
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
    table.add(backButton).fill().colspan(100).pad(10, 20, 10, 20);
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
      experimentLevels.add(label).width(THUMBNAIL_WIDTH).left().top().pad(5);
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
      experimentLevels.add(experimentThumb).width(THUMBNAIL_WIDTH).height(THUMBNAIL_HEIGHT);
    }
    experimentLevels.row();

    for (int i = 0; i < levels.size; i++) {
      OrderedMap<String, ?> levelInfo = (OrderedMap<String, ?>) levels.get(i);
      String description = (String) levelInfo.get("description");
      Label label = new Label(description, smallLabelStyle);
      label.setWrap(true);
      ScrollPane scrollPane = new ScrollPane(label, getSkin());
      scrollPane.setScrollingDisabled(true, false);
      experimentLevels.add(scrollPane).width(THUMBNAIL_WIDTH).height(INFO_HEIGHT).left().pad(5);
    }
    experimentLevels.row();
    experimentLevelPane.setScrollingDisabled(false, true);
    return experimentLevelPane;
  }

  private Actor createResourcePane() {
    Table resourcesTable = super.getTable().newTable();
    resourcesTable.defaults().fill();
    FlickScrollPane resourcePane = new FlickScrollPane(resourcesTable, "Resource");
    
    for (int i = 0; i < resources.size; i++) {
      Table resource = resourcesTable.newTable();
      OrderedMap<String, ?> resourceInfo = (OrderedMap<String, ?>) resources.get(i);
      final String type = (String) resourceInfo.get("type");
      final String url = (String) resourceInfo.get("url");
      String attribution = (String) resourceInfo.get("attribution");
      String description = (String) resourceInfo.get("description");
      resource.defaults().fill().left();

      Image play = null;
      if (type.equals("video")) {
        play = new Image(new Texture("images/videoplay.png"));
        play.setClickListener(new ClickListener() {
          @Override
          public void click(Actor actor, float x, float y) {
            scienceEngine.browseURL(url);
          }
        });
      } else if (type.equals("web")) {
        play = new Image(new Texture("images/browser.png"));
        play.setClickListener(new ClickListener() {
          @Override
          public void click(Actor actor, float x, float y) {
            scienceEngine.browseURL(url);
          }
        });
      }
      
      resource.add(play).pad(0, 10, 0, 10).width(30).height(30).top().center();
      resource.row();
      Label attributionLabel = new Label("From: " + attribution + "\n" + 
          description, smallLabelStyle);
      attributionLabel.setWrap(true);
      ScrollPane scrollPane = new ScrollPane(attributionLabel, getSkin());
      scrollPane.setScrollingDisabled(true,  false);
      resource.add(scrollPane)
          .width(RESOURCE_WIDTH)
          .height(INFO_HEIGHT)
          .left()
          .top()
          .pad(0, 5, 0, 5);
      resourcesTable.add(resource).top().left();
    }
    resourcesTable.row();
    resourcePane.setScrollingDisabled(false,  true);
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
