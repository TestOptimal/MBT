package com.testoptimal.exec.mscript.groovy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.codehaus.groovy.syntax.SyntaxException;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.mscript.TRIGGER;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;

public class GroovyScriptExec {
	private GroovyScript scriptDef;
	private Script scriptExec;
	private Map<String, GroovyMethod> methodMap = new java.util.HashMap<>();
	private List<ScriptRuntimeException> exceptList = new java.util.ArrayList<>();
	
	public GroovyScriptExec (GroovyScriptEngine scriptEng_p, GroovyScript scriptDef_p, Binding binding_p)
			throws ScriptRuntimeException {
		this.scriptDef = scriptDef_p;
		try {
			this.scriptExec = scriptEng_p.createScript(this.scriptDef.getScriptFileName(), binding_p);
			this.scriptExec = this.scriptExec.getClass().newInstance();
			this.scriptExec.setBinding(binding_p);
			this.scriptExec.run();
			String scriptName = this.scriptDef.getScriptName();
			binding_p.setProperty("$" + scriptName, this.scriptExec);
			this.indexMethods();
//		    if (scriptName.equalsIgnoreCase("PAGES")) {
//	    		this.scriptExec.run();
//		    }
		}
		catch (Exception e) {
			String scriptName = this.scriptDef.getScriptName();
			ScriptRuntimeException retExcept = null;
			if (e instanceof MultipleCompilationErrorsException) {
				this.exceptList = parseException(scriptName, (MultipleCompilationErrorsException) e);
				if (!this.exceptList.isEmpty()) {
					retExcept = this.exceptList.get(0);
				}
			} 
			else {
				StackTraceElement errLine = this.getStackTrace(e);
				int lineNum = 0;
				if (errLine!=null) lineNum = errLine.getLineNumber();
				retExcept = new ScriptRuntimeException(scriptName, "",
						lineNum, 0, e.getMessage());
				this.exceptList.add(retExcept);
			}
			throw retExcept;
		}
	}
	
	public Script newInstance (Binding binding_p) throws ScriptRuntimeException {
		try {
			Script retScript = this.scriptExec.getClass().newInstance();
			retScript.setBinding(binding_p);
			return retScript;
		}
		catch (IllegalAccessException e) {
			StackTraceElement errLine = this.getStackTrace(e);
			int lineNum = 0;
			if (errLine!=null) lineNum = errLine.getLineNumber();
			ScriptRuntimeException retExcept = new ScriptRuntimeException(
					this.scriptDef.getScriptName(), "", lineNum, 
					0, e.getMessage());
			throw retExcept;
		}
		catch (InstantiationException ie) {
			StackTraceElement errLine = this.getStackTrace(ie);
			int lineNum = 0;
			if (errLine!=null) lineNum = errLine.getLineNumber();
			ScriptRuntimeException retExcept = new ScriptRuntimeException(
					this.scriptDef.getScriptName(), "", 
					lineNum, 0, ie.getMessage());
			throw retExcept;
		}
	}
	
    public Object evalExpr (String scriptName_p, String scripts_p) throws ScriptRuntimeException {
//    	try {
//	    	FileUtil.writeToFile(scriptName_p, scripts_p);
//    	}
//    	catch (Exception e) {
//    		throw new ScriptRuntimeException(scriptName_p, "", 0, 0, e.getMessage());
//    	}
    	try {
    		return this.scriptExec.evaluate(scripts_p);
    	}
    	catch (Exception e) {
    		throw parseException(this.scriptDef.getScriptName(), e);
    	}
    }

    public Object runScript () throws ScriptRuntimeException {
    	try {
    		return this.scriptExec.run();
    	}
    	catch (Exception e) {
			if (e.getCause() instanceof MBTAbort) {
				throw e;
			}
    		throw parseException(this.scriptDef.getScriptName(), e);
    	}
    }

    /**
     * runs state or transition trigger for the uid, returns true if trigger function
     * is found and executed, else false if function is not found.
     * 
     * @param uid_p
     * @return
     * @throws Exception
     */
    public boolean runTrigger (String uid_p) throws ScriptRuntimeException, MBTAbort {
    	GroovyMethod gMethod = this.methodMap.get(uid_p);
    	if (gMethod == null) {
    		return false;
    	}
    	
		try {
			// call(eng, binding);
			Object ret = this.scriptExec.invokeMethod(gMethod.method.getName(), null);
			// script.run();
			return true;
		} 
		catch (Exception e) {
			if (e.getCause() instanceof MBTAbort) {
				throw (MBTAbort) e.getCause();
			}
			throw parseException(this.scriptDef.getScriptName(), e);
		}

	    //  Class scriptClass = eng.loadScriptByName( "src/main/resources/groovyScript.groovy");
	    //  Object scriptInstance = scriptClass.newInstance() ;
	    //  scriptClass.getDeclaredMethod( "hello_rochester", new Class[] {}).invoke( scriptInstance);
    }
    
    public Object runFunc (String funcName_p, Object [] params_p, boolean errIfNotExist_p) throws Exception {
    	GroovyMethod method = this.findMethod(funcName_p);
    	if (method == null) {
    		if (errIfNotExist_p) {
    			throw new ScriptRuntimeException(this.scriptDef.getScriptName(), "", 0, 0, "Function not found for " + funcName_p);
    		}
    		return null;
    	}
    	
		try {
			// call(eng, binding);
			return this.scriptExec.invokeMethod(method.method.getName(), params_p);
			// script.run();
		} 
		catch (Exception e) {
			if (e.getCause() instanceof MBTAbort) {
				throw e;
			}
			throw parseException(this.scriptDef.getScriptName(), e);
		}

	    //  Class scriptClass = eng.loadScriptByName( "src/main/resources/groovyScript.groovy");
	    //  Object scriptInstance = scriptClass.newInstance() ;
	    //  scriptClass.getDeclaredMethod( "hello_rochester", new Class[] {}).invoke( scriptInstance);
    }
    
