// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.experiments.model.electromagnetism.EMField.IConsumer;

import edu.colorado.phet.common.phetcommon.model.clock.IClock;

/**
 * Compass is the model of a compass.
 * <p/>
 * Several types of compass behavior can be specified using setBehavior. In the
 * case of KINEMATIC_BEHAVIOR, the compass needle attempts to be physically
 * accurate with respect to force, friction, inertia, etc. Instead of jumping to
 * an orientation, the needle will overshoot, then gradually reach equilibrium.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class Compass extends Body implements IConsumer {

  // ----------------------------------------------------------------------------
  // Class data
  // ----------------------------------------------------------------------------

  // Public interface for specifying behavior.
  public static final int SIMPLE_BEHAVIOR = 0; // see SimpleBehavior
  public static final int INCREMENTAL_BEHAVIOR = 1; // see IncrementalBehavior
  public static final int KINEMATIC_BEHAVIOR = 2; // see KinematicBehavior

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  // Magnet that the compass is observing.
  private AbstractMagnet emField;
  // the clock
  private IClock _clock;
  // The rotation behavior.
  private IBehavior behavior;
  // A reusable point.
  private Vector2 somePoint;
  // A reusable vector
  private Vector2 someVector;
  // Simple behavior, for when clock is paused
  private IBehavior _clockPausedBehavior;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Sole constructor.
   * 
   * @param emField
   */
  public Compass(EMField emField, IClock clock) {
    super();
    emField = emField;
    emField.registerConsumer(this);

    _clock = clock;

    behavior = new ImmediateBehavior(this);
    _clockPausedBehavior = new ImmediateBehavior(this);

    somePoint = new Vector2();
    someVector = new Vector2();

    Vector2 fieldStrength = new Vector2();
    emField.getBField(position, fieldStrength);
    setAngle(fieldStrength.angle());
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Sets the compass behavior.
   * 
   * @param behavior
   *          SIMPLE, INCREMENTAL or KINEMATIC
   * @throws IllegalArgumentException
   *           if rotationStrategy is invalid
   */
  public void setBehavior(int behavior) {
    switch (behavior) {
    case SIMPLE_BEHAVIOR:
      this.behavior = new ImmediateBehavior(this);
      break;
    case INCREMENTAL_BEHAVIOR:
      this.behavior = new IncrementalBehavior(this);
      break;
    case KINEMATIC_BEHAVIOR:
      this.behavior = new KinematicBehavior(this);
      break;
    default:
      throw new IllegalArgumentException("invalid behavior requested: "
          + behavior);
    }
    // No need to notify observers, handled by stepInTime.
  }

  /**
   * Workaround to get the compass moving immediately. In some situations, such
   * as when the magnet polarity is flipped, it can take quite awhile for the
   * magnet to start moving.
   */
  public void startMovingNow() {
    behavior.startMovingNow();
  }

  /**
   * @see edu.colorado.phet.common.phetcommon.util.SimpleObserver#update()
   */
  public void update() {
    // if the clock is running, updates are handled via stepInTime
    if (_clock.isPaused()) {
      getPosition(somePoint /* output */);
      emField.getBField(somePoint, someVector /* output */);
      _clockPausedBehavior.setDirection(someVector, 1 /* don't care */);
    }
  }

  // ----------------------------------------------------------------------------
  // ModelElement implementation
  // ----------------------------------------------------------------------------

  /**
   * If rotational kinematics is enabled (see setRotationalKinematicsEnabled),
   * the compass needle's behavior is based on a Verlet algorithm. The algorithm
   * was reused from edu.colorado.phet.microwave.model.WaterMolecule in Ron
   * LeMaster's "microwaves" simulation, with some minor changes. The algorithm
   * was verified by Mike Dubson.
   * 
   * @see edu.colorado.phet.common.phetcommon.model.ModelElement#singleStep(double)
   */
  public void singleStep(double dt) {
    getPosition(somePoint /* output */);
    emField.getBField(somePoint, someVector /* output */);
    if (someVector.len() != 0) {
      behavior.setDirection(someVector, dt);
    }
  }
}

// ----------------------------------------------------------------------------
// Behaviors
// ----------------------------------------------------------------------------

/**
 * IBehavior is the interface implemented by all compass behaviors.
 */
interface IBehavior {
  /*
   * Sets the compass needle angle.
   * 
   * @param fieldVector the B-field vector at the compass position
   * 
   * @param dt time step, in simulation clock ticks
   */
  public void setDirection(Vector2 fieldVector, double dt);

  /*
   * Starts the compass needle moving immediately.
   */
  public void startMovingNow();
}

/**
 * AbstractBehavior contains a base implementation shared by all behaviors.
 */
abstract class AbstractBehavior implements IBehavior {

  private Compass _compassModel;

  public AbstractBehavior(Compass compassModel) {
    super();
    _compassModel = compassModel;
  }

