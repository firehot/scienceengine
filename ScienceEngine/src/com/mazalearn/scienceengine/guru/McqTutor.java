package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.DomainHomeScreen;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public class McqTutor extends AbstractTutor {

  public static class ChoiceListener extends ClickListener {
    
    private boolean correct;
    private ITutor tutor;

    public ChoiceListener(ITutor tutor, boolean correct) {
      this.correct = correct;
      this.tutor = tutor;
    }
    
    @Override
    public void clicked (InputEvent event, float x, float y) {
      tutor.prepareToFinish(correct);
    }
  }

  private Skin skin;

  public McqTutor(IScience2DController science2dController, ITutor parent,
      String goal, String id, Array<?> components, Array<?> configs,
      Skin skin,
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2dController, parent, goal, id, components, configs,
        deltaSuccessScore, deltaFailureScore, hints);
    this.skin = skin;
    this.setSize(ScreenComponent.Prober.getWidth(), ScreenComponent.Prober.getHeight());
  }
  
  @Override
  public void prepareToFinish(boolean success) {
    if (success) {
      guru.showSuccess(getSuccessScore());
    } else {
      guru.showFailure(getFailureScore());
    }
    this.isComplete = success;
    guru.showNextButton(true);
  }

  /**
   * Initialize this tutor
   * @param options
   * @param answerMask - reversed binary string on options - true = 1, false = 0
   */
  public void initialize(String[] options, String answerMask) {
    int index = 0;
    float y = ScreenComponent.McqOption.getY(0);
    for (String option: options) {
      Button optionButton = DomainHomeScreen.createTextButton(option, 
          ScreenComponent.McqOption.getX(200), y - 50 * 2 * index, 
          200, 40, skin.get(TextButtonStyle.class));
      optionButton.setColor(Color.YELLOW);
      this.addActor(optionButton);
      ClickListener listener = new ChoiceListener(this, answerMask.charAt(index) == '1');
      optionButton.addListener(listener);
      index++;
    }
  }
}
