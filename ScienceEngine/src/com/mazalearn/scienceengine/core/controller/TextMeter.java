package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.IScience2DView;

/**
 * Utility class for science2DModel text meter
 *
 */
public class TextMeter implements IControl {
  @SuppressWarnings("rawtypes")
  private final IModelConfig property;
  private final Table table;
  private final Label label;
  boolean pinned = false;
  private Image pinflat;
  private Image pinup;
  
  @SuppressWarnings("rawtypes")
  public TextMeter(final IModelConfig property, final Skin skin) {
    this.table = new Table(skin);
    table.setName(property.getParameter().name());
    this.label = new Label(property.getParameter().name(), skin);
    label.setColor(Color.YELLOW);
    this.property = property;
    label.setName(property.getName());
    label.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        ScienceEngine.selectParameter(property.getParameter(),
            (String) property.getValue(),
            (IScience2DView) label.getStage());
        return super.touchDown(event, localX, localY, pointer, button);
      }
    });
    pinflat = new Image(new Texture("images/pinflat.png"));
    pinup = new Image(new Texture("images/pinup.png"));
    pinflat.setVisible(!pinned);
    pinup.setVisible(pinned);
    table.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        property.doCommand();
        pinned = !pinned;
        pinflat.setVisible(!pinned);
        pinup.setVisible(pinned);
      }
    });
    table.add(label);
    table.add(pinflat);
    table.add(pinup);
  }
  
  public Actor getActor() {
    return table;
  }

  @Override
  public void syncWithModel() {
    label.setText(String.valueOf(property.getValue()));
  }
  
  public boolean isAvailable() {
    return property.isAvailable();
  }
}