package com.langproc;

import java.io.IOException;

public class LangProcOutput
{
	public static java.io.Writer writer = new java.io.OutputStreamWriter(
			System.out);

	public static void print(String s) {
		try {
			writer.write(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void println() {
		print("\n");
	}

	public static void println(String s) {
		print(s);
		print("\n");
	}

	public static void print(Object o) {
		print(o.toString());
	}

	public static void println(Object o) {
		println(o.toString());
	}
}
