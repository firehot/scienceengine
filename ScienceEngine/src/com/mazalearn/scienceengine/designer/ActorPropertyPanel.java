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
  private Label nameLabel;
  private CheckBox visibleField, allowMoveField;
  public ActorPropertyPanel(Skin skin, final LevelEditor levelEditor) {
    super(skin, null, "ActorPropertyPanel");
    this.debug();
    this.skin = skin;
    this.nameLabel = new Label("Name", skin);
    this.add("Name").left();
    this.add(nameLabel).width(50);
    this.row();
    visibleField = addCheckBoxProperty("Visible");
    visibleField.setClickListener(new ClickListener() {
      @Override
      public void click(Actor clickActor, float x, float y) {
        actor.visible = visibleField.isChecked();
        if (actor instanceof Science2DActor) {
          ((Science2DActor) actor).getBody().setActive(actor.visible);
          levelEditor.refreshOnVisibilityChange();
        }
      }
    });
    xField = addLabeledProperty("X");
    yField = addLabeledProperty("Y");
    widthField = addLabeledProperty("Width");
    heightField = addLabeledProperty("Height");
    rotationField = addLabeledProperty("Rotation");
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
    actor.visible = visibleField.isChecked();
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
    visibleField.setChecked(actor.visible);
    if (actor instanceof Science2DActor) {
      allowMoveField.setChecked(((Science2DActor) actor).isAllowMove());
    }
  }    
}