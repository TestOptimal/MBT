package com.testoptimal.client;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeAssist implements Comparable <CodeAssist> {
	
	/**
	 * to be exported as json for javascript client.
	 */
	public String displayText;
	public String text;
	public int paramNum = 0;
	public String returnType;
	
	public CodeAssist (String displayText_p, String text_p, String returnType_p) {
		this.displayText = displayText_p;
		this.text = text_p;
		this.returnType = returnType_p.replace("$", ".");
		String retType = this.returnType;
		int idx = retType.lastIndexOf(".");
		if (idx>=0) {
			retType = retType.substring(idx+1);
		}
		this.displayText += ": " + retType;
	}
	
	public CodeAssist(Method method_p) {
		this.returnType = method_p.getReturnType().getName().replace("$", ".");
		String retType = this.returnType;
		int idx = retType.lastIndexOf(".");
		if (idx>=0) {
			retType = retType.substring(idx+1);
		}
		String mName = method_p.getName();
		List<Parameter> pList = Arrays.asList(method_p.getParameters());
		this.paramNum = pList.size();
		String pText = pList.stream().map(p -> {
			String pType = p.getType().getName();
			int i = pType.lastIndexOf(".");
			if (i > 0) {
				pType = pType.substring(i + 1);
			}
			String pName = p.getName();
			if (pName.endsWith("_p")) {
				pName = pName.substring(0, pName.length()-2);
			}
			return pType + " " + pName;
		})
		.collect(Collectors.joining(", "));
		this.displayText = mName + "(" + pText + "): " + retType;

		// temp fix to RestAssure when().get().*then()
		if ((mName.equals("get") ||
			mName.equals("post") ||
			mName.equals("put") ||
			mName.equals("patch") ||
			mName.equals("delete")) &&
			this.returnType.contains("ResponseOptions")) {
			this.returnType = "io.restassured.response.Response";
		}

		
		String pString = pList.stream().map(p -> {
			String pName = p.getName();
			if (pName.endsWith("_p")) {
				pName = pName.substring(0, pName.length()-2);
			}
			if (p.getType() == String.class) {
				return "'" + pName + "'";
			}
			else return pName;
		})
		.collect(Collectors.joining(", "));
		this.text = mName + "(" + pString + ")";
	}
	
	public void setIdPath(String p) {
		this.returnType = p;
	}

	public String getName () {
		int idx = this.displayText.indexOf("(");
		if (idx > 0) {
			return this.displayText.substring(0, idx).trim();
		}
		idx = this.displayText.indexOf(":");
		if (idx > 0) {
			return this.displayText.substring(0, idx).trim();
		}
		else return this.displayText;
	}
	
	@Override
	public int compareTo(CodeAssist ca_p) {
		return this.displayText.compareTo(ca_p.displayText);
	}
}

