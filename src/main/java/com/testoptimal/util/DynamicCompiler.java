package com.testoptimal.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
 
/**
 * Dynamically compiles a java class and load it into memory for execution.
 *
 */
public class DynamicCompiler {
    /** where shall the compiled class be saved to (should exist already) */
    private static String classOutputFolder = "temp";
//    private static ClassLoader loader;
    
//    static {
//    	try {
//	        File file = new File(classOutputFolder);
//	        URL url = file.toURI().toURL(); // file:/classes/demo
//	        URL[] urls = new URL[] { url };
//	        loader = new URLClassLoader(urls);
//    	}
//    	catch (Exception e) {
//    		e.printStackTrace();
//    	}
//    }
    
    private List<String> codeList = new java.util.ArrayList<String>();
    private List<String> errList = new java.util.ArrayList<String>();
    public List<String> getErrList () {
    	return this.errList;
    }
    
    public boolean isError() {
    	return !this.errList.isEmpty();
    }
    
    private class MScriptDiagnosticListener implements DiagnosticListener<JavaFileObject> {
    	private DynamicCompiler compiler;
    	public MScriptDiagnosticListener (DynamicCompiler comp) {
    		this.compiler = comp;
    	}
    	
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
//        	int lineNum =  Math.round(diagnostic.getLineNumber());
//            this.compiler.errList.add ("Line " + lineNum + ": " + this.compiler.codeList.get(lineNum-1)); //diagnostic.getCode());
//            this.compiler.errList.add (diagnostic.getMessage(Locale.ENGLISH));
//            this.compiler.errList.add ("Source: " + diagnostic.getSource());
//            this.compiler.errList.add (" ");
        	this.compiler.errList.add(diagnostic.toString());
        }
    }
 
    /** java File Object represents an in-memory java source file <br>
     * so there is no need to put the source file on hard disk  **/
    private class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private String contents = null;
 
        public InMemoryJavaFileObject(String className, String contents) throws Exception {
            super(URI.create("string:///" + className.replace('.', '/')
                             + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }
 
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }
    }
 
    /** compile your files by JavaCompiler */
    public boolean compile (String classPath, List<String> javaCodeList) throws Exception {
    	this.errList.clear();
    	StringBuffer buf = new StringBuffer();
    	this.codeList = javaCodeList;
    	for (String c: this.codeList) {
    		buf.append(c).append("\n");
    	}
    	JavaFileObject file = new InMemoryJavaFileObject(classPath, buf.toString());
        Iterable<? extends JavaFileObject> files = Arrays.asList(file);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler==null) {
        	throw new Exception ("Java Compiler not found, check make sure you have JDK (not JRE) in your system classpath and tools.jar exists in JDK lib folder.");
        }
        MScriptDiagnosticListener c = new MScriptDiagnosticListener(this);
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(c, Locale.ENGLISH, null);
        Iterable<String> options = Arrays.asList("-d", classOutputFolder);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, c, options, null, files);
        Boolean result = task.call();
        return result;
    }
 
//    public Class load (String classPath) throws Exception {
//        Class thisClass = loader.loadClass(classPath);
//        return thisClass;
//    }
    
    public static Class reloadClass (String classPath) throws Exception {
    	Class<?> myClass = DynamicCompiler.class;
    	File file = new File(classOutputFolder);
    	URL url = file.toURI().toURL();
    	URL[] urls= { url, myClass.getProtectionDomain().getCodeSource().getLocation() };
//    	ClassLoader delegateParent = myClass.getClassLoader().getParent();
		URLClassLoader cl = new URLClassLoader(urls);
		Class<?> reloaded = cl.loadClass(classPath);
		return reloaded;
    }

    public Class compileAndLoad (String classPath, List<String> javaCodeList) throws Exception {
    	if (compile (classPath, javaCodeList)) {
	    	Class thisClass = reloadClass (classPath);
	    	return thisClass;
    	}
    	else {
    		throw new Exception ("MScriptImpl class compiling error: " + ArrayUtil.join(this.errList, ";"));
    	}
    }
}