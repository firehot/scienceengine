package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.view.DrawingActor;

public class UserHomeDialog extends Dialog {
  
  private Profile profile;

  public UserHomeDialog(final Skin skin, final Image userImage) {
    super("", skin);
    
    Label title = new Label("Home", skin);
    title.setWidth(600);
    title.setAlignment(Align.center, Align.center);

    Label name = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Name"), skin);
    name.setWidth(600);

    final Label registration = new Label("", skin);
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    final boolean alreadyRegistered = profile.getUserEmail().length() > 0;
    Button registerButton = null;
    if (alreadyRegistered) {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.Registered") + profile.getUserEmail());
    } else {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.Registration"));
      registerButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Register"), skin);
      registerButton.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          // Bring up registration form
          ScienceEngine.getPlatformAdapter().browseURL("http://" + ScienceEngine.getHostPort() + "/registration.jsp?" + 
              Profile.INSTALL_ID + "=" + profile.getInstallationId().toLowerCase());
        }
      });
      // Sync profile so that registration will work.
      ScienceEngine.getPreferencesManager().syncProfiles();
    }
    registration.setWidth(800);
    registration.setWrap(true);

    getContentTable().debug();
    getContentTable().add(title).width(800).pad(10).center().colspan(2);
    getContentTable().row();
    getContentTable().add(name).width(800).pad(10).colspan(2);
    getContentTable().row();
    getContentTable().add(registration).width(800).pad(10).colspan(2);
    getContentTable().row();
    if (!alreadyRegistered) {
      this.getButtonTable().add(registerButton).width(150).center();      
    }
    getContentTable().row();
    Image image = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
    getContentTable().add(image).height(DrawingActor.FACE_HEIGHT).width(DrawingActor.FACE_WIDTH).fill();
    getContentTable().row();
    image.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        new ChangeFaceDialog(skin, userImage).show(getStage());
      }
    });

    TextButton closeButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Close"), skin);
    this.getButtonTable().add(closeButton).width(150).center();
  }
}