package com.mazalearn.scienceengine.app.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class MessageDialog extends Dialog {
  
  public MessageDialog(final Skin skin, final String messageText) {
    super("", skin, "buydialog");
    
    Label message = new Label(messageText, skin, "buy");
    message.setAlignment(Align.center, Align.center);


    getContentTable().add(message).pad(10).center();
    button("OK");
  }
}