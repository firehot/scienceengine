package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.Messages;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DStage;

/**
 * Experiment Home screen - shows all levels for that experiment.
 */
public class ExperimentHomeScreen extends AbstractScreen {

  private static final int RESOURCE_WIDTH = 120;
  private static final int THUMBNAIL_WIDTH = 200;
  private static final int THUMBNAIL_HEIGHT = 150;
  private static final int INFO_HEIGHT = 50;
  IScience2DController science2DController;
  private Image[] experimentThumbs;
  private LevelManager levelManager;
  private Array<?> levels;
  private Array<?> resources;
  private LabelStyle smallLabelStyle;

  public ExperimentHomeScreen(ScienceEngine scienceEngine, 
      IScience2DController science2DController) {
    super(scienceEngine);
    this.science2DController = science2DController;
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
    table.add(science2DController.getName() + ": " + Messages.getString("ScienceEngine.Levels")); //$NON-NLS-1$ //$NON-NLS-2$
    table.row();
    
    table.add(createExperimentLevelPane()).fill();    
    table.row();
    table.add(science2DController.getName() + ": " + Messages.getString("ScienceEngine.ResourcesOnTheInternet")).colspan(100); //$NON-NLS-1$ //$NON-NLS-2$
    table.row();
    table.add(createResourcePane()).fill();
    table.row();
    
    // register the back button
    TextButtonStyle textButtonStyle = new TextButtonStyle(getSkin().get("default", TextButtonStyle.class));
    textButtonStyle.font = getSkin().getFont("kannada");
    TextButton backButton = 
        new TextButton(Messages.getString("ScienceEngine.BackToExperiments"), textButtonStyle); //$NON-NLS-1$
    backButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(new ExperimentMenuScreen(scienceEngine));
      }
    });
    table.row();
    table.add(backButton).fill().colspan(100).pad(10, 20, 10, 20);
  }

  private Actor createExperimentLevelPane() {
    final IScience2DStage science2DStage = science2DController.getView();
    Profile profile = ScienceEngine.getProfileManager().retrieveProfile();
    int level = Math.max(profile.getCurrentLevelId(), 1);
    levelManager = science2DStage.getLevelManager();
    levelManager.setLevel(level);
    
    Table experimentLevels = new Table(getSkin());
    experimentLevels.setName("Experiment Levels");
    ScrollPane experimentLevelPane = 
        new ScrollPane(experimentLevels, getSkin());
    experimentThumbs = new Image[levels.size];
    
    for (int i = 0; i < levels.size; i++) {
      OrderedMap<String, ?> levelInfo = (OrderedMap<String, ?>) levels.get(i);
      Label label = new Label((String) levelInfo.get("name"), smallLabelStyle); //$NON-NLS-1$
      label.setWrap(true);
      experimentLevels.add(label).width(THUMBNAIL_WIDTH).left().top().pad(5);
    }
    experimentLevels.row();
    
    for (int i = 0; i < levels.size; i++) {
      final int iLevel = i + 1;
      Image experimentThumb = 
          new Image(LevelManager.getThumbnail(science2DController.getName(), iLevel));
      experimentThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          Screen experimentLevelScreen = 
              new ExperimentScreen(scienceEngine, levelManager, 
                  iLevel, science2DController);
          scienceEngine.setScreen(experimentLevelScreen);
        }
      });
      experimentThumbs[i] = experimentThumb;
      experimentLevels.add(experimentThumb).width(THUMBNAIL_WIDTH).height(THUMBNAIL_HEIGHT);
    }
    experimentLevels.row();

    for (int i = 0; i < levels.size; i++) {
      OrderedMap<String, ?> levelInfo = (OrderedMap<String, ?>) levels.get(i);
      String description = (String) levelInfo.get("description"); //$NON-NLS-1$
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
    Table resourcesTable = new Table(getSkin());
    resourcesTable.setName("Resources");
    resourcesTable.defaults().fill();
    ScrollPane resourcePane = new ScrollPane(resourcesTable, getSkin());
    
    for (int i = 0; i < resources.size; i++) {
      Table resource = new Table(getSkin());
      resource.setName("Resource");
      OrderedMap<String, ?> resourceInfo = (OrderedMap<String, ?>) resources.get(i);
      String type = (String) resourceInfo.get("type"); //$NON-NLS-1$
      if (!type.equals("video") && !type.equals("web")) continue; //$NON-NLS-1$ //$NON-NLS-2$
      
      String attribution = (String) resourceInfo.get("attribution"); //$NON-NLS-1$
      String description = (String) resourceInfo.get("description"); //$NON-NLS-1$
      final String url = (String) resourceInfo.get("url"); //$NON-NLS-1$
      final String fileName = (String) resourceInfo.get("file"); //$NON-NLS-1$
      resource.defaults().fill().left();

      Image play = null;
      if (type.equals("video")) { //$NON-NLS-1$
        play = new Image(new Texture("images/videoplay.png")); //$NON-NLS-1$
        play.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            boolean playedVideo = false;
            if (fileName != null) {
              // Movie file extensions - we allow a limited set.
              for (String extension: new String[] {".mp4", ".3gp", ".mov", ".wmv", ""}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                FileHandle file = Gdx.files.external(fileName + extension);
                if (file.exists()) {
                  playedVideo = scienceEngine.playVideo(file.file());
                  break;
                }
              }
            }
            if (url != null && !playedVideo) { // Fallback to the browser
             scienceEngine.browseURL(url);
            }
          }
        });
      } else if (type.equals("web")) { //$NON-NLS-1$
        play = new Image(new Texture("images/browser.png")); //$NON-NLS-1$
        play.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            scienceEngine.browseURL(url);
          }
        });
      }
      
      resource.add(play).pad(0, 10, 0, 10).width(30).height(30).top().center();
      resource.row();
      Label attributionLabel = new Label(Messages.getString("ScienceEngine.From") + ": " + attribution + "\n" +  //$NON-NLS-1$ //$NON-NLS-2$
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
    String fileName = "data/" + science2DController.getName() + ".json"; //$NON-NLS-1$ //$NON-NLS-2$
    Gdx.app.log(ScienceEngine.LOG, "Opening file: " + fileName); //$NON-NLS-1$
    file = Gdx.files.internal(fileName);
    if (file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open file: " + fileName); //$NON-NLS-1$
    }
    String fileContents = file.readString();
    OrderedMap<String, ?> rootElem = 
        (OrderedMap<String, ?>) new JsonReader().parse(fileContents);
    this.levels = (Array<?>) rootElem.get("Levels"); //$NON-NLS-1$
    this.resources = (Array<?>) rootElem.get("Resources");   //$NON-NLS-1$
  }

}
