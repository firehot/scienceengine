package com.mazalearn.scienceengine.molecule;

public class Molecule {

  public double x,y,vx,vy,ax,ay;  // position, velocity, acceleration
  public Molecule nextMoleculeInCell;
  
  // construct a new molecule at the given position, with random velocity:
  Molecule(double x, double y) {
      this.x = x;
      this.y = y;
      this.vx = Math.random() - 0.5;
      this.vy = Math.random() - 0.5;
      this.ax = 0.0;
      this.ay = 0.0;
   }    
}
