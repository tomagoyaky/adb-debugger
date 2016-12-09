package com.tomagoyaky.local.debug;

public class Logger {

	public static void logd(int msg) {
		logd(String.valueOf(msg));
	}
	
	public static void loge(int msg) {
		loge(String.valueOf(msg));
	}
	
	public static void logd(String msg) {
		System.out.println("[+]" + msg);
	}
	
	public static void loge(String msg) {
		System.out.println("[-]" + msg);
	}

	public static void logd(Exception e) {
		logd(e.toString());
		StackTraceElement[] stackTraceElement = e.getStackTrace();
		for (int i = 0; i < stackTraceElement.length; i++) {
			logd(stackTraceElement[i].getClassName() + "." + stackTraceElement[i].getMethodName() + "() Line:" + stackTraceElement[i].getLineNumber());
		}
	}
	
	public static void loge(Exception e) {
		loge(e.toString());
		StackTraceElement[] stackTraceElement = e.getStackTrace();
		for (int i = 0; i < stackTraceElement.length; i++) {
			loge(stackTraceElement[i].getClassName() + "." + stackTraceElement[i].getMethodName() + "() Line:" + stackTraceElement[i].getLineNumber());
		}
	}
}
