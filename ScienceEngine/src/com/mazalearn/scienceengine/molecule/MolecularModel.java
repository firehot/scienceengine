package com.mazalearn.scienceengine.molecule;

public interface MolecularModel {

  public abstract void initialize();

  public abstract void simulateSteps(int n);

  public abstract double getSimulatedTime();

  public abstract double getEnergy();

  public abstract Molecule getMolecule(int i);
  
  public abstract double getTemperature();

}