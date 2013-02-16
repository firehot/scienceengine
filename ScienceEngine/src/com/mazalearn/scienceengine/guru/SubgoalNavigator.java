package com.mazalearn.scienceengine.guru;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.guru.ITutor.GroupType;

public class SubgoalNavigator extends Group {
  
  private static final float SUBGOAL_WIDTH = 200;
  private static final float SUBGOAL_HEIGHT = 150;
  Color c = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5f);
  TextureRegion gray = ScreenUtils.createTexture(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, c);
  private TextButton activeSubgoalButton;
  private Label[] subgoalTimeLabels;
  private Image userImage;

  public SubgoalNavigator(List<ITutor> subgoals, final Guru guru, Skin skin) {
    super();
    setVisible(false);
    setPosition(0, 0);
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    subgoalTimeLabels = new Label[subgoals.size()];
    addActor(createSubgoalsPane(subgoals, guru, skin));
    userImage = new Image(new Texture("images/user.png"));
  }

  /**
   * Refresh subgoal activity times and show.
   * @param activeSubgoal - subgoal which is active.
   */
  public void show(ITutor activeSubgoal) {
    // Update times spent per subgoal
    Profile profile = ScienceEngine.getPreferencesManager().getProfile();
    for (Label timeLabel: subgoalTimeLabels) {
      String subgoalId = timeLabel.getName();
      int timeSpent = Math.round(profile.getTimeSpent(subgoalId));
      timeLabel.setText("Time: " + String.valueOf(timeSpent));
    }
    // Update active subgoal
    activeSubgoalButton = (TextButton) findActor(activeSubgoal.getId());
    if (activeSubgoalButton != null) {
      activeSubgoalButton.addActor(userImage);
    }
  }
  
  private Actor createSubgoalsPane(List<ITutor> subgoals, final Guru guru, Skin skin) {
    Table subgoalsTable = new Table(skin);
    subgoalsTable.setName("Subgoals");
    
    int count = 0;
    for (final ITutor subgoal: subgoals) {
      TextButton subgoalButton = new TextButton(subgoal.getGoal(), skin);
      subgoalButton.addListener(new ClickListener() {
        @Override
        public void clicked (InputEvent event, float x, float y) {
          SubgoalNavigator.this.setVisible(false);
          if (activeSubgoalButton != null) { // remove user image
            activeSubgoalButton.removeActor(userImage);
          }
          guru.goTo(subgoal);
        }        
      });
      subgoalButton.setName(subgoal.getId());
      
      Label timeLabel = new Label("", skin);
      timeLabel.setName(subgoal.getId());
      timeLabel.setAlignment(Align.center, Align.center);
      timeLabel.setWidth(ScreenComponent.getScaledX(50));
      timeLabel.setHeight(ScreenComponent.getScaledY(30));
      timeLabel.setPosition(5, ScreenComponent.getScaledY(SUBGOAL_HEIGHT - 30));
      subgoalTimeLabels[count++] = timeLabel;
      subgoalButton.addActor(timeLabel);
      
      if (subgoal.getParentTutor().getGroupType() == GroupType.Challenge) {
        subgoalButton.setColor(Color.RED);
      } else {
        subgoalButton.setColor(Color.YELLOW);
      }
      subgoalButton.getLabel().setWrap(true);
      subgoalsTable
          .add(subgoalButton)
          .width(ScreenComponent.getScaledX(SUBGOAL_WIDTH))
          .height(ScreenComponent.getScaledY(SUBGOAL_HEIGHT))
          .pad(5);
    }
    ScrollPane subgoalsPane = new ScrollPane(subgoalsTable, skin);
    subgoalsPane.setFadeScrollBars(false);
    subgoalsPane.setScrollingDisabled(false, true);
    subgoalsPane.setPosition(ScreenComponent.getScaledX(50), 
        ScreenComponent.VIEWPORT_HEIGHT / 2 - ScreenComponent.getScaledY(SUBGOAL_HEIGHT / 2));
    subgoalsPane.setSize(ScreenComponent.VIEWPORT_WIDTH - 2 * ScreenComponent.getScaledX(50),
        ScreenComponent.getScaledY(SUBGOAL_HEIGHT + 15));
    return subgoalsPane;
  }

  public void draw(SpriteBatch batch, float parentAlpha) {
    batch.draw(gray, getX(), getY());
    super.draw(batch, parentAlpha);
  }
}