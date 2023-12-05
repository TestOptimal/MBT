package com.testoptimal.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.mcase.MCase;
import com.testoptimal.exec.mscript.Exec;
import com.testoptimal.exec.mscript.Util;
import com.testoptimal.page.Page;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.plugin.MScriptInterface.NOT_MSCRIPT_METHOD;
import com.testoptimal.plugin.RandPlugin;
import com.testoptimal.scxml.ScxmlNode;

public class CodeAssistMgr {
	private static Logger logger = LoggerFactory.getLogger(CodeAssistMgr.class);

	private static List<String> TOP_CA_NAMES = new java.util.ArrayList<>(); 
	private static Map<String, List<CodeAssist>> CA_CacheList = new java.util.HashMap<>();
	private static Map<String, Map<String, List<CodeAssist>>> CA_CacheMapList = new java.util.HashMap<>();

	/**
	 * auto generate CA files on startup
	 */
	public static void init () {
		try {
			logger.info("generating caList");
			initCAList();
		}
		catch (Exception e) {
			logger.error("Unable to code assist dictionary", e);
		}
	}

	
	/**
	 * add java classes to have CA file generated for Script editor.  The $SYS, etc. 
	 * are instantiated in MbtScriptExecutor.initGroovyMScript for model execution.
	 * 
	 * @TO-DO: make this configurable
	 * @throws Exception
	 */
	private static void initCAList () throws Exception {
		CA_CacheList = new java.util.HashMap<>();
		CA_CacheList.put("EXEC", getMScriptMethods(Exec.class));
		
		TOP_CA_NAMES.add("EXEC");
		CA_CacheList.put("UTIL", getMScriptMethods(Util.class));
		
		TOP_CA_NAMES.add("UTIL");
		TOP_CA_NAMES.add("VAR");
		List<CodeAssist> varCAList = new java.util.ArrayList<>();
		varCAList.add(new CodeAssist("set variable", "varname = ", ""));
		varCAList.add(new CodeAssist("get variable", "varname", ""));
		CA_CacheList.put("VAR", varCAList);

//		TOP_CA_NAMES.add("DATASET");
//		List<CodeAssist> datasetCAList = new java.util.ArrayList<>();
//		datasetCAList.add(new CodeAssist("get dataset", "dsname", ""));
//		CA_CacheList.put("DATASET", datasetCAList);

//		TOP_CA_NAMES.add("RAND");
//		CA_CacheList.put("RAND", getMScriptMethods(RandPlugin.class));
//

		CA_CacheMapList = new java.util.HashMap<>(CA_CacheList.size());
		CA_CacheList.entrySet().stream()
			.forEach( entry -> {
				Map<String, List<CodeAssist>> m = entry.getValue().stream().collect(Collectors.groupingBy(CodeAssist::getName, Collectors.toList()));
				CA_CacheMapList.put(entry.getKey(), m);
			});
	}

	private static List<CodeAssist> getMScriptMethods (Class objClass_p) {
		List<Method> methods = getMethodsAnnotated(objClass_p);
		List<CodeAssist> retList = methods.stream().map(m -> {
			CodeAssist ca = new CodeAssist(m);
			return ca;
		}).collect(Collectors.toList());
		Collections.sort(retList);
		return retList;
	}

	private static List<Method> getMethodsAnnotated(final Class<?> class_p) {
		List<Method> methods = new ArrayList<Method>();
		boolean excludeInheritedClass = class_p.isAnnotationPresent(IGNORE_INHERITED_METHOD.class);
		Class klass = class_p;
	    final Method[] allMethods;
	    if (excludeInheritedClass) {
	    	allMethods = klass.getDeclaredMethods();
	    }
	    else {
	    	allMethods = klass.getMethods();
	    }
	    for (final Method method : allMethods) {
	        if (Modifier.isPublic(method.getModifiers()) &&
	        	!method.isAnnotationPresent(NOT_MSCRIPT_METHOD.class)) {
	            // Deprecated annotInstance =
	            // method.getAnnotation(Deprecated.class);
	            // if (annotInstance.x() == 3 && annotInstance.y() == 2) {
	            methods.add(method);
	            // }
	        }
	    }

	    methods.sort(new Comparator<Method>() {
	        @Override
	        public int compare(Method m1, Method m2) {
	        	
	            String m1Name = m1.getName();
	            String m2Name = m2.getName();
	            int ret = m1Name.compareTo(m2Name);
	            if (ret==0) {
	            	return m1.getParameterCount() - m2.getParameterCount();
	            }
	            else return ret;
	         }
	    });
	    return methods;
	}
	

