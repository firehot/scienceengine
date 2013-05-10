package com.mazalearn.scienceengine.tutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.ActivityScreen;
import com.mazalearn.scienceengine.app.services.AggregatorFunction;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.lang.IFunction;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.SizeAction;

public class TutorHelper extends Group {

  private Skin skin;
  private final SoundManager soundManager;
  private final ScoreImage correctImage, wrongImage;
  SuccessFailureImage successImage, failureImage;
  private McqActor mcqActor;
  private ITutor activeTutor;
  private Hinter hinter;
  private ExplanationBox explanation;
  private Profile profile;
  private IScience2DView science2DView;
  private Guru guru;
  private TimeTracker timeTracker;
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private TutorNavigator tutorNavigator;
  
  private static class RevisionFrame {
    private ITutor tutorUnderRevision;
    private List<ITutor> childTutors;
    
    RevisionFrame(List<ITutor> childTutors, ITutor tutorUnderRevision) {
      this.childTutors = childTutors;
      this.tutorUnderRevision = tutorUnderRevision;
    }
  };
  private Stack<RevisionFrame> revisionStack = new Stack<RevisionFrame>();
  private static final String[] ENCOURAGEMENTS = 
      {"Well Done", "Bravo", "You Rock", "Fantastic", "Excellent", "Correct"};
  
  public TutorHelper(Guru guru, Skin skin, IScience2DView science2DView) {
    this.skin = skin;
    this.science2DView = science2DView;
    this.guru = guru;
    this.activeTutor = guru;
    
    // No direct user interaction
    this.setSize(0, 0);

    this.profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();

    this.soundManager = ScienceEngine.getSoundManager();
    this.successImage = new SuccessFailureImage(ScienceEngine.getTextureRegion("greenballoon"), skin, true);
    this.failureImage = new SuccessFailureImage(ScienceEngine.getTextureRegion("redballoon"), skin, false);
    this.correctImage = new ScoreImage(ScienceEngine.getTextureRegion("check"), skin);
    this.wrongImage = new ScoreImage(ScienceEngine.getTextureRegion("cross"), skin);
    
    this.addActor(successImage);
    this.addActor(failureImage);
    this.addActor(correctImage);
    this.addActor(wrongImage);   
    
    hinter = new Hinter(skin);
    this.addActor(hinter);
    this.addActor(explanation = new ExplanationBox(this, skin, "explanation"));
    
    timeTracker = (TimeTracker) science2DView.findActor(ScreenComponent.TimeTracker.name());
    if (timeTracker != null) {
      timeTracker.setActiveTutor(guru);
    }
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
    hinter.setActiveTutor(activeTutor);
    guru.setActiveTutor(activeTutor);
    timeTracker.setActiveTutor(activeTutor);
    tutorNavigator.setActiveTutor(activeTutor);
  }

  public Profile getProfile() {
    return profile;
  }

  public List<Actor> getExcludedActors() {
    return excludedActors;
  }

  public Skin getSkin() {
    return skin;
  }
  
  // TODO: why create a parser for each tutor? can share parser but 
  // variables are being obtained as a global from the parser.
  public Parser createParser() {
    Parser parser = new Parser();
    Map<String, IFunction.A0> functions0 = new HashMap<String, IFunction.A0>();
    Map<String, IFunction.A1> functions1 = new HashMap<String, IFunction.A1>();
    Map<String, IFunction.A2> functions2 = new HashMap<String, IFunction.A2>();

    for (AggregatorFunction aggregatorFunction: AggregatorFunction.values()) {
      functions1.put(aggregatorFunction.name(), aggregatorFunction);
    }

    for (final IModelConfig<?> command: science2DView.getCommands()) {
      functions0.put(command.getName(), new IFunction.A0() {
         @Override
         public float eval() { command.doCommand(); return 0; }
      });
    }

    parser.allowFunctions(functions0, functions1, functions2);
    return parser;
  }
  
  private String scoreToString(int score) {
    if (score != 0) {
      return String.valueOf(score);
    } 
    return ENCOURAGEMENTS[MathUtils.random(ENCOURAGEMENTS.length - 1)];
  }

  public void showWrong(int score) {
    addActor(wrongImage); // Bring to top
    soundManager.play(ScienceEngineSound.FAILURE);
    wrongImage.show(scoreToString(-score));
    profile.addPoints(-score);
  }
  
  public void showFailure(int score) {
    addActor(failureImage); // bring to top
    soundManager.play(ScienceEngineSound.FAILURE);
    failureImage.show("Oops!");
    profile.addPoints(-score);
  }

  public void showCorrect(int score) {
    addActor(correctImage); // bring to top
    soundManager.play(ScienceEngineSound.SUCCESS);
    correctImage.show(scoreToString(score));
    profile.addPoints(score);
    hinter.clearHint();
  }
  
  public void showSuccess(int score) {
    addActor(successImage); // bring to top
    soundManager.play(ScienceEngineSound.SUCCESS);
    successImage.show(scoreToString(score));
    hinter.clearHint();
    profile.addPoints(score);
   }
  
  public McqActor getMcqActor() {
    if (mcqActor == null) {
      mcqActor = new McqActor(skin);
    }
    return mcqActor;
  }

