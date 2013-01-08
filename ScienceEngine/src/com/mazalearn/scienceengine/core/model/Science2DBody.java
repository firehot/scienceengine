
package com.mazalearn.scienceengine.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Parameter;

/**
 * Science2DBody has basic attributes for an element in an EM Field.
 * We would like to inherit from box2D body but that is technically a challenge - 
 * so we wrap the box2D body interface with a static proxy.
 * 
 * @author sridhar
 */
public class Science2DBody implements IBody {
  public enum MovementMode {Move, Rotate, None}

  // Must be set before using this class
  private static World box2DWorld;
  // Ground body
  private static Body GROUND;
  // Model in which this body lives
  private IScience2DModel model;
  // Body instance to which methods are proxied
  Body body;
  // Configs exposed by this body
  protected List<IModelConfig<?>> configs;

  // Used for temporary work
  private final Vector2 aPosition = new Vector2();
  private IComponentType componentType;
  // count of this body among bodies of same type - starts from 1
  private int count;
  // Body type specific initialization information
  private String extra;
  private MovementMode movementMode = MovementMode.Move;
  private RevoluteJointDef rotationJointDef = new RevoluteJointDef();
  private Joint rotationJoint;
  // Detecting # revolutions
  float prevAngularVelocity = 0;
  protected float angleCovered = 0;
  private float prevAngle;
  
  protected Science2DBody(IComponentType componentType, float x, float y, float angle) {
    this.componentType = componentType;
    BodyDef bodyDef = new BodyDef();
    bodyDef.position.set(x, y);
    bodyDef.angle = angle;
    bodyDef.type = BodyDef.BodyType.StaticBody;
    bodyDef.active = false;
    this.body = box2DWorld.createBody(bodyDef);
    this.configs = new ArrayList<IModelConfig<?>>();
  }
  