	public static Map<String, List<CodeAssist>> getMScriptCAList () throws Exception {
		Map<String, List<CodeAssist>> caList = new TreeMap<String, List<CodeAssist>>();
		List<CodeAssist> topLevel = new java.util.ArrayList<CodeAssist>();
		for (String n: TOP_CA_NAMES) {
			String m = "$" + n;
			topLevel.add(new CodeAssist(m, m + ".", m));
		}
		caList.put("TOPLEVEL", topLevel);
		Collections.sort(topLevel);
		return caList;
	}
	
	public static Map<String, List<CodeAssist>> getMScriptCAListData () throws Exception {
		Map<String, List<CodeAssist>> caList = new TreeMap<String, List<CodeAssist>>();
		List<CodeAssist> topLevel = new java.util.ArrayList<CodeAssist>();
		topLevel.add(new CodeAssist("$UTIL", "$UTIL.", "$UTIL"));
		topLevel.add(new CodeAssist("$VAR", "$VAR.", "$VAR"));
//		topLevel.add(new CodeAssist("$DATASET", "$DATASET.", "$DATASET"));
		topLevel.add(new CodeAssist("$RAND", "$RAND.", "$RAND"));
		caList.put("TOPLEVEL", topLevel);
		Collections.sort(topLevel);
		return caList;
	}

	private static Map<String, List<CodeAssist>> genMScriptCAList (String classPath_p) throws Exception {
		Map<String, List<CodeAssist>> retList = CA_CacheMapList.get(classPath_p);
		if (retList==null) {
			try {
				Class caClass = Class.forName(classPath_p);
				List<CodeAssist> cList = getMScriptMethods(caClass);
				CA_CacheList.put(classPath_p, cList);
				retList = cList.stream().collect(Collectors.groupingBy(CodeAssist::getName, Collectors.toList()));
				CA_CacheMapList.put(classPath_p, retList);
			}
			catch (ClassNotFoundException e) {
				return new java.util.HashMap<String, List<CodeAssist>>();
			}
		}
		return retList;
	}
	
	
	public static List<CodeAssist> getMScriptCAListByExpr (List<String> exprList_p) throws Exception {
		if (exprList_p.isEmpty()) {
			return new java.util.ArrayList<CodeAssist>();
		}
		
		String curKey = exprList_p.get(0);
		if (curKey.startsWith("$")) curKey = curKey.substring(1);
		Map<String, List<CodeAssist>> curObjMap = CA_CacheMapList.get(curKey);
		if (curObjMap==null) {
			curObjMap = genMScriptCAList(curKey);
		}
		for (int i = 1; i < exprList_p.size(); i++) {
			String funcName = exprList_p.get(i);
			List<CodeAssist> funcList = curObjMap.get(funcName);
			if (funcList==null || funcList.isEmpty()) {
				return new java.util.ArrayList<CodeAssist>();
			}
			curKey = funcList.get(0).returnType;
			curObjMap = genMScriptCAList(curKey);
		}
		List<CodeAssist> retList = CA_CacheList.get(curKey);
		if (retList==null) {
			return new java.util.ArrayList<CodeAssist>();
		}
		return retList;
	}
	
//	public static void main (String[] args) {
//		getMethodsAnnotated (io.restassured.RestAssured.given().queryParam("parameterName", "parameterValues").when().put("asf").getClass());
//	}
}
