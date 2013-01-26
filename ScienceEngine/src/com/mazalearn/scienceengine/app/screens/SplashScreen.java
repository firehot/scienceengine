package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

/**
 * Shows a splash image and moves on to the next screen.
 */
public class SplashScreen extends AbstractScreen {

  private Dialog loginDialog;
  private Label touchToEnter;
  private Profile profile;

  private final class SplashImage extends Image {

    private SplashImage(TextureRegion region) {
      super(region);
      this.setFillParent(true);

      // this is needed for the fade-in effect to work correctly; we're just
      // making the image completely transparent
      this.getColor().a = 0f;

      // configure the fade-in effect on the splash image
      this.addAction(Actions.fadeIn(0.75f));
      
      this.addListener(new ClickListener() {
        public void clicked (InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          if (ScienceEngine.DEV_MODE == DevMode.DEBUG &&
                  !profile.getUserEmail().isEmpty() &&
                  !profile.getUserName().isEmpty()) {
            scienceEngine.setScreen(new ChooseDomainScreen(scienceEngine));
          } else if (ScienceEngine.getPlatformAdapter().getPlatform() == IPlatformAdapter.Platform.IOS){
            // Onscreen keyboard not showing in IOS - this is a workaround.
            Gdx.input.getTextInput(new TextInputListener() {
              @Override
              public void input(String email) {
                profile.setUserName(email.substring(0, email.indexOf("@")));
                profile.setUserEmail(email);
                ScienceEngine.getProfileManager().persist();
                scienceEngine.setScreen(new ChooseDomainScreen(scienceEngine));
              }
              
              @Override
              public void canceled() {}
            }, "Enter email address", profile.getUserEmail());
          } else {
            loginDialog.show(stage);
          }
        }      
      });      
    }
  }

  public SplashScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
    ProfileManager profileManager = ScienceEngine.getProfileManager();
    profile = profileManager.retrieveProfile();
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(true);
    }
  }

  @Override
  public void show() {
    super.show();

    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);
    
    touchToEnter = new Label("Touch to Enter", scienceEngine.getSkin());
    touchToEnter.setFontScale(2f);
    touchToEnter.setPosition(VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2);
    touchToEnter.addAction(
        Actions.forever(
            Actions.sequence(
                Actions.fadeIn(1f), 
                Actions.delay(0.5f), 
                Actions.fadeOut(1f))));

    // retrieve the splash image's region from the atlas
    AtlasRegion splashRegion = getAtlas().findRegion(
        "splash-screen/splash-image"); //$NON-NLS-1$

    loginDialog = createLoginDialog(scienceEngine.getSkin());

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
    stage.addActor(touchToEnter);
    stage.addActor(options);
  }

  private Dialog createLoginDialog(Skin skin) {
    final Dialog dialog = new Dialog("", skin);
    dialog.setSize(400, 150);
    dialog.setPosition(AbstractScreen.VIEWPORT_WIDTH / 2, 100);
    dialog.setBackground((Drawable) null);

    dialog.getContentTable().add(new Label("Name: ", skin));
    final TextField name = new TextField(profile.getUserName(), skin);
    dialog.getContentTable().add(name);
    dialog.getContentTable().row();
    dialog.getContentTable().add(new Label("Email: ", skin));
    final TextField email = new TextField(profile.getUserEmail(), skin);
    email.setWidth(200);
    dialog.getContentTable().add(email).width(200);
    dialog.getContentTable().row();
    Label emailUseNote = new Label("Activation link will be sent to this email address", skin);
    emailUseNote.setFontScale(0.9f);
    dialog.getContentTable().add(emailUseNote).colspan(2);
    dialog.getContentTable().row();

    TextButton loginButton = new TextButton("Login", skin);
    dialog.getContentTable().add(loginButton).colspan(2).center();
    loginButton.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        profile.setUserName(name.getText());
        profile.setUserEmail(email.getText());
        ScienceEngine.getProfileManager().persist();
        
        // Primitive validation
        if (!name.getText().matches("[a-zA-Z ]{2,30}") || !email.getText().contains("@")) {
          if (!name.getText().matches("[a-zA-Z ]{2,30}")) {
            name.setColor(Color.RED);
            name.addAction(Actions.repeat(-1, Actions.sequence(Actions.fadeOut(1), Actions.fadeIn(1))));
          } else {
            name.setColor(1, 1, 1, 1);
            name.clearActions();
          }
          if (!email.getText().contains("@")) {
            email.setColor(Color.RED);
            email.addAction(Actions.repeat(-1, Actions.sequence(Actions.fadeOut(1), Actions.fadeIn(1))));
          } else {
            email.setColor(1, 1, 1, 1);
            email.clearActions();
          }
          return;
        }
        dialog.hide();
        scienceEngine.setScreen(new ChooseDomainScreen(scienceEngine));
      }      
    });      
    
    return dialog;
  }

  @Override
  protected void goBack() {
    ScienceEngine.getProfileManager().persist();
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    Gdx.app.log(ScienceEngine.LOG, "Exiting engine"); //$NON-NLS-1$
    Gdx.app.exit();
  }
  
}