  /**
   * When there are multiple instances of same component type, 
   * the count of the instances may be set to differentiate them.
   * @param count
   */
  public void setCount(int count) {
    this.count = count;
  }

  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.RotationVelocity, -10, 10) {
      public Float getValue() { return getAngularVelocity(); }
      public void setValue(Float value) { setAngularVelocity(value); }
      public boolean isPossible() { return isActive() &&  MovementMode.Rotate.name().equals(getMovementMode()); }
    });
  }

  public List<IModelConfig<?>> getConfigs() {
    return Collections.unmodifiableList(configs);
  }
  
  public void singleStep(float dt) {
    float delta = body.getAngle() - prevAngle;
    // Cover discontinuity of 0 being equal to 2*pi
    if (delta > MathUtils.PI){
      delta -= 2 * MathUtils.PI;
    } else if (delta < -MathUtils.PI){
      delta += 2 * MathUtils.PI;
    }
    angleCovered += delta;
    prevAngle = body.getAngle();
  }
  
  public int getNumRevolutions() {
    float numRevs = angleCovered / (2 * MathUtils.PI);
    return (int) (numRevs < 0 ? Math.ceil(numRevs) : Math.floor(numRevs));
  }
  
  public void reset() {
    this.setAngularVelocity(0);
    this.setLinearVelocity(Vector2.Zero);
  }
  
  /**
   * Sets the position and angle in 2D space.
   * 
   * @param position - the position
   * @param angle - the orientation in radians
   */
  public void setPositionAndAngle(Vector2 position, float angle) {
    // Ensure angle is in range 0, 2 * pi
    angle = angle % (2 * MathUtils.PI);
    if (angle != body.getAngle() || !position.equals(body.getPosition())) {
      body.setTransform(position, angle);
      if (this instanceof IMagneticField.Producer && isActive()) {
        getModel().notifyFieldChange();
      }
    }
  }

  /**
   * Sets the position in 2D space.
   * 
   * @param x position X coordinate
   * @param y position Y coordinate
   * @param angle - the orientation
   */
  public void setPositionAndAngle(float x, float y, float angle) {
    aPosition.set(x, y);
    this.setPositionAndAngle(aPosition, angle);
  }

  public static World getBox2DWorld() {
    return box2DWorld;
  }

  public static void setBox2DWorld(World box2DWorld) {
    Science2DBody.box2DWorld = box2DWorld;
    Science2DBody.GROUND = box2DWorld.createBody(new BodyDef());
  }
  
  public static Body getGround() {
    return Science2DBody.GROUND;
  }
  
  public String getComponentTypeName() {
    return componentType.name();
  }

  // name of body
  public String name() {
    return componentType.name() + (count == 0 ? "" : "." + count);
  }
  
  // localized name of body
  public String toString() {
    return componentType.toString() + (count == 0 ? "" : " " + count);
  }

  public IComponentType getComponentType() {
    return componentType;
  }
  
  public Body getBody() {
    return body;
  }
  
  public IScience2DModel getModel() {
    return this.model;
  }
  
  public void setModel(IScience2DModel model) {
    this.model = model;
  }

  public String getMovementMode() {
    return movementMode.name();
  }

  public void setMovementMode(String mode) {
    setMovementMode(MovementMode.valueOf(mode));
  }

  private void setMovementMode(MovementMode movementMode) {
    this.movementMode = movementMode;
    if (rotationJoint != null) {
      getModel().getBox2DWorld().destroyJoint(rotationJoint);
      rotationJoint = null;
    }
    if (this.movementMode == MovementMode.Rotate) {
      rotationJointDef.initialize(getBody(), Science2DBody.getGround(), 
          getWorldPoint(Vector2.Zero));
      rotationJoint = 
          getModel().getBox2DWorld().createJoint(rotationJointDef);
    }
  }
  
  public IModelConfig<?> findConfig(Parameter parameter) {
    for (IModelConfig<?> config: getConfigs()) {
      if (config.getParameter().equals(parameter)) {
        return config;
      }
    }
    return null;
  }

  public boolean allowsConfiguration() {
    return true;
  }
  
  public String getExtra() {
    return extra;
  }

  public void setExtra(String extra) {
    this.extra = extra;
  }
  //////////////////////////////////////////////////////////////////////////
  ///  Static Proxy envelope for Box2D body
  //////////////////////////////////////////////////////////////////////////
  @Override
  public Fixture createFixture(FixtureDef def) {
    return body.createFixture(def);
  }

  @Override
  public Fixture createFixture(Shape shape, float density) {
    return body.createFixture(shape, density);
  }

  @Override
  public void destroyFixture(Fixture fixture) {
    body.destroyFixture(fixture);
  }

  @Override
  public void setTransform(Vector2 position, float angle) {
    body.setTransform(position, angle);
  }

  @Override
  public void setTransform(float x, float y, float angle) {
    body.setTransform(x, y, angle);
  }

  @Override
  public Transform getTransform() {
    return body.getTransform();
  }

  @Override
  public Vector2 getPosition() {
    return body.getPosition();
  }

  @Override
  public float getAngle() {
    float angle = body.getAngle();
    while (angle < 0) {
      angle += 2 * MathUtils.PI;
    }  
    return angle % (2 * MathUtils.PI);
  }

  @Override
  public Vector2 getWorldCenter() {
    return body.getWorldCenter();
  }

  @Override
  public Vector2 getLocalCenter() {
    return body.getLocalCenter();
  }

  @Override
  public void setLinearVelocity(Vector2 v) {
    body.setLinearVelocity(v);
  }

  @Override
  public void setLinearVelocity(float vX, float vY) {
    body.setLinearVelocity(vX, vY);
  }

  @Override
  public Vector2 getLinearVelocity() {
    return body.getLinearVelocity();
  }

  @Override
  public void setAngularVelocity(float omega) {
    body.setAngularVelocity(omega);
  }

  @Override
  public float getAngularVelocity() {
    return body.getAngularVelocity();
  }

  @Override
  public void applyForce(Vector2 force, Vector2 point) {
    body.applyForce(force, point);
  }

  @Override
  public void applyForce(float forceX, float forceY, float pointX, float pointY) {
    body.applyForce(forceX, forceY, pointX, pointY);
  }

  @Override
  public void applyForceToCenter(Vector2 force) {
    body.applyForceToCenter(force);
  }

  @Override
  public void applyForceToCenter(float forceX, float forceY) {
    body.applyForceToCenter(forceX, forceY);
  }

  @Override
  public void applyTorque(float torque) {
    body.applyTorque(torque);
  }

  @Override
  public void applyLinearImpulse(Vector2 impulse, Vector2 point) {
    body.applyLinearImpulse(impulse, point);
  }

  @Override
  public void applyLinearImpulse(float impulseX, float impulseY, float pointX,
      float pointY) {
    body.applyLinearImpulse(impulseX, impulseY, pointX, pointY);
  }

  @Override
  public void applyAngularImpulse(float impulse) {
    body.applyAngularImpulse(impulse);
  }

  @Override
  public float getMass() {
    return body.getMass();
  }

  @Override
  public float getInertia() {
    return body.getInertia();
  }

  @Override
  public MassData getMassData() {
    return body.getMassData();
  }

  @Override
  public void setMassData(MassData data) {
    body.setMassData(data);
  }

  @Override
  public void resetMassData() {
    body.resetMassData();
  }

  @Override
  public Vector2 getWorldPoint(Vector2 localPoint) {
    return body.getWorldPoint(localPoint);
  }

  @Override
  public Vector2 getWorldVector(Vector2 localVector) {
    return body.getWorldVector(localVector);
  }

  @Override
  public Vector2 getLocalPoint(Vector2 worldPoint) {
    return body.getLocalPoint(worldPoint);
  }

  @Override
  public Vector2 getLocalVector(Vector2 worldVector) {
    return body.getLocalVector(worldVector);
  }

  @Override
  public Vector2 getLinearVelocityFromWorldPoint(Vector2 worldPoint) {
    return body.getLinearVelocityFromWorldPoint(worldPoint);
  }

  @Override
  public Vector2 getLinearVelocityFromLocalPoint(Vector2 localPoint) {
    return body.getLinearVelocityFromLocalPoint(localPoint);
  }

  @Override
  public float getLinearDamping() {
    return body.getLinearDamping();
  }

  @Override
  public void setLinearDamping(float linearDamping) {
    body.setLinearDamping(linearDamping);
  }

  @Override
  public float getAngularDamping() {
    return body.getAngularDamping();
  }

  @Override
  public void setAngularDamping(float angularDamping) {
    body.setAngularDamping(angularDamping);
  }

  @Override
  public void setType(BodyType type) {
    body.setType(type);
  }

  @Override
  public BodyType getType() {
    return body.getType();
  }

  @Override
  public void setBullet(boolean flag) {
    body.setBullet(flag);
  }

  @Override
  public boolean isBullet() {
    return body.isBullet();
  }

  @Override
  public void setSleepingAllowed(boolean flag) {
    body.setSleepingAllowed(flag);
  }

  @Override
  public boolean isSleepingAllowed() {
    return body.isSleepingAllowed();
  }

  @Override
  public void setAwake(boolean flag) {
    body.setAwake(flag);
  }

  @Override
  public boolean isAwake() {
    return body.isAwake();
  }

  @Override
  public void setActive(boolean flag) {
    body.setActive(flag);
  }

  @Override
  public boolean isActive() {
    return body.isActive();
  }

  @Override
  public void setFixedRotation(boolean flag) {
    body.setFixedRotation(flag);
  }

  @Override
  public boolean isFixedRotation() {
    return body.isFixedRotation();
  }

  @Override
  public ArrayList<Fixture> getFixtureList() {
    return body.getFixtureList();
  }

  @Override
  public ArrayList<JointEdge> getJointList() {
    return body.getJointList();
  }

  @Override
  public float getGravityScale() {
    return body.getGravityScale();
  }

  @Override
  public void setGravityScale(float scale) {
    body.setGravityScale(scale);
  }

  @Override
  public World getWorld() {
    return body.getWorld();
  }

  @Override
  public Object getUserData() {
    return body.getUserData();
  }

  @Override
  public void setUserData(Object userData) {
    body.setUserData(userData);
  }
}