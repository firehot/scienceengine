package com.mazalearn.scienceengine.app.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class MessageDialog extends Dialog {
  
  public MessageDialog(final Skin skin, final String messageText) {
    super("", skin);
    
    Label message = new Label(messageText, skin);
    message.setWidth(600);
    message.setAlignment(Align.center, Align.center);


    getContentTable().add(message).width(800).pad(10).center().colspan(2);
    getContentTable().row();
    button("OK");
  }
}