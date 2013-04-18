package com.mazalearn.scienceengine.tutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
  private Map<String, Integer> correctParameters;
  private Image[] life = new Image[3];
  private int numLivesLeft = 3;
  private TextureRegionDrawable increase, decrease, noeffect, question;
  private List<IModelConfig<?>> configList;
  
  public Abstractor(final IScience2DController science2DController, TutorType tutorType, ITutor parent, String goal, 
      String name, Array<?> components, Array<?> configs, Skin skin, 
      ModelControls modelControls, String[] hints, String[] explanation, String[] refs) {
    super(science2DController, tutorType, parent, goal, name, components, configs, 
        hints, explanation, refs);
    this.skin = skin;
    this.modelControls = modelControls;
    /* Abstractor allows user to interact with bodies on screen as well as its
       own GUI. But does not directly interact - hence size 0.  */
    this.setSize(0, 0);
    decrease = new TextureRegionDrawable(ScienceEngine.getTextureRegion("fieldarrow-left"));
    noeffect = new TextureRegionDrawable(ScienceEngine.getTextureRegion("cross"));
    increase = new TextureRegionDrawable(ScienceEngine.getTextureRegion("fieldarrow"));
    question = new TextureRegionDrawable(ScienceEngine.getTextureRegion("questionmark"));
  }
  
  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
    
    if (configTable == null) {
      createConfigTable(science2DController.getModel(), skin);
    } else {
      for (final IModelConfig<?> config: configList) {
        Image image = (Image) configTable.findActor(config.getName());
        image.setDrawable(question);
      }
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
    ScreenComponent.scalePosition(configTable, 200, 340);
    this.addActor(configTable);

    TextureRegion ideaTexture = ScienceEngine.getTextureRegion("idea");
    configList = new ArrayList<IModelConfig<?>>();
    for (final IModelConfig<?> config: science2DModel.getAllConfigs().values()) {
      if (config.isPossible() && config.isPermitted() && config.getBody() != null) {
        configList.add(config);
      }
    }
    // Shuffle parameters
    Utils.shuffle(configList);
    // Add parameters to table
    TextButton submitButton = createSubmitButton(skin);
    final ChangeOptions changeOptions = new ChangeOptions(tutorHelper, question, submitButton, 
        configTable.getChildren(), decrease, noeffect, increase);
    this.addActor(changeOptions);
    for (final IModelConfig<?> config: configList) {
      configTable.add(config.getName()).left();
      final Image image = new Image();
      image.setDrawable(question);
      image.setName(config.getName());
      configTable.add(image).width(30).height(30);
      final Label label = new Label("", skin);
      configTable.add(label).width(30);
      image.addListener(new ClickListener() {
        public void clicked (InputEvent event, float x, float y) {
          image.setVisible(false);
          changeOptions.setPosition(event.getStageX(), event.getStageY());
          changeOptions.setImageAndLabel(image, label);
          changeOptions.setVisible(true);
        }        
      });
      configTable.row();      
    }
    // Add lives to table
    Table lifeTable = new Table(skin);
    for (int i = 0; i < 3; i++) {
      life[i] = new Image(ideaTexture);
      life[i].setSize(ScreenComponent.Idea.getWidth() / 2,
          ScreenComponent.Idea.getHeight() / 2);
      lifeTable.add(life[i]).width(ScreenComponent.Idea.getWidth());
    }
    configTable.add(lifeTable).fill().spaceTop(20);
    configTable.add(submitButton).fill().spaceTop(20);
    configTable.row();
  }
  
  private static class ChangeOptions extends Table {
    Image img;
    private Label label;
    public ChangeOptions(TutorHelper tutorHelper, final TextureRegionDrawable question, 
        final Button submitButton, final Array<Actor> choices, final TextureRegionDrawable... options) {
      super(tutorHelper.getSkin());
      for (TextureRegionDrawable option: options) {
        final Image opt = new Image(); opt.setDrawable(option);
        opt.addListener(new ClickListener() {
          public void clicked (InputEvent event, float x, float y) {
            img.setDrawable(opt.getDrawable());
            img.setVisible(true);
            if (img.getDrawable() == options[2]) {
              label.setText("Increase");
            } else if (img.getDrawable() == options[0]) {
              label.setText("Decrease");            
            } else if (img.getDrawable() == options[1]) {
              label.setText("No Effect");
            }
            ChangeOptions.this.setVisible(false);
            boolean disableSubmit = false;
            for (Actor actor: choices) {
              if (!(actor instanceof Image)) continue;
              Image image = (Image) actor;
              if (image.getDrawable() == question) {
                disableSubmit = true;
                break;
              }
            }
            submitButton.setDisabled(disableSubmit);
          }              
        });
        this.add(opt).width(30).height(30).left();
      }
    }
    
    public void setImageAndLabel(Image image, Label label) {
      if (this.img != null) {
        this.img.setVisible(true);
      }
      this.img = image;
      this.label = label;
    }
  }

  private TextButton createSubmitButton(Skin skin) {
    final TextButton submitButton = new TextButton("Submit", skin);

    submitButton.addListener(new ClickListener() {
      @Override
      public void clicked (InputEvent event, float x, float y) {
        if (submitButton.isDisabled()) return;
        Map<String, Integer> chosenParameters = new HashMap<String, Integer>();
        for (Actor actor: configTable.getChildren()) {
          if (!(actor instanceof Image)) continue;
          Image image = (Image) actor;
          if (image.getDrawable() == increase) {
            chosenParameters.put(image.getName(), 1);
          } else if (image.getDrawable() == decrease) {
            chosenParameters.put(image.getName(), -1);
          }
        }
        boolean success = correctParameters.equals(chosenParameters);
        systemReadyToFinish(success);
      }
    });
    submitButton.setDisabled(true);
    return submitButton;
  }
  
  @Override
  public void systemReadyToFinish(boolean success) {
    if (!success) {
      life[--numLivesLeft].getColor().a = 0.3f;
      tutorHelper.showWrong(getFailurePoints());
      stats[ITutor.POINTS] -= getFailurePoints();
      recordStats();
      if (numLivesLeft == 0) {
        super.systemReadyToFinish(false);
      }
      return;
    }
    tutorHelper.showCorrect(getSuccessPoints());
    stats[ITutor.POINTS] += getSuccessPoints();
    super.systemReadyToFinish(true);
  }

  @Override
  public void teach() {
    super.teach();
    modelControls.refresh();
  }

  public void initialize(Map<String, Integer> parameters) {
    correctParameters = parameters;
  }

}
