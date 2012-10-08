package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Messages;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;

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

      // configure the fade-in/out effect on the splash image
      this.addAction(Actions.sequence(Actions.fadeIn(0.75f), Actions.delay(2.5f)));
      
      this.addListener(new ClickListener() {
        public void clicked (InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          scienceEngine.setScreen(new ExperimentMenuScreen(scienceEngine));
        }      
      });
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
      super.draw(batch, parentAlpha);
      BitmapFont font = getFont();
      font.setScale(2.0f);
      font.draw(batch, "Touch to Enter", MENU_VIEWPORT_WIDTH / 2, MENU_VIEWPORT_HEIGHT / 2);
    }
  }

  public SplashScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
  }

  @Override
  public void show() {
    super.show();

    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the splash image's region from the atlas
    AtlasRegion splashRegion = getAtlas().findRegion(
        "splash-screen/splash-image"); //$NON-NLS-1$

    // We create the splash image actor; its size is set when the
    // resize() method gets called
    stage.addActor(new SplashImage(splashRegion));
    TextButton options = new TextButton("Options...", getSkin());
    options.setPosition(VIEWPORT_WIDTH - 100, 60);
    Color c = options.getColor();
    options.setColor(c.r, c.g, c.b, 0.3f);
    options.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(new OptionsScreen(scienceEngine));
      }      
    });
    stage.addActor(options);
  }

  @Override
  protected void goBack() {
    ScienceEngine.getProfileManager().persist();
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    Gdx.app.log(ScienceEngine.LOG, "Exiting engine"); //$NON-NLS-1$
    Gdx.app.exit();
  }
  
}
