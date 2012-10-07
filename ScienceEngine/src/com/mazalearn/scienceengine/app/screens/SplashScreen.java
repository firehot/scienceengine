package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;

/**
 * Shows a splash image and moves on to the next screen.
 */
public class SplashScreen extends AbstractScreen {
  private Image splashImage;

  public SplashScreen(ScienceEngine game) {
    super(game);
  }

  @Override
  public void show() {
    super.show();

    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the splash image's region from the atlas
    AtlasRegion splashRegion = getAtlas().findRegion(
        "splash-screen/splash-image"); //$NON-NLS-1$

    // here we create the splash image actor; its size is set when the
    // resize() method gets called
    splashImage = new Image(splashRegion);
    splashImage.setFillParent(true);

    // this is needed for the fade-in effect to work correctly; we're just
    // making the image completely transparent
    splashImage.getColor().a = 0f;

    // configure the fade-in/out effect on the splash image
    splashImage.addAction(Actions.sequence(Actions.fadeIn(0.75f), Actions.delay(2.5f), Actions.fadeOut(0.75f),
        new Action() {
          @Override
          public boolean act(float delta) {
            // the last action will move to the next screen
            scienceEngine.setScreen(new StartScreen(scienceEngine));
            return true;
          }
        }));

    // and finally we add the actor to the stage
    stage.addActor(splashImage);
  }


  @Override
  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    Gdx.app.exit();
  }
  
}
