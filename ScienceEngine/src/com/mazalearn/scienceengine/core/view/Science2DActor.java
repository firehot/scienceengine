package com.mazalearn.scienceengine.core.view;

/*
 * -----------------------------------------------------------------------
 * Copyright 2012 - Sridhar Sundaram
 * -----------------------------------------------------------------------
 * 
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.HelpTour.IHelpComponent;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.model.Science2DBody.MovementMode;

/**
 * Science2DActor - Takes as model a Science2DBody which is a Box2D body
 *                and creates an actor view for it.
 * 
 * 
 */
public class Science2DActor extends Actor implements IHelpComponent {
  protected static final float TOLERANCE = 0.1f;
  private Science2DBody body;
  private TextureRegion textureRegion;
  private Vector2 viewPos = new Vector2(), box2DPos = new Vector2();
  protected Vector2 lastTouch = new Vector2();    // view coordinates
  private boolean drag = false; // indicates 0th pointer touched.
  private Vector3 currentTouch = new Vector3();
  private float moveDistance;

  /**
   * Constructor.
   * 
   * @param body - Box2D body
   * @param textureRegion - texture to use to represent body in view
   */
  public Science2DActor(final Science2DBody body, TextureRegion textureRegion) {
    super();
    this.setName(body.getComponentTypeName());
    this.body = body;
    this.textureRegion = textureRegion;
    // Set the sprite width and height.
    if (textureRegion != null) {
      this.setSize(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
      this.setOrigin(getWidth() / 2, getHeight() / 2);
    }
    ClickListener touchLlistener = new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        if (pointer != 0) return drag = false;
        switch(MovementMode.valueOf(getMovementMode())) {
        case None: return false;
        case Move: moveDistance = 0; drag = true; // fall thru
        case Rotate: lastTouch.set(event.getStageX(), event.getStageY());
        }
        return true;
      }

      @Override
      public void touchDragged(InputEvent event, float localX, float localY, int pointer) {
        if (pointer != 0) return;
        // Get negative of movement vector
        if (drag) moveToCurrent();
      }

      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (pointer != 0) return;
        switch(MovementMode.valueOf(getMovementMode())) {
        case None: return;
        case Move: 
          drag = false;
          if (moveDistance > TOLERANCE) {
            ScienceEngine.selectParameter(body, Parameter.Move, 0f, (IScience2DView) getStage());
          }
          return;
        case Rotate:
          // Get negative of movement vector
          lastTouch.sub(event.getStageX(), event.getStageY());
          // Scale moveDistance vector suitably to get a proportional force
          lastTouch.mul(getRotationForceScaler());
          // view coords of current touch
          viewPos.set(event.getStageX(), event.getStageY());
          // box2d point of current touch
          getBox2DPositionFromViewPosition(viewPos, viewPos, getRotation());
          // Use center as origin - dont understand why this step
          viewPos.sub(getWidth() / (2 * ScreenComponent.PIXELS_PER_M), 
              getHeight() / (2 * ScreenComponent.PIXELS_PER_M));
          body.applyForce(lastTouch, viewPos);
          if (lastTouch.len() > TOLERANCE) {
            ScienceEngine.selectParameter(body, Parameter.Rotate, lastTouch.len(), (IScience2DView) getStage());
          }
          return;
        }
      }

    };
    this.addListener(touchLlistener);
    ClickListener helpListener = new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        if (pointer != 0) return false;
        super.touchDown(event, localX, localY, pointer, button);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.selectBody(body, (IScience2DView) getStage());
        return false;
      }
    };
    this.addListener(helpListener);     
  }
  
  private void moveToCurrent() {
    // Screen coords of current touch
    currentTouch.set(Gdx.input.getX(0), Gdx.input.getY(0), 0);
    // Logical coords of current touch
    getStage().getCamera().unproject(currentTouch);
    // Get negative of movement vector
    lastTouch.sub(currentTouch.x, currentTouch.y);
    moveDistance += lastTouch.len();
    setPosition(getX() - lastTouch.x, getY() - lastTouch.y);
    setPositionFromViewCoords(true);
    // Recalibrate lastTouch to new coordinates
    lastTouch.set(currentTouch.x, currentTouch.y);
  }

  public Science2DBody getBody() {
    return body;
  }
  
  /**
   * Science2DActor is visible iff its associated Science2DBody is active.
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    getBody().setActive(visible);
  }
  
  @Override
  public void act(float delta) {
    // TODO: Adjust height, width, originx, originy of actor based on body - see MagnetActor, DynamoActor
    getViewPositionFromBox2DPosition(viewPos);
    this.setX(viewPos.x);
    this.setY(viewPos.y);
    this.setRotation((body.getAngle() * MathUtils.radiansToDegrees) % 360);
    super.act(delta);
  }

  /**
   * return name of the body associated with the actor
   */
  @Override
  public String getName() {
    return body.name();
  }
  
  /**
   * return name of the body associated with the actor
   */
  @Override
  public String getLocalizedName() {
    return body.getLocalizedName();
  }
  
  /**
   * return type of the body associated with the actor
   */
  @Override
  public String getComponentType() {
    return body.getComponentTypeName();
  }
  
  @Override
  public void showHelp(Stage stage, boolean animate) {
    if (animate) {
      addAction(AnimateAction.animateSize(getWidth(), getHeight(), isVisible()));
    } else {
      clearActions();
    }
  }
  
  protected void setTextureRegion(TextureRegion textureRegion) {
    this.textureRegion = textureRegion;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    if (drag) moveToCurrent();
    batch.draw(textureRegion, getX(), getY(), this.getOriginX(), 
        this.getOriginY(), getWidth(), getHeight(), 1, 1, getRotation());
    // debugDraw(batch);
  }

  float px, py, pbx, pby, pa;
  public void debugDraw(SpriteBatch batch) {
    Color c = batch.getColor();
    batch.setColor(Color.RED);
    batch.draw(textureRegion, getX(), getY(), 0, 0, getWidth(), getHeight(), 1, 1, getRotation());
    batch.setColor(c);
    if (getX() != px || getY() != py || body.getPosition().x != pbx || body.getPosition().y != pby || body.getAngle() != pa) {
      System.out.println(getName() + " x = " + getX() + " y = " + getY() + 
          " origin x = " + this.getOriginX() + " origin y = " + this.getOriginY() + 
          " body = " + body.getPosition() + " angle = " + body.getAngle());
      px = getX(); py = getY(); pbx = body.getPosition().x; pby = body.getPosition().y; pa = body.getAngle();
    }
  }

  public TextureRegion getTextureRegion() {
    return textureRegion;
  }
  
  /**
   * @param viewPos (output) - position of view origin of body
   * position of body in scene2d view is output
   * ASSUMPTION: ORIGIN is BOX2D position for rotating body and does not 
   * change under rotation.
   */
  public void getViewPositionFromBox2DPosition(Vector2 viewPos) {
    viewPos.set(body.getWorldCenter());
    viewPos.mul(ScreenComponent.PIXELS_PER_M);
    viewPos.sub(getOriginX(), getOriginY());
  }
  
  /**
   * Output box2d position of body if scene2d view position and rotation are as given
   * @param box2DPos (output)
   * @param viewPos - scene2d position of body
   * @param rotation - rotation of body
   */
  public void getBox2DPositionFromViewPosition(Vector2 box2DPos, Vector2 viewPos, float rotation) {
    box2DPos.set(viewPos.x, viewPos.y);
    box2DPos.add(getOriginX(), getOriginY());
    box2DPos.mul(1f / ScreenComponent.PIXELS_PER_M);
  }
  
  public void setPositionFromViewCoords(boolean isUserChange) {
    viewPos.set(getX(), getY());
    getBox2DPositionFromViewPosition(box2DPos, viewPos, getRotation());
    float angle = getRotation() * MathUtils.degreesToRadians;
    if (isUserChange) { // Change initiated by user, hence propagate
      ((AbstractScience2DView) getStage()).notifyLocationChangedByUser(this, box2DPos, angle);
    }
    body.setPositionAndAngle(box2DPos, angle);
    body.setActive(isVisible());
  }
  
  public String getMovementMode() {
    return body.getMovementMode();
  }

  public void setMovementMode(String movementMode) {
    body.setMovementMode(movementMode);
  }

  protected int getRotationForceScaler() {
    return -10000;
  }

  public void prepareActor() {
  }
}
