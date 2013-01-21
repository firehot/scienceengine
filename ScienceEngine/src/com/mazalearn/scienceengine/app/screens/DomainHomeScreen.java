package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;

/**
 * Activity Home screen - shows all activity levels for that domain.
 */
public class DomainHomeScreen extends AbstractScreen {

  private static final int RESOURCE_WIDTH = 130;
  private static final int THUMBNAIL_WIDTH = 200;
  private static final int THUMBNAIL_HEIGHT = 150;
  private static final int INFO_HEIGHT = 50;
  private Image[] activityThumbs;
  private Array<?> levels;
  private Array<?> resources;
  private LabelStyle smallLabelStyle;
  private Profile profile;
  private String domain;
  
  public DomainHomeScreen(ScienceEngine scienceEngine, String domain) {
    super(scienceEngine);
    this.domain = domain;
    setBackgroundColor(Color.DARK_GRAY);
    readDomainActivityInfo();
    smallLabelStyle = new LabelStyle(getSmallFont(), Color.WHITE);
    profile = ScienceEngine.getProfileManager().retrieveProfile();
    profile.setDomain(domain);
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    profile.setDomain("");
    if (ScienceEngine.getPlatformAdapter().getPlatform() == Platform.GWT) {
      scienceEngine.setScreen(new SplashScreen(scienceEngine));
    } else {
      scienceEngine.setScreen(new ChooseDomainScreen(scienceEngine));
    }
  }
  
