package config.svt;

import org.python.util.PythonInterpreter; 

/**
 *  Create a Singleton class which holds a Python Interpreter instance
 *  which will be used exclusively for interactive histogramming. A singleton
 *  may not be the best solution here, but if I know what i am doing ....
 *
 *  @author S. Sarkar
 *  @version 0.1   05/2003
 */
public class Interpreter {
   /** Static reference to <CODE>this</CODE> */
  private static Interpreter _instance = null;
  // Jython interpreter
  private PythonInterpreter interp;
   /**
    * The constructor could be made private
    * to prevent others from instantiating this class.
    * But this would also make it impossible to
    * create instances of Singleton subclasses.
    */
  private Interpreter() {
    interp = new PythonInterpreter(); 
    interp.exec("import string,re");
    interp.exec("from math import *");
  }
   /** 
    * Singleton constructor. If the class is already instantiated
    * returns the instance, else creates it. Only one object of Interpreter
    * class manages all the interactions using static methods.
    *
    * @return  An instance of <CODE>Interpreter</CODE> class
    */
  public static synchronized Interpreter getInstance() {
    if (_instance == null) {
      _instance = new Interpreter();
    }
    return _instance;
  }
  public PythonInterpreter getInterpreter() {
    return interp;
  }
}
