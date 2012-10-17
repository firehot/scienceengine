package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Helper extends Group {
  private TextButton helpBox;
  private float originalX;
  private float originalY;
  private Vector2 lastTouch = new Vector2();
  private Vector3 currentTouch = new Vector3();
  private Image helpIcon;

  public Helper(Skin skin, float x, float y) {
    super();
    this.setPosition(0, 0);
    this.setName("Help");
    this.helpIcon = new Image(new Texture("images/help.png"));
    helpIcon.setSize(48, 48);
    helpIcon.setPosition(x, y);
    this.helpBox = new TextButton("Really long Helptext", skin);
    helpBox.setColor(Color.YELLOW);
    helpBox.setVisible(false);
    //helpBox.getLabel().setWrap(true);
    helpBox.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        helpBox.setVisible(false);
      }
    });
    this.addActor(helpIcon);
    this.addActor(helpBox);
    helpIcon.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        getStage().getCamera().unproject(currentTouch);
        lastTouch.set(currentTouch.x, currentTouch.y);
        originalX = helpIcon.getX();
        originalY = helpIcon.getY();
        return true;
      }

      @Override
      public void touchDragged(InputEvent event, float localX, float localY, int pointer) {
        // Screen coords of current touch
        currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        // Stage coords of current touch
        getStage().getCamera().unproject(currentTouch);
        // Get negative of movement vector
        lastTouch.sub(currentTouch.x, currentTouch.y);
        helpIcon.setPosition(helpIcon.getX() - lastTouch.x, helpIcon.getY() - lastTouch.y);
        // Recalibrate lastTouch to new coordinates
        lastTouch.set(currentTouch.x, currentTouch.y);
      }

      @Override
      public void touchUp(InputEvent event, float localX, float localY, int pointer, int button) {
        // Screen coords of current touch
        currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        // Stage coords of current touch
        getStage().getCamera().unproject(currentTouch);
        helpIcon.setPosition(originalX, originalY);
        Actor actor = getStage().getRoot().hit(currentTouch.x, currentTouch.y, true);
        while (actor != null && actor.getName() == null) {
          actor = actor.getParent();
        }
        if (actor != null && actor.getName() != null) {
          helpBox.setVisible(true);
          helpBox.setText(actor.getName());
          helpBox.invalidate();
          // Translate to group coordinates
          stageToLocalCoordinates(lastTouch.set(currentTouch.x, currentTouch.y));
          helpBox.setPosition(lastTouch.x - helpBox.getWidth()/2, 
              lastTouch.y - helpBox.getHeight()/2);
          helpBox.addAction(Actions.sequence(Actions.delay(2f), new Action() {
            @Override
            public boolean act(float delta) {
              helpBox.setVisible(false);
              return true;
            }
          }));
        }
      }
    });
 }
}