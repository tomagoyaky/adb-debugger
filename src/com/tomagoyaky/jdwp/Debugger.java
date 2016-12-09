package com.tomagoyaky.jdwp;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.tools.jdi.ClassTypeImpl;
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

	public void displayCallStack(ThreadReference threadRef) throws IncompatibleThreadStateException, AbsentInformationException{
        
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

	public void loop() throws IncompatibleThreadStateException, InterruptedException, AbsentInformationException, ClassNotLoadedException {
//		
//		EventQueue eventQueue = vm.eventQueue();
//		while (true) {
//			logd("eventQueue loop... ");
//			
//			EventSet eventSet = eventQueue.remove();
//			for (Event event : eventSet) {
//				if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
//					// exit
//					logd("VMDeathEvent or VMDisconnectEvent.");
//					return;
//				}
//				else if (event instanceof BreakpointEvent) {
//					BreakpointEvent breakpointEvt = (BreakpointEvent) event;
//					logd(" > BreakpointEvent: " + breakpointEvt.location().toString());
//					StackFrame stackFrame = breakpointEvt.thread().frame(0);
//					List<LocalVariable> localVariableList = stackFrame.visibleVariables();
//					if(localVariableList.size() != 0){
//						for (LocalVariable localVar : localVariableList) {
//							Value val = stackFrame.getValue(localVar);
//							logd("varName:" + localVar.name() + ", valStr:" + val.toString() + ", type:" + localVar.typeName());
//						}
//					}else{
//						logd("No arguments " + breakpointEvt.location().toString());
//						displayCallStack(stackFrame.thread());
//					}
//				} else if (event instanceof ModificationWatchpointEvent) {
//					ModificationWatchpointEvent modEvent = (ModificationWatchpointEvent) event;
//					logd("old=" + modEvent.valueCurrent() + ", new=" + modEvent.valueToBe());
//				} else if(event instanceof MethodExitEvent){
//					MethodExitEvent methodExitEvt = (MethodExitEvent)event;
//					if(methodExitEvt.virtualMachine().canGetMethodReturnValues()){
//						Object val = methodExitEvt.returnValue();
//						if(methodExitEvt.method().returnType().getClass().equals(String.class)){
//							logd(">>> " + methodExitEvt.method().name() + val);
//						}
//					}else{
//						if(Integer.parseInt(vm.version().substring(2, 3)) <= 6){
//							loge("java version [" + vm.version() + "] Should not have method return values capabilities.");
//							return;
//						}
//					}
//				}
//			}
//			eventSet.resume();
//			Thread.sleep(3000);
//		}
		EventQueue queue = vm.eventQueue();
        while (true) {
            EventSet eventSet = queue.remove();
            EventIterator it = eventSet.eventIterator();
            while (it.hasNext()) {
            	Event event = it.nextEvent();
				if (event instanceof ExceptionEvent) {
//					exceptionEvent((ExceptionEvent) event);
				} else if (event instanceof BreakpointEvent) {
					breakpointEvent((BreakpointEvent)event);
				}else if (event instanceof ModificationWatchpointEvent) {
//					fieldWatchEvent((ModificationWatchpointEvent) event);
				} else if (event instanceof MethodEntryEvent) {
					methodEntryEvent((MethodEntryEvent) event);
				} else if (event instanceof MethodExitEvent) {
//					methodExitEvent((MethodExitEvent) event);
				} else if (event instanceof StepEvent) {
//					stepEvent((StepEvent) event);
				} else if (event instanceof ThreadDeathEvent) {
//					threadDeathEvent((ThreadDeathEvent) event);
				} else if (event instanceof ClassPrepareEvent) {
//					classPrepareEvent((ClassPrepareEvent) event);
				} else if (event instanceof VMStartEvent) {
//					vmStartEvent((VMStartEvent) event);
				} else if (event instanceof VMDeathEvent) {
//					vmDeathEvent((VMDeathEvent) event);
				} else if (event instanceof VMDisconnectEvent) {
//					vmDisconnectEvent((VMDisconnectEvent) event);
				} else {
					throw new Error("Unexpected event type");
				}
            }
            eventSet.resume();
//            Thread.sleep(100);
        }
	}

	public void trace(boolean watchFields) {

		String[] excludes = { "java.*", "javax.*", "sun.*", "com.sun.*" };
		EventRequestManager mgr = vm.eventRequestManager();

		// want all exceptions
		ExceptionRequest excReq = mgr.createExceptionRequest(null, true, true);
		// suspend so we can step
		excReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		excReq.enable();

		MethodEntryRequest menr = mgr.createMethodEntryRequest();
		for (int i = 0; i < excludes.length; ++i) {
			menr.addClassExclusionFilter(excludes[i]);
		}
		menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		menr.enable();

		MethodExitRequest mexr = mgr.createMethodExitRequest();
		for (int i = 0; i < excludes.length; ++i) {
			mexr.addClassExclusionFilter(excludes[i]);
		}
		mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		mexr.enable();

		ThreadDeathRequest tdr = mgr.createThreadDeathRequest();
		// Make sure we sync on thread death
		tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
		tdr.enable();

		if (watchFields) {
			ClassPrepareRequest cpr = mgr.createClassPrepareRequest();
			for (int i = 0; i < excludes.length; ++i) {
				cpr.addClassExclusionFilter(excludes[i]);
			}
			cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			cpr.enable();
		}
	}

	private void vmStartEvent(VMStartEvent event) {
		logd(">>> -- VM Started --");
	}

	// Forward event for thread specific processing
	private void methodEntryEvent(MethodEntryEvent event) {
		MethodEntryEvent methodEntryEvent = (MethodEntryEvent)event;
		Method method = null;
		try {
			method = methodEntryEvent.method();
			String nativeStr = method.isNative() ? "[Native]" : "";
			String methodName = method.declaringType().name();
			if(!(methodName.startsWith("com.android") 
//					|| methodName.startsWith("org.java")
					|| methodName.startsWith("android.")
					|| methodName.startsWith("com.google.")
					|| methodName.startsWith("libcore.")
					|| methodName.startsWith("de.robv.android.xposed.")
					|| methodName.startsWith("dalvik."))){
				
				if(methodName.startsWith("org.apache.http")){
					logd(">>> [MethodEntryEvent] " + method.declaringType().name() + "." + method.name() + method.signature() + "\t" + nativeStr);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
//			loge(e);
		}
	}

	// Forward event for thread specific processing
	private void methodExitEvent(MethodExitEvent event) {
		logd(">>> [MethodExitEvent] " + event.method().name() + "  --  " + event.method().declaringType().name());
		if (vm.canGetMethodReturnValues()) {
			logd("Method '" + event.method().name() + "' exitedValue:" + event.returnValue() + "");
		} else {
			if (Integer.parseInt(vm.version().substring(2, 3)) <= 6) {
				loge("java version [" + vm.version() + "] Should not have method return values capabilities.");
				return;
			}
		}
	}

	// Forward event for thread specific processing
	private void stepEvent(StepEvent event) {

	}

	private void breakpointEvent(BreakpointEvent event)
			throws IncompatibleThreadStateException, AbsentInformationException {
		BreakpointEvent breakpointEvt = (BreakpointEvent) event;
		logd(" > BreakpointEvent: " + breakpointEvt.location().toString());
//		displayCallStack(main);

		Method method = breakpointEvt.location().method();
		List<LocalVariable> localVariableList = method.variables();
		for (int i = 0; i < localVariableList.size(); i++) {
			LocalVariable localVariable = localVariableList.get(i);
			Value val = breakpointEvt.thread().frame(0).getValue(localVariable);
			if(localVariable.isArgument()){
				logd("\t" + localVariable.name() + ":\"" + val + "\"");
			}else{
				logd("\t" + localVariable.name() + ":\"" + val + "\"");
			}
		}
	}

	// Forward event for thread specific processing
	private void fieldWatchEvent(ModificationWatchpointEvent event) {
		Field field = event.field();
		Value value = event.valueToBe();
		logd(">>> [ModificationWatchpointEvent] " + field.name() + " = " + value);
	}

	private void threadDeathEvent(ThreadDeathEvent event) {
		if (event.thread() != null) { // only want threads we care about
			logd(">>> [ThreadDeathEvent] " + event.thread().name() + " end.");
		}
	}

	/**
	 * A new class has been loaded. Set watchpoints on each of its fields
	 */
	private void classPrepareEvent(ClassPrepareEvent event) {
		// EventRequestManager mgr = vm.eventRequestManager();
		// List<Field> fields = event.referenceType().visibleFields();
		// for (Field field : fields) {
		// ModificationWatchpointRequest req =
		// mgr.createModificationWatchpointRequest(field);
		// for (int i=0; i<excludes.length; ++i) {
		// req.addClassExclusionFilter(excludes[i]);
		// }
		// req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
		// req.enable();
		// }
	}

	private void exceptionEvent(ExceptionEvent event) {
		ExceptionEvent exceptionEvent = (ExceptionEvent)event;
		if (exceptionEvent.thread() != null) { // only want threads we care about
			logd(">>> [ExceptionEvent] " + exceptionEvent.thread().name() + " exception:" + exceptionEvent.exception().toString());
		}
	}

	private void vmDeathEvent(VMDeathEvent event) {
		logd(">>> [VMDeathEvent] The application exited.");
	}

	private void vmDisconnectEvent(VMDisconnectEvent event) {
		logd(">>> [VMDisconnectEvent] The application has been disconnected.");
	}
}
