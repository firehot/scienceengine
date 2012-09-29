package com.mazalearn.scienceengine.designer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.core.view.Science2DActor;

class ActorPropertyPanel extends Table {
  private Actor actor;
  private Skin skin;
  private TextField widthField, heightField, xField, yField, rotationField;
  private TextField originXField, originYField;
  private Label nameLabel;
  private CheckBox allowMoveField;
  public ActorPropertyPanel(Skin skin, final LevelEditor levelEditor) {
    super(skin, null, "ActorPropertyPanel");
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
    CheckBox checkBox = new CheckBox(skin);
    this.add(label).left();
    this.add(checkBox).width(50);
    this.row();
    return checkBox;
  }

  protected TextField addLabeledProperty(String label) {
    TextField textField = new TextField(skin);
    this.add(label).left(); 
    this.add(textField).width(50);
    this.row();
    return textField;
  }

  public void setActor(Actor actor) {
    if (this.actor != null && this.actor != actor) {
      saveActorProperties(this.actor);
    }
    this.actor = actor;
    if (actor != null) {
      showActorProperties(actor);
    }
  }
  
  private void saveActorProperties(Actor actor) {
    actor.x = Float.parseFloat(xField.getText());
    actor.y = Float.parseFloat(yField.getText());
    actor.width = Float.parseFloat(widthField.getText());
    actor.height = Float.parseFloat(heightField.getText());
    actor.rotation = Float.parseFloat(rotationField.getText());
    actor.originX = Float.parseFloat(originXField.getText());
    actor.originY = Float.parseFloat(originYField.getText());
    if (actor instanceof Science2DActor) {
      ((Science2DActor) actor).setAllowMove(allowMoveField.isChecked());
    }
  }
  
  private void showActorProperties(Actor actor) {
    nameLabel.setText(actor.name != null ? actor.name : "null");
    xField.setText(String.valueOf(actor.x));
    yField.setText(String.valueOf(actor.y));
    widthField.setText(String.valueOf(actor.width));
    heightField.setText(String.valueOf(actor.height));
    rotationField.setText(String.valueOf(actor.rotation));
    originXField.setText(String.valueOf(actor.originX));
    originYField.setText(String.valueOf(actor.originY));
    if (actor instanceof Science2DActor) {
      allowMoveField.setChecked(((Science2DActor) actor).isAllowMove());
    }
  }    
}