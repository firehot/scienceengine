package com.mazalearn.scienceengine.molecule;

import com.mazalearn.scienceengine.molecule.MolecularModel.TemperatureLevel;

public abstract class AbstractMolecularModel {

  private double[] DAMPING = {1.0, 0.9985, 1.0002};
  protected double dt = 0.020;
  protected double dtOver2 = 0.5 * dt;
  protected double dtSquaredOver2 = 0.5 * dt * dt;
  protected static final double WALL_STIFFNESS = 50.0;
  protected static final double GRAVITY = -0.005;
  protected static final double WALL_DISTANCE_THRESHOLD = 1.122462048309373017;
  protected static final double MIN_DISTANCE = WALL_DISTANCE_THRESHOLD * 0.8;
  protected static final double MIN_DISTANCE_SQUARED = MIN_DISTANCE * MIN_DISTANCE;
  protected Molecule[] molecules;
  protected int boxWidth = 50;
  protected int boxHeight = 50;
  protected double temperature = 0.5;
  protected int N = 200; // Number of molecules
  protected double ke;
  protected double pe;
  protected double energy;
  protected double timeElapsed;
  protected TemperatureLevel temperatureLevel = TemperatureLevel.NEUTRAL;

  public AbstractMolecularModel(int boxWidth, int boxHeight, int N,
      double temperature) {
    this.N = N;
    this.boxWidth = boxWidth;
    this.boxHeight = boxHeight;
    this.temperature = temperature;
  }
  
  public void initialize() {
    this.molecules = new Molecule[N];
    this.timeElapsed = 0;
    distributeMoleculesHomogeneously();
    normalizeVelocities();
  }
  
  private void reScaleDt() {
    
    // Set dt to be 1/10 of the average collision time
    // If box has area A, number of particles N, 
    // Average inter particle distance is sqrt(A)/N
    // Average Velocity is sqrt(2*ke/N)
    // Hence timestep = sqrt(A/2*ke*N)/10
    dt = 0.17 * Math.sqrt((boxWidth * boxHeight)/(2 * ke)) / N;
    dtOver2 = dt * 0.5;
    dtSquaredOver2 = dt * dt * 0.5;
  }

  private void distributeMoleculesHomogeneously() {
    double volumePerMolecule = (boxWidth * boxHeight) / N;
    double averageSpacing = Math.sqrt(volumePerMolecule);
    double rows = Math.ceil(boxHeight / averageSpacing);
    double columns = Math.ceil(boxWidth / averageSpacing);
    averageSpacing = Math.min(boxHeight / rows, boxWidth / columns);
    if (averageSpacing < 1.0) {
      averageSpacing = 1.0; // not too close!
    }
    double x, y;
    x = y = averageSpacing / 2.0;
    for (int i = 0; i < N; i++) {
      molecules[i] = new Molecule(x, y);
      x = x + averageSpacing;
      if (x > (boxWidth - WALL_DISTANCE_THRESHOLD)) {
        // if end of row, move down to next row
        x = averageSpacing / 2.0;
        y = y + averageSpacing;
        // ??? if (y > (boxHeight - WALL_DISTANCE_THRESHOLD)) N = i + 1; // if
        // no more room, this one was the last
      }
    }
  }

  private void normalizeVelocities() {
    int nv = 2 * N;
    int ng = nv - 4;
    // Scale the velocity to satisfy the partition theorem
    double ek = 0;
    for (Molecule m: molecules) {
      ek += m.vx * m.vx + m.vy * m.vy;
    }
    double vs = Math.sqrt(1.0 * ek * nv / (ng * temperature)) / 10;
    for (Molecule m: molecules) {
      m.vx /= vs;
      m.vy /= vs;
    }
  }
  
  public Molecule getMolecule(int i) {
    return molecules[i];
  }

  public double getEnergy() {
    return energy;
  }

  public double getSimulatedTime() {
    return timeElapsed;
  }

  public void simulateSteps(int n) {
    for (int i = 0; i < n; i++) {
      singleStep();
    }
    reScaleDt();
  }

  public void setTemperatureLevel(TemperatureLevel temperatureLevel) {
    this.temperatureLevel = temperatureLevel;
  }
  
  public double getTemperature() {
    return temperature;
  }
  
  protected double getGravity() {
    return GRAVITY;
  }

  private void singleStep() {
  
    // Scale velocities up or down
    // vi *= (2-0.999995); or vi *= 0.999995;
    // Update velocities half-way with old acceleration
    double damping = DAMPING[temperatureLevel.level()];
    for (Molecule m: molecules) {
      m.vx *= damping;
      m.vy *= damping;
    }
  
    
    // Update Positions - 2 term Taylor series
    for (Molecule m: molecules) {
      m.x += (m.vx * dt) + (m.ax * dtSquaredOver2);
      m.y += (m.vy * dt) + (m.ay * dtSquaredOver2);
    }
  
    // Update velocities half-way with old acceleration
    for (Molecule m: molecules) {
      m.vx += m.ax * dtOver2;
      m.vy += m.ay * dtOver2;
    }
  
    pe = computeAccelerations();
  
    // Finish updating the velocities with new acceleration
    ke = 0.0;
    for (Molecule m: molecules) {
      m.vx += m.ax * dtOver2;
      m.vy += m.ay * dtOver2;
      ke += (m.vx * m.vx + m.vy * m.vy) / 2;
    }
  
    temperature = ke / N;
    energy = ke + pe;
    timeElapsed += dt;
  }

  protected double computeElasticWallForce(Molecule m) {
    if (m.x < WALL_DISTANCE_THRESHOLD) {
      m.ax = WALL_STIFFNESS * (WALL_DISTANCE_THRESHOLD - m.x);
    } else if (boxWidth - m.x < WALL_DISTANCE_THRESHOLD) {
      m.ax =  WALL_STIFFNESS * (boxWidth - WALL_DISTANCE_THRESHOLD - m.x);
    } else {
      m.ax = 0.0;
    }
    if (m.y < WALL_DISTANCE_THRESHOLD) {
      m.ay = (WALL_STIFFNESS * (WALL_DISTANCE_THRESHOLD - m.y)) + getGravity();
    } else if (boxHeight - m.y < WALL_DISTANCE_THRESHOLD) {
      m.ay = (WALL_STIFFNESS * (boxHeight - WALL_DISTANCE_THRESHOLD - m.y))
          + getGravity();
    } else {
      m.ay = getGravity();
    }
    return 0;
  }

  abstract double computeAccelerations();
}