    private void indexMethods () {
        Method [] methods = this.scriptExec.getClass().getMethods();
        for (Method method: methods) {
           Annotation[] annotations = method.getAnnotations();
           for (Annotation ann : annotations) {
        	  if (ann instanceof TRIGGER) {
        		  TRIGGER st = (TRIGGER) ann;
        		  this.methodMap.put(st.value(), new GroovyMethod(method, st));
              }
           }
        }
    }
    


    private GroovyMethod findMethod (String name_p) {
        Method [] methods = this.scriptExec.getClass().getMethods();
        Object annFound = null;
        
        for (Method method: methods) {
           Annotation[] annotations = method.getAnnotations();
           if (method.getName().equalsIgnoreCase(name_p)) {
        	   return new GroovyMethod(method, null);
           }
           
           for (Annotation ann : annotations) {
        	  if (ann instanceof TRIGGER && ((TRIGGER) ann).value().equals(name_p)) {
                  annFound = ann;
                  break;
              }
           }
           if (annFound != null) {
        	   return new GroovyMethod(method, (TRIGGER) annFound);
           }
        }
        return null;
    }
    
    public static ScriptRuntimeException parseException (String scriptName_p, Throwable e) {
		String errMsg = e.getMessage();
		Throwable pe = e;
		if (e instanceof MissingPropertyException) {
			errMsg = "Unknown property: " + ((MissingPropertyException) e).getProperty();
		} 
		else if (e instanceof MissingMethodException) {
			errMsg = "Method found without compatible params: " + ((MissingMethodException) e).getMethod();
		}
		else if (e instanceof InvokerInvocationException) {
			pe = e.getCause();
			if (pe instanceof MissingMethodException) {
				errMsg = pe.getMessage();
			}
			else {
				errMsg = "Invokation error: " + e.getMessage();
			}
		}
		else if (e instanceof ArithmeticException) {
			ArithmeticException ae = (ArithmeticException) e;
			errMsg = "Arithmetic error: " + ae.getMessage();
		}
		else if (e instanceof ArrayIndexOutOfBoundsException) {
			ArrayIndexOutOfBoundsException ae = (ArrayIndexOutOfBoundsException) e;
			errMsg = "Array index out of bound error: " + ae.getMessage();
		}
		else {
			errMsg = pe.getMessage();
		}
		StackTraceUtils.sanitize(pe);
		List<StackTraceElement> sel = sanitizeStackTrace(pe);
		int lineNum = 0, colNum = 0;
		String errMethodName = "";
		String errFileName = scriptName_p;
		for (StackTraceElement errLine: sel) {
			String fName = errLine.getFileName();
			if (fName!=null && (fName.endsWith(".gvy") || fName.endsWith(".groovy"))) {
				errFileName = errLine.getFileName();
				errMethodName =  errLine.getMethodName();
				lineNum = errLine.getLineNumber();
				break;
			}
		}
		return new ScriptRuntimeException(errFileName, errMethodName, lineNum, colNum, errMsg);
    }
    
    private static List<ScriptRuntimeException> parseException (String scriptName_p, MultipleCompilationErrorsException me_p) {
    	List<ScriptRuntimeException> retList = new java.util.ArrayList<>();
		ErrorCollector ec = me_p.getErrorCollector();
		@SuppressWarnings("unchecked")
		List<Message> el = (List<Message>) ec.getErrors();
		for (Message sem : el) {
			Throwable ex = sem instanceof SyntaxErrorMessage? ((SyntaxErrorMessage) sem).getCause(): ((ExceptionMessage) sem).getCause();
			if (ex instanceof GroovyRuntimeException)  {
				ex = ((GroovyRuntimeException) ex).getCause();
			}
			if (ex instanceof SyntaxException) {
				SyntaxException se = (SyntaxException) ex;
				ScriptRuntimeException except = new ScriptRuntimeException(scriptName_p, "", 
						se.getStartLine(), se.getStartColumn(), se.getMessage());
				retList.add(except);
			}
			else {
				ScriptRuntimeException except = new ScriptRuntimeException(scriptName_p, "", 
						0, 0, ex.getMessage());
				retList.add(except);
			}
		}
		return retList;
	} 

    private class GroovyMethod {
    	private Method method;
    	private TRIGGER ann;
    	
    	public GroovyMethod (Method method_p, TRIGGER ann_p) {
    		this.method = method_p;
    		this.ann = ann_p;
    	}
    	
    	public String toString() {
    		StringBuffer retBuf = new StringBuffer();
    		retBuf.append(method.getName());
    		if (this.ann!=null) {
    			retBuf.append(" at UID(").append(ann.value()).append(")");
    		}
    		return retBuf.toString();
    	}
    }
    
    
    public Script getScriptExec () {
    	return this.scriptExec;
    }

    public static List<StackTraceElement> sanitizeStackTrace(Throwable e) {
		StackTraceUtils.deepSanitize(e);
    	StackTraceElement [] list = e.getStackTrace();
    	List<StackTraceElement> retList = new java.util.ArrayList<>(list.length);
    	for (StackTraceElement elem: list) {
    		String cname = elem.getClassName();
    		if (cname.startsWith("com.testoptimal.")) {
    			break;
    		}
			retList.add(elem);
    	}
    	return retList;
    }

    private StackTraceElement getStackTrace(Throwable e) {
    	List<StackTraceElement> list = sanitizeStackTrace(e);
    	if (list.size()>0) return list.get(0);
    	else return  null;
    }

}