  @Override
  public void show() {
    super.show();
    if (profile.getCurrentLevel() != 0) {
      gotoActivityLevel(profile.getCurrentLevel());
      return;
    }
    
    Table table = super.getTable();
    
    table.defaults().fill().center().padLeft(30);
    table.add(getMsg().getString("ScienceEngine." + domain) +
        "- " + getMsg().getString("ScienceEngine.Levels")); //$NON-NLS-1$ //$NON-NLS-2$
    table.row();
    
    table.add(createActivityLevelPane()).fill();    
    table.row();
    table.add(getMsg().getString("ScienceEngine." + domain) + "- " + 
        getMsg().getString("ScienceEngine.ResourcesOnTheInternet")).colspan(100); //$NON-NLS-1$ //$NON-NLS-2$
    table.row();
    table.add(createResourcePane()).fill();
    table.row();
    
    // register the back button
    TextButton backButton = 
        new TextButton(getMsg().getString("ScienceEngine.BackToActivities"), getSkin()); //$NON-NLS-1$
    backButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        goBack();
      }
    });
    table.row();
    table.add(backButton).fill().colspan(100).pad(10, 20, 10, 20);
  }

  @SuppressWarnings("unchecked")
  private Actor createActivityLevelPane() {
    Table activityLevels = new Table(getSkin());
    activityLevels.setName("Activity Levels");
    ScrollPane activityLevelPane = new ScrollPane(activityLevels, getSkin());
    activityThumbs = new Image[levels.size];
    
    for (int i = 0; i < levels.size; i++) {
      OrderedMap<String, ?> levelInfo = (OrderedMap<String, ?>) levels.get(i);
      String activityName = (String) levelInfo.get("name");
      Label label = new Label(activityName, smallLabelStyle); //$NON-NLS-1$
      label.setWrap(true);
      activityLevels.add(label).width(THUMBNAIL_WIDTH).left().top().pad(5);
    }
    activityLevels.row();
    
    Texture overlayLock = new Texture("images/lock.png");
    boolean lock = false;
    for (int i = 0; i < levels.size; i++) {
      final int iLevel = i + 1;
      String filename = LevelUtil.getLevelFilename(domain, ".png", iLevel);
      Pixmap pixmap;
      if (ScienceEngine.assetManager.isLoaded(filename)) {
        pixmap = ScienceEngine.assetManager.get(filename, Pixmap.class);
      } else {
        pixmap = LevelUtil.getEmptyThumbnail();
      }
      Image activityThumb = 
          lock ? new OverlayImage(new Texture(pixmap), overlayLock) 
               : new Image(new Texture(pixmap));
      activityThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          gotoActivityLevel(iLevel);
        }
      });
      activityThumbs[i] = activityThumb;
      activityLevels.add(activityThumb).width(THUMBNAIL_WIDTH).height(THUMBNAIL_HEIGHT);
    }
    activityLevels.row();

    for (int i = 0; i < levels.size; i++) {
      OrderedMap<String, ?> levelInfo = (OrderedMap<String, ?>) levels.get(i);
      String description = (String) levelInfo.get("description"); //$NON-NLS-1$
      Label label = new Label(description, smallLabelStyle);
      label.setWrap(true);
      ScrollPane scrollPane = new ScrollPane(label, getSkin());
      scrollPane.setScrollingDisabled(true, false);
      scrollPane.setFlickScroll(false);
      activityLevels.add(scrollPane).width(THUMBNAIL_WIDTH).height(INFO_HEIGHT).left().pad(5);
    }
    activityLevels.row();
    activityLevelPane.setScrollingDisabled(false, true);
    return activityLevelPane;
  }

  @SuppressWarnings("unchecked")
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
      
      Float rating = (Float) resourceInfo.get("rating"); //$NON-NLS-1$
      String duration = (String) resourceInfo.get("duration"); //$NON-NLS-1$
      if (duration == null) {
        duration = "";
      }
      String attribution = (String) resourceInfo.get("attribution"); //$NON-NLS-1$
      String description = (String) resourceInfo.get("description"); //$NON-NLS-1$
      final String url = (String) resourceInfo.get("url"); //$NON-NLS-1$
      final String fileName = (String) resourceInfo.get("file"); //$NON-NLS-1$
      resource.defaults().fill();

      Image play = null;
      if (type.equals("video")) { //$NON-NLS-1$
        play = new Image(new Texture("images/videoplay.png")); //$NON-NLS-1$
        play.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
            boolean playedVideo = false;
            if (fileName != null) {
              // Movie file extensions - we allow a limited set.
              for (String extension: new String[] {".mp4", ".3gp", ".mov", ".wmv", ""}) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                try {
                  FileHandle file = Gdx.files.external(fileName + extension);
                  if (!file.exists()) { // Try out absolute path
                    file = Gdx.files.absolute("/LocalDisk/" + fileName + extension);
                  }
                  if (file.exists()) {
                    playedVideo = scienceEngine.playVideo(file.file());
                    break;
                  }
                } catch (GdxRuntimeException e) {
                  // Ignore - it is ok for file to be inaccessible
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
            ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
            scienceEngine.browseURL(url);
          }
        });
      }
      String rated = "*****".substring(0, (int) Math.floor(rating));
      Label ratingLabel = new Label(rated, getSkin(), "en", Color.YELLOW);
      resource.add(ratingLabel).right().width(50);
      resource.add(play).width(30).height(30).top().center();
      resource.add(new Label(duration, smallLabelStyle)).padLeft(10).width(40);
      resource.row();
      Label attributionLabel = 
          new Label(getMsg().getString("ScienceEngine.From") + ": " + 
                    attribution + "\n" +  //$NON-NLS-1$ //$NON-NLS-2$
                    description, smallLabelStyle);
      attributionLabel.setWrap(true);
      ScrollPane scrollPane = new ScrollPane(attributionLabel, getSkin());
      scrollPane.setScrollingDisabled(true,  false);
      scrollPane.setFlickScroll(false);
      if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
        resource.debug();
      }
      resource.add(scrollPane)
          .width(RESOURCE_WIDTH)
          .height(INFO_HEIGHT)
          .left()
          .top()
          .pad(0, 5, 0, 5)
          .colspan(3);
      resource.row();
      resourcesTable.add(resource).top().left();
    }
    resourcesTable.row();
    resourcePane.setScrollingDisabled(false,  true);
    return resourcePane;   
  }


  private void gotoActivityLevel(final int iLevel) {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    AbstractScreen activityLevelScreen = 
        new ActivityScreen(scienceEngine, domain, iLevel);
    // Set loading screen
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, activityLevelScreen));
  }
  
  @SuppressWarnings("unchecked")
  public void readDomainActivityInfo() {
    FileHandle file;
    String fileName = "data/" + domain + ".json"; //$NON-NLS-1$ //$NON-NLS-2$
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
  
  @Override
  public void addAssets() {
    for (int i = 0; i < levels.size; i++) {
      String filename = LevelUtil.getLevelFilename(domain, ".png", i + 1);
      ScienceEngine.assetManager.load(filename, Pixmap.class);
    }
  }

}
