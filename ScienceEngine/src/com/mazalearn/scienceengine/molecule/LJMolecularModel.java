package com.mazalearn.scienceengine.molecule;

public class LJMolecularModel extends AbstractMolecularModel implements MolecularModel {
  // Assumptions
  // LJ_EPSILON = 1.0 = Depth of potential well
  // LJ_SIGMA = 1.0 = Distance at which inter-particle potential is 0

  // Cutoff distance beyond which Force is not calculated
  protected static final double LJ_CUTOFF = 2.5;
  protected static final double LJ_CUTOFF_SQUARED = LJ_CUTOFF * LJ_CUTOFF;
  // Truncation correction for LJ_CUTOFF=2.5 * LJ_SIGMA
  protected static final double LJ_CUTOFF_CORRECTION = 0.016316891136;
  // Truncation correction for LJ_CUTOFF=3.5 * LJ_SIGMA is 0.00217478039165499;

  static final double MIN_LJ_FORCE_OVER_R = 48/Math.pow(MIN_DISTANCE, 14) - 24/Math.pow(MIN_DISTANCE, 8);
  private static final double[] DAMPING_FORCE_OVER_R = 
      {0, 0.02 * MIN_LJ_FORCE_OVER_R, 0.005 * MIN_LJ_FORCE_OVER_R};
  static final double MIN_LJ_POTENTIAL_ENERGY = 4/Math.pow(MIN_DISTANCE, 12) - 1/Math.pow(MIN_DISTANCE, 6) + LJ_CUTOFF_CORRECTION;
  
  public LJMolecularModel(int boxWidth, int boxHeight, int N, double temperature) {
    super(boxWidth, boxHeight, N, temperature);
  }

  double computeAccelerations() {
    
    double potentialEnergy = 0.0;
    potentialEnergy += computeWallForces();
    potentialEnergy += computeInterMolecularForces();
    return potentialEnergy;
  }

  private double computeInterMolecularForces() {
    double potentialEnergy = 0;
    Molecule m[] = molecules;
    // now compute interaction forces (Lennard-Jones potential):
    double dampingForceOverR = DAMPING_FORCE_OVER_R[temperatureLevel.level()];
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < i; j++) {
        double dx = m[i].x - m[j].x;
        double dy = m[i].y - m[j].y;
        double r2 = dx * dx + dy * dy;
        if (r2 < LJ_CUTOFF_SQUARED) {
          double fx, fy, pe;
          if (r2 < MIN_DISTANCE_SQUARED ) { // Precomputed for min distance
            fx = MIN_LJ_FORCE_OVER_R * dx;
            fy =  MIN_LJ_FORCE_OVER_R * dy;
            pe = MIN_LJ_POTENTIAL_ENERGY;
          } else { // Compute afresh
            double r2inv = 1.0 / r2;
            double r6inv = r2inv * r2inv * r2inv;
            double ljForceOverR = 48.0 * (r6inv - 0.5) * r6inv * r2inv;
            ljForceOverR -= dampingForceOverR * temperature / 4;
            fx = ljForceOverR * dx;
            fy = ljForceOverR * dy;
            pe = 4 * r6inv * (r6inv - 1) + LJ_CUTOFF_CORRECTION;
          }
          m[i].ax += fx; // add this force on to i's acceleration (mass = 1)
          m[i].ay += fy;
          m[j].ax -= fx; // Newton's 3rd law reaction force
          m[j].ay -= fy;
          potentialEnergy += pe;
        }
      }
    }
    return potentialEnergy;
  }
  
  private double computeWallForces() {
    double potentialEnergy = 0;
    // Check for bounces off walls, and include GRAVITY (if any):
    for (Molecule m: molecules) {
      potentialEnergy += computeLJWallForce(m);
    }
    return potentialEnergy;
  }

  /**
   * Calculate the force exerted on a particle at the provided position by the
   * walls of the container. The result is returned in the provided vector.
   * Only calculate the force if the particle is inside the container ???
   * 
   * @param m - Molecule
   */
  protected double computeLJWallForce(Molecule m) {
    double pe = 0;

    // Calculate the force in the X direction if close enough to either wall
    // Limit the distance, and thus the force, if we are really close.
    if (m.x < WALL_DISTANCE_THRESHOLD) {
      double dx = Math.max(m.x, MIN_DISTANCE);
      m.ax = 48 / Math.pow(dx, 13) - 24 / Math.pow(dx, 7);
      pe += 4 / Math.pow(dx, 12) - 4 / Math.pow(dx, 6) + 1;
    } else if (boxWidth - m.x < WALL_DISTANCE_THRESHOLD) {
      // Close enough to the right wall to feel the force.
      double dx = Math.max(boxWidth - m.x, MIN_DISTANCE);
      m.ax = -(48 / Math.pow(dx, 13) - 24 / Math.pow(dx, 7));
      pe += 4 / Math.pow(dx, 12) - 4 / Math.pow(dx, 6) + 1;
    } else {
      m.ax = 0;
    }

    // Calculate the force in the Y direction.
    if (m.y < WALL_DISTANCE_THRESHOLD) {
      // Close enough to the bottom wall to feel the force.
      double dy = Math.max(m.y, MIN_DISTANCE);
      m.ay = 48 / Math.pow(dy, 13) - 24 / Math.pow(dy, 7) + GRAVITY;
      pe += 4 / Math.pow(dy, 12) - 4 / Math.pow(dy, 6) + 1;
    } else if (boxHeight - m.y < WALL_DISTANCE_THRESHOLD) {
      // Close enough to the top to feel the force.
      double dy = Math.max(boxHeight - m.y, MIN_DISTANCE);
      m.ay = -(48 / Math.pow(dy, 13) - 24 / Math.pow(dy, 7)) + GRAVITY;
      pe += 4 / Math.pow(dy, 12) - 4 / Math.pow(dy, 6) + 1;
    } else {
      m.ay = GRAVITY;
    }
    return pe;
  }
  
}
