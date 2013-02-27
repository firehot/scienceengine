package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.esotericsoftware.tablelayout.Cell;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.Format;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.tutor.Guru;

/**
 * Activity Home screen - shows all activity numLevels for that topic.
 */
public class TopicHomeScreen extends AbstractScreen {

  public static class BrowseUrlListener extends ClickListener {
    private final String url;

    public BrowseUrlListener(String url) {
      this.url = url;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
      ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      ScienceEngine.getPlatformAdapter().browseURL(url);
    }
  }

  public static class VideoPlayListener extends ClickListener {
    private final String fileName;
    private final String url;

    public VideoPlayListener(String fileName, String url) {
      this.fileName = fileName;
      this.url = url;
    }

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
              playedVideo = ScienceEngine.getPlatformAdapter().playVideo(file.file());
              break;
            }
          } catch (GdxRuntimeException e) {
            // Ignore - it is ok for file to be inaccessible
          }
        }
      }
      if (url != null && !playedVideo) { // Fallback to the browser
       ScienceEngine.getPlatformAdapter().browseURL(url);
      }
    }
  }

  private static final int RESOURCE_WIDTH = 254;
  private static final int THUMBNAIL_WIDTH = 369; // 242;
  private static final int THUMBNAIL_HEIGHT = 279; // 182;
  private static final int RESOURCE_INFO_HEIGHT = 210;
  private TextButton[] activityThumbs;
  private Array<?> resources;
  private Profile profile;
  private Topic topic;
  
  public TopicHomeScreen(ScienceEngine scienceEngine, Topic topic) {
    super(scienceEngine);
    this.topic = topic;
    readTopicResourcesInfo();
    profile = ScienceEngine.getPreferencesManager().getProfile();
    profile.setCurrentTopic(topic);
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  protected void goBack() {
    profile.setCurrentTopic(null);
    scienceEngine.setScreen(new ChooseTopicScreen(scienceEngine));
  }
  
  @Override
  public void show() {
    super.show();
    if (profile.getCurrentActivity() != 0) {
      gotoActivityLevel(profile.getCurrentActivity());
      return;
    }
    
    Table table = super.getTable();
    table.debug();
    
    String title = getMsg().getString("ScienceEngine." + topic) +
        " - " + getMsg().getString("ScienceEngine.Activities"); //$NON-NLS-1$ //$NON-NLS-2$
    setTitle(title);
    
    final Actor activitiesPane = createActivitiesPane();
    final Actor resourcesPane = createResourcePane();
    @SuppressWarnings("unchecked")
    final Cell<Actor> scrollPane = 
        table.add(activitiesPane).fill().width(ScreenComponent.VIEWPORT_WIDTH - 40);    
    table.row();
    final TextButton contentTypeButton = 
        new TextButton(getMsg().getString("ScienceEngine.ResourcesOnTheInternet"), getSkin(), "body");
    contentTypeButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        if (contentTypeButton.isChecked()) {
          scrollPane.setWidget(resourcesPane);
          contentTypeButton.setText(getMsg().getString("ScienceEngine.Activities"));
        } else {
          scrollPane.setWidget(activitiesPane);
          contentTypeButton.setText(getMsg().getString("ScienceEngine.ResourcesOnTheInternet"));
        }
      }
    });
    table.add(contentTypeButton).width(400).height(50).padTop(50);
  }

  private Actor createActivitiesPane() {
    Table activities = new Table(getSkin());
    activities.setName("Activity Levels");
    ScrollPane activitiesPane = new ScrollPane(activities, getSkin(), "thumbs");
    activitiesPane.setFadeScrollBars(false);
    activityThumbs = new TextButton[topic.getNumLevels()];
    
    LabelStyle blueBackground = new LabelStyle(getSkin().get(LabelStyle.class));
    blueBackground.background = 
        new TextureRegionDrawable(ScreenUtils.createTexture(20, 20, Color.BLUE));

    for (int level = 1; level <= topic.getNumLevels(); level++) {
      String activityName = getMsg().getString(topic + "." + level + ".Name");
      String filename = LevelUtil.getLevelFilename(topic.name(), ".png", level);
      Pixmap pixmap;
      if (ScienceEngine.assetManager.isLoaded(filename)) {
        pixmap = ScienceEngine.assetManager.get(filename, Pixmap.class);
      } else {
        pixmap = LevelUtil.getEmptyThumbnail();
      }
      TextButton activityThumb = createImageButton(new Texture(pixmap), getSkin());
      
      // Name Label
      activityThumb.addActor(createLabel(activityName, 2, 40, THUMBNAIL_WIDTH - 4, 50, blueBackground));
      // Level Label
      activityThumb.addActor(createLabel(String.valueOf(level), THUMBNAIL_WIDTH - 34, THUMBNAIL_HEIGHT - 34, 30, 30, blueBackground));
      // Progress bar
      float percent = profile.getCompletionPercent(level, Guru.ID);
      createProgressPercentageBar(blueBackground, activityThumb, percent, THUMBNAIL_WIDTH);
      // Timespent label
      String timeSpent = Format.formatTime(profile.getTimeSpent(level, Guru.ID));
      activityThumb.addActor(createLabel(timeSpent, 2, THUMBNAIL_HEIGHT - 34, 50, 30, blueBackground));

      final int iLevel = level;
      activityThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          gotoActivityLevel(iLevel);
        }
      });
      activityThumbs[level - 1] = activityThumb;
      ScreenComponent.scaleSize(activityThumb, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
      activities
          .add(activityThumb)
          .width(activityThumb.getWidth())
          .height(activityThumb.getHeight())
          .padTop(5);
    }
    activities.row();

    activitiesPane.setScrollingDisabled(false, true);
    setLastActiveLevel(activitiesPane);
    return activitiesPane;
  }

  // Used from ChooseTopic screen
  // TODO: move to common place
  public static TextButton createImageButton(Texture texture, Skin skin) {
    TextureRegionDrawable image = 
        new TextureRegionDrawable(new TextureRegion(texture));
    TextButton activityThumb = new TextButton("", skin) {
      @Override
      public void drawBackground(SpriteBatch batch, float parentAlpha) {
        getBackground().draw(batch, getX()+5, getY()+5, getWidth()-10, getHeight()-10);
      }
    };
    activityThumb.setBackground(image);
    return activityThumb;
  }

  // Used from Tutor Navigator
  // TODO: Move to common place
  public static Label createLabel(String text, 
      float x, float y, float width, float height, LabelStyle labelStyle) {
    Label nameLabel = new Label(text, labelStyle);
    nameLabel.setWrap(true);
    nameLabel.setAlignment(Align.center, Align.center);
    ScreenComponent.scalePositionAndSize(nameLabel, x, y, width, height);
    return nameLabel;
  }

  // Used from Tutor Navigator
  // TODO: Move to common place
  public static Button createTextButton(String text, 
      float x, float y, float width, float height, TextButtonStyle textButtonStyle) {
    TextButton button = new TextButton(text, textButtonStyle);
    button.getLabel().setWrap(true);
    button.getLabel().setAlignment(Align.center, Align.center);
    ScreenComponent.scaleSize(button, width, height);
    button.setPosition(x, y);
    return button;
  }

  // Also used from ChooseTopicScreen.
  // TODO: Move to common area.
  public static void createProgressPercentageBar(LabelStyle labelStyle,
      TextButton thumbnail, float percent, int width) {
    TextureRegion bar = ScreenUtils.createTexture(10, 10, Color.GRAY);
    Image fullBar = new Image(bar);
    ScreenComponent.scalePositionAndSize(fullBar, 10, 20, width - 20, 10);
    thumbnail.addActor(fullBar);
    Image successBar = new Image(ScreenUtils.createTexture(10, 10, Color.RED));
    ScreenComponent.scalePositionAndSize(successBar, 10, 20, percent * (width - 20) / 100f, 10);
    thumbnail.addActor(successBar);
    Label percentLabel = new Label(String.valueOf(Math.round(percent)) + "%", labelStyle);
    percentLabel.setAlignment(Align.center, Align.center);
    ScreenComponent.scalePositionAndSize(percentLabel, 5, 12, 40, 20);
    thumbnail.addActor(percentLabel);
  }

  private void setLastActiveLevel(ScrollPane activitiesPane) {
    int lastActiveLevel = profile.getLastActivity() - 1;
    if (lastActiveLevel >= 0) {
      Image userImage = new Image(new Texture("images/user.png"));
      ScreenComponent.scalePosition(userImage, 2, THUMBNAIL_HEIGHT / 2);
      activityThumbs[lastActiveLevel].addActor(userImage);
      activitiesPane.layout();
      activitiesPane.setScrollX(ScreenComponent.getScaledX(THUMBNAIL_WIDTH) * lastActiveLevel);
    }
  }

  @SuppressWarnings("unchecked")
  private Actor createResourcePane() {
    Table resourcesTable = new Table(getSkin());
    resourcesTable.setName("Resources");
    resourcesTable.defaults().fill();
    ScrollPane resourcePane = new ScrollPane(resourcesTable, getSkin());
    LabelStyle blackBackground = new LabelStyle(getSkin().get(LabelStyle.class));
    blackBackground.background = 
        new TextureRegionDrawable(ScreenUtils.createTexture(20, 20, Color.BLACK));
    
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
      ClickListener clickListener = null;
      if (type.equals("video")) { //$NON-NLS-1$
        play = new Image(new Texture("images/videoplay.png")); //$NON-NLS-1$
        play.addListener(clickListener = new VideoPlayListener(fileName, url));
      } else if (type.equals("web")) { //$NON-NLS-1$
        play = new Image(new Texture("images/browser.png")); //$NON-NLS-1$
        play.addListener(clickListener = new BrowseUrlListener(url));
      }
      resource.addListener(clickListener);
      
      String rated = "*****".substring(0, (int) Math.floor(rating));
      Label ratingLabel = new Label(rated, getSkin(), "en", Color.YELLOW);
      resource.add(ratingLabel).right().width(50);
      resource.add(play).width(60).height(60).top().center();
      resource.add(new Label(duration, getSkin())).padLeft(10).width(40);
      resource.row();
      Label attributionLabel = 
          new Label(getMsg().getString("ScienceEngine.From") + ": " + 
                    attribution + "\n\n\n" +  //$NON-NLS-1$ //$NON-NLS-2$
                    description, blackBackground);
      attributionLabel.setAlignment(Align.top, Align.left);
      attributionLabel.setWrap(true);
      ScrollPane scrollPane = new ScrollPane(attributionLabel, getSkin());
      scrollPane.setScrollingDisabled(true,  false);
      scrollPane.setFlickScroll(false);
      if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
        resource.debug();
      }
      ScreenComponent.scaleSize(scrollPane, RESOURCE_WIDTH, RESOURCE_INFO_HEIGHT);
      resource.add(scrollPane)
          .width(scrollPane.getWidth())
          .height(scrollPane.getHeight())
          .left()
          .top()
          .pad(0, 5, 5, 5)
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
        new ActivityScreen(scienceEngine, topic, iLevel);
    // Set loading screen
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, activityLevelScreen));
  }
  
  @SuppressWarnings("unchecked")
  public void readTopicResourcesInfo() {
    FileHandle file;
    String fileName = "data/" + topic + ".json"; //$NON-NLS-1$ //$NON-NLS-2$
    Gdx.app.log(ScienceEngine.LOG, "Opening file: " + fileName); //$NON-NLS-1$
    file = Gdx.files.internal(fileName);
    if (file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open file: " + fileName); //$NON-NLS-1$
    }
    String fileContents = file.readString();
    OrderedMap<String, ?> rootElem = 
        (OrderedMap<String, ?>) new JsonReader().parse(fileContents);
    this.resources = (Array<?>) rootElem.get("Resources");   //$NON-NLS-1$
  }
  
  @Override
  public void addAssets() {
    for (int level = 1; level <= topic.getNumLevels(); level++) {
      String filename = LevelUtil.getLevelFilename(topic.name(), ".png", level);
      ScienceEngine.assetManager.load(filename, Pixmap.class);
    }
  }

}