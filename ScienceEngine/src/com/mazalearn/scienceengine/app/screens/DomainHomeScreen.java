package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;

/**
 * Activity Home screen - shows all activity numLevels for that domain.
 */
public class DomainHomeScreen extends AbstractScreen {

  private static final int RESOURCE_WIDTH = 130;
  private static final int THUMBNAIL_WIDTH = 242;
  private static final int THUMBNAIL_HEIGHT = 182;
  private static final int LEVEL_INFO_HEIGHT = 0;
  private static final int RESOURCE_INFO_HEIGHT = 90;
  private TextButton[] activityThumbs;
  private int numLevels;
  private Array<?> resources;
  private LabelStyle smallLabelStyle;
  private Profile profile;
  private String domain;
  private TextButtonStyle smallButtonStyle;
  
  public DomainHomeScreen(ScienceEngine scienceEngine, String domain) {
    super(scienceEngine);
    this.domain = domain;
    readDomainActivityInfo();
    smallLabelStyle = new LabelStyle(getSmallFont(), Color.BLACK);
    smallButtonStyle = new TextButton.TextButtonStyle();
    smallButtonStyle.font = getSmallFont();
    profile = ScienceEngine.getProfileManager().retrieveProfile();
    profile.setDomain(domain);
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  protected void goBack() {
    profile.setDomain("");
    scienceEngine.setScreen(new ChooseDomainScreen(scienceEngine));
  }
  
  @Override
  public void show() {
    super.show();
    // setBackgroundColor(new Color(0x84/255f,0x99/255f,0xa2/255f,1f));
    if (profile.getCurrentLevel() != 0) {
      gotoActivityLevel(profile.getCurrentLevel());
      return;
    }
    
    Table table = super.getTable();
    table.debug();
    
    String title = getMsg().getString("ScienceEngine." + domain) +
        " - " + getMsg().getString("ScienceEngine.Levels"); //$NON-NLS-1$ //$NON-NLS-2$
    setTitle(title);
    
    table.add(createActivityLevelPane()).fill().width(AbstractScreen.VIEWPORT_WIDTH - 40);    
    table.row();
    
    Table resourcesOnInternet = new Table(getSkin());
    resourcesOnInternet.debug();
    resourcesOnInternet.add(getMsg().getString("ScienceEngine.ResourcesOnTheInternet")).fill();
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(getSkin().getColor("separator"));
    pixmap.drawRectangle(0, 0, 1, 1);
    resourcesOnInternet.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pixmap), 
        AbstractScreen.VIEWPORT_WIDTH - 20, 10)));
    pixmap.dispose();
    table.add(resourcesOnInternet).padTop(20);
    table.row();
    table.add(createResourcePane());
    table.row();
  }

  private Actor createActivityLevelPane() {
    Table activityLevels = new Table(getSkin());
    activityLevels.setName("Activity Levels");
    ScrollPane activityLevelPane = new ScrollPane(activityLevels, getSkin(), "thumbs");
    activityLevelPane.setFadeScrollBars(false);
    activityThumbs = new TextButton[numLevels];
    
    for (int level = 1; level <= numLevels; level++) {
      String activityName = getMsg().getString(domain + "." + level + ".Name");
      String filename = LevelUtil.getLevelFilename(domain, ".png", level);
      Pixmap pixmap;
      if (ScienceEngine.assetManager.isLoaded(filename)) {
        pixmap = ScienceEngine.assetManager.get(filename, Pixmap.class);
      } else {
        pixmap = LevelUtil.getEmptyThumbnail();
      }
      final TextureRegionDrawable image = 
          new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
      TextButton activityThumb = new TextButton(level + "\n" + activityName, getSkin()) {
        @Override
        public void drawBackground(SpriteBatch batch, float parentAlpha) {
          Color color = getColor();
          batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * 0.5f);
          image.draw(batch, getX()+5, getY()+5, getWidth()-10, getHeight()-10);
        }
      };
      activityThumb.getLabel().setWrap(true);
      activityThumb.setBackground(image);
      final int iLevel = level;
      activityThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          gotoActivityLevel(iLevel);
        }
      });
      activityThumbs[level - 1] = activityThumb;
      activityLevels.add(activityThumb).width(THUMBNAIL_WIDTH).height(THUMBNAIL_HEIGHT).padTop(5);
    }
    activityLevels.row();

    for (int level = 1; level <= numLevels; level++) {
      String activityDescription = getMsg().getString(domain + "." + level + ".Description");
      Label label = new Label(activityDescription, smallLabelStyle);
      label.setWrap(true);
      ScrollPane scrollPane = new ScrollPane(label, getSkin());
      scrollPane.setScrollingDisabled(true, false);
      scrollPane.setFlickScroll(false);
      activityLevels.add(scrollPane).width(THUMBNAIL_WIDTH).height(LEVEL_INFO_HEIGHT).left().pad(5);
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
          .height(RESOURCE_INFO_HEIGHT)
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
    this.numLevels = Math.round((Float) rootElem.get("Levels")); //$NON-NLS-1$
    this.resources = (Array<?>) rootElem.get("Resources");   //$NON-NLS-1$
  }
  
  @Override
  public void addAssets() {
    for (int level = 1; level <= numLevels; level++) {
      String filename = LevelUtil.getLevelFilename(domain, ".png", level);
      ScienceEngine.assetManager.load(filename, Pixmap.class);
    }
  }

}
