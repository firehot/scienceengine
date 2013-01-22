package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.esotericsoftware.tablelayout.Cell;

public class IntroductionDialog extends Dialog {
  private AbstractScreen screen;
  
  public IntroductionDialog(final AbstractScreen screen, String title, 
      String contents, String instructions) {
    super(title, screen.getSkin());
    this.screen = screen;

    Skin skin = screen.getSkin();
    setSize(AbstractScreen.VIEWPORT_WIDTH, AbstractScreen.VIEWPORT_HEIGHT);
    setBackground((Drawable) null);
    screen.setBackgroundColor(Color.GRAY);
    
    Label description = new Label("\n\n" + contents, skin);
    description.setWidth(400);
    description.setWrap(true);
    
    final Label navigation = new Label(instructions, skin);
    navigation.setWidth(400);
    navigation.setWrap(true);

    final TextButton navigationButton = new TextButton("Instructions >>", skin);

    getContentTable().add(description).width(400).pad(10);
    getContentTable().row();
    getContentTable().add(navigationButton);
    getContentTable().row();
    @SuppressWarnings("unchecked")
    final Cell<Label> navCell = (Cell<Label>) getContentTable().add(navigation).width(400).pad(10);
    getContentTable().setHeight(400);
    getContentTable().row();
    navCell.setWidget(null);

    navigationButton.addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        if (navCell.getWidget() != null) {
          navCell.setWidget(null);
          navigationButton.setText("Instructions >>");
        } else {
          navCell.setWidget(navigation);
          navigationButton.setText("Instructions <<");
        }
        hide();
        show(screen.getStage());
      }
    });

    button("OK", null);
  }

  @Override
  protected void result(Object obj) {
    screen.setBackgroundColor(Color.BLACK);
  }
}