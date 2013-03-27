package com.mazalearn.scienceengine.app.screens;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;

public class RegistrationDialog extends Dialog {
  
  private static final String DEMOUSER = "demouser@mazalearn.com";
  private final Stage stage;
  private Profile profile;

  public RegistrationDialog(Stage stage, final Skin skin) {
    super("Registration", skin);
    this.stage = stage;
    
    Label name = new Label(ScienceEngine.getMsg().getString("ScienceEngine.Name"), skin);
    name.setWidth(600);

    final Label registration = new Label("", skin);
    profile = ScienceEngine.getPreferencesManager().getProfile();
    boolean alreadyRegistered = profile.getUserEmail().length() > 0;
    if (alreadyRegistered) {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.Registered") + profile.getUserEmail());
    } else {
      registration.setText(ScienceEngine.getMsg().getString("ScienceEngine.Registration"));
    }
    registration.setWidth(600);
    registration.setWrap(true);

    String installationId = profile.getInstallationId();
    // Show 4-digit PIN - we use last 4 digits of installation-id
    // TODO: allow pin to be changed by user.
    final Label pin = 
        new Label(ScienceEngine.getMsg().getString("ScienceEngine.PIN") + ": " + 
           installationId.substring(installationId.length() - 4), skin);
    pin.setWidth(600);
    pin.setColor(Color.RED);
    
    getContentTable().add(name).width(600).pad(10);
    getContentTable().row();
    getContentTable().add(registration).width(600).pad(10);
    getContentTable().row();
    getContentTable().add(pin).width(600).center();

    TextButton cancelButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Cancel"), skin);
    this.getButtonTable().add(cancelButton).width(150).center();
    
    if (!alreadyRegistered) {
      Button registerButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Register"), skin);
      registerButton.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          // Onscreen keyboard to collect registration email address
          getRegistrationEmail();
        }
      });
      this.getButtonTable().add(registerButton).width(150).center();
    }
  }

  private void getRegistrationEmail() {
    Gdx.input.getTextInput(new TextInputListener() {
      @Override
      public void input(String email) {
        if (DEMOUSER.equals(email.toLowerCase())) { // Try again
          getRegistrationEmail();
          return;
        }
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put(Profile.USER_EMAIL, email);
        postParams.put(Profile.INSTALL_ID, profile.getInstallationId().toLowerCase());
        try {
          ScienceEngine.getPlatformAdapter().httpPost("/registrationemail", "text", postParams, new byte[0]);
        } catch (GdxRuntimeException e) {
          Gdx.app.error(ScienceEngine.LOG, "Could not get proper server response");
          ScienceEngine.displayStatusMessage(stage, 
              "Server response improper: Could not register");
        }
      }
      
      @Override
      public void canceled() {}
    }, "Enter email address for registration", DEMOUSER);
  }
}