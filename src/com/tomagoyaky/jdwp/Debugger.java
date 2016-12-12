package com.tomagoyaky.jdwp;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.tomagoyaky.jdwp.trace.Trace;
import com.tomagoyaky.local.debug.Logger;

/**
 * @author tomagoyaky
 * xref_01: http://illegalargumentexception.blogspot.com/2009/03/java-using-jpda-to-write-debugger.html
 * xref_02: https://github.com/tomagoyaky/eclipse.jdt.debug/tree/master/org.eclipse.jdt.debug
 * */ 
public class Debugger extends Logger {

	private static Debugger Debugger;
	private ThreadReference main;
	private VirtualMachine vm;
	private EventRequestManager erm;
	private Connections conn;
	public Trace trace;
	
	private ArrayList<BreakpointRequest> breakpointRequestList;

	public static Debugger getInstance() {
		if (Debugger == null) {
			Debugger = new Debugger();
		}
		return Debugger;
	}

	public void connect(int port) {
		try {
			vm = new Connections("localhost", port).connect();
			erm = vm.eventRequestManager();
			breakpointRequestList = new ArrayList<BreakpointRequest>();
			trace = new Trace(vm);
			// enable jdb global environment;
//			Env.init(vm);
//			ThreadInfo.invalidateAll();
//			for (ThreadReference tRef : vm.allThreads()) {
//				if (tRef.name().contains(" main")) {
//					ThreadInfo.setCurrentThread(tRef);
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logd(">>> jdwp connect successfuly! <<<");
	}

	public void setBreakPoint(String methodSignature) {

		MethodRequest methodRequest = MethodRequest.getMethodRequest(methodSignature);
		for (ReferenceType refer : vm.allClasses()) {
			String className = StringUtil.getClassName(refer);
			if (className.equals(methodRequest.className)) {
				List<Method> methodList = refer.allMethods();
				for (int i = 0; i < methodList.size(); i++) {
					Method method = methodList.get(i);
					
					// add method breakPoint
					if (method.declaringType().name().equals(methodRequest.className)){
						if (method.name() != null && method.name().equals(methodRequest.methodName)) {
							String tmpSignature = method.signature();
							if (tmpSignature.equals(methodRequest.signature)) {

								if(method.isNative()){
									loge("NativeMethodException: Cannot set breakpoints on native method {" + methodRequest.methodSignature +"}");
									return;
								}
								BreakpointRequest br = setBreakPoint(method.location());
								logd(">>> add breakPoint: [" + methodSignature + "]  at " + method.location());
							}
						}
					}
				}
			}
		}
	}

	public BreakpointRequest setBreakPoint(Location location) {
		BreakpointRequest br = erm.createBreakpointRequest(location);
		br.setEnabled(true);
		breakpointRequestList.add(br);
		return br;
	}
	
	public void resume(){
		vm.resume();
	}

	private ReferenceType getReferenceTypeFromToken(String idToken) {
        ReferenceType cls = null;
        if (Character.isDigit(idToken.charAt(0))) {
            cls = null;
        } else if (idToken.startsWith("*.")) {
        idToken = idToken.substring(1);
        for (ReferenceType type : vm.allClasses()) {
            if (type.name().endsWith(idToken)) {
                cls = type;
                break;
            }
        }
    } else {
            // It's a class name
            List<ReferenceType> classes = vm.classesByName(idToken);
            if (classes.size() > 0) {
                cls = classes.get(0);
            }
        }
        return cls;
    }
	public void printClass(String idClass) {
		
		ReferenceType type = getReferenceTypeFromToken(idClass);
		if (type == null) {
			logd("is not a valid id or class name:" + idClass);
            return;
        }
        if (type instanceof ClassType) {
            ClassType clazz = (ClassType)type;
            logd("Class: " + clazz.name());

            ClassType superclass = clazz.superclass();
            while (superclass != null) {
                logd("\t extends: " + superclass.name());
                superclass = superclass.superclass();
            }

            List<InterfaceType> interfaces = clazz.allInterfaces();
            for (InterfaceType interfaze : interfaces) {
                logd("\t implements: " + interfaze.name());
            }

            for (ClassType sub : clazz.subclasses()) {
                logd("\t subclass: " + sub.name());
            }
            for (ReferenceType nest : clazz.nestedTypes()) {
                logd("\t nested: " + nest.name());
            }
        } else if (type instanceof InterfaceType) {
            InterfaceType interfaze = (InterfaceType)type;
            logd("\t Interface: " + interfaze.name());
            for (InterfaceType superinterface : interfaze.superinterfaces()) {
                logd("\t extends: " + superinterface.name());
            }
            for (InterfaceType sub : interfaze.subinterfaces()) {
                logd("\t subinterface: " + sub.name());
            }
            for (ClassType implementor : interfaze.implementors()) {
                logd("\t implementor: " + implementor.name());
            }
            for (ReferenceType nest : interfaze.nestedTypes()) {
                logd("\t nested: " + nest.name());
            }
        } else {  // array type
            ArrayType array = (ArrayType)type;
            logd("\t Array: " + array.name());
        }
	}

	public void printallThread() throws AbsentInformationException, IncompatibleThreadStateException {
		for (ThreadReference tRef : vm.allThreads()) {
			// just use main thread for now...
			if (tRef.name().contains(" main")) {
				main = tRef;
			}
			if (tRef != null) {
				logd("name=" + tRef.name() + ", status=" + tRef.status());
			}
		}
	}
}
