package com.tomagoyaky.local.debug;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.exec.ExecuteException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ADB extends Executor {

	private static ADB adb;
	private String adb_path;
	public ADB(String path) {
		adb_path = path;
	}

	public static ADB getInstance() {
		if(adb == null){
			try {
				adb = new ADB(Configure.getItemValue("adb"));
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return adb;
	}
	
	public void waitForDevice() {
		try {
			execute(adb_path + " wait-for-device", System.getProperty("user.dir"), 0, 30);
		} catch (ExecuteException e) {
			loge(e);
			System.exit(-2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void install(String apkFilePath, boolean isReInstall) throws ExecuteException, IOException {
		if(!new File(apkFilePath).exists()){
			throw new IOException("file is not exist! {" + apkFilePath + "}");
		}
		if(isReInstall){
			execute(adb_path + " install -r " + apkFilePath, System.getProperty("user.dir"), 0, System.out, 0);
		}else{
			execute(adb_path + " install " + apkFilePath, System.getProperty("user.dir"), 0, System.out, 0);
		}
	}

	public void push(String localPath, String remotePath) throws ExecuteException, IOException{
		execute(adb_path + " push " + localPath + " " + remotePath, System.getProperty("user.dir"), 0, System.out, 0);
	}

	public void pull(String localPath, String remotePath) throws ExecuteException, IOException{
		execute(adb_path + " pull " + remotePath + " " + localPath, System.getProperty("user.dir"), 0, System.out, 0);
	}

	public void forward_removeAll() throws ExecuteException, IOException {
		execute(adb_path + " forward --remove-all", System.getProperty("user.dir"), 0, System.out, 0);// TODO Auto-generated method stub
	}
	
	public void forward(int localPort, int remotePort, String typeName) throws ExecuteException, IOException {
		// adb forward tcp:23946 tcp:23946
		execute(adb_path + " forward tcp:" + localPort + " " + typeName + ":" + remotePort, System.getProperty("user.dir"), 0, System.out, 0);
	}

	public String shell(String cmdlineStr, String cwd, int exitValue, int timeout) throws ExecuteException, IOException {
		this.waitForDevice();
		return execute(adb_path + " shell " + cmdlineStr, cwd, exitValue, timeout);
	}

	public String sushell(String cmdlineStr, String cwd, int exitValue, int timeout) throws ExecuteException, IOException {
		this.waitForDevice();
		return execute(adb_path + " shell su -c \"" + cmdlineStr + "\"", cwd, exitValue, timeout);
	}

	public String isXposedSupport() throws ExecuteException, IOException{
		String app_process_retStr = adb.shell("app_process", System.getProperty("user.dir"), 0, 0).replace("\n", "");
		if(app_process_retStr.toLowerCase().contains("with xposed support")){
			String[] items = app_process_retStr.split("\r");
			for (int i = 0; i < items.length; i++) {
				String item = items[i];
				if(item.toLowerCase().contains("with xposed support")){
					return item;
				}
			}
			return app_process_retStr;
		}
		return null;
	}

	public void amStartActivityWithDebug(ApkInfo apkInfo) throws ExecuteException, IOException {
		amStartActivity(apkInfo, apkInfo.launchableActivity, true);
	}
	
	public void amStartActivity(ApkInfo apkInfo) throws ExecuteException, IOException{
		amStartActivity(apkInfo, apkInfo.launchableActivity, false);
	}
	
	public void amStartActivity(ApkInfo apkInfo, String activityName) throws ExecuteException, IOException{
		amStartActivity(apkInfo, activityName, false);
	}

	public void amStartActivity(ApkInfo apkInfo, String activityName, boolean isDebugOpen) throws ExecuteException, IOException{
		String optStr = "";
		if(isDebugOpen){
			optStr = "-D";
		}
		adb.shell("am start " + optStr + " -n " + apkInfo.pkg + "/" + activityName, System.getProperty("user.dir"), 0, 0);
		logd("{" + activityName + "} has been started successfully");
	}
	
	public boolean isDebuggable() throws ExecuteException, IOException {
		boolean isDebuggable = false;
		// check is debuggable
		String debuggableStr = adb.shell("getprop ro.debuggable", System.getProperty("user.dir"), 0, 0).replace("\r", "").replace("\n", "");
		if(Integer.parseInt(debuggableStr) == 0){
			// 直接读属性发现是不可调试，那么就调用android api来试试
			String supportStr = null;
			if((supportStr = isXposedSupport()) != null){
				logd(supportStr);
				// /data/data/de.robv.android.xposed.installer/shared_prefs/enabled_modules.xml
				try{
					String enabled_modules_Str = adb.sushell("cat /data/data/de.robv.android.xposed.installer/shared_prefs/enabled_modules.xml", System.getProperty("user.dir"), 0, 0);
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(new StringReader(enabled_modules_Str)));
					Node root = doc.getElementsByTagName("map").item(0);
					NodeList nodeList = root.getChildNodes();
					
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE){
							NamedNodeMap namedNodeMap = node.getAttributes();
							for (int j = 0; j < namedNodeMap.getLength(); j++) {
								if(namedNodeMap.item(j).getNodeValue().equals("com.example.administrator.hookdebug")){
									String value = namedNodeMap.getNamedItem("value").getNodeValue();
									isDebuggable = value.equals("1") ? true : false; 
								}
							}
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			isDebuggable = true;
		}
		return isDebuggable;
	}
}
