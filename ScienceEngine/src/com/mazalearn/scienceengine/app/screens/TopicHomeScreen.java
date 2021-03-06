package com.mazalearn.scienceengine.app.screens;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.esotericsoftware.tablelayout.Cell;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.dialogs.MessageDialog;
import com.mazalearn.scienceengine.app.dialogs.PurchaseDialog;
import com.mazalearn.scienceengine.app.services.InstallProfile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.Format;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.tutor.Guru;
import com.mazalearn.scienceengine.tutor.ITutor;

/**
 * Activity Home screen - shows all activity numLevels for that topic.
 */
public class TopicHomeScreen extends AbstractScreen {

  public static class BrowseUrlListener extends CommandClickListener {
    private final String url;

    public BrowseUrlListener(String url) {
      this.url = url;
    }

    @Override
    public void doCommand() {
      ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      ScienceEngine.getPlatformAdapter().browseURL(url);
    }
  }

  public static class VideoPlayListener extends CommandClickListener {
    private final String fileName;
    private final String url;

    public VideoPlayListener(String fileName, String url) {
      this.fileName = fileName;
      this.url = url;
    }

    @Override
    public void doCommand() {
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
  public static final int THUMBNAIL_WIDTH = 369;
  public static final int THUMBNAIL_HEIGHT = 279;
  private static final int RESOURCE_INFO_HEIGHT = 210;
  private TextButton[] activityThumbs;
  private Array<?> resources;
  private Topic topic;
  
  public TopicHomeScreen(ScienceEngine scienceEngine, Topic topic) {
    super(scienceEngine);
    this.topic = topic;
    readTopicResourcesInfo();
    getProfile().setCurrentTopic(topic);
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  protected void goBack() {
    getProfile().setCurrentTopic(null);
    ScienceEngine.unloadAtlas("images/" + topic.name() + "/pack.atlas");
    scienceEngine.setScreen(new ChooseTopicScreen(scienceEngine));
  }
  
  @Override
  public void show() {
    super.show();
    Topic activity = getProfile().getCurrentActivity();
    if (activity != null) {
      InstallProfile installProfile = ScienceEngine.getPreferencesManager().getInstallProfile();
      // Was this an attempt to purchase which did not succeed?
      if (installProfile.isAvailableTopic(activity)) {
        gotoActivityLevel(activity);
        return;
      } else {
        getProfile().setCurrentActivity(null);
      }
    }
    
    Table table = super.getTable();
    table.debug();
    
    String title = getMsg("ScienceEngine." + topic) +
        " - " + getMsg("ScienceEngine.Activities"); //$NON-NLS-1$ //$NON-NLS-2$
    setTitle(title);
    
    final Actor activitiesPane = createActivitiesPane();
    final Actor resourcesPane = createResourcePane();
    @SuppressWarnings("unchecked")
    final Cell<Actor> scrollPane = 
        table.add(activitiesPane).fill().width(ScreenComponent.VIEWPORT_WIDTH - 40);    
    table.row();
    final TextButton contentTypeButton = 
        new TextButton(getMsg("ScienceEngine.ResourcesOnTheInternet"), getSkin(), "body");
    contentTypeButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        if (contentTypeButton.isChecked()) {
          scrollPane.setWidget(resourcesPane);
          contentTypeButton.setText(getMsg("ScienceEngine.Activities"));
        } else {
          scrollPane.setWidget(activitiesPane);
          contentTypeButton.setText(getMsg("ScienceEngine.ResourcesOnTheInternet"));
        }
      }
    });
    table.add(contentTypeButton)
        .width(ScreenComponent.getScaledX(400))
        .height(ScreenComponent.getScaledY(50))
        .padTop(ScreenComponent.getScaledY(50));
  }

