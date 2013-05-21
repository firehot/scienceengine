package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.tutor.Guru;
import com.mazalearn.scienceengine.tutor.IDoneCallback;
import com.mazalearn.scienceengine.tutor.ITutor;

public class ChooseTopicScreen extends AbstractScreen {
  private static final int THUMBNAIL_WIDTH = 242;
  private static final int THUMBNAIL_HEIGHT = 182;

  private Profile profile;

  public ChooseTopicScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  @Override
  public void show() {
    super.show();
    if (profile.getCurrentTopic() != null) {
      gotoTopicHome(profile.getCurrentTopic());
      return;
    }
    
    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    String title = getMsg("ScienceEngine.ChooseTopic");
    setTitle(title);

    // retrieve the default table
    Table table = super.getTable();
    table.defaults().spaceBottom(20).fill().center();

    // create the Topics Table
    table.add(createTopicsSelector());    
    table.row();
  }

  public Actor createTopicsSelector() {
    Table table = new Table(getSkin());
    table.setName("Topic Selector");
    ScrollPane flickScrollPane = new ScrollPane(table, getSkin());
    table.setFillParent(false);
    table.defaults().fill();
    TextureRegion comingsoon = ScienceEngine.getTextureRegion("comingsoon");
    for (final Topic topic: Topic.values()) {
      // Ignore leaf level topics
      if (topic.getChildren().length == 0) continue;
      final boolean lock = !topic.equals(Topic.Electromagnetism);
      Texture levelThumbnail = LevelUtil.getLevelThumbnail(topic, topic.getCanonicalChild(), 1);
      TextButton topicThumb = 
          ScreenUtils.createImageButton(new TextureRegion(levelThumbnail), getSkin(), "default");
      ScreenComponent.scaleSize(topicThumb, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
      if (lock) {
        Image lockImage = new Image(comingsoon);
        lockImage.setSize(THUMBNAIL_WIDTH / 2, THUMBNAIL_HEIGHT / 2);
        lockImage.setPosition(0, topicThumb.getHeight() - lockImage.getHeight());
        topicThumb.addActor(lockImage);
      } else {
        int progressPercentage = findTopicProgressPercentage(topic);
        ScreenUtils.createProgressPercentageBar(getSkin().get(LabelStyle.class),
            topicThumb, progressPercentage, THUMBNAIL_WIDTH);
      }
      topicThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          if (lock) {
            purchaseTopic(topic);         
          } else {
            gotoTopicHome(topic);
          }
        }

      });
      Table levelTable = new Table(getSkin());
      levelTable.setName("Level");
      levelTable.add(topic.name());
      levelTable.row();
      levelTable.add(topicThumb)
          .width(topicThumb.getWidth())
          .height(topicThumb.getHeight());
      table.add(levelTable).pad(5);
    }
    return flickScrollPane;
  }
  
  private int findTopicProgressPercentage(Topic topic) {
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    float percent = 0;
    int numTopics = 0;
    for (Topic childTopic: topic.getChildren()) {
      float[] stats = profile.getStats(childTopic, Guru.ID);
      percent += stats[ITutor.PERCENT_PROGRESS];
      numTopics++;
    }
    return Math.round(percent * 100 / (100f * numTopics));
  }

  @Override
  protected void goBack() {
    scienceEngine.setScreen(new SplashScreen(scienceEngine));
  }
  
  private void gotoTopicHome(final Topic topic) {
    Gdx.app.log(ScienceEngine.LOG, "Starting " + topic); //$NON-NLS-1$
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    AbstractScreen topicHomeScreen = new TopicHomeScreen(scienceEngine, topic);
    scienceEngine.setScreen(new LoadingScienceTrain(scienceEngine, topicHomeScreen));
  }

  private void purchaseTopic1(final Topic topic) {
    Gdx.input.getTextInput(new TextInputListener() {
      @Override
      public void input(String passcode) {
        if ("9n8e7s6s".equals(passcode)) {
          gotoTopicHome(topic);
        }
      }
      
      @Override
      public void canceled() {}
    }, "Enter key", "");
  }

  private void purchaseTopic(final Topic topic) {
    // android.test.purchased - item already owned not handled
    // android.test.canceled - BUG: either purchasedata or datasignature is null
    // android.test.item_unavailable - shows item unavailable
    ScienceEngine.getPlatformAdapter().launchPurchaseFlow("android.test.purchased", "inapp", new IDoneCallback() {
      @Override
      public void done(boolean success) {
        if (success) {
          gotoTopicHome(topic);
        }
      }
    }, topic.name());
  }
}
