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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.view.DrawingActor;

public class ChangeFaceDialog extends Dialog {
  
  private Profile profile;

  public ChangeFaceDialog(final Skin skin, final Image userImage) {
    super("", skin);
    
    profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    Label title = new Label(profile.getUserName(), skin);
    title.setWidth(600);
    title.setAlignment(Align.center, Align.center);


    getContentTable().debug();
    getContentTable().add(title).width(800).pad(10).center().colspan(2);
    getContentTable().row();
    getContentTable().add("Your current face");
    getContentTable().add("Your new face");
    getContentTable().row();
    Image image = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
    getContentTable().add(image).height(DrawingActor.SCALED_FACE_HEIGHT).width(DrawingActor.SCALED_FACE_WIDTH).fill();
    final DrawingActor face = new DrawingActor(skin);
    getContentTable().add(face).height(DrawingActor.SCALED_FACE_HEIGHT).width(DrawingActor.SCALED_FACE_WIDTH).fill();
    getContentTable().row();

    TextButton cancelButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Cancel"), skin, "body");
    this.getButtonTable().add(cancelButton).width(150).center();
    
    Button saveButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Save"), skin, "body");
    saveButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        profile.setUserPixmap(face.getPixmap());
        userImage.setDrawable(new TextureRegionDrawable(ScienceEngine.getTextureRegion(ScienceEngine.USER)));
      }
    });
    this.getButtonTable().add(saveButton).width(150).center();
  }
}