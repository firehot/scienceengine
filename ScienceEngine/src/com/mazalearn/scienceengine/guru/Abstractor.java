package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
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
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.ModelControls;

public class Abstractor extends AbstractTutor {

  Vector2 points[] = new Vector2[] { new Vector2() };
  private Table configTable;
  private ModelControls modelControls;
  private Skin skin;
  private Set<String> correctParameters;
  private Image[] life = new Image[3];
  private int numLivesLeft = 3;
  
  public Abstractor(final IScience2DController science2DController, String goal, 
      Array<?> components, Array<?> configs, Skin skin, 
      ModelControls modelControls, int deltaSuccessScore,
      int deltaFailureScore) {
    super(science2DController, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    this.skin = skin;
    this.modelControls = modelControls;
    this.setSize(0, 0);
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#reinitialize(float, float, float, float)
   */
  /**
   * Abstractor allows user to interact with bodies on screen as well as its
   * own GUI. But does not directly interact - hence size 0.
   */
  @Override
  public void reinitialize(boolean probeMode) {
    super.reinitialize(probeMode);
    
    if (configTable == null) {
      createConfigTable(science2DController.getModel(), skin);
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
    configTable.setPosition(150, 325);
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
            modelControls.refresh();
          }
        });
      }
    }
    shuffle(checkBoxList);
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

  private <T> void shuffle(List<T> list) {
    for (int i = list.size(); i > 1; i--) {
      T tmp = list.get(i - 1);
      int j = MathUtils.random(i - 1);
      list.set(i - 1, list.get(j));
      list.set(j, tmp);
    }
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
        science2DController.getGuru().done(success);
      }
    });
    return doneButton;
  }

  @Override
  public void activate(boolean activate) {
    if (activate) {
      modelControls.refresh();
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
