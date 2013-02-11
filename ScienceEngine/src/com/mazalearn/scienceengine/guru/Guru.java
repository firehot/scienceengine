package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;

/**
 * Cycles through the eligible registeredTutors - probing the user with each one.
 * This is the root of the tutor hierarchy.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements ITutor {
  int tutorIndex = -1;
  ITutor currentTutor;
  protected Dashboard dashboard;
  private List<ITutor> registeredTutors = new ArrayList<ITutor>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final ModelControls modelControls;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage, correctImage, wrongImage;
  private Hinter hinter;
  private IScience2DController science2DController;
  private ViewControls viewControls;
  private List<ITutor> activeTutors = new ArrayList<ITutor>();
  private final String goal;
  
  public Guru(final Skin skin, IScience2DController science2DController, String goal) {
    super();
    this.science2DController = science2DController;
    this.goal = goal;
    activeTutors.add(this);
    this.setPosition(0, 0);
    // Guru has no direct user interaction - hence 0 size
    this.setSize(0, 0);
    
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.modelControls = science2DController.getModelControls();
    this.viewControls = science2DController.getViewControls();
     
    this.successImage = new ScoreImage(ScienceEngine.assetManager.get("images/greenballoon.png", Texture.class), skin, true);
    this.failureImage = new ScoreImage(ScienceEngine.assetManager.get("images/redballoon.png", Texture.class), skin, false);
    this.correctImage = new ScoreImage(ScienceEngine.assetManager.get("images/check.png", Texture.class), skin, true);
    this.wrongImage = new ScoreImage(ScienceEngine.assetManager.get("images/cross.png", Texture.class), skin, false);
    
    ((Stage)science2DController.getView()).addActor(successImage);
    ((Stage)science2DController.getView()).addActor(failureImage);
    ((Stage)science2DController.getView()).addActor(correctImage);
    ((Stage)science2DController.getView()).addActor(wrongImage);
    hinter = new Hinter(skin);
    this.addActor(hinter);
    this.setVisible(false);
  }

  public void registerTutor(AbstractTutor tutor) {
    registeredTutors.add(tutor);
    this.addActor(tutor);
  }

  public void startChallenge() {
    // Mark start of challenge in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Challenge.name());
    // Reset scores and bring dashboard to top
    dashboard.resetScore();
    getStage().addActor(this); // bring Guru to top

    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: science2DController.getView().getActors()) {
      if (actor.isVisible() && actor != this && !ScreenComponent.Background.name().equals(actor.getName())) {
        excludedActors.add(actor);
      }
    }
    
    if (registeredTutors.size() == 0) { // No activeTutors available
      endChallenge();
      return;
    }
    
    prepareToTeach();
    teach();
  }
  
  public void endChallenge() {
    // Reinitialize current prober, if any
    if (currentTutor != null) {
      currentTutor = null;
      // Remove all except self from active tutors
      activeTutors.clear();
      activeTutors.add(this);
    }

    science2DController.getView().done(false);
    ScienceEngine.setProbeMode(false);
    this.setVisible(false);
    // Clear event log
    ScienceEngine.getEventLog().clear();
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  @Override
  public void done(boolean success) {
    hinter.setHint(null);
    if (success) {            
      // Success and no more activeTutors == WIN
      if (tutorIndex >= registeredTutors.size() - 1) {
        soundManager.play(ScienceEngineSound.CELEBRATE);
        science2DController.getView().done(true);
        dashboard.clearGoals();
        this.setVisible(false);
        return;
      }
      teach();
    } else {
      science2DController.getView().done(false);
      this.setVisible(false);
      return;
    }
  }

  public void showWrong(int score) {
    soundManager.play(ScienceEngineSound.FAILURE);
    dashboard.addScore(-score);
    wrongImage.show(String.valueOf(score));
  }
  
  public void showFailure(String message) {
    soundManager.play(ScienceEngineSound.FAILURE);
    failureImage.show(message);
  }

  public void showCorrect(int score) {
    soundManager.play(ScienceEngineSound.SUCCESS);
    dashboard.addScore(score);
    correctImage.show(String.valueOf(score));
    hinter.clearHint();
  }
  
  public void showSuccess(String message) {
    soundManager.play(ScienceEngineSound.SUCCESS);
    successImage.show(message);
    hinter.clearHint();
  }
  
  @Override
  public void act(float dt) {
    super.act(dt);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentTutor != null) {
      if (!hinter.hasHint()) {
        hinter.setHint(activeTutors.get(activeTutors.size() - 1).getHint());
      }
    }
  }
  
  @Override
  public String getGoal() {
    return goal;
  }

  public void pushTutor(ITutor tutor) {
    activeTutors.add(tutor);
    setGoalsInDashboard();
    hinter.clearHint();
  }
  
  public void popTutor(ITutor tutor) {
    if (activeTutors.size() > 0 && activeTutors.get(activeTutors.size() - 1).equals(tutor)) {
      activeTutors.remove(activeTutors.size() - 1);
      hinter.clearHint();
    }
  }
  
  // Prerequisite: registeredTutors.size() >= 1
  @Override
  public void teach() {
    // Move on to next tutor
    tutorIndex++;
    if (tutorIndex == registeredTutors.size()) {
      done(true);
      return;
    }
    currentTutor = registeredTutors.get(tutorIndex);
    currentTutor.prepareToTeach();
    currentTutor.teach();
    setGoalsInDashboard();
  }

  private void setGoalsInDashboard() {
    List<String> goals = new ArrayList<String>();
    for (ITutor tutor: activeTutors) {
      goals.add(tutor.getGoal());
    }
    dashboard.setGoals(goals);
  }
  
  public void setupProbeConfigs(List<IModelConfig<?>> configs, boolean enableControls) {
    configGenerator.generateConfig(configs);
    modelControls.syncWithModel(); // Force sync with model
    modelControls.refresh();
    // Turn off access to parts of control panel
    modelControls.enableControls(enableControls);
    viewControls.enableControls(enableControls);
  }

  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }

  public void reset() {
    if (currentTutor != null) {
      currentTutor.reset();
    }
  }

  @Override
  public void prepareToTeach() {
    this.setVisible(true);
    tutorIndex = -1;
  }

  @Override
  public String getHint() {
    return null;
  }

  @Override
  public int getSuccessScore() {
    return 0;
  }

  @Override
  public int getFailureScore() {
    return 0;
  }
}