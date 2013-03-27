package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.tutor.IDoneCallback;

/**
 * Shows a splash image and moves on to the next screen.
 */
public class SplashScreen extends AbstractScreen {

  private final class SplashImage extends Image {

    private SplashImage(TextureRegion region) {
      super(region);
      this.setFillParent(true);
      // this is needed for the fade-in effect to work correctly; we're just
      // making the image completely transparent
      this.getColor().a = 0f;
      // configure the fade-in effect on the splash image
      this.addAction(Actions.fadeIn(0.75f));
    }
  }

  public SplashScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(true);
    }
  }

  @Override
  protected boolean needsLayout() {
    return false;
  }

  private void enterApplication() {
    // TODO: sync when login user changes otherwise not useful here.
    ScienceEngine.getPreferencesManager().syncProfiles();
    scienceEngine.setScreen(new ChooseTopicScreen(scienceEngine));
  }

  @Override
  public void show() {
    super.show();

    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);
    
    ClickListener startListener = new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        boolean enterpriseInstall = false;
        if (enterpriseInstall) {
          final Dialog loginDialog = new LoginDialog(scienceEngine.getSkin(), new IDoneCallback() {
              @Override
              public void done(boolean success) {
                enterApplication();
              }
            });
            loginDialog.show(stage);
        } else {
          enterApplication();
        }
      }
    };

    Label touchToStart = new Label("Touch to Start", scienceEngine.getSkin());
    touchToStart.setColor(Color.WHITE);
    touchToStart.setFontScale(2f);
    touchToStart.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - 120, ScreenComponent.VIEWPORT_HEIGHT / 2 - 80);
    touchToStart.addAction(
        Actions.forever(
            Actions.sequence(
                Actions.fadeIn(1f), 
                Actions.delay(0.5f), 
                Actions.fadeOut(1f))));

    // retrieve the splash image
    TextureRegion splashRegion = new TextureRegion(new Texture("images/splash.jpg")); //$NON-NLS-1$

    // We create the splash image actor; its size is set when the
    // resize() method gets called
    SplashImage splashImage = new SplashImage(splashRegion);
    stage.addActor(splashImage);
    stage.addActor(touchToStart);
    splashImage.addListener(startListener);
    touchToStart.addListener(startListener);
    
    // Do a sync of all profiles here
    ScienceEngine.getPreferencesManager().syncProfiles();    
  }

  @Override
  protected void goBack() {
    Gdx.app.log(ScienceEngine.LOG, "Exiting engine"); //$NON-NLS-1$
    Gdx.app.exit();
  }
  
}
