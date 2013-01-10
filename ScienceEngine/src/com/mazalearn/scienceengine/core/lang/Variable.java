// Variables associate values with names.
// Copyright 1996 by Darius Bacon; see the file COPYING.

package com.mazalearn.scienceengine.core.lang;

import java.util.HashMap;

/**
 * A variable is a simple expression with a name (like "x") and a
 * settable value.
 */
public class Variable extends Expr {
    private static HashMap<String, Variable> variables = new HashMap<String, Variable>();
    
    /**
     * Return a unique variable named `name'.  There can be only one
     * variable with the same name returned by this method; that is,
     * make(s1) == make(s2) if and only if s1.equals(s2).
     * @param name the variable's name
     * @return the variable; create it initialized to 0 if it doesn't
     *         yet exist */
    static public synchronized Variable make(String name) {
        Variable result = (Variable) variables.get(name);
        if (result == null)
            variables.put(name, result = new Variable(name));
        return result;
    }

    private String name;
    private double fval;
    private String sval;
    private boolean bval;

    /**
     * Create a new variable, with initial value 0.
     * @param name the variable's name
     */
    public Variable(String name) { 
        this.name = name; fval = 0; 
    }

    public String name() { return name; }
    /** Return the name. */
    public String toString() { return name(); }

    /** Get the value.
     * @return the current value */
    public double fvalue() { 
      if (type == Type.STRING) return Double.parseDouble(sval);
      if (type == Type.DOUBLE) return fval;
      return bval ? 1.0 : 0;
    }
    
    /** Get the value.
     * @return the current value */
    public String svalue() {
      if (type == Type.STRING) return sval;
      if (type == Type.DOUBLE) return String.valueOf(fval);
      return bval ? "1.0" : "0.0";
    }
    
    /** Get the value.
     * @return the current value */
    public boolean bvalue() {
      if (type == Type.STRING) return Double.parseDouble(sval) != 0;
      if (type == Type.DOUBLE) return fval != 0;
      return bval; 
    }
    
    /** Set the value.
     * @param value the new value */
    public void setValue(boolean value) {
        type = Type.BOOL;
        bval = value; 
    }

    /** Set the value.
     * @param value the new value */
    public void setValue(double value) {
        type = Type.DOUBLE;
        fval = value; 
    }

    /** Set the value.
     * @param value the new value */
    public void setValue(String value) {
        type = Type.STRING;
        sval = value; 
    }
}
