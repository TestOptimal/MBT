package com.testoptimal.exec.mscript;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface MScriptInterface {
	/**
	 * Indicates that the annotated class is a plugin implementation.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TO_PLUGIN { }

	@Retention(RetentionPolicy.RUNTIME)
	public @interface IGNORE_INHERITED_METHOD { }


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface NOT_MSCRIPT_METHOD { }
}
