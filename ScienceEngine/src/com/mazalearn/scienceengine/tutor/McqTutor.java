package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.TopicHomeScreen;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public class McqTutor extends AbstractTutor {

  public static class ChoiceListener extends ClickListener {
    
    private ITutor tutor;
    private Button[] optionButtons;
    private Button myButton;

    public ChoiceListener(ITutor tutor, Button[] optionButtons, Button myButton) {
      this.tutor = tutor;
      this.optionButtons = optionButtons;
      this.myButton = myButton;
    }
    
    @Override
    public void clicked (InputEvent event, float x, float y) {
      if (myButton != null && myButton.isChecked()) {
        // Clear all buttons except this one.
        for (Button optionButton: optionButtons) {
          if (optionButton != myButton) {
            optionButton.setChecked(false);
          }
        }
      }
      tutor.prepareToFinish(false);
    }
  }

  private Skin skin;
  private Button[] optionButtons;
  private String answerMask;
  private boolean singleAnswer;

  public McqTutor(IScience2DController science2dController, ITutor parent,
      String goal, String id, Array<?> components, Array<?> configs,
      Skin skin, int deltaSuccessScore, int deltaFailureScore, String[] hints,
      boolean singleAnswer) {
    super(science2dController, parent, goal, id, components, configs,
        deltaSuccessScore, deltaFailureScore, hints);
    this.skin = skin;
    this.singleAnswer = singleAnswer;
    this.setSize(ScreenComponent.Prober.getWidth(), ScreenComponent.Prober.getHeight());
  }
  
  @Override
  public void prepareToFinish(boolean success) {
    guru.showNextButton(true);
  }
  
  @Override
  public void finish() {
    boolean success = true;
    for (int index = 0; index < optionButtons.length; index++) {
      success &= (answerMask.charAt(index) == '1') == optionButtons[index].isChecked();
    }
    if (success) {
      guru.showSuccess(getSuccessScore());
    } else {
      guru.showFailure(getFailureScore());
    }
    this.isComplete = true;
    super.finish();
  }

  /**
   * Initialize this tutor
   * @param options
   * @param answerMask - sequence of truth value of options - true = 1, false = 0
   */
  public void initialize(String[] options, String answerMask) {
    float y = ScreenComponent.McqOption.getY();
    this.optionButtons = new Button[options.length];
    this.answerMask = answerMask;
    int index = 0;
    int numAnswers = 0;
    for (String option: options) {
      Button optionButton = TopicHomeScreen.createTextButton(option, 
          ScreenComponent.McqOption.getX(ScreenComponent.getScaledX(400)), 
          y - ScreenComponent.getScaledY(30 * 2 * index), 
          400, 30,
          skin.get("toggle", TextButtonStyle.class));
      optionButton.setColor(Color.YELLOW);
      this.addActor(optionButton);
      optionButtons[index] = optionButton;
      numAnswers += (answerMask.charAt(index) == '1') ? 1 : 0;
      ClickListener listener = new ChoiceListener(this, optionButtons, 
          singleAnswer ? optionButton : null);
      optionButton.addListener(listener);
      index++;
    }
    if (answerMask.length() != options.length) {
      Gdx.app.error(ScienceEngine.LOG, "Answer mask does not match number of options");      
    } else if (singleAnswer && numAnswers != 1) {
      Gdx.app.error(ScienceEngine.LOG, "Single Answer MCQ does not have one answer specified");
    } else if (numAnswers == 0) {
      Gdx.app.error(ScienceEngine.LOG, "No answers specified in Answer mask");            
    }
  }
}
