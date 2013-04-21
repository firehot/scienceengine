package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;

public class ShowGiftDialog extends Dialog {
  
  private Dialog parentDialog;

  public ShowGiftDialog(final Skin skin, Message gift, Dialog parentDialog) {
    super("", skin);
    this.parentDialog = parentDialog;
    parentDialog.hide();
    
    Table contentTable = getContentTable();
    contentTable.debug();
    contentTable.add(new Label(gift.email, skin));
    Label points = new Label("Maza coins: " + String.valueOf(gift.points), skin);
    contentTable.add(points).center();
    contentTable.row();
    Label text = new Label(gift.text, skin);
    text.setWrap(true);
    text.setWidth(400);
    contentTable.add(text).width(400).fill();
    try {
      Image image = new Image(new Texture("images/trivia/" + gift.image));
      contentTable.add(image);
    } catch (GdxRuntimeException e) {
      // Ignore - image file not found is not a problem.
    }
    contentTable.row();

    TextButton closeButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Close"), skin, "body");
    this.button(closeButton);
  }
  
  @Override
  protected void result(Object object) {
    parentDialog.show(getStage());
  }
}