package com.mazalearn.scienceengine.designer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.mazalearn.scienceengine.core.view.Science2DActor;

class ActorPropertyPanel extends Table {
  private Actor actor;
  private Skin skin;
  private TextField widthField, heightField, xField, yField, rotationField;
  private TextField originXField, originYField;
  private Label nameLabel;
  private CheckBox allowMoveField;
  public ActorPropertyPanel(Skin skin, final LevelEditor levelEditor) {
    super(skin);
    this.skin = skin;
    this.nameLabel = new Label("Name", skin);
    this.add("Name").left();
    this.add(nameLabel).width(50);
    this.row();
    xField = addLabeledProperty("X");
    yField = addLabeledProperty("Y");
    widthField = addLabeledProperty("Width");
    heightField = addLabeledProperty("Height");
    rotationField = addLabeledProperty("Rotation");
    originXField = addLabeledProperty("Origin X");
    originYField = addLabeledProperty("Origin Y");
    allowMoveField = addCheckBoxProperty("AllowMove");
  }

  protected CheckBox addCheckBoxProperty(String label) {
    CheckBox checkBox = new CheckBox(label, skin);
    this.add(checkBox).width(50);
    this.row();
    return checkBox;
  }

  protected TextField addLabeledProperty(String label) {
    TextField textField = new TextField("", skin);
    this.add(label).left(); 
    this.add(textField).width(50);
    this.row();
    return textField;
  }

  public void setActor(Actor actor) {
    if (this.actor == actor) {
      showActorProperties(actor);
      return;
    }
    if (this.actor != null) {
      saveActorProperties(this.actor);
    }
    if (actor != null) {
      showActorProperties(actor);
    }
    
    this.actor = actor;
  }
  
  private void saveActorProperties(Actor actor) {
    actor.setX(Float.parseFloat(xField.getText()));
    actor.setY(Float.parseFloat(yField.getText()));
    actor.setWidth(Float.parseFloat(widthField.getText()));
    actor.setHeight(Float.parseFloat(heightField.getText()));
    actor.setRotation(Float.parseFloat(rotationField.getText()));
    actor.setOriginX(Float.parseFloat(originXField.getText()));
    actor.setOriginY(Float.parseFloat(originYField.getText()));
    if (actor instanceof Science2DActor) {
      ((Science2DActor) actor).setAllowMove(allowMoveField.isChecked());
    }
  }
  
  private void showActorProperties(Actor actor) {
    nameLabel.setText(actor.getName() != null ? actor.getName() : "null");
    xField.setText(String.valueOf(actor.getX()));
    yField.setText(String.valueOf(actor.getY()));
    widthField.setText(String.valueOf(actor.getWidth()));
    heightField.setText(String.valueOf(actor.getHeight()));
    rotationField.setText(String.valueOf(actor.getRotation()));
    originXField.setText(String.valueOf(actor.getOriginX()));
    originYField.setText(String.valueOf(actor.getOriginY()));
    if (actor instanceof Science2DActor) {
      allowMoveField.setDisabled(false);
      allowMoveField.setChecked(((Science2DActor) actor).isAllowMove());
    } else {
      allowMoveField.setDisabled(true);
    }
  }    
}