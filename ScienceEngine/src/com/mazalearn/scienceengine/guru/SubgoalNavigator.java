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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.guru.ITutor.GroupType;

public class SubgoalNavigator extends Group {
  
  private static final float SUBGOAL_WIDTH = 200;
  private static final float SUBGOAL_HEIGHT = 150;
  Color c = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5f);
  TextureRegion gray = ScreenUtils.createTexture(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, c);
  private TextButton activeTutorButton;
  private Image userImage;

  public SubgoalNavigator(List<ITutor> subgoals, final Guru guru, Skin skin) {
    super();
    setVisible(false);
    setPosition(0, 0);
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    addActor(createSubgoalsPane(subgoals, guru, skin));
    userImage = new Image(new Texture("images/user.png"));
  }

  public void setActiveTutor(ITutor activeTutor) {
    activeTutorButton = (TextButton) findActor(activeTutor.getGoal());
    if (activeTutorButton != null) {
      activeTutorButton.addActor(userImage);
    }
  }
  
  private Actor createSubgoalsPane(List<ITutor> subgoals, final Guru guru, Skin skin) {
    Table subgoalsTable = new Table(skin);
    subgoalsTable.setName("Subgoals");
    
    for (final ITutor subgoal: subgoals) {
      TextButton subgoalButton = new TextButton(subgoal.getGoal(), skin);
      subgoalButton.addListener(new ClickListener() {
        @Override
        public void clicked (InputEvent event, float x, float y) {
          SubgoalNavigator.this.setVisible(false);
          if (activeTutorButton != null) { // remove user image
            activeTutorButton.removeActor(userImage);
          }
          guru.goTo(subgoal);
        }        
      });
      subgoalButton.setName(subgoal.getGoal());
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