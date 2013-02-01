package com.mazalearn.scienceengine.domains.statesofmatter.model;

import java.util.LinkedList;
import java.util.List;

/**
 * The LJMoleculeBox holds many molecules and implements an integration
 * strategy based on the The Lennard Johnson potential function.
 * This potential function has a distance cutoff LJ_CUTOFF beyond which
 * interaction is assumed to be negligible. It is repulsive when molecules are 
 * very close and attractive when molecules are further away.
 * 
 * Strategy:
 * Since N molecules can result in an N^2 integration which is expensive, we 
 * will use a cell based approach to reduce this to O(N).
 * The Box is divided into square cells of length CELL_SIZE.
 * Each molecule belongs to a unique cell. Molecules in a cell will interact
 * only with molecules in neighbouring cells.
 * 
 * Data Structure:
 * We will maintain an Interaction List of cells which need to interact with
 * each other. A cell also interacts with itself.
 * The molecules inside a cell will be maintained in a linked list
 * with each cell pointing to its first molecule and each molecule pointing to 
 * the next one.
 * 
 * Algorithm:
 * Clear out all cells and all molecules.
 * for each molecule m do
 *   find cell for molecule and add to the cell's list
 *   m.force = force from boundary on m + force from gravity (if any)
 * od
 * for each pair of interaction cells c1, c2 do
 *   for each distinct pair of molecules m1 in c1 and m2 in c2 do
 *     if distance(m1, m2) < LJ_CUTOFF then
 *       f1 = force on m1 by m2
 *       m1.force += f1
 *       m2.force -= f1
 *     fi
 *   od
 * od
 * @author sridhar
 *
 */

public class CellularLJMoleculeBox extends LJMoleculeBox {
  // Assumptions
  // LJ_EPSILON = 1.0 = Depth of potential well
  // LJ_SIGMA = 1.0 = Distance at which inter-particle potential is 0

  private static final double CELL_SIZE = LJ_CUTOFF * 3;
  // Integration data structures
  private List<Cell[]> neighbourCells;
  private int nXCells;
  private int nYCells;
  private Cell[][] cells;

  CellularLJMoleculeBox(int boxWidth, int boxHeight, int N, double temperature) {
    super(boxWidth, boxHeight, N, temperature);
  }
  
  @Override
  public void reset() {
    super.reset();
    this.neighbourCells = createNeighbourCells();
  }
  
  // compute accelerations of all molecules from current positions, using
  // Lennard-Jones force law:
  // return potential Energy
  @Override
  double computeAccelerations() {
    double potentialEnergy;
    
    potentialEnergy = computeWallForces();
    assignMoleculesToCells();   
    potentialEnergy += computeForcesWithinCells();
    potentialEnergy += computeForcesWithNeighbourCells();
    
    return potentialEnergy;
  }

  // first check for bounces off walls, and include GRAVITY (if any):
  private double computeWallForces() {
    double potentialEnergy = 0;
    for (Molecule m: molecules) {
      potentialEnergy += computeLJWallForce(m);
    }
    return potentialEnergy;
  }

  // Interact molecules in each cell with molecules in neighbouring cells
  private double computeForcesWithNeighbourCells() {
    double potentialEnergy = 0;
    for (Cell[] cellPair: neighbourCells) {     
      if (cellPair[1].firstMolecule == null) continue;  // If cell2 is empty, skip
      
      for (Molecule m1 = cellPair[0].firstMolecule; m1 != null; m1 = m1.nextMoleculeInCell) {
        for (Molecule m2 = cellPair[1].firstMolecule; m2 != null; m2 = m2.nextMoleculeInCell) {
          double pe = computeForceBetweenMolecules(m1, m2);
          if (pe != 0) {
            potentialEnergy += pe;
          }
        }
      }
    }
    return potentialEnergy;
  }

