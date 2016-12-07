package com.tomagoyaky.local.debug;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;

public class DeviceInfo extends Executor {

	public String ModelName;
	public String SystemInfo;
	public String SerialNum;
	public String abi;
	
	private static DeviceInfo deviceInfo;
	private static ADB adb;
	public static DeviceInfo getInstance(ADB _adb) {
		if(deviceInfo == null){
			deviceInfo = new DeviceInfo();
		}
		adb = _adb;
		return deviceInfo;
	}

	public DeviceInfo getDevice() throws ExecuteException, IOException {
		adb.waitForDevice();
		this.ModelName	= adb.shell("getprop ro.product.model", 	System.getProperty("user.dir"), 0, 0).replace("\n", "");
		this.SerialNum	= adb.shell("getprop ro.serialno", 			System.getProperty("user.dir"), 0, 0).replace("\n", "");
		this.abi 		= adb.shell("getprop ro.product.cpu.abi", 	System.getProperty("user.dir"), 0, 0).replace("\n", "");
		this.SystemInfo = adb.shell("getprop ro.build.description", System.getProperty("user.dir"), 0, 0).replace("\n", "");
		if(this != null){
			logd("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% device %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			logd("%%|  ModelName: " + this.ModelName);
			logd("%%|   CPU_eabi: " + this.abi);
			logd("%%| SystemInfo: " + this.SystemInfo);
			logd("%%|  SerialNum: " + this.SerialNum);
			logd("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		}
		return this;
	}
}
