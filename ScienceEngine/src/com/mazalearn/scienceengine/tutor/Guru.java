package com.mazalearn.scienceengine.tutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.CoreComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;
/**
 * Root of the tutor hierarchy, handles all the tutors under management of a root tutor.
 * Provides various services to the tutors through tutorHelper - 
 * showing success, failure, correct, wrong
 * group animations for challenge, rapidfire.
 * Provides a TutorNavigator service for random access to tutors.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements ITutor {
  public static final String ID = "Guru";
  public static final String ROOT_ID = "Root";
  private final ModelControls modelControls;
  private final ConfigGenerator configGenerator;
  private IScience2DController science2DController;
  private ViewControls viewControls;
  private String goal;
  private Skin skin;
  private TutorGroup rootTutor;
  private ITutor activeTutor;
  private TutorHelper tutorHelper;
  private ITutor gotoTutor;
  
  public Guru(final Skin skin, IScience2DController science2DController, String goal) {
    super();
    this.setName(ScreenComponent.TUTOR_GROUP);
    this.science2DController = science2DController;
    this.goal = goal;
    this.skin = skin;
    this.setPosition(0, 0);
    // Guru has no direct user interaction - hence 0 size
    this.setSize(0, 0);
    
    this.tutorHelper = new TutorHelper(this, skin, science2DController.getView());
    this.addActor(tutorHelper);   
    
    this.configGenerator = new ConfigGenerator();
    this.modelControls = science2DController.getModelControls();
    this.viewControls = science2DController.getViewControls();
    
    activeTutor = this;
  }
  
  public ITutor getActiveTutor() {
    return activeTutor;
  }

  public ITutor getRootTutor() {
    if (rootTutor == null) {
      this.rootTutor = new TutorGroup(science2DController, TutorType.Root, science2DController.getLevel(), this, goal, ROOT_ID,
          null, null, new String[0], new String[0], new String[0]);
      this.addActor(rootTutor);      
    }
    return rootTutor;
  }
  
  public void initialize(List<ITutor> childTutors) {
    rootTutor.initialize(childTutors, null);
    tutorHelper.populateTutors(rootTutor);
    // Bring tutorhelper to top
    addActor(tutorHelper);
  }
  
  public void beginTutoring() {
    Gdx.app.log(ScienceEngine.LOG, "Start Tutoring: " + getId());
    // Mark start of Tutoring in event log
    ScienceEngine.getEventLog().logEvent(CoreComponentType.Global.name(), 
        Parameter.Tutoring.name());
    // bring Guru to top
    Group root = getStage().getRoot();
    root.addActorBefore(root.findActor(ScreenComponent.CORE_GROUP), this);

    if (rootTutor.getChildTutors().size() == 0) { // No activeTutors available
      endTutoring();
      return;
    }
    
    prepareToTeach(null);
    teach();
  }
  
  public void endTutoring() {
    Gdx.app.log(ScienceEngine.LOG, "End Tutoring: " + getId());
    // Reinitialize current prober, if any
    activeTutor.finish();
 
    ScienceEngine.setProbeMode(false);
    // Clear event log
    ScienceEngine.getEventLog().clear();
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
  }
  
  public void setupProbeConfigs(Collection<IModelConfig<?>> configs, boolean enableControls) {
    configGenerator.generateConfig(configs);
    modelControls.syncWithModel(); // Force sync with model
    modelControls.refresh();
    // Turn off access to parts of control panel
    modelControls.enableControls(enableControls);
    viewControls.enableControls(enableControls);
  }

  public void checkProgress() {
    if (activeTutor == this) return;
    activeTutor.checkProgress();
  }

  public void goTo(ITutor tutor) {
    activeTutor.abort();
    ScienceEngine.setProbeMode(false);
    setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), true);
    // ??? why above series ??? 
    prepareTutors(tutor);
    gotoTutor = tutor;
    tutor.prepareToTeach(null);
    teach();
  }

  private void prepareTutors(ITutor tutor) {
    if (tutor.getParentTutor() != null) {
      prepareTutors(tutor.getParentTutor());
      tutor.getParentTutor().prepareToTeach(tutor);
    }
  }

  public Skin getSkin() {
    return skin;
  }

  public TutorHelper getTutorHelper() {
    return tutorHelper;
  }
  
  public String getLevelEndMessage(boolean success) {
    int progress = Math.round(rootTutor.getStats()[ITutor.PERCENT_PROGRESS]);
    String progressStr = "Progress = " + progress + "%\n\n\n\n";
    if (success && progress >= 80) {
      // If we are in revision mode, show a level end message for revision completed.
      if (tutorHelper.isRevisionMode() && rootTutor.getChildTutors().get(0).getType() != TutorType.Reviewer) {
        return progressStr + ScienceEngine.getMsg().getString("Revision.Success");        
      }
      // Assumption - second last level in any topic is the certification level
      // last level is the Science Train level.
      Topic[] topicLevels = science2DController.getTopic().getChildren();
      if (science2DController.getLevel() == topicLevels[topicLevels.length - 2]) {
        Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
        profile.addCertificate(science2DController.getTopic().name());
      }
      return progressStr + ScienceEngine.getMsg().getString(science2DController.getTopic() + "." + 
          science2DController.getLevel() + ".Success");
    }
    // TODO: Each level should have own failure message. 
    return progressStr + ScienceEngine.getMsg().getString("Level.Failure");
  }

  ////////////////////////////////
  /// ITutor Implementation     //
  ////////////////////////////////
  @Override
  public String getGoal() {
    return goal;
  }

  @Override
  public void finish() {
    recordStats();
    if (rootTutor.getState() == State.Aborted) {
      return;
    }
    // No more tutors available - show success or failure for level
    if (!rootTutor.isSuccess()) {
      science2DController.getView().done(false);
      this.setVisible(false);
      return;
    }
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CELEBRATE);
    science2DController.getView().done(true);
    tutorHelper.clearActiveTutor();
    this.setVisible(false);
  }

  @Override
  public void recordStats() {
    tutorHelper.getProfile().saveStats(getStats(), ID);
  }
  
  @Override
  public float[] getStats() {
    return rootTutor.getStats();
  }
  
  @Override
  public void teach() {
    this.setVisible(true);
    science2DController.reset();
    // If progress on this level is 0 and not a GOTO tutor, then show initial goal
    if (getStats()[ITutor.PERCENT_PROGRESS] == 0 && gotoTutor == null) {
      goal = rootTutor.getGoal() + "\nTouch Next to get started";
      tutorHelper.setActiveTutor(this);
      tutorHelper.showNextAndExplanation(true, false);
    } else {
      rootTutor.prepareToTeach(null);
      rootTutor.teach();
    }
  }
  
  // TODO: prepareToTeach should be same as goto at this level?
  @Override
  public void prepareToTeach(ITutor childTutor) {
    science2DController.reset();
  }
  
  @Override
  public void prepareStage() {
  }

  @Override
  public String getHint() {
    return null;
  }
  
  @Override
  public String getId() {
    return ID;
  }
  
  @Override
  public TutorType getType() {
    return TutorType.Root;
  }
  
  @Override
  public List<ITutor> getChildTutors() {
    return Arrays.asList(new ITutor[] { rootTutor});
  }
  
  @Override
  public ITutor getParentTutor() {
    return null;
  }
  
  @Override
  public void abort() {
  }

  @Override
  public void addTimeSpent(float timeTaken) {
  }

  @Override
  public void systemReadyToFinish(boolean success) {
    finish();
  }
  
  @Override
  public void userReadyToFinish() {
    rootTutor.prepareToTeach(null);
    rootTutor.teach();
  }

  @Override
  public State getState() {
    return rootTutor.getState();
  }

  @Override
  public String[] getExplanation() {
    return rootTutor.getExplanation();
  }
  
  @Override
  public void setParentTutor(ITutor tutor) {    
  }

  @Override
  public String[] getRefs() {
    return rootTutor.getRefs();
  }

  @Override
  public String getProgressText() {
    return "1 of many";
  }
  
}