package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.dialogs.UserHomeDialog;
import com.mazalearn.scienceengine.app.services.InstallProfile;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.core.view.CommandClickListener;

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
    ScienceEngine.getPreferencesManager().syncProfiles(false);
    scienceEngine.setScreen(new ChooseTopicScreen(scienceEngine));
  }

  @Override
  public void show() {
    super.show();
    
    // Load sounds
    addAssets();
    
    // start playing the hum music
    ScienceEngine.getMusicManager().stop();
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.HUM);
    
    ClickListener startListener = new CommandClickListener() {
      @Override
      public void doCommand() {
        enterApplication();
      }
    };
    
    Table userInfo = new Table(getSkin());
    final Image userImage = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
    userImage.setSize(ScreenComponent.User.getWidth() * 2, ScreenComponent.User.getHeight() * 2);
    userInfo.add(userImage).width(userImage.getWidth()).height(userImage.getHeight());
    Label userName = new Label(getProfile().getUserName(), getSkin(), "default-big");
    userInfo.add(userName).right();
    userInfo.setPosition(ScreenComponent.getScaledX(100), ScreenComponent.getScaledY(100));

    Label touchToStart = new Label("Touch to Start", getSkin(), "default-big");
    touchToStart.setFontScale(1.5f);
    touchToStart.setColor(Color.WHITE);
    
    touchToStart.setPosition(
        ScreenComponent.VIEWPORT_WIDTH / 2 - ScreenComponent.getScaledX(touchToStart.getWidth() / 2),
        ScreenComponent.VIEWPORT_HEIGHT / 2 - ScreenComponent.getScaledY(touchToStart.getHeight()) * 2);
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
    final PreferencesManager preferencesManager = ScienceEngine.getPreferencesManager();
    final InstallProfile installProfile = preferencesManager.getInstallProfile();
    userInfo.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        boolean multipleUsers = installProfile.getUserIds() != null;
        if (multipleUsers) {
          Table userTable = new Table(getSkin());
          userTable.setFillParent(false);
          userTable.defaults().fill();
          for (final String userId: installProfile.getUserIds()) {
            final Profile userProfile = preferencesManager.getUserProfile(userId);
            String name;
            if (userProfile.isRegistered()) {
              name = userProfile.getUserName();
            } else {
              name = userId.substring(0, userId.indexOf("@"));
              // set user id locally - this has effect only on this client
              userProfile.setUserEmailLocally(userId);
            }
            // precondition: this user has a name and pixmap
            Label userLabel = new Label(name, getSkin());
            ClickListener listener = new CommandClickListener() {
              @Override
              public void doCommand() {
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
            userTable.add(userImage).width(ScreenComponent.getScaledX(60)).height(ScreenComponent.getScaledY(60));
            userTable.row();
            ScrollPane usersPane = new ScrollPane(userTable, getSkin());
            usersPane.setPosition(ScreenComponent.getScaledX(50), ScreenComponent.getScaledY(50));
            usersPane.setSize(ScreenComponent.getScaledX(200), ScreenComponent.getScaledY(150));
            usersPane.setScrollingDisabled(true, false);

            stage.addActor(usersPane);
          }
        } else {
          new UserHomeDialog(getSkin(), userImage).show(stage);
        }
      }
    });
    splashImage.addListener(startListener);
    touchToStart.addListener(startListener);
    
    // Installation Info
    Label version = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Name"), getSkin(), "default-small");
    Label installation = new Label(installProfile.getInstallationId(), getSkin(), "default-small");
    version.setPosition(ScreenComponent.getScaledX(10), ScreenComponent.getScaledY(40));
    installation.setPosition(ScreenComponent.getScaledX(10), ScreenComponent.getScaledY(25));
    stage.addActor(version);
    stage.addActor(installation);

    // Registration Info if registered
    String owner = (installProfile.getRegisteredUserId() != null) ? installProfile.getRegisteredUserId() : "Not registered";
    Label registration = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Registered") + 
          ": " + owner, getSkin(), "default-small");
    registration.setPosition(ScreenComponent.getScaledX(10), ScreenComponent.getScaledY(10));
    stage.addActor(registration);
    
    // Do a sync of all profiles here
    ScienceEngine.getPreferencesManager().syncProfiles(false);    
  }

  @Override
  protected void goBack() {
    Gdx.app.log(ScienceEngine.LOG, "Exiting engine"); //$NON-NLS-1$
    Gdx.app.exit();
  }
  
}