  private Actor createActivitiesPane() {
    Table activities = new Table(getSkin());
    activities.setName("Activity Levels");
    ScrollPane activitiesPane = new ScrollPane(activities, getSkin(), "thumbs");
    activitiesPane.setFadeScrollBars(false);
    activityThumbs = new TextButton[topic.getChildren().length];
    
    LabelStyle blueBackground = new LabelStyle(getSkin().get(LabelStyle.class));
    blueBackground.background = 
        new TextureRegionDrawable(ScreenUtils.createTextureRegion(20, 20, Color.BLUE));

    int numTopics = 0;
    TextureRegion lockTexture = ScienceEngine.getTextureRegion("lock");
    final InstallProfile installProfile = ScienceEngine.getPreferencesManager().getInstallProfile();
    for (final Topic level: topic.getChildren()) {
      numTopics++;
      // Assumption: Last level is the Science train level - unlocked only on certification
      final boolean isScienceTrainLevel = numTopics == topic.getChildren().length;
      boolean isUnlocked = level.isFree() ||
          (!isScienceTrainLevel && installProfile.isAvailableTopic(level)) || 
          (isScienceTrainLevel && getProfile().getCertificates().contains(topic.name()));
      String activityName = getMsg(topic + "." + level + ".Name");
      String filename = LevelUtil.getLevelFilename(topic, level, ".jpg");
      FileHandle file = Gdx.files.internal(filename);
      Texture texture;
      if (file.exists()) {
        texture = new Texture(file);
      } else {
        texture = new Texture(LevelUtil.getEmptyThumbnail());
      }
      TextButton activityThumb = 
          ScreenUtils.createImageButton(new TextureRegion(texture), getSkin(), "default");
      ScreenComponent.scaleSize(activityThumb, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
      
      // Name Label
      activityThumb.addActor(ScreenUtils.createLabel(activityName, 2, 40, THUMBNAIL_WIDTH - 4, 50, blueBackground));
      // Level Label
      activityThumb.addActor(ScreenUtils.createLabel(String.valueOf(numTopics), THUMBNAIL_WIDTH - 34, THUMBNAIL_HEIGHT - 34, 30, 30, blueBackground));
      if (!isUnlocked) {
        Image lockImage = new Image(lockTexture);
        lockImage.setSize(activityThumb.getWidth() / 2, activityThumb.getHeight() / 2);
        lockImage.setPosition(activityThumb.getWidth() / 2 - lockImage.getWidth() / 2, 
            activityThumb.getHeight() / 2 - lockImage.getHeight() / 2);
        activityThumb.addActor(lockImage);
        activityThumb.addListener(new CommandClickListener() {
          @Override
          public void doCommand() {
            if (isScienceTrainLevel) {
              new MessageDialog(getSkin(), "To unlock this level, you have to earn the " + topic.name() + " Certificate").show(stage);
            } else if (!installProfile.isAvailableTopic(level)) {
              new PurchaseDialog(topic, level, getStage(), getSkin(), scienceEngine).show(getStage());
            }
          }
        });
      } else {
        // Progress bar
        float[] stats = getProfile().getStats(level, Guru.ID, ITutor.NUM_STATS);
        float percent = stats[ITutor.PERCENT_PROGRESS];
        ScreenUtils.createProgressPercentageBar(blueBackground, activityThumb, percent, THUMBNAIL_WIDTH);
        // Timespent label
        String timeSpent = Format.formatTime(stats[ITutor.TIME_SPENT]);
        activityThumb.addActor(ScreenUtils.createLabel(timeSpent, 2, THUMBNAIL_HEIGHT - 34, 60, 30, blueBackground));
  
        final Topic iLevel = level;
        activityThumb.addListener(new CommandClickListener() {
          @Override
          public void doCommand() {
            gotoActivityLevel(iLevel);
          }
        });
      }
      activityThumbs[numTopics - 1] = activityThumb;
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

  private void setLastActiveLevel(ScrollPane activitiesPane) {
    Topic lastActiveLevel = getProfile().getLastActivity();
    if (lastActiveLevel != null) {
      Image userImage = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
      ScreenComponent.scalePosition(userImage, 2, THUMBNAIL_HEIGHT / 2);
      userImage.setSize(ScreenComponent.User.getWidth(), ScreenComponent.User.getHeight());
      
      int idx = Arrays.asList(topic.getChildren()).indexOf(lastActiveLevel);
      if (idx != -1) {
        activityThumbs[idx].addActor(userImage);
        activitiesPane.layout();
        activitiesPane.setScrollX(ScreenComponent.getScaledX(THUMBNAIL_WIDTH) * idx);
      }
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
        new TextureRegionDrawable(ScreenUtils.createTextureRegion(20, 20, getSkin().getColor("background")));
    
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
        play = new Image(ScienceEngine.getTextureRegion("videoplay")); //$NON-NLS-1$
        play.addListener(clickListener = new VideoPlayListener(fileName, url));
      } else if (type.equals("web")) { //$NON-NLS-1$
        play = new Image(ScienceEngine.getTextureRegion("browser")); //$NON-NLS-1$
        play.addListener(clickListener = new BrowseUrlListener(url));
      }
      resource.addListener(clickListener);
      
      String rated = "*****".substring(0, (int) Math.floor(rating));
      Label ratingLabel = new Label(rated, getSkin(), "en-big", Color.YELLOW);
      resource.add(ratingLabel).right().width(ScreenComponent.getScaledX(50));
      resource.add(play)
          .width(ScreenComponent.getScaledX(60))
          .height(ScreenComponent.getScaledY(60))
          .top()
          .center();
      resource.add(new Label(duration, getSkin())).padLeft(10).width(ScreenComponent.getScaledX(40));
      resource.row();
      Label attributionLabel = 
          new Label(getMsg("ScienceEngine.From") + ": " + 
                    attribution + "\n\n\n" +  //$NON-NLS-1$ //$NON-NLS-2$
                    description, blackBackground);
      attributionLabel.setAlignment(Align.top, Align.left);
      attributionLabel.setWrap(true);
      ScrollPane scrollPane = new ScrollPane(attributionLabel, getSkin());
      scrollPane.setScrollingDisabled(true,  false);
      scrollPane.setFlickScroll(false);
      if (ScienceEngine.DEV_MODE.isDebug()) {
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


  private void gotoActivityLevel(final Topic level) {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    AbstractScreen activityLevelScreen = 
        new ActivityScreen(scienceEngine, topic, level);
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
    super.addAssets();
    // Topic resources
    ScienceEngine.loadAtlas("images/" + topic.name() + "/pack.atlas");
  }

}
