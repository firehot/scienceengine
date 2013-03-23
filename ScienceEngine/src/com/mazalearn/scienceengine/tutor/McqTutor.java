package com.mazalearn.scienceengine.tutor;

import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public class McqTutor extends AbstractTutor {

  private ButtonGroup optionsGroup;
  private String answerMask;
  private boolean singleAnswer;
  private int[] permutation;

  private List<String> optionList;
  private String explanation;

  public McqTutor(IScience2DController science2DController, TutorType tutorType, ITutor parent,
      String goal, String id, Array<?> components, Array<?> configs,
      Skin skin, int successPoints, int failurePoints, String[] hints,
      boolean singleAnswer) {
    super(science2DController, tutorType, parent, goal, id, 
        components, configs, successPoints, failurePoints, hints);
    this.singleAnswer = singleAnswer;
    this.setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
  }
  
  @Override
  public void systemReadyToFinish(boolean ignored) {
    if (state == State.SystemFinished) return;
    success = true;
    int failureTracker = 0;
    Array<Button> optionButtons = optionsGroup.getButtons();
    for (int i = 0; i < answerMask.length(); i++) {
      if ((answerMask.charAt(permutation[i]) == '1') != optionButtons.get(i).isChecked()) {
        success = false;
        failureTracker += 1 << (permutation[i] * 3);
      }
    }
    if (success) {
      guru.showSuccess(getSuccessPoints());
    } else {
      this.stats.stats[TutorStats.FAILURE_TRACKER] += failureTracker;
      guru.showFailure(getFailurePoints());
    }
    // Flash correct options.
    for (int i = 0; i < answerMask.length(); i++) {
      if (answerMask.charAt(permutation[i]) == '1') {
        optionButtons.get(i).addAction(
            Actions.repeat(-1, 
                Actions.sequence(
                    Actions.alpha(0, 0.5f),
                    Actions.alpha(1, 0.5f),
                    Actions.delay(1))));
      }
    }
    super.systemReadyToFinish(success);
  }
  
  /**
   * Initialize this tutor
   * @param options
   * @param answerMask - sequence of truth value of options - true = 1, false = 0
   */
  public void initialize(String[] options, String explanation, String answerMask) {
    this.answerMask = answerMask;
    this.explanation = explanation;
    this.optionList = Arrays.asList(options);
    this.permutation = Utils.shuffle(optionList);
    
    int numAnswers = 0;
    for (int i = 0; i < answerMask.length(); i++) {
      numAnswers += (answerMask.charAt(i) == '1') ? 1 : 0;
    }
    if (answerMask.length() != options.length) {
      Gdx.app.error(ScienceEngine.LOG, getGoal() + ": Answer mask does not match number of options");      
    } else if (singleAnswer && numAnswers != 1) {
      Gdx.app.error(ScienceEngine.LOG, getGoal() + ": Single Answer MCQ does not have one answer specified");
    } else if (numAnswers == 0) {
      Gdx.app.error(ScienceEngine.LOG, getGoal() + ": No answers specified in Answer mask");            
    }
  }
  
  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
    McqActor mcqActor = guru.getMcqActor();
    optionsGroup = mcqActor.setUp(this, optionList, explanation, singleAnswer);
    addActor(mcqActor);
  }
}
