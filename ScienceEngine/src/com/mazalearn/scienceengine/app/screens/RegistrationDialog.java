package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.view.DrawingActor;

public class RegistrationDialog extends Dialog {
  
  private Profile profile;

  public RegistrationDialog(final Skin skin) {
    super("Registration", skin);
    
    Label name = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Name"), skin);
    name.setWidth(600);

    final Label registration = new Label("", skin);
    profile = ScienceEngine.getPreferencesManager().getProfile();
    final boolean alreadyRegistered = profile.getUserEmail().length() > 0;
    if (alreadyRegistered) {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.Registered") + profile.getUserEmail());
    } else {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.Registration"));
    }
    registration.setWidth(600);
    registration.setWrap(true);

    getContentTable().debug();
    getContentTable().add(name).width(600).pad(10).colspan(2);
    getContentTable().row();
    getContentTable().add(registration).width(600).pad(10).colspan(2);
    getContentTable().row();
    getContentTable().add("Draw your image").width(600).center().colspan(2);
    getContentTable().row();
    final DrawingActor face = new DrawingActor(skin);
    getContentTable().add(face).height(128).width(128).fill();
    Image image = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
    getContentTable().add(image).height(128).width(128).fill();

    TextButton cancelButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Cancel"), skin);
    this.getButtonTable().add(cancelButton).width(150).center();
    
    Button registerButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Continue"), skin);
    registerButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        profile.setUserPixmap(face.getPixmap());
        if (!alreadyRegistered) {
          // Bring up registration form
          ScienceEngine.getPlatformAdapter().browseURL("http://" + ScienceEngine.getHostPort() + "/registration.jsp?" + 
              Profile.INSTALL_ID + "=" + profile.getInstallationId().toLowerCase());
        }
      }
    });
    this.getButtonTable().add(registerButton).width(150).center();
  }
}