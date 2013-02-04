package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.guru.IDoneCallback;

public class LoginDialog extends Dialog {
  
  private Profile profile;
  private IDoneCallback doneCallback;
  private Skin skin;

  public LoginDialog(Skin skin, IDoneCallback doneCallback) {
    super("", skin);
    ProfileManager profileManager = ScienceEngine.getProfileManager();
    this.profile = profileManager.retrieveProfile();
    this.doneCallback = doneCallback;
    this.skin = skin;
  }

  private void setupDialog() {
    this.setSize(400, 150);
    this.setPosition(AbstractScreen.VIEWPORT_WIDTH / 2, 100);
    
    this.getContentTable().add(new Label("Name: ", skin));
    final TextField name = new TextField(profile.getUserName(), skin);
    this.getContentTable().add(name);
    this.getContentTable().row();
    this.getContentTable().add(new Label("Email: ", skin));
    final TextField email = new TextField(profile.getUserEmail(), skin);
    email.setWidth(200);
    this.getContentTable().add(email).width(200);
    this.getContentTable().row();
    Label emailUseNote = new Label("Activation link will be sent to this email address", skin);
    emailUseNote.setFontScale(0.9f);
    this.getContentTable().add(emailUseNote).colspan(2);
    this.getContentTable().row();

    Table buttonsTable = new Table(skin);
    TextButton loginButton = new TextButton("Login", skin);
    buttonsTable.add(loginButton).uniform();
    TextButton cancelButton = new TextButton("Cancel", skin);
    buttonsTable.add(cancelButton).uniform();
    this.getContentTable().add(buttonsTable).fill().colspan(2);
    cancelButton.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        LoginDialog.this.hide();
      }
    });

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
        LoginDialog.this.hide();
        if (doneCallback != null) {
          doneCallback.done(true);
        }
      }      
    });
  }
  
  @Override
  public Dialog show(Stage stage) {
    if (ScienceEngine.DEV_MODE == DevMode.DEBUG && !profile.getUserEmail().isEmpty() &&
        !profile.getUserName().isEmpty()) {
      doneCallback.done(true);
      return null;
    } else if (ScienceEngine.getPlatformAdapter().getPlatform() == IPlatformAdapter.Platform.IOS){
      // Onscreen keyboard not showing in IOS - this is a workaround.
      Gdx.input.getTextInput(new TextInputListener() {
        @Override
        public void input(String email) {
          profile.setUserName(email.substring(0, email.indexOf("@")));
          profile.setUserEmail(email);
          ScienceEngine.getProfileManager().persist();
          doneCallback.done(true);
        }
        
        @Override
        public void canceled() {}
      }, "Enter email address", profile.getUserEmail());
      return null;
    } else {
      setupDialog();      
      return super.show(stage);
    }
  }
}