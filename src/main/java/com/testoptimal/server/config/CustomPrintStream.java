package com.testoptimal.server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class  CustomPrintStream extends PrintStream {

	public CustomPrintStream() throws FileNotFoundException {
		super(new File ("test"));
	}

	@Override
	public void println(String s) {
		String cl = Thread.currentThread().getStackTrace()[1].getClassName();
		String s1 = s;
	}
	}