  public void doChallengeAnimation(final ITutor tutor) {
    final Image challenge = new Image(ScienceEngine.getTextureRegion("challenge"));
    challenge.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2,
        ScreenComponent.VIEWPORT_HEIGHT - 60);
    challenge.setSize(32, 32);
    soundManager.play(ScienceEngineSound.CHALLENGE);
    this.addActor(challenge);
    challenge.addAction(
        Actions.sequence(
            Actions.parallel(
                Actions.moveTo(ScreenComponent.VIEWPORT_WIDTH / 2 - 256 / 2, 
                    ScreenComponent.VIEWPORT_HEIGHT / 2 - 256 / 2, 3),
                SizeAction.sizeTo(256, 256, 3),
                Actions.rotateBy(360, 3)),
            Actions.delay(1),
            new Action() {
              @Override
              public boolean act(float delta) {
                removeActor(challenge);
                tutor.teach();
                return true;
              }
            }));
  }

  public void doRapidFireAnimation(final ITutor tutor) {
    final LabelStyle large = new LabelStyle(skin.get(LabelStyle.class));
    BitmapFont font = skin.getFont(ScreenComponent.getFont(2.5f));
    large.font = font;
    final TextButton start = new TextButton("RapidFire", skin);
    start.setSize(250, 70);
    start.setColor(Color.YELLOW);
    start.getLabel().setStyle(large);
    start.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - start.getWidth() / 2,
        ScreenComponent.VIEWPORT_HEIGHT / 2 - start.getHeight() / 2);
    soundManager.play(ScienceEngineSound.RAPID_FIRE);
    this.addActor(start);
    start.addAction(
        Actions.sequence(
            Actions.alpha(0, 1),
            new Action() {
              @Override
              public boolean act(float delta) {
                start.setText("3");
                start.setSize(70, 70);
                start.setPosition(ScreenComponent.VIEWPORT_WIDTH / 2 - start.getWidth() / 2,
                    ScreenComponent.VIEWPORT_HEIGHT / 2 - start.getHeight() / 2);
                return true;
              }
            },
            Actions.repeat(3, 
                Actions.sequence(
                    Actions.alpha(1),
                    Actions.alpha(0.5f, 0.5f),
                    new Action() {
                      @Override
                      public boolean act(float delta) {
                        start.setText(String.valueOf(Integer.parseInt(start.getText().toString()) - 1));
                        return true;
                      }
                    }
              )),
              new Action() {
                @Override
                public boolean act(float delta) {
                  removeActor(start);
                  tutor.teach();
                  return true;
                }
              }));
  }

  public void showNextAndExplanation(boolean showNext, boolean showExplanation) {
    tutorNavigator.showNextButton(showNext);
    if (showExplanation) {
      if (activeTutor.getExplanation() != null && activeTutor.getExplanation().length > 0) {
        explanation.setExplanation(activeTutor.getExplanation(), activeTutor.getRefs().length > 0);
        explanation.setVisible(true);
        addActor(explanation); // Bring to top
        addActor(tutorNavigator);
      }
    } else {
      explanation.setVisible(false);
    }
  }

  public void clearActiveTutor() {
    tutorNavigator.clearActiveTutor();
  }

  public void populateTutors(TutorGroup rootTutor) {
    if (tutorNavigator != null) {
      tutorNavigator.remove();
    }
    tutorNavigator = new TutorNavigator(rootTutor, guru, this, skin);
    this.addActor(tutorNavigator);
    // Reinitialize excluded actors
    // Collect visible actors to be excluded from probe points.
    excludedActors.clear();
    excludedActors.add(tutorNavigator);
    for (Actor actor: science2DView.getActors()) {
      // actor is visible
      if (actor.isVisible() && actor != guru) {
        excludedActors.add(actor);
      }
    }            
  }

  public void pushRevisionMode() {
    List<ITutor> tutorList = new ArrayList<ITutor>();
    for (String tutorId: activeTutor.getRefs()) {
      ITutor tutor = tutorNavigator.id2Tutor(tutorId);
      if (tutor == null) continue;
      tutorList.add(tutor);
    }
    // Push following two to stack and save in profile
    RevisionFrame revisionFrame = new RevisionFrame(guru.getRootTutor().getChildTutors(), activeTutor);
    revisionStack.push(revisionFrame);
    // TODO: store in profile
    // profile.pushRevisionTutors(refs);
    
    // Set up for revision
    activeTutor.abort();
    guru.initialize(tutorList);
    setActiveTutor(guru);
    Gdx.app.log(ScienceEngine.LOG, "Revise: " + activeTutor.getId());
    ActivityScreen activityScreen = (ActivityScreen) ScienceEngine.SCIENCE_ENGINE.getScreen();
    activityScreen.enterRevisionMode(revisionFrame.tutorUnderRevision);
  }

  /**
   * pops revision frame and returns tutor under revision on stack.
   */
  public ITutor popRevisionMode() {
    RevisionFrame revisionFrame = revisionStack.pop();
    guru.initialize(revisionFrame.childTutors);
    guru.goTo(revisionFrame.tutorUnderRevision);
    return isRevisionMode() ? revisionStack.peek().tutorUnderRevision : null;
  }

  public boolean isRevisionMode() {
    return !revisionStack.isEmpty() || guru.getRootTutor().getChildTutors().get(0).getType() == TutorType.Reviewer;
  }

}
