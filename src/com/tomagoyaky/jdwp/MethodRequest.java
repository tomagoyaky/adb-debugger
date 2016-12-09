package com.tomagoyaky.jdwp;

import java.util.StringTokenizer;

public class MethodRequest {

	public String className;
	public String methodName;
	public String signature;
	public String methodSignature;

	public MethodRequest(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public static MethodRequest getMethodRequest(String methodSignature) {
		// for example:
		//		java.lang.StringBuilder.toString()java.lang.String;
		
		MethodRequest methodRequest = new MethodRequest(methodSignature);
		int startIndex = 0;
		int endIndex = methodSignature.indexOf("(");
		String remainStr = methodSignature.substring(startIndex, endIndex);
		
		methodRequest.signature = methodSignature.substring(endIndex);
		StringTokenizer st = new StringTokenizer(remainStr, ".");
		StringBuilder classStrBuilder = new StringBuilder();
		int curTokenCount = 1;
		int totalTokenCount = st.countTokens();
		while(st.hasMoreTokens()){
			String item = st.nextToken();
			if(curTokenCount == totalTokenCount){
				methodRequest.methodName = item;
				break;
			}
			classStrBuilder.append(item);
			if(curTokenCount != totalTokenCount - 1){
				classStrBuilder.append(".");
			}
			curTokenCount++;
		}
		methodRequest.className = classStrBuilder.toString();
//		logd("---> " + methodRequest.className + "." + methodRequest.methodName + methodRequest.signature);
		return methodRequest;
	}

}
