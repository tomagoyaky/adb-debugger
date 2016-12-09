package com.tomagoyaky.jdwp;

import java.util.StringTokenizer;

import com.sun.jdi.ReferenceType;

public class StringUtil {

	public static String getClassName(ReferenceType refer) {
		// class org.json.JSONObject$1 (no class loader)
		// class de.robv.android.xposed.XC_MethodHook (loaded by instance of dalvik.system.PathClassLoader(id=830031343480))
		StringTokenizer st = new StringTokenizer(refer.toString(), " ");
		String type = st.nextToken();
		switch (type) {
		case "class":
		case "interface":
			return st.nextToken();
		case "array":
			st.nextToken();
			return st.nextToken();
		default:
			break;
		}
		return null;
	}

}
