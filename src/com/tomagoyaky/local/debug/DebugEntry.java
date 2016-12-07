package com.tomagoyaky.local.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.exec.ExecuteException;
import org.xml.sax.SAXException;

public class DebugEntry extends Executor {

	private static ApkInfo apkInfo;
	private static DeviceInfo deviceInfo;
	private static ADB adb;

	private static String dir_remote = null;
	private static Thread androidServer = new Thread(new Runnable() {
		
		@Override
		public void run() {
			try {
				dir_remote = Configure.getItemValue("dirremote");
				adb.sushell(dir_remote + "/android_server", System.getProperty("user.dir"), 0, 0);
			} catch (ExecuteException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	});
	public static void main(String[] args) {
		boolean islooprunning = false;
		try{
			JudgeArguments(args);
			String apkFilePath = args[0];
			adb = ADB.getInstance();
			deviceInfo = DeviceInfo.getInstance(adb).getDevice();
			apkInfo = ApkInfo.getInstance(apkFilePath).parseApkFile();

			while(true){
				if(islooprunning){
					readDataFromConsole("... >>> any key to continue <<< ...");
				}
				islooprunning = true;
				String inputStr = showMenu();
				if(inputStr.startsWith("shell ")){
					execute(inputStr.replaceAll("shell ", ""), System.getProperty("user.dir"), System.out, 0);
				}else{
					int selectIndex = 0;
					try{
						selectIndex = Integer.parseInt(inputStr);
					}catch (NumberFormatException e) {
						loge("error input.");
						continue;
					}
					try{
						switch (selectIndex) {
						case 0:
							dir_remote = Configure.getItemValue("dirremote");
							adb.sushell("mkdir -p " + dir_remote, System.getProperty("user.dir"), 0, 0);
							
							apkInfo.printSimpleInfo();
							adb.install(apkFilePath, true);
							if(!adb.isDebuggable()){
								logd("try to crack android system's prop's value with 'ro.debuggable'");
								if(deviceInfo.abi.contains("armeabi") && deviceInfo.abi.contains("64")){
									adb.push(Configure.getItemValue("arm64-v8a"), dir_remote);
								}else if(deviceInfo.abi.contains("armeabi") ){
									adb.push(Configure.getItemValue("mprop_armv7"), dir_remote);
								}
								adb.sushell("chmod 777 " + dir_remote + "/mprop", System.getProperty("user.dir"), 0, 0);
								adb.sushell(dir_remote + "/mprop ro.debuggable 1", System.getProperty("user.dir"), 0, 0);
							}
							adb.amStartActivityWithDebug(apkInfo);
							int pid = Runntime.getpid(adb, apkInfo.pkg);
							logd("pid=" + pid);

							// prepare debug server
							adb.push(Configure.getItemValue("android_server"), dir_remote);
							adb.sushell("chmod 777 " + dir_remote + "/android_server", System.getProperty("user.dir"), 0, 0);

							// runing android_server
//							if(androidServer != null){
//								logd("Listening on port #23946...");
//								androidServer.start();
//							}else{
//								log("[!]wait for last running quit ...");
//								int count = 0;
//								while(true){
//									try {
//										Thread.sleep(1000);
//									} catch (InterruptedException e) {
//										loge("android_server cann't run, error:" + e.getMessage());
//										break;
//									}
//									count++;
//									if(count > 30){
//										loge("android_server cann't run due to timeout! (30s)");
//										break;
//									}
//								}
//							}
							
//							if(HostSystem.isMircoWindows()){
//								String retStr = execute("tasklist | findstr ida", System.getProperty("user.dir"), 0, 0);
//								int ida_pid = 123;
//								IDAHook.getInstance(Configure.getItemValue("IDAinject")).waitForAttach(ida_pid);
//							}
							
							readDataFromConsole(">>>> any key to continue, when your andriod application is attached successfully by IDA-pro");
							
							// avoid the DDMS's affect, we remove all of the forward-port.
							/**
							 * adb forward --remove tcp:8700				; only remove sigle port
							 * adb forword --list							;display all port
							 * */
							adb.forward_removeAll();
							String jdwpStr = execute(Configure.getItemValue("adb") + " jdwp", System.getProperty("user.dir"), 0, 0);
							if(jdwpStr.contains("" + pid)){
								adb.forward(9600, pid, "jdwp");
								execute("jdb -connect com.sun.jdi.SocketAttach:hostname=localhost,port=" + 9600, 
										System.getProperty("user.dir"), 0, System.out, 0);
							}else{
								loge("the application '" + apkInfo.pkg + "' has no jdwp port to open, Maybe you should startActivity with debug model.");
							}
							break;
						case 1:
							apkInfo.printSimpleInfo();
							break;
						case 2:
							adb.install(apkFilePath, true);
							break;
						case 3:
							adb.amStartActivityWithDebug(apkInfo);
							break;
						default:
							logd("exit OK.");
							System.exit(0);
							break;
						}
					}catch (IOException e) {
						loge(e);
					}
				}
			}
		} catch (IOException e) {
			loge(e);
			if(androidServer != null){ androidServer.interrupt(); androidServer = null; }
		} catch (ParserConfigurationException e) {
			loge(e);
			if(androidServer != null){ androidServer.interrupt(); androidServer = null; }
		} catch (SAXException e) {
			loge(e);
			if(androidServer != null){ androidServer.interrupt(); androidServer = null; }
		} catch (FileNoExecutePermision e) {
			loge(e);
			if(androidServer != null){ androidServer.interrupt(); androidServer = null; }
		}
	}

	private static String readDataFromConsole(String prompt) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String str = null;
		try {
			System.out.print(prompt);
			str = br.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}
	
	private static void JudgeArguments(String[] args){
		if(args.length == 0){
			loge("Usage:adbdebug xxx.apk");
			System.exit(0);
		}
	}
	
	private static String showMenu() throws IOException {
		logd("========================================================================");
		logd("| (1) display simple info about apk.");
		logd("| (2) install apk.");
		logd("| (3) 'am' command start activity with debug model.");
		return readDataFromConsole("remote-device$ ");
	}
}