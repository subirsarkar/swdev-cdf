package config.util;

import java.lang.reflect.Field; 
import java.lang.reflect.Array; 

public class ToStringGenerator  { 
  public static String toStringClass(String className)  { 
    // Buffer Construction 
    StringBuilder sb = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);

    try { 
      Class targetClass = Class.forName(className); 
      if (!targetClass.isPrimitive() && targetClass != String.class)  { 
         Field fields[] = targetClass.getDeclaredFields(); 
         Class cSuper = targetClass.getSuperclass(); // Retrieving the super class 

         if (cSuper != null && cSuper != Object.class) { 
           sb.append(cSuper.toString()); // Super class's toString() 
           sb.append("\n");
         } 

         for (int j = 0; j < fields.length; j++) { 
           sb.append(fields[j].getName() + " = "); // Append Field name 

           // Check for a primitive or string 
           if (fields[j].getType().isPrimitive() || fields[j].getType() == String.class) {
             // Append the primitive field value 
             sb.append(fields[j].getName());
           }
           else  { 
             // It is NOT a primitive field so this requires a check 
             // for the NULL value for the aggregated object 
             if (fields[j].getName()  != null)
               sb.append(fields[j].getName().toString());
             else 
               sb.append("value is null"); 
           } 
           sb.append("\n");
         }
      } 
    } catch (ClassNotFoundException e) { 
      e.printStackTrace();
      System.out.println("Class not found in the class path"); 
    } 
    return sb.toString(); 
  } 
  public static String toStringObject(Object arr) {
    // If object reference is null or not an array, call String.valueOf()
    if (arr == null || !arr.getClass().isArray()) 
      return String.valueOf(arr);
     
    // Set up a string buffer and get length of array
    StringBuilder sb = new StringBuilder(AppConstants.MEDIUM_BUFFER_SIZE);
    int len = Array.getLength(arr);
     
    sb.append('[');
     
    // Iterate across array elements
    for (int i = 0; i < len; i++) {
      if (i > 0) sb.append(',');
     
      // Get the i-th element
      Object obj = Array.get(arr, i);
     
      // Convert it to a string by recursive toString() call
      sb.append(toStringObject(obj));
    }

    sb.append(']');
     
    return sb.toString();
  }
                       
  public static void main(String args[]) {
  
    // example #1
    System.out.println(toStringObject("testing"));
    
    // example #2
    System.out.println(toStringObject(null));
    
    // example #3
    int arr3[] = new int[]{1, 2, 3};
    System.out.println(toStringObject(arr3));
    
    // example #4
    long arr4[][] = new long[][]{
        {1, 2, 3},
        {4, 5, 6},
        {7, 8, 9}
    };
    System.out.println(toStringObject(arr4));
    
    // example #5
    double arr5[] = new double[0];
    System.out.println(toStringObject(arr5));
    
    // example #6
    String arr6[] = new String[]{"testing", null, "123"};
    System.out.println(toStringObject(arr6));
    
    // example #7
    Object arr7[] = new Object[]{
        new Object[]{null, new Object(), null},
        new int[]{1, 2, 3},
        null
    };
    System.out.println(toStringObject(arr7));
    
    if (args.length > 0) System.out.println(toStringClass(args[0]));
  }
} 
