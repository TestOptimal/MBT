package com.testoptimal.exec.mscript;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepMethod {
	String prefix;
	String modelName;
	String pattern;
	List<String> tokens;
	List<Parameter> methodParams;
	Method methodObj;
	Pattern regExprPtn;

   public StepMethod (String modelName_p, String prefix_p, String pattern_p, Method methodObj_p) {
	  this.modelName = modelName_p;
	  this.prefix = "$" + prefix_p + ".";
//	  this.prefix = prefix_p + ".";
      this.pattern = "^" + pattern_p + "[ .;\\t]*$";
      this.methodObj = methodObj_p;
      List<String> tempTokens = new java.util.ArrayList<>(); // = Arrays.asList(pattern_p.split("X"));
      Pattern r1 = Pattern.compile("\\{(.+?)\\}");
      Matcher matcher = r1.matcher(this.pattern);
      while (matcher.find()) {
             String temp = matcher.group();
             tempTokens.add(temp.substring(1,temp.length()-1));
      }

      this.methodParams = Arrays.asList(this.methodObj.getParameters());
      this.tokens = tempTokens.stream().filter(t -> {
                   return this.methodParams.stream()
                                 .anyMatch(p -> p.getName().equalsIgnoreCase(t));
             }).collect(toList());

      String expr = this.pattern;
      for (String t: this.tokens) {
             expr = expr.replace("{" + t + "}", "(.+)");
      }
      this.regExprPtn = Pattern.compile(expr);
   }

   public String toString () {
      return this.methodObj.getName() + ": " + this.pattern;
   }

   private boolean match (String annText_p) {
      return this.regExprPtn.matcher(annText_p).find();
   }

   private String[] getTokens (String annText_p) {
      Matcher matcher = this.regExprPtn.matcher(annText_p);
      if (matcher.find()) {
             String[] retList = new String[matcher.groupCount()];
             for (int i=0; i < retList.length; i++) {
                   retList[i] = matcher.group(i+1);
             }
             return retList;
      }
      else return null;
   }

   public String genOutput (String annText_p) throws Exception {
      if (this.methodParams.isEmpty()) {
         return this.prefix + this.methodObj.getName() + "()";
      }

      String[] groups = this.getTokens(annText_p);
	  if (groups.length != this.methodParams.size()) {
		  throw new Exception ("Token mismatch " + annText_p);
	  }

      Map<String,String> fieldMap = new java.util.HashMap<>();
      for (int i=0; i < this.methodParams.size(); i++) {
         fieldMap.put(this.methodParams.get(i).getName(), groups[i]);
      }

      List <String> pList = new java.util.ArrayList<>();
      for (int i=0; i < this.methodParams.size(); i++) {
         String pName = this.methodParams.get(i).getName();
         pList.add(fieldMap.get(pName));
      }
      String ret = this.prefix + this.methodObj.getName() + "('" + String.join("','", pList) + "')";
      return ret;
   }
   
   public static StepMethod findMethod (String modelName_p, List<StepMethod> methodList_p, String annText_p) {
      List<StepMethod> list = (List<StepMethod>) methodList_p.stream()
                   .filter(m -> m.match(annText_p) && m.modelName.equalsIgnoreCase(modelName_p))
                   .collect(toList());
      return list.isEmpty()? null: list.get(0);
   }
}