package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.domains.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.domains.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.domains.waves.WaveController;

public class ChooseDomainScreen extends AbstractScreen {
  private static final int THUMBNAIL_WIDTH = 200;
  private static final int THUMBNAIL_HEIGHT = 150;

  private Profile profile;

  public ChooseDomainScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
    profile = ScienceEngine.getProfileManager().retrieveProfile();
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(false);
      Gdx.graphics.requestRendering();
    }
  }

  @Override
  public void show() {
    super.show();
    if (!profile.getDomain().equals("")) {
      gotoDomainHome(profile.getDomain());
      return;
    }
    setBackgroundColor(Color.DARK_GRAY);

    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the default table
    Table table = super.getTable();
    table.defaults().spaceBottom(20).fill().center();
    table.add(getMsg().getString("ScienceEngine.ScienceEngine")).colspan(10).spaceBottom(20); //$NON-NLS-1$
    table.row();

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
    final String[] domains = 
        new String[] {StatesOfMatterController.DOMAIN, 
                      WaveController.DOMAIN, 
                      ElectroMagnetismController.DOMAIN};
    Texture overlayLock = new Texture("images/lock.png");
    for (final String domain: domains) {
      final boolean lock = !domain.equals(ElectroMagnetismController.DOMAIN);
      Texture levelThumbnail = LevelUtil.getLevelThumbnail(domain, 1);
      Image domainThumb = 
          lock ? new OverlayImage(levelThumbnail, overlayLock) : new Image(levelThumbnail);
      domainThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          if (lock) {
            Gdx.input.getTextInput(new TextInputListener() {
              @Override
              public void input(String passcode) {
                if ("9876".equals(passcode)) {
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
      levelTable.add(domain);
      levelTable.row();
      levelTable.add(domainThumb).width(THUMBNAIL_WIDTH).height(THUMBNAIL_HEIGHT);
      table.add(levelTable).pad(5);
    }
    return flickScrollPane;
  }
  
  @Override
  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    scienceEngine.setScreen(new SplashScreen(scienceEngine));
  }
  
  private void gotoDomainHome(final String domain) {
    Gdx.app.log(ScienceEngine.LOG, "Starting " + domain); //$NON-NLS-1$
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    AbstractScreen domainHomeScreen = new DomainHomeScreen(scienceEngine, domain);
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, domainHomeScreen));
  }
}
