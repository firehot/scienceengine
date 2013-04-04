package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.InstallProfile;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

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
        enterApplication();
      }
    };
    
    Table userInfo = new Table(getSkin());
    Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    final Image userImage = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
    userInfo.add(profile.getUserName()).left();
    userInfo.add(userImage).width(60).height(60);
    userInfo.setPosition(100, 100);

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
    stage.addActor(userInfo);
    userInfo.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        final PreferencesManager preferencesManager = ScienceEngine.getPreferencesManager();
        InstallProfile installProfile = preferencesManager.getInstallProfile();
        boolean multipleUsers = installProfile.getUserIds() != null;
        if (multipleUsers) {
          Table userTable = new Table(getSkin());
          userTable.setFillParent(false);
          userTable.defaults().fill();
          for (final String userId: installProfile.getUserIds()) {
            final Profile userProfile = preferencesManager.getUserProfile(userId);
            // precondition: this user has a name and pixmap
            Label userLabel = new Label(userProfile.getUserName(), getSkin());
            ClickListener listener = new ClickListener() {
              public void clicked (InputEvent event, float x, float y) {
                // change to selected user as active
                preferencesManager.setActiveUserProfile(userProfile);
                enterApplication();
              }
            };
            Pixmap userPixmap = userProfile.getUserPixmap();
            Image userImage;
            if (userPixmap != null) {
              userImage = new Image(new Texture(userPixmap));
              userPixmap.dispose();
            } else {
              userImage = new Image();
            }
            
            userLabel.addListener(listener);
            userImage.addListener(listener);
            userTable.add(userLabel).center();
            userTable.add(userImage).width(60).height(60);
            userTable.row();
            ScrollPane usersPane = new ScrollPane(userTable, getSkin());
            usersPane.setPosition(50, 50);
            usersPane.setSize(200, 150);
            usersPane.setScrollingDisabled(true, false);

            stage.addActor(usersPane);
          }
        } else {
          new RegistrationDialog(getSkin(), userImage).show(stage);
        }
      }
    });
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
