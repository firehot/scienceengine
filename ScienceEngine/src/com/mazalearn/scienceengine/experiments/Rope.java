package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.scenes.scene2d.Group;

public class Rope extends Group {
  private static final float ORIGIN_Y = 5.5f;
  private static final float ORIGIN_X = 1.5f;
  private static final int BALL_DIAMETER = 8;
  Body body, endBody, groundBody;
  public World box2DWorld;
  DistanceJointDef distanceJoint = new DistanceJointDef();
  PrismaticJoint prismJoint;
  private TextureRegion ballTexture;
  
  public Rope(float width, float height) {
    // Initialize the world for Box2D
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);

    ballTexture = createBallTexture();
    // ceiling
    groundBody = createRopeSegment(null, ORIGIN_X, ORIGIN_Y, 0);
    groundBody.setType(BodyType.StaticBody);
    endBody = body = createRopeSegment(null, ORIGIN_X, ORIGIN_Y, 2f);
    
    // Anchor the end and provide a motor to oscillate it
    prismJoint = createPrismaticJoint(groundBody, endBody); 
    
    distanceJoint.frequencyHz = 1.2f;
    distanceJoint.dampingRatio = 0.5f;
    // rope segments 
    for (int i = 1; i <= 30; i++) {
      body = createRopeSegment(body, ORIGIN_X + (float)(i*1.5), ORIGIN_Y, 2f);
    }
    // last segment
    body.setType(BodyType.StaticBody);

    Vector2 WORLD_SIZE = new Vector2(10, 10);
    float pixelsPerMetre = width / WORLD_SIZE.x;
    Box2DAction action = new Box2DAction(box2DWorld, endBody, pixelsPerMetre, false);
    this.action(action);
  }

  private PrismaticJoint createPrismaticJoint(Body body1, Body body2) {
    PrismaticJointDef prismJointDef = new PrismaticJointDef();
    prismJointDef.lowerTranslation = -3;
    prismJointDef.upperTranslation = 3;
    prismJointDef.enableLimit = true;
    prismJointDef.maxMotorForce = 1000;
    prismJointDef.motorSpeed = 1.0f;
    prismJointDef.enableMotor = true;
    prismJointDef.initialize(body1, body2, body2.getPosition(), new Vector2(0, 1));
    return (PrismaticJoint) box2DWorld.createJoint(prismJointDef);
  }

  private TextureRegion createBallTexture() {
    // Create texture region for ball
    Pixmap pixmap = new Pixmap(BALL_DIAMETER, BALL_DIAMETER, Format.RGBA8888);
    pixmap.setColor(Color.RED);
    pixmap.fillCircle(BALL_DIAMETER/2, BALL_DIAMETER/2, BALL_DIAMETER/2);
    TextureRegion ballTexture = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return ballTexture;
  }

  private Body createRopeSegment(Body previousBody, float x, float y, float density) {
    // rope segment
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.x = x;
    bodyDef.position.y = y;
    FixtureDef ballDef = new FixtureDef();
   
    CircleShape ballShape = new CircleShape();
    ballShape.setRadius(0.5f);
    ballDef.density = density;
    ballDef.friction = 0; // 0.5f;
    ballDef.restitution = 0.2f;
    ballDef.shape = ballShape;
    Body body = box2DWorld.createBody(bodyDef);
    body.createFixture(ballDef);
    // joint
    if (previousBody != null) {
      distanceJoint.initialize(previousBody, body, previousBody.getPosition(), body.getPosition());
      box2DWorld.createJoint(distanceJoint);
    }
    if (density != 0) {
      Box2DActor actor = new Box2DActor(body, ballTexture);
      this.addActor(actor);
    }
    return body;
  }
    
  /**
   * Draw and advance world
   * 
   */
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);    
    box2DWorld.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
    if (prismJoint.getJointTranslation() >= prismJoint.getUpperLimit()) {
      prismJoint.setMotorSpeed(-prismJoint.getMotorSpeed());
    } else if (prismJoint.getJointTranslation() <= prismJoint.getLowerLimit()) {
      prismJoint.setMotorSpeed(-prismJoint.getMotorSpeed());
    }
  }
}
