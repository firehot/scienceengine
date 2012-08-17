package com.mazalearn.scienceengine.experiments.model.molecule;

public class SimpleMolecularModel extends AbstractMolecularModel implements MolecularModel {

  // Cutoff distance beyond which particles do not interact
  private static final double DISTANCE_CUTOFF = 2.5;
  private static final double DISTANCE_CUTOFF_SQUARED = DISTANCE_CUTOFF * DISTANCE_CUTOFF;
  protected static final double GRAVITY = -0.003;
  
  SimpleMolecularModel(int boxWidth, int boxHeight, int N, double temperature) {
    super(boxWidth, boxHeight, N, temperature);
  }

  protected double getGravity() {
    return GRAVITY;
  }

  double computeAccelerations() {
    
    double potentialEnergy = 0.0;
    // first check for bounces off walls, and include GRAVITY (if any):
    for (Molecule m: molecules) {
      potentialEnergy += computeElasticWallForce(m);
    }
  
    Molecule m[] = molecules;
    // now compute interaction forces (Lennard-Jones potential):
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < i; j++) {
        double dx = m[i].x - m[j].x;
        double dy = m[i].y - m[j].y;
        double r2 = dx * dx + dy * dy;
        if (r2 < DISTANCE_CUTOFF_SQUARED) {
          double fx, fy, pe;
          final double damping = 0.5; // 0.5;
          fx = damping * (m[j].vx - m[i].vx) / dt;
          fy = damping * (m[j].vy - m[i].vy) / dt;
          pe = 0;
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
}
