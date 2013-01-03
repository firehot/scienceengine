package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class Abstractor extends AbstractTutor {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Table configTable;
  private ControlPanel controlPanel;
  private float guruWidth;
  private float guruHeight;
  private Skin skin;
  private Set<String> correctParameters;
  private Image[] life = new Image[3];
  private int numLivesLeft = 3;
  
  public Abstractor(final IScience2DModel science2DModel, 
      final IScience2DView science2DView, String goal, 
      Array<?> components, Array<?> configs, Skin skin, 
      ControlPanel controlPanel, int deltaSuccessScore,
      int deltaFailureScore) {
    super(science2DModel, science2DView, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    this.skin = skin;
    this.controlPanel = controlPanel;

  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#reinitialize(float, float, float, float)
   */
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x, y, 0, 0, probeMode);
    this.guruWidth = width;
    this.guruHeight = height;
    this.setPosition(0, 0);
    
    if (configTable == null) {
      createConfigTable(science2DModel, skin);
    }
    configTable.setVisible(true);
    numLivesLeft = 3;
    for (int i = 0; i < 3; i++) {
      life[i].getColor().a = 1f;
    }
  }
  
  private void createConfigTable(IScience2DModel science2DModel, Skin skin) {
    configTable = new Table(skin);
    configTable.setName("Configs");
    configTable.setPosition(150, -125);
    this.addActor(configTable);
    Texture shoppingCartTexture = new Texture("images/shoppingcart.png");
    final Image cart = new Image(shoppingCartTexture);
    cart.setSize(50, 50);
    cart.setPosition(40, -40);
    cart.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        configTable.setVisible(!configTable.isVisible());
      }      
    });
    this.addActor(cart);
    List<CheckBox> checkBoxList = new ArrayList<CheckBox>();
    for (final IModelConfig<?> config: science2DModel.getAllConfigs()) {
      if (config.isPossible() && config.isPermitted() && config.getBody() != null) {
        final CheckBox configCheckBox = new CheckBox(config.getName(), skin);
        configCheckBox.setName(config.getName());
        checkBoxList.add(configCheckBox);
        configCheckBox.setChecked(false);
        configCheckBox.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (configCheckBox.isChecked()) {
              ScienceEngine.pin(config.getBody(), true);
            }
            controlPanel.refresh();
          }
        });
      }
    }
    Collections.shuffle(checkBoxList);
    for (CheckBox checkBox: checkBoxList) {
      configTable.add(checkBox).left().colspan(4);
      configTable.row();      
    }
    // Shuffle rows
    for (int i = 0; i < 3; i++) {
      life[i] = new Image(shoppingCartTexture);
      life[i].setSize(25, 25);
      configTable.add(life[i]).width(25);
    }
    configTable.add(createDoneButton(skin)).fill();
    configTable.row();
  }

  private TextButton createDoneButton(Skin skin) {
    TextButton doneButton = new TextButton("Done", skin);

    doneButton.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        Set<String> chosenParameters = new HashSet<String>();
        for (Actor actor: configTable.getChildren()) {
          if (!(actor instanceof CheckBox)) continue;
          CheckBox checkBox = (CheckBox) actor;
          if (checkBox.isChecked()) {
            chosenParameters.add(checkBox.getName());
          }
        }
        boolean success = correctParameters.equals(chosenParameters);
        if (!success) {
          life[--numLivesLeft].getColor().a = 0.3f;
        }
        science2DView.getGuru().done(success);
      }
    });
    return doneButton;
  }

  @Override
  public void activate(boolean activate) {
    if (activate) {
      science2DView.getGuru().setSize(guruWidth,  50);
      science2DView.getGuru().setPosition(0, guruHeight - 50);
      controlPanel.refresh();
    }
    this.setVisible(activate);
  }

  @Override
  public boolean hasSucceeded() {
    return true;
  }

  @Override
  public boolean hasFailed() {
    return numLivesLeft <= 0;
  }

  public void initialize(String[] parameters) {
    correctParameters = new HashSet<String>();
    for (String parameter: parameters) {
      correctParameters.add(parameter);
    }
  }

}