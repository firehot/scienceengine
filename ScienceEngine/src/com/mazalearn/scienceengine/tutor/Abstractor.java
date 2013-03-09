package com.mazalearn.scienceengine.tutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.mazalearn.scienceengine.ScreenComponent;
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
  
  public Abstractor(final IScience2DController science2DController, TutorType tutorType, ITutor parent, String goal, 
      String name, Array<?> components, Array<?> configs, Skin skin, 
      ModelControls modelControls, int successPoints,
      int failurePoints, String[] hints) {
    super(science2DController, tutorType, parent, goal, name, components, configs, successPoints, failurePoints, hints);
    this.skin = skin;
    this.modelControls = modelControls;
    /* Abstractor allows user to interact with bodies on screen as well as its
       own GUI. But does not directly interact - hence size 0.  */
    this.setSize(0, 0);
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.tutor.AbstractTutor#reinitialize(float, float, float, float)
   */
  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
    
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
    ScreenComponent.scalePosition(configTable, 150, 325);
    this.addActor(configTable);

    TextureRegion shoppingCartTexture = ScienceEngine.getTextureRegion("shoppingcart");
    // TODO: Create cart as a screencomponent
    final Image cart = new Image(shoppingCartTexture);
    cart.setSize(ScreenComponent.ShoppingCart.getWidth(), ScreenComponent.ShoppingCart.getHeight());
    cart.setPosition(ScreenComponent.ShoppingCart.getX(), 
        ScreenComponent.ShoppingCart.getY());
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
    // Shuffle parameters
    Utils.shuffle(checkBoxList);
    // Add parameters to table
    for (CheckBox checkBox: checkBoxList) {
      configTable.add(checkBox).left().colspan(4);
      configTable.row();      
    }
    // Add lives to table
    for (int i = 0; i < 3; i++) {
      life[i] = new Image(shoppingCartTexture);
      life[i].setSize(ScreenComponent.ShoppingCart.getWidth() / 2, ScreenComponent.ShoppingCart.getHeight() / 2);
      configTable.add(life[i]).width(ScreenComponent.ShoppingCart.getWidth() / 2);
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
        systemReadyToFinish(success);
      }
    });
    return doneButton;
  }
  
  @Override
  public void systemReadyToFinish(boolean success) {
    if (!success) {
      life[--numLivesLeft].getColor().a = 0.3f;
      guru.showWrong(getFailurePoints());
      if (numLivesLeft == 0) {
        super.systemReadyToFinish(false);
      }
      return;
    }
    guru.showCorrect(getSuccessPoints());
    super.systemReadyToFinish(true);
  }

  @Override
  public void teach() {
    super.teach();
    modelControls.refresh();
  }

  public void initialize(String[] parameters) {
    correctParameters = new HashSet<String>();
    for (String parameter: parameters) {
      correctParameters.add(parameter);
    }
  }

}
