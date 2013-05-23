package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.dialogs.OptionsDialog;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.IControl;

public class ViewControls extends Table implements IControl {
  private static final int VIEW_BUTTON_HEIGHT = 29;
  private static final int VIEW_BUTTON_WIDTH = 110;
  protected IMessage messages;
  private boolean isActivated = false;
  protected Table viewControlPanel;
  protected Skin skin;
  
  public ViewControls(Skin skin) {
    super(skin);
    this.skin = skin;
    this.setName("ViewControls");
    messages = ScienceEngine.getMsg();
    this.defaults().fill();
    Image image = new Image(ScienceEngine.getTextureRegion("settings"));
    ScreenComponent.scalePositionAndSize(image, 0, 0, VIEW_BUTTON_HEIGHT, VIEW_BUTTON_HEIGHT);
    Button imageButton = new TextButton("", skin, "body");
    imageButton.addActor(image);
    imageButton.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        setActivated(!isActivated());
      }
    });
    this.add(imageButton)
        .width(image.getWidth())
        .height(image.getHeight())
        .left();
    this.row();
    viewControlPanel = createViewControlPanel(skin);
    this.add(viewControlPanel);
    setActivated(isActivated);
    addActivityControls();
  }
  
  private IMessage getMsg() {
    return messages;
  }
  
  private Table createViewControlPanel(final Skin skin) {
    Table viewControlPanel = new Table(skin);
    viewControlPanel.setName("ViewControls");
    viewControlPanel.defaults()
        .fill()
        .height(ScreenComponent.getScaledX(VIEW_BUTTON_HEIGHT))
        .width(ScreenComponent.getScaledX(VIEW_BUTTON_WIDTH))
        .pad(0);
    return viewControlPanel;
  }
  
  public void syncWithModel() {
  }
  
  public void enableControls(boolean enable) {
    this.invalidate();
  }

  @Override
  public Actor getActor() {
    return this;
  }

  public boolean isActivated() {
    return isActivated;
  }
  
  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
    viewControlPanel.setVisible(isActivated);
  }

  private void addActivityControls() {
    // Add options dialog for controlling language, music, sound.
    Button optionsButton = new TextButton(
        getMsg().getString("ScienceEngine.Options") + "...", skin, "body");
    optionsButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setActivated(false);
        new OptionsDialog(getStage(), skin).show(getStage());
      }
    });
    
    viewControlPanel.add(optionsButton);
    viewControlPanel.row();    
 }
}