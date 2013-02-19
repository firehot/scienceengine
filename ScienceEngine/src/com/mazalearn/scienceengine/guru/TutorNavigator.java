package com.mazalearn.scienceengine.guru;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.DomainHomeScreen;
import com.mazalearn.scienceengine.app.utils.Format;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.guru.ITutor.GroupType;

public class TutorNavigator extends Group {
  
  private static final float TUTOR_WIDTH = 200;
  private static final float TUTOR_HEIGHT = 150;
  Color c = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5f);
  TextureRegion gray = ScreenUtils.createTexture(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, c);
  private TextButton activeTutorButton;
  private Label[] tutorTimeLabels;
  private final Collection<ITutor> tutors;
  private Image userImage;

  public TutorNavigator(Collection<ITutor> tutors, final Guru guru, Skin skin) {
    super();
    this.tutors = tutors;
    setVisible(false);
    setPosition(0, 0);
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    tutorTimeLabels = new Label[tutors.size()];
    addActor(createTutorsPane(tutors, guru, skin));
    userImage = new Image(new Texture("images/user.png"));
  }

  /**
   * Refresh tutor activity times and show.
   * @param activeTutor - subgoal which is active.
   */
  public void show(ITutor activeTutor) {
    // Update times spent per subgoal
    int count = 0;
    for (final ITutor tutor: this.tutors) {
      Label timeLabel = tutorTimeLabels[count++];
      timeLabel.setText(Format.formatTime(tutor.getTimeSpent()));
      if (tutor.getSuccessPercent() == 100) {
        Image status = new Image(new Texture("images/check.png"));
        TextButton subgoalButton = (TextButton) findActor(tutor.getId());
        subgoalButton.addActor(status);
        status.setPosition(ScreenComponent.getScaledX(70),
            ScreenComponent.getScaledY(TUTOR_HEIGHT - 64));
        status.setSize(ScreenComponent.getScaledX(60), ScreenComponent.getScaledY(60));
      }
    }
    // Update active subgoal
    activeTutorButton = (TextButton) findActor(activeTutor.getId());
    if (activeTutorButton != null) {
      activeTutorButton.addActor(userImage);
    }
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
          TutorNavigator.this.setVisible(false);
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
      tutorTimeLabels[count++] = 
          DomainHomeScreen.createLabel("", tutorButton, 0, TUTOR_HEIGHT - 30,
              50, 30, labelBackground);
      
      // Count
      DomainHomeScreen.createLabel(String.valueOf(count), tutorButton, TUTOR_WIDTH - 20, 
          TUTOR_HEIGHT - 30, 20, 30, labelBackground);
      if (tutor.getParentTutor().getGroupType() == GroupType.Challenge) {
        tutorButton.setColor(Color.RED);
      } else {
        tutorButton.setColor(Color.YELLOW);
      }
      tutorButton.getLabel().setWrap(true);
      tutorsTable
          .add(tutorButton)
          .width(ScreenComponent.getScaledX(TUTOR_WIDTH))
          .height(ScreenComponent.getScaledY(TUTOR_HEIGHT))
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

  public void draw(SpriteBatch batch, float parentAlpha) {
    batch.draw(gray, getX(), getY());
    super.draw(batch, parentAlpha);
  }
}