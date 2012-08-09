package com.mazalearn.scienceengine.molecule;

public interface MolecularModel {

  public abstract void initialize();

  public abstract void simulateSteps(int n);

  public abstract double getSimulatedTime();

  public abstract double getEnergy();

  public abstract Molecule getMolecule(int i);
  
  public abstract double getTemperature();

  public abstract void setTemperatureLevel(TemperatureLevel temperatureLevel);

  public enum TemperatureLevel {
    NEUTRAL(0), COLD(1), HOT(2);
    private int level;
    private TemperatureLevel(int level) { this.level = level; }
    public int level() { return level; }
  }
}