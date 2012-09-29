package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;

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
    scienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the splash image's region from the atlas
    AtlasRegion splashRegion = getAtlas().findRegion(
        "splash-screen/splash-image"); //$NON-NLS-1$

    // here we create the splash image actor; its size is set when the
    // resize() method gets called
    splashImage = new Image(splashRegion, Scaling.stretch);
    splashImage.setFillParent(true);

    // this is needed for the fade-in effect to work correctly; we're just
    // making the image completely transparent
    splashImage.color.a = 0f;

    // configure the fade-in/out effect on the splash image
    splashImage.action(Sequence.$(FadeIn.$(0.75f), Delay.$(2.5f), FadeOut.$(0.75f),
        new AnimationAction() {
          @Override
          public void act(float delta) {
            // the last action will move to the next screen
            scienceEngine.setScreen(new StartScreen(scienceEngine));
          }

          @Override
          public void setTarget(Actor actor) {}

          @Override
          public Action copy() {
            return null;
          }
        }));

    // and finally we add the actor to the stage
    stage.addActor(splashImage);
  }
}
