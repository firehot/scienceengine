// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.app.utils.Dimension;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;

/**
 * Model of a bar magnet that uses a grid of precomputed B-field values.
 * <p/>
 * It was not feasible to implement a numerical model directly in Java, as it
 * relies on double integrals. So the model was implemented in MathCAD, and
 * MathCAD was used to create 3 grids of B-field vectors. The MathCAD model can
 * be found at faraday/doc/BarMagnet-MathCAD.pdf.
 * <p/>
 * The 3 B-field grids are:
 * <ul>
 * <li>internal: field internal to the magnet
 * <li>external-near: field near the magnet
 * <li>external-far: field far from the magnet
 * </ul>
 * <p/>
 * In order to model the discontinuity that appears at the top and bottom magnet
 * edges, internal and external-near have points that lie exactly on those
 * edges, and have different values for those points.
 * <p/>
 * The external-far grid is a sparse grid, and provides an approximate B-field
 * for use by the compass.
 * <p/>
 * The 3 grids overlap such that external-near contains internal, and
 * external-far contains external-near. Each grid assumes that the magnet's
 * center is at the origin, starts are xy=(0,0), and includes only the quadrant
 * where x and y are both positive (lower-right quadrant in our coordinate
 * system).
 * <p/>
 * Each grid is stored in 2 files - one for Bx, one for By. This simulation
 * reads those files, and computes the B-field at a specified point using a
 * linear interpolation algorithm.
 * <p/>
 * Our coordinate system has +x to the left, and +y down, with quadrants
 * numbered like this: <code>
 * Q3 | Q2
 * -------
 * Q4 | Q1
 * </code> The grid files contain the B-field components for Q1, with values in
 * column-major order. (This means that the x coordinate changes more slowly
 * than the y coordinate.) x and y coordinates both start at 0.
 * <p/>
 * After locating a B-field vector in Q1, here's how to map it to one of the
 * other quadrants:
 * <ul>
 * <li>Q2: reflect about the x axis, so multiply By by -1
 * <li>Q3: reflect through the origin, so no change
 * <li>Q4: reflect about the x axis and reflect through the origin, so so
 * multiply By by -1
 * </ul>
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class BarMagnet extends AbstractMagnet {
  
  // values used in MathCAD for generating the grid files
  //strength of the magnet, in Gauss
  private static final float GRID_MAGNET_STRENGTH = 1;
  //spacing between points in the internal grid, same in both dimensions
  private static final double INTERNAL_GRID_SPACING = 5;
  //spacing between points in the external-near grid, same in both dimensions
  private static final double EXTERNAL_NEAR_GRID_SPACING = 5;
  //spacing between points in the external-far grid, same in both dimensions
  private static final double EXTERNAL_FAR_GRID_SPACING = 20;
  // number of points in the internal grid
  private static final Dimension INTERNAL_GRID_SIZE = new Dimension(26, 6);
  // number of points in the external-near grid
  private static final Dimension EXTERNAL_NEAR_GRID_SIZE = new Dimension(101, 81);
  // number of points in the external-far grid
  private static final Dimension EXTERNAL_FAR_GRID_SIZE = new Dimension(126, 61);

  // grid file names
  private static final String BX_INTERNAL_RESOURCE_NAME = "data/bfield/BX_internal.csv";
  private static final String BY_INTERNAL_RESOURCE_NAME = "data/bfield/BY_internal.csv";
  private static final String BX_EXTERNAL_NEAR_RESOURCE_NAME = "data/bfield/BX_external_near.csv";
  private static final String BY_EXTERNAL_NEAR_RESOURCE_NAME = "data/bfield/BY_external_near.csv";
  private static final String BX_EXTERNAL_FAR_RESOURCE_NAME = "data/bfield/BX_external_far.csv";
  private static final String BY_EXTERNAL_FAR_RESOURCE_NAME = "data/bfield/BY_external_far.csv";
  // There is a problem with bar magnet field model near edge. We scale to avoid this.
  private static final float PERIPHERY_SCALE_DOWN = 1.2f;
  // Canonical BarMagnet grid model is of size 250,50. 
  // We have a much smaller magnet in box2D coordinates
  // This scales it up appropriately.
  private static final float MODEL_SCALE = 5 * PERIPHERY_SCALE_DOWN;

  private final Grid internalGrid; // internal to the magnet
  private final Grid externalNearGrid; // near the magnet
  private final Grid externalFarGrid; // far from the magnet

  public BarMagnet(float x, float y, float angle) {
    super(ComponentType.BarMagnet, x, y, angle);
    
    this.setSize(32, 8);
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(getWidth()/2, getHeight()/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0001;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    rectangleShape.dispose();
    this.setAngularDamping(0.1f);

    internalGrid = new Grid(BX_INTERNAL_RESOURCE_NAME,
        BY_INTERNAL_RESOURCE_NAME, INTERNAL_GRID_SIZE, INTERNAL_GRID_SPACING);
    externalNearGrid = new Grid(BX_EXTERNAL_NEAR_RESOURCE_NAME,
        BY_EXTERNAL_NEAR_RESOURCE_NAME, EXTERNAL_NEAR_GRID_SIZE,
        EXTERNAL_NEAR_GRID_SPACING);
    externalFarGrid = new Grid(BX_EXTERNAL_FAR_RESOURCE_NAME,
        BY_EXTERNAL_FAR_RESOURCE_NAME, EXTERNAL_FAR_GRID_SIZE,
        EXTERNAL_FAR_GRID_SPACING);
  }

  @Override
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.MagnetStrength, 0f, 10000f) {
      public Float getValue() { return getStrength(); }
      public void setValue(Float value) { setStrength(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.RotationAngle, 0f, MathUtils.PI * 2) {
      public Float getValue() { return getAngle(); }
      public void setValue(Float value) { setAngle(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.MagnetMode, MovementMode.values()) {
      public String getValue() { return getMovementMode(); }
      public void setValue(String value) { setMovementMode(value); }
      public boolean isPossible() { return isActive(); }
    });
  }

  /**
   * Gets the B-field vector at a point in the magnet's local 2D coordinate
   * frame.
   * 
   * @param p
   * @param outputVector
   * @return outputVector
   */
  protected Vector2 getBFieldRelative(Vector2 p, Vector2 outputVector) {

    assert (p != null);
    assert (outputVector != null);

    // find B-field by interpolating grid points
    // Bar Magnet model is slightly off, so scale coordinates up
    // Bar Magnet model is at 250, 50 scale - so scale up to this magnitude
    float x = getBx(p.x * MODEL_SCALE, p.y * MODEL_SCALE);
    float y = getBy(p.x * MODEL_SCALE, p.y * MODEL_SCALE);
    outputVector.set(x, y);

    // scale based on magnet strength
    outputVector.mul(getStrength() / GRID_MAGNET_STRENGTH);

    return outputVector;
  }

  /*
   * Get Bx component for a point relative to the magnet's origin. This
   * component is identical in all 4 quadrants. See class javadoc.
   */
  private float getBx(final double x, final double y) {
    Grid grid = chooseGrid(x, y);
    return (float) interpolate(Math.abs(x), Math.abs(y), grid.getMaxX(),
        grid.getMaxY(), grid.getBxArray(), grid.getSpacing());
  }

  /*
   * Get By component for a point relative to the magnet's origin. This
   * component is the same in 2 quadrants, but must be reflected about the y
   * axis for 2 quadrants. See class javadoc.
   */
  private float getBy(final double x, final double y) {
    Grid grid = chooseGrid(x, y);
    double by = interpolate(Math.abs(x), Math.abs(y), grid.getMaxX(),
        grid.getMaxY(), grid.getByArray(), grid.getSpacing());
    if ((x > 0 && y < 0) || (x < 0 && y > 0)) {
      by *= -1; // reflect about the y axis
    }
    return (float) by;
  }

  /*
   * Chooses the appropriate grid.
   */
  private Grid chooseGrid(double x, double y) {
    Grid grid = null;
    if (internalGrid.contains(x, y)) {
      grid = internalGrid;
    } else if (externalNearGrid.contains(x, y)) {
      grid = externalNearGrid;
    } else {
      grid = externalFarGrid;
    }
    return grid;
  }

  /*
   * Locates the 4 grid points that form a rectangle enclosing the specified
   * point. Then performs a linear interpolation of the B-field component at
   * those 4 points. Variable names in this method corresponds to those used in
   * Mike Dubson's documentation, ie:
   * 
   * f00-----------f10 | | | xy | | | f01-----------f11
   */
  private double interpolate(final double x, final double y, double maxX,
      double maxY, double[][] componentValues, double gridSpacing) {
    if (!(x >= 0 && y >= 0)) {
      // ...because our grid is only for that quadrant
      throw new IllegalArgumentException("x and y must be positive"); 
    }

    double value = 0; // B-field outside the grid is zero
    if (x >= 0 && x <= maxX && y >= 0 && y <= maxY) {

      // compute array indicies
      int columnIndex = (int) (x / gridSpacing);
      int rowIndex = (int) (y / gridSpacing);

      /*
       * If we're at one of the index maximums, then we're exactly on the outer
       * edge of the grid. Back up by 1 so that we'll have a bounding rectangle.
       */
      if (columnIndex == componentValues.length - 1) {
        columnIndex -= 1;
      }
      if (rowIndex == componentValues[0].length - 1) {
        rowIndex -= 1;
      }

      // xy coordinates that define the enclosing rectangle
      double x0 = columnIndex * gridSpacing;
      double x1 = x0 + gridSpacing;
      double y0 = rowIndex * gridSpacing;
      double y1 = y0 + gridSpacing;

      // values at the 4 corners of the enclosing rectangle
      double f00 = componentValues[columnIndex][rowIndex];
      double f10 = componentValues[columnIndex + 1][rowIndex];
      double f01 = componentValues[columnIndex][rowIndex + 1];
      double f11 = componentValues[columnIndex + 1][rowIndex + 1];

      // interpolate
      value = (f00 * ((x1 - x) / (x1 - x0)) * ((y1 - y) / (y1 - y0)))
          + (f10 * ((x - x0) / (x1 - x0)) * ((y1 - y) / (y1 - y0)))
          + (f01 * ((x1 - x) / (x1 - x0)) * ((y - y0) / (y1 - y0)))
          + (f11 * ((x - x0) / (x1 - x0)) * ((y - y0) / (y1 - y0)));
    }
    return value;
  }

  /*
   * A grid of B-field vectors. The vector components (Bx, By) are stored in
   * separate files. Files are in CSV (Comma Separated Value) format, with
   * values in column-major order. (This means that the x coordinate changes
   * more slowly than the y coordinate.)
   */
  private static class Grid {

    private static final String TOKEN_DELIMITER = ","; // CSV format

    private final double[][] bxArray, byArray; // double[columns][rows]
    private final Dimension size;
    private final double spacing;

    public Grid(String bxResourceName, String byResourceName, Dimension size,
        double spacing) {
      this.size = new Dimension(size.width, size.height);
      this.spacing = spacing;
      bxArray = readComponent(bxResourceName, size);
      byArray = readComponent(byResourceName, size);
    }

    public double getSpacing() {
      return spacing;
    }

    public double[][] getBxArray() {
      return bxArray;
    }

    public double[][] getByArray() {
      return byArray;
    }

    /**
     * Determines if this grid contains the specified point. Since the grid
     * consists one quadrant of the the space (where x & y are positive), this
     * is determined based on the absolute value of the xy coordinates.
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean contains(double x, double y) {
      double absX = Math.abs(x);
      double absY = Math.abs(y);
      return (absX >= 0 && absX <= getMaxX() && absY >= 0 && absY <= getMaxY());
    }

    private double getMaxX() {
      return spacing * (size.width - 1);
    }

    private double getMaxY() {
      return spacing * (size.height - 1);
    }

    /**
     * Gets an input stream for the specified resource.
     * 
     * @param resource
     * @return InputStream
     */
    public InputStream getResourceAsStream(String resource) throws IOException {
      //InputStream stream = BarMagnet.class.getResourceAsStream(resource);
      InputStream stream = Gdx.files.internal(resource).read();
      if (stream == null) {
        throw new IOException("invalid resource: " + resource);
      }
      return stream;
    }

    /*
     * Reads one B-field component from the specified resource file. If any
     * problem occurs, this displays a dialog and exits the sim. The dialog
     * displays a localized error message, but the specific cause of the error
     * is not localized.
     */
    private double[][] readComponent(String resourceName, Dimension size) {
      double array[][] = new double[size.width][size.height];
      int count = 0;
      String errorString = null;
      try {
        InputStream is = getResourceAsStream(resourceName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        int row = 0;
        int column = 0;
        while (line != null) {
          StringTokenizer stringTokenizer = new StringTokenizer(line,
              TOKEN_DELIMITER);
          while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            array[column][row] = Double.parseDouble(token); // column-major
                                                            // order
            count++;
            row++;
            if (row == size.height) {
              row = 0;
              column++;
            }
          }
          line = br.readLine();
        }
      } catch (IOException e) {
        errorString = "could not read " + resourceName;
        e.printStackTrace();
      } catch (NumberFormatException nfe) {
        errorString = "could not parse number in " + resourceName;
        nfe.printStackTrace();
      } catch (ArrayIndexOutOfBoundsException be) {
        errorString = "more values than expected in " + resourceName;
        be.printStackTrace();
      }

      if (errorString == null) {
        int expectedCount = size.width * size.height;
        if (count < expectedCount) {
          errorString = "fewer values than expected in " + resourceName;
        }
      }

      // if we had problems, this sim can't function, so bail.
      if (errorString != null) {
        System.out.println("ERROR_BAR_MAGNET_INITIALIZATION");
        System.exit(1);
      }

      return array;
    }
  }
}
