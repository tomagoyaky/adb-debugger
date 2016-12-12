/*
 * Copyright (c) 2001, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation and proper error handling, might not be present in
 * this sample code.
 */


package com.tomagoyaky.jdwp.trace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.tools.jdi.SocketAttachingConnector;
import com.tomagoyaky.local.debug.Logger;

/**
 * This program traces the execution of another program.
 * See "java Trace -help".
 * It is a simple example of the use of the Java Debug Interface.
 *
 * @author Robert Field
 */
public class Trace extends Logger{

    // Running remote VM
    private VirtualMachine vm;

    // Mode for tracing the Trace program (default= 0 off)
    private int debugTraceMode = 0;

    //  Do we want to watch assignments to fields
    private boolean watchFields = false;

    // Class patterns for which we don't want events
    private String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};

    /**
     * Parse the command line arguments.
     * Launch target VM.
     * Generate the trace.
     */
    public Trace(VirtualMachine vm) {
        this.vm = vm;
    }

    /**
     * Generate the trace.
     * Enable events, start thread to display events,
     * start threads to forward remote error and output streams,
     * resume the remote VM, wait for the final event, and shutdown.
     */
    public void generateTrace() {
        vm.setDebugTraceMode(debugTraceMode);
        EventRunnable eventRunnable = new EventRunnable(vm, excludes);
        eventRunnable.setEventRequests(watchFields);
        
        Thread eventThread = new Thread(eventRunnable);
        eventThread.start();

        // Shutdown begins when event thread terminates
        try {
            eventThread.join();
        } catch (InterruptedException exc) {
            // we don't interrupt
        }
    }

    String getHOSTPORT(String argStr, int index){
    	String retStr = null;
    	StringTokenizer st = new StringTokenizer(argStr, ":");
    	switch (index) {
		case 0:
			retStr = st.nextToken();
			break;
		case 1:
			st.nextToken();
			retStr = st.nextToken();
			break;

		default:
			break;
		}
    	return retStr;
    }
    String getArgumentItemValue(String argStr, String name){
    	String value = null;
    	StringTokenizer st = new StringTokenizer(argStr, " ");
    	while(st.hasMoreTokens()){
    		String arg = st.nextToken();
    		if(arg.startsWith(name)){
    			StringTokenizer st_1 = new StringTokenizer(arg, "=");
    			st_1.nextToken();
    			value = st_1.nextToken();
    			break;
    		}
    	}
		return value;
    }
    /**
     * Launch target VM.
     * Forward target's output and error.
     * @throws IllegalConnectorArgumentsException 
     * @throws IOException 
     */
    VirtualMachine socketAttachingConnect(String mainArgs) throws IOException, IllegalConnectorArgumentsException {

    	// argumet:
    	//		-socketAttachConnector=localhost:9999 -class=com.bradzhao.crackme.MainActivity
        SocketAttachingConnector connector = findSocketConnector();
        @SuppressWarnings("unchecked")
		Map<String, Connector.Argument> arguments = connector.defaultArguments();  
        String socketArrachConnectorArg = getArgumentItemValue(mainArgs, "-socketAttachConnector");
        Connector.Argument hostArg = (Connector.Argument) arguments.get("hostname");  
        Connector.Argument portArg = (Connector.Argument) arguments.get("port"); 
        hostArg.setValue(getHOSTPORT(socketArrachConnectorArg, 0));
        portArg.setValue(getHOSTPORT(socketArrachConnectorArg, 1));
        return connector.attach(arguments);
    }
    
    /**
     * Find a com.sun.jdi.CommandLineLaunch connector
     */
    SocketAttachingConnector findSocketConnector() {
        List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
        for (Connector connector : connectors) {
            if (connector.name().equals("com.sun.jdi.SocketAttach")) {
                return (SocketAttachingConnector)connector;
            }
        }
        throw new Error("No socketAttachingConnector connector");
    }

    /**
     * Return the launching connector's arguments.
     */
    Map<String, Connector.Argument> connectorArguments(Connector connector, String mainArgs) {
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        Connector.Argument mainArg =
                           (Connector.Argument)arguments.get("main");
        if (mainArg == null) {
            throw new Error("Bad launching connector");
        }
        mainArg.setValue(mainArgs);

        if (watchFields) {
            // We need a VM that supports watchpoints
            Connector.Argument optionArg =
                (Connector.Argument)arguments.get("options");
            if (optionArg == null) {
                throw new Error("Bad launching connector");
            }
            optionArg.setValue("-classic");
        }
        return arguments;
    }

    /**
     * Print command line usage help
     */
    void usage() {
        System.err.println("Usage: java Trace <options> <class> <args>");
        System.err.println("<options> are:");
        System.err.println(
"  -output <filename>   Output trace to <filename>");
        System.err.println(
"  -all                 Include system classes in output");
        System.err.println(
"  -help                Print this help message");
        System.err.println("<class> is the program to trace");
        System.err.println("<args> are the arguments to <class>");
    }
}
