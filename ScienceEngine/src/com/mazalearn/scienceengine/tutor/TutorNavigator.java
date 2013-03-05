package com.mazalearn.scienceengine.tutor;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.TopicHomeScreen;
import com.mazalearn.scienceengine.app.utils.Format;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.tutor.ITutor.GroupType;

public class TutorNavigator extends Group {
  
  private static final float TUTOR_WIDTH = 200;
  private static final float TUTOR_HEIGHT = 150;
  Color c = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5f);
  TextureRegion gray = ScreenUtils.createTexture(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, c);
  private TextButton activeTutorButton;
  private Label[] tutorTimeLabels;
  private final Collection<ITutor> tutors;
  private Image userImage;
  TextButton goal;
  private ITutor activeTutor;
  private Group tutorsPanel;
  private Button nextButton;
  
  public TutorNavigator(Collection<ITutor> tutors, final Guru guru, Skin skin) {
    super();
    this.tutors = tutors;
    setPosition(0, 0);
    tutorTimeLabels = new Label[tutors.size()];
    tutorsPanel = new Group() {
      @Override
      public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(gray, getX(), getY());
        super.draw(batch, parentAlpha);
      }      
    };
    tutorsPanel.setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    tutorsPanel.addActor(createTutorsPane(tutors, guru, skin));
    tutorsPanel.setVisible(false);
    addActor(tutorsPanel);
    userImage = new Image(ScienceEngine.getTextureRegion("user"));
    ClickListener clickListener = new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        if (tutorsPanel.isVisible()) {
          tutorsPanel.setVisible(false);
          return;
        }
        // Bring tutor navigator to top and make tutorsPanel visible
        tutorsPanel.setVisible(true);
        show(activeTutor);
        getStage().addActor(TutorNavigator.this);
      }
    };
    goal = new TextButton("This is a very long long long test goal" + 
        "This is a very long long long test goal", skin);
    goal.getLabel().setWrap(true);
    goal.addListener(clickListener);
    addActor(goal);
    this.setVisible(false);
    // Create a button NEXT for learner to click when ready to move on.
    createNextButton(skin);    
  }

  private void createNextButton(Skin skin) {
    nextButton = new TextButton("Next", skin, "body");
    nextButton.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        nextButton.setVisible(false);
        activeTutor.userReadyToFinish();
      }
    });
    nextButton.setPosition(ScreenComponent.NextButton.getX(nextButton.getWidth()),
        ScreenComponent.NextButton.getY(nextButton.getHeight()));
    nextButton.setVisible(false);
    addActor(nextButton);
  }
  
  /**
   * Refresh tutor activity times and show.
   * @param activeTutor - tutor which is active.
   */
  public void show(ITutor activeTutor) {
    // Update times spent per tutor
    int count = 0;
    for (final ITutor tutor: this.tutors) {
      Label timeLabel = tutorTimeLabels[count++];
      timeLabel.setText(Format.formatTime(tutor.getTimeSpent()));
      if (tutor.getAttemptPercent() == 100) {
        Image status = new Image(ScienceEngine.getTextureRegion("check"));
        TextButton tutorButton = (TextButton) findActor(tutor.getId());
        tutorButton.addActor(status);
        ScreenComponent.scalePositionAndSize(status, 70, TUTOR_HEIGHT - 64, 60, 60);
      }
    }
    // Update active tutor
    activeTutorButton = (TextButton) findActor(activeTutor.getId());
    if (activeTutorButton != null) {
      activeTutorButton.addActor(userImage);
    }
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
    goal.setText(activeTutor.getGoal());
    /*
     * TODO: setting color of goal causes the color to tint entire screen.
    if (activeTutor.getGroupType() != GroupType.None) { 
      goal.setColor(activeTutor.getGroupType().getColor());
    } else {
      goal.setColor(activeTutor.getParentTutor().getGroupType().getColor());
    }
    */
    goal.addAction(Actions.sequence(
        Actions.alpha(0),
        Actions.alpha(1, 2)));
    goal.setPosition(ScreenComponent.Goal.getX(goal.getWidth()), 
        ScreenComponent.Goal.getY(goal.getHeight()));
    goal.setVisible(true);
  }

  public void clearActiveTutor() {
    activeTutor = null;
    goal.setVisible(false);
    goal.setText("");
  }

  private Actor createTutorsPane(Collection<ITutor> tutors, final Guru guru, Skin skin) {
    Table tutorsTable = new Table(skin);
    tutorsTable.setName("Tutors");
    
    int count = 0;
    for (final ITutor tutor: tutors) {
      TextButton tutorButton = new TextButton(tutor.getGoal(), skin);
      tutorButton.addListener(new ClickListener() {
        @Override
        public void clicked (InputEvent event, float x, float y) {
          tutorsPanel.setVisible(false);
          if (activeTutorButton != null) { // remove user image
            activeTutorButton.removeActor(userImage);
          }
          guru.goTo(tutor);
        }        
      });
      tutorButton.setName(tutor.getId());
      
      LabelStyle labelBackground = new LabelStyle(skin.get(LabelStyle.class));
      labelBackground.background = 
          new TextureRegionDrawable(ScreenUtils.createTexture(20, 20, new Color(61f/255, 83f/255, 58f/255, 1)));

      // Time spent
      tutorTimeLabels[count] = 
          TopicHomeScreen.createLabel("", 0, TUTOR_HEIGHT - 30,
              50, 30, labelBackground);
      tutorButton.addActor(tutorTimeLabels[count++]);
      
      // Count
      tutorButton.addActor(TopicHomeScreen.createLabel(String.valueOf(count), TUTOR_WIDTH - 20, 
          TUTOR_HEIGHT - 30, 20, 30, labelBackground));
      ScreenComponent.scaleSize(tutorButton, TUTOR_WIDTH, TUTOR_HEIGHT);
      if (tutor.getGroupType() != GroupType.None) { 
        tutorButton.setColor(tutor.getGroupType().getColor());
      } else {
        tutorButton.setColor(tutor.getParentTutor().getGroupType().getColor());
      }
      tutorButton.getLabel().setWrap(true);
      tutorsTable
          .add(tutorButton)
          .width(tutorButton.getWidth())
          .height(tutorButton.getHeight())
          .pad(5);
    }
    ScrollPane tutorsPane = new ScrollPane(tutorsTable, skin);
    tutorsPane.setFadeScrollBars(false);
    tutorsPane.setScrollingDisabled(false, true);
    tutorsPane.setPosition(ScreenComponent.getScaledX(50), 
        ScreenComponent.VIEWPORT_HEIGHT / 2 - ScreenComponent.getScaledY(TUTOR_HEIGHT / 2));
    tutorsPane.setSize(ScreenComponent.VIEWPORT_WIDTH - 2 * ScreenComponent.getScaledX(50),
        ScreenComponent.getScaledY(TUTOR_HEIGHT + 15));
    return tutorsPane;
  }

  public void showNextButton(boolean show) {
    nextButton.setVisible(show);
  }
}