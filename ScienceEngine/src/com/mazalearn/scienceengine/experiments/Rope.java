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
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.scenes.scene2d.Group;

public class Rope extends Group {
  private static final int BALL_DIAMETER = 8;
  Body body, mainBody, groundBody;
  public World box2DWorld;
  public int m_iterations = 10;
  public double  m_timeStep = 1.0/30.0;
  MouseJointDef MouseJoint;
  Vector2 mousePVec = new Vector2();
  DistanceJointDef distanceJoint = new DistanceJointDef();
  PrismaticJointDef prismJoint = new PrismaticJointDef();
  private TextureRegion ballTexture;
  
  public Rope(float width, float height) {
    // Initialize the world for Box2D
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);

    prismJoint.lowerTranslation = -2;
    prismJoint.upperTranslation = 2;
    prismJoint.enableLimit = true;
    prismJoint.maxMotorForce = 100;
    prismJoint.motorSpeed = 4.0f;
    prismJoint.enableMotor = false;

    ballTexture = createBallTexture();
    // ceiling
    groundBody = createRopeSegment(null, 8.5f, 8.5f, 0);
    groundBody.setType(BodyType.StaticBody);
    mainBody = body = createRopeSegment(null, 8.5f, 8.5f, 2f);
    
    prismJoint.initialize(groundBody, body, body.getPosition(), new Vector2(0, 10));
    box2DWorld.createJoint(prismJoint);
    
    distanceJoint.frequencyHz = 1.2f;
    distanceJoint.dampingRatio = 0.5f;
    // rope segments 
    for (int i = 1; i <= 20; i++) {
      body = createRopeSegment(body, 8.5f + i, 8.5f, 2f);
    }
    // last segment
    body.setType(BodyType.StaticBody);

    mainBody.applyForce(new Vector2(0f, 50f), mainBody.getPosition());

    Vector2 WORLD_SIZE = new Vector2(10, 10);
    float pixelsPerMetre = width / WORLD_SIZE.x;
    Box2DAction action = new Box2DAction(box2DWorld, mainBody, pixelsPerMetre, false);
    this.action(action);
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
    Body body=box2DWorld.createBody(bodyDef);
    body.createFixture(ballDef);
    // joint
    if (previousBody != null) {
      distanceJoint.initialize(previousBody, body, previousBody.getPosition(), body.getPosition());
      box2DWorld.createJoint(distanceJoint);
    }
    Box2DActor actor = new Box2DActor(body, ballTexture);
    this.addActor(actor);
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
  }
  /*
    public void createMouse(MouseEvent evt) {
      Body body = GetBodyAtMouse();
      if (body) {
        MouseJointDef mouseJointDef = new MouseJointDef();
        mouseJointDef.body1 = box2DWorld.GetGroundBody();
        mouseJointDef.body2 = body;
        mouseJointDef.target.Set(mouseX/30, mouseY/30);
        mouseJointDef.maxForce = 30000;
        mouseJointDef.timeStep = m_timeStep;
        MouseJoint mouseJoint = box2DWorld.createJoint(mouseJointDef);
      }
    }
    public void destroyMouse(MouseEvent evt) {
      if (mouseJoint) {
        box2DWorld.DestroyJoint(mouseJoint);
        mouseJoint = null;
      }
    }
    public Body getBodyAtMouse(boolean includeStatic) {
      var mouseXWorldPhys = (mouseX)/30;
      var mouseYWorldPhys = (mouseY)/30;
      mousePVec.Set(mouseXWorldPhys, mouseYWorldPhys);
      AABB aabb = new AABB();
      aabb.lowerBound.Set(mouseXWorldPhys - 0.001, mouseYWorldPhys - 0.001);
      aabb.upperBound.Set(mouseXWorldPhys + 0.001, mouseYWorldPhys + 0.001);
      int k_maxCount = 10;
      Array shapes = new Array();
      int count = box2DWorld.Query(aabb,shapes,k_maxCount);
      Body body = null;
      for (int i = 0; i < count; ++i) {
        if (shapes[i].GetBody().IsStatic()==false||includeStatic) {
          Shape tShape = shapes[i];
          Boolean inside = tShape.TestPoint(tShape.GetBody().GetXForm(), mousePVec);
          if (inside) {
            body=tShape.GetBody();
            break;
          }
        }
      }
      return body;
    }
    public void Update(Event e) {
      box2DWorld.Step(m_timeStep, m_iterations);
      if (mouseJoint) {
        var mouseXWorldPhys = mouseX/30;
        var mouseYWorldPhys = mouseY/30;
        Vector2 p2 = new Vector2(mouseXWorldPhys, mouseYWorldPhys);
        mouseJoint.SetTarget(p2);
      }
 
    }
    */
  }