  public Compass getCompass() {
    return _compassModel;
  }

  public abstract void setDirection(Vector2 fieldVector, double dt);

  public void startMovingNow() {
  }
}

/**
 * ImmediateBehavior immediately sets the compass angle to match the
 * angle of the B-field.
 */
class ImmediateBehavior extends AbstractBehavior {

  public ImmediateBehavior(Compass compassModel) {
    super(compassModel);
  }

  public void setDirection(Vector2 fieldVector, double dt) {
    getCompass().setAngle(fieldVector.angle());
  }
}

/**
 * IncrementalBehavior tracks the B-field exactly, except when the delta angle
 * exceeds some threshold. When the threshold is exceeded, the needle angle
 * changes incrementally over time.
 */
class IncrementalBehavior extends AbstractBehavior {

  private static final double MAX_INCREMENT = Math.toRadians(45);

  public IncrementalBehavior(Compass compassModel) {
    super(compassModel);
  }

  public void setDirection(Vector2 fieldVector, double dt) {

    // Calculate the delta angle
    double fieldAngle = fieldVector.angle();
    double needleAngle = getCompass().getAngle();
    double delta = fieldAngle - needleAngle;

    // Normalize the angle to the range -355...+355 degrees
    if (Math.abs(delta) >= (2 * Math.PI)) {
      int sign = (delta < 0) ? -1 : +1;
      delta = sign * (delta % (2 * Math.PI));
    }

    // Convert to an equivalent angle in the range -180...+180 degrees.
    if (delta > Math.PI) {
      delta = delta - (2 * Math.PI);
    } else if (delta < -Math.PI) {
      delta = delta + (2 * Math.PI);
    }

    if (Math.abs(delta) < MAX_INCREMENT) {
      // If the delta is small, perform simple rotation.
      getCompass().setAngle(fieldAngle);
    } else {
      // If the delta is large, rotate incrementally.
      int sign = (delta < 0) ? -1 : 1;
      delta = sign * MAX_INCREMENT;
      getCompass().setAngle(needleAngle + delta);
    }
  }
}

/**
 * KinematicBehavior rotates the compass needle using the Verlet algorithm to
 * mimic rotational kinematics. The needle must overcome inertia, and it has
 * angular velocity and angular acceleration. This causes the needle to
 * accelerate at it starts to move, and to wobble as it comes to rest.
 */
class KinematicBehavior extends AbstractBehavior {

  private static final double SENSITIVITY = 0.01; // increase this to make the
                                                  // compass more sensitive to
                                                  // smaller fields
  private static final double DAMPING = 0.08; // increase this to make the
                                              // needle wobble less
  private static final double THRESHOLD = Math.toRadians(0.2); // angle at which
                                                               // the needle
                                                               // stops wobbling
                                                               // and snaps to
                                                               // the actual
                                                               // field
                                                               // orientation

  // Angle of needle orientation (in radians)
  private double _theta;
  // Angular velocity, the change in angle over time.
  private double _omega;
  // Angular accelaration, the change in angular velocity over time.
  private double _alpha;

  public KinematicBehavior(Compass compassModel) {
    super(compassModel);
    _theta = _omega = _alpha = 0.0;
  }

  public void setDirection(Vector2 fieldVector, double dt) {

    double magnitude = fieldVector.len();
    double angle = fieldVector.angle();

    // Difference between the field angle and the compass angle.
    double phi = ((magnitude == 0) ? 0.0 : (angle - _theta));

    if (Math.abs(phi) < THRESHOLD) {
      // When the difference between the field angle and the compass angle is
      // insignificant,
      // simply set the angle and consider the compass to be at rest.
      _theta = angle;
      _omega = 0;
      _alpha = 0;
      getCompass().setAngle(_theta);
    } else {
      // Use the Verlet algorithm to compute angle, angular velocity, and
      // angular acceleration.

      // Step 1: orientation
      double thetaOld = _theta;
      double alphaTemp = (SENSITIVITY * Math.sin(phi) * magnitude)
          - (DAMPING * _omega);
      _theta = _theta + (_omega * dt) + (0.5 * alphaTemp * dt * dt);
      if (_theta != thetaOld) {
        // Set the compass needle angle.
        getCompass().setAngle(_theta);
      }

      // Step 2: angular accelaration
      double omegaTemp = _omega + (alphaTemp * dt);
      _alpha = (SENSITIVITY * Math.sin(phi) * magnitude)
          - (DAMPING * omegaTemp);

      // Step 3: angular velocity
      _omega = _omega + (0.5 * (_alpha + alphaTemp) * dt);
    }
  }

  /**
   * Workaround to get the compass moving immediately. In some situations, such
   * as when the magnet polarity is flipped, it can take quite awhile for the
   * magnet to start moving. So we give the compass needle a small amount of
   * angular velocity to get it going.
   */
  public void startMovingNow() {
    _omega = 0.03; // adjust as needed for desired behavior
  }
}