  // Interact molecules within each cell
  private double computeForcesWithinCells() {
    double potentialEnergy = 0;
    
    for (int i = 0; i < nXCells; i++) {
      for (int j = 0; j < nYCells; j++) {
        for (Molecule m1 = cells[i][j].firstMolecule; m1 != null; m1 = m1.nextMoleculeInCell) {
          for (Molecule m2 = m1.nextMoleculeInCell; m2 != null; m2 = m2.nextMoleculeInCell) {
            double pe = computeForceBetweenMolecules(m1, m2);
            if (pe != 0) {
              potentialEnergy += pe;
            }
          }
        }
      }
    }
    return potentialEnergy;
  }

  private void assignMoleculesToCells() {
    // Make all cells empty - no molecules
    for (int i = 0; i < nXCells; i++) {
      for (int j = 0; j < nYCells; j++) {
        cells[i][j].firstMolecule = null;
      }
    }
    
    // Assign molecules to cells - each molecule has a unique cell
    for (Molecule m: molecules) {
      int xCell = (int) Math.floor(m.x / CELL_SIZE);
      if (xCell < 0) xCell = 0;
      if (xCell >= nXCells) xCell = nXCells - 1;
      int yCell = (int) Math.floor(m.y / CELL_SIZE);
      if (yCell < 0) yCell = 0;
      if (yCell >= nYCells) yCell = nYCells - 1;
      m.nextMoleculeInCell = cells[xCell][yCell].firstMolecule;
      cells[xCell][yCell].firstMolecule = m;
    }
  }

  /**
   * Interact two given molecules m1 and m2 - find force exerted by one on 
   * another and corresponding potential energy.
   * @param m1 - molecule
   * @param m2 - molecule
   * @return potential energy
   */
  private double computeForceBetweenMolecules(Molecule m1, Molecule m2) {
    double potentialEnergy = 0;
    double dx = m1.x - m2.x;
    double dy = m1.y - m2.y;
    double r2 = dx * dx + dy * dy;
    if (r2 >= LJ_CUTOFF_SQUARED) {
      return 0.0;
    }
    if ( r2 < MIN_DISTANCE_SQUARED ) {
      r2 = MIN_DISTANCE_SQUARED;
    }
    double r2inv = 1.0 / r2;
    double r6inv = r2inv * r2inv * r2inv;
    double ljForceOverR = 48.0 * (r6inv - 0.5) * r6inv * r2inv;
    double fx = ljForceOverR * dx;
    double fy = ljForceOverR * dy;
    m1.ax += fx; // add this force on to i's acceleration (mass = 1)
    m1.ay += fy;
    m2.ax -= fx; // Newton's 3rd law reaction force
    m2.ay -= fy;
    potentialEnergy += 4 * r6inv * (r6inv - 1);
    potentialEnergy += LJ_CUTOFF_CORRECTION;
    return potentialEnergy;
  }

  private List<Cell[]> createNeighbourCells() {
    List<Cell[]> neighbourCells = new LinkedList<Cell[]>();
    
    nXCells = (int) Math.ceil(boxWidth / CELL_SIZE);
    nYCells = (int) Math.ceil(boxHeight / CELL_SIZE);
    
    cells = new Cell[nXCells][nYCells];
    for (int i = 0; i < nXCells; i++) {
      for (int j = 0; j < nYCells; j++) {
        cells[i][j] = new Cell();
      }
    }
    
    // Identify pairs of cells which will interact
    for (int i = 0; i < nXCells; i++) {
      for (int j = 0; j < nYCells; j++) {
        if (i + 1 < nXCells) {
          neighbourCells.add(new Cell[]{cells[i][j], cells[i + 1][j]});
        }
        if (j + 1 < nYCells) {
          neighbourCells.add(new Cell[]{cells[i][j], cells[i][j + 1]});
          if (i - 1 >= 0) {
            neighbourCells.add(new Cell[]{cells[i][j], cells[i - 1][j + 1]});
          }
          if (i + 1 < nYCells) {
            neighbourCells.add(new Cell[]{cells[i][j], cells[i + 1][j + 1]});
          }
        }
      }
    }
    return neighbourCells;
  }

}
