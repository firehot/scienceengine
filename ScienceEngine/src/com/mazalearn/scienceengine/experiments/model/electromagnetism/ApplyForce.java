package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import java.util.HashMap;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Transform;

/**
 * Class used to apply "magnetic" forces.
 */
static class MagneticSteppable implements Steppable {
   /** Listing of the bodies to apply the effect to. */
  private HashMap<Body, Integer> targets;
   /** Shape indicating the area of effect. Must be a Circle or Polygon and usually a sensor. */
   private Fixture fixture;
  /** Maximum distance square the body could be from the origin and still have the effect applied. */
  private float maxDistSquared;
  /** Maximum effect that could be applied (if distance is 0). */
  private float magnitude;
  /** Whether to attract the bodies to the origin or repel them from it. */
   private boolean attract;

   public MagneticSteppable(final Fixture fixtureToMagnetize, final float maxDist,
         final HashMap<Body, Integer> curntContacts, final float magnitude, final boolean attract) {
      this.fixture = fixtureToMagnetize;
      this.maxDistSquared = maxDist * maxDist; // Square it so we can avoid the squareroot when calc distances.
      this.targets = curntContacts;
      this.magnitude = magnitude;
      this.attract = attract;
   }

   /** Apply the effect to any of the relevant bodies.*/
  public void step(final float dt) {
      Set<Body> contacts = targets.keySet();
      if (contacts.isEmpty()) return;

      // Get the current center of the shape and use as our origin.
      Vector2 origin;
      Shape shape = fixture.getShape();
      if (shape.getType() == Shape.Type.Circle) {
         CircleShape circle = (CircleShape) shape;
         origin = new Vector2(circle.getPosition());
         Transform transform = fixture.getBody().getTransform();
         transform.mul(origin);
         origin.add(transform.getPosition());
      } else if (shape.getType() == Shape.Type.Polygon) {
         PolygonShape poly = (PolygonShape) shape;
//         origin = poly.centroid(fixture.getBody().getXForm());
         origin = poly.centroid(fixture.getBody().getTransform());
      } else {
         // Not a supported shape. Probably a PointShape or EdgeShape.
         return;
      }

      // Loop through all bodies in contact with the field and apply force.
     Vector2 distVec;
      for (Body target : contacts) {
         if (attract) {
            distVec = origin.sub(target.getWorldCenter()); // attract (toward origin)
         } else {
            distVec = target.getWorldCenter().sub(origin); // repel (away from origin)
         }
         // Force equal to magnitude is applied when the center of the body is at maxdist. If is it
         // closer to the center then the force is higher (clamped to prevent infinity), further is
         // less (approaching 0 at maxDistSquared).
        float amt = (magnitude / MathHelper.clampMin(distVec.len2() / maxDistSquared, 0.01f)) * dt;
           if (amt > 0f) {
               distVec.nor();
               // mulLocal since we are done with this Vec (created each time via sub call).
              target.applyForce(distVec.mul(amt), target.getWorldCenter());
           }
      }
  }
}
