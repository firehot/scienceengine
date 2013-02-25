package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.Domain;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.guru.Guru;

public class ChooseDomainScreen extends AbstractScreen {
  private static final int THUMBNAIL_WIDTH = 242;
  private static final int THUMBNAIL_HEIGHT = 182;

  private Profile profile;

  public ChooseDomainScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
    profile = ScienceEngine.getPreferencesManager().getProfile();
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  @Override
  public void show() {
    super.show();
    if (profile.getCurrentDomain() != null) {
      gotoDomainHome(profile.getCurrentDomain());
      return;
    }
    
    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    String title = getMsg().getString("ScienceEngine.ChooseDomain");
    setTitle(title);

    // retrieve the default table
    Table table = super.getTable();
    table.defaults().spaceBottom(20).fill().center();

    // create the domains Table
    table.add(createDomainsSelector());    
    table.row();
  }

  public Actor createDomainsSelector() {
    Table table = new Table(getSkin());
    table.setName("Domain Selector");
    ScrollPane flickScrollPane = new ScrollPane(table, getSkin());
    table.setFillParent(false);
    table.defaults().fill();
    Texture overlayLock = new Texture("images/lock.png");
    for (final Domain domain: Domain.values()) {
      final boolean lock = !domain.equals(Domain.Electromagnetism);
      Texture levelThumbnail = LevelUtil.getLevelThumbnail(domain.name(), 1);
      TextButton domainThumb = DomainHomeScreen.createImageButton(levelThumbnail, getSkin());
      if (lock) {
        Image lockImage = new Image(overlayLock);
        lockImage.setPosition(THUMBNAIL_WIDTH / 2 - lockImage.getWidth() / 2,
            THUMBNAIL_HEIGHT / 2 - lockImage.getHeight() / 2);
        domainThumb.addActor(lockImage);
      } else {
        int progressPercentage = findDomainProgressPercentage(domain);
        DomainHomeScreen.createProgressPercentageBar(getSkin().get(LabelStyle.class),
            domainThumb, progressPercentage, THUMBNAIL_WIDTH);
      }
      domainThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          if (lock) {
            Gdx.input.getTextInput(new TextInputListener() {
              @Override
              public void input(String passcode) {
                if ("9n8e7s6s".equals(passcode)) {
                  gotoDomainHome(domain);
                }
              }
              
              @Override
              public void canceled() {}
            }, "Enter key", "");         
          } else {
            gotoDomainHome(domain);
          }
        }

      });
      Table levelTable = new Table(getSkin());
      levelTable.setName("Level");
      levelTable.add(domain.name());
      levelTable.row();
      levelTable.add(domainThumb)
          .width(ScreenComponent.getScaledX(THUMBNAIL_WIDTH))
          .height(ScreenComponent.getScaledY(THUMBNAIL_HEIGHT));
      table.add(levelTable).pad(5);
    }
    return flickScrollPane;
  }
  
  private int findDomainProgressPercentage(Domain domain) {
    profile = ScienceEngine.getPreferencesManager().getProfile();
    int numLevels = getDomainLevels(domain);
    int percent = 0;
    for (int level = 1; level <= numLevels; level++) {
      percent += profile.getCompletionPercent(domain, level, Guru.ID);
    }
    return Math.round(percent * 100 / (100f * numLevels));
  }

  @SuppressWarnings("unchecked")
  public int getDomainLevels(Domain domain) {
    FileHandle file;
    String fileName = "data/" + domain.name() + ".json"; //$NON-NLS-1$ //$NON-NLS-2$
    Gdx.app.log(ScienceEngine.LOG, "Opening file: " + fileName); //$NON-NLS-1$
    file = Gdx.files.internal(fileName);
    if (file == null) {
      Gdx.app.log(ScienceEngine.LOG, "Could not open file: " + fileName); //$NON-NLS-1$
    }
    String fileContents = file.readString();
    OrderedMap<String, ?> rootElem = 
        (OrderedMap<String, ?>) new JsonReader().parse(fileContents);
    return Math.round((Float) rootElem.get("Levels")); //$NON-NLS-1$
  }
  
  @Override
  protected void goBack() {
    scienceEngine.setScreen(new SplashScreen(scienceEngine));
  }
  
  private void gotoDomainHome(final Domain domain) {
    Gdx.app.log(ScienceEngine.LOG, "Starting " + domain); //$NON-NLS-1$
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    AbstractScreen domainHomeScreen = new DomainHomeScreen(scienceEngine, domain);
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, domainHomeScreen));
  }
}
