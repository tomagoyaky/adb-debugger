package com.tomagoyaky.local.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

public class ApkInfo extends Executor {

	public String name;
	public String pkg;
	public String launchableActivity;
	public String fileSize;
	public String hashValue;
	public String versionCode;
	public String versionName;
	public String icon;
	public String logoUrl;
	public File apkFile;
	
	private String retval;
	private AAPT aapt;

	private static ApkInfo apkInfo;
	public ApkInfo(String apkFilePath) {
		this.apkFile = new File(apkFilePath);
	}

	public static ApkInfo getInstance(String apkFilePath) throws IOException, ParserConfigurationException, SAXException, FileNoExecutePermision {

		if(!new File(apkFilePath).exists()){
			throw new IOException("{" + apkFilePath + "}, file is not exist!");
		}
		String dir_appInfo = Configure.getItemValue("dirappinfo");
		if(dir_appInfo.equals("")){
			dir_appInfo = System.getProperty("java.io.tmpdir");
		}
		String aapt_path = Configure.getItemValue("aapt");
		return getInstance(apkFilePath, dir_appInfo, aapt_path);
	}
	
	public void printSimpleInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("*************************************************************************\n");
		sb.append("*  应用名称: " + this.name + "\n");
		sb.append("*  发行版本: " + this.versionName + "\n");
		sb.append("*  应用包名: " + this.pkg + "\n");
		sb.append("*  启动界面: " + this.launchableActivity + "\n");
		sb.append("*  样本路径: " + this.apkFile.getAbsolutePath() + "\n");
		sb.append("*************************************************************************\n");
		logd(sb.toString());
	}

	public ApkInfo parseApkFile() {

		try {
			apkInfo.retval = aapt.badging(this.apkFile.getAbsolutePath());

			// package: name='me.ele' versionCode='73' versionName='5.8.5'
			// sdkVersion:'14'
			// targetSdkVersion:'22'
			// application-label:'饿了么'
			// application-label-zh_CN:'饿了么'
			// application-icon-160:'res/mipmap-mdpi-v4/icon.png'
			// application-icon-240:'res/mipmap-hdpi-v4/icon.png'
			// application-icon-320:'res/mipmap-xhdpi-v4/icon.png'
			// application-icon-480:'res/mipmap-xxhdpi-v4/icon.png'
			// application-icon-640:'res/mipmap-xxxhdpi-v4/icon.png'
			// application-icon-65535:'res/mipmap-xxxhdpi-v4/icon.png'
			// application: label='饿了么' icon='res/mipmap-mdpi-v4/icon.png'
			// supports-screens: 'small' 'normal' 'large' 'xlarge'
			// supports-any-density: 'true'
			// locales: '--_--' 'zh_CN'
			// densities: '160' '240' '320' '480' '640' '65535'
			// native-code: 'armeabi' 'armeabi-v7a' 'x86'
			// launchable-activity: name='com.bradzhao.crackme.MainActivity'  label='crackme' icon=''
			int max_int_pix = 0;
			ArrayList<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
			String[] lines = retval.split("\n");
			for (String line : lines) {
				String[] lineItem = line.split(":");
				for (int i = 0; i < lineItem.length; i++) {
					if (lineItem[0].equals("package")) {
						// 获取value
						String[] valueItem = lineItem[1].split(" "); // name='me.ele'
																		// versionCode='73'
																		// versionName='5.8.5'
						for (int j = 0; j < valueItem.length; j++) { // name='me.ele'
							String[] valueItemObj = valueItem[j].split("=");// name
																			// 'me.ele'
							if (apkInfo.pkg == null && valueItemObj[0].equals("name"))
								apkInfo.pkg = valueItemObj[1].replace("'", "");
							else if (apkInfo.versionCode == null && valueItemObj[0].equals("versionCode"))
								apkInfo.versionCode = valueItemObj[1].replace("'", "");
							else if (apkInfo.versionName == null && valueItemObj[0].equals("versionName"))
								apkInfo.versionName = valueItemObj[1].replace("'", "");
						}
					} else if (lineItem[0].startsWith("application-icon-")) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(lineItem[0], lineItem[1]);
						mapList.add(map);
						String cur_pix = lineItem[0].replace("application-icon-", "");
						int cur_int_pix = Integer.parseInt(cur_pix);
						max_int_pix = cur_int_pix > max_int_pix ? cur_int_pix : max_int_pix;
					} else if (lineItem[0].startsWith("application-label")) {
						if (apkInfo.name == null) {
							apkInfo.name = lineItem[1].replace("'", "");
						}
					} else if(lineItem[0].startsWith("launchable-activity")){
						// launchable-activity: name='com.bradzhao.crackme.MainActivity'  label='crackme' icon=''
						int startIndex = lineItem[1].indexOf("'");
						int endIndex = lineItem[1].substring(startIndex + 1, lineItem[1].length()).indexOf("'");
						apkInfo.launchableActivity = lineItem[1].substring(startIndex + 1, endIndex + startIndex + 1);
						if(apkInfo.launchableActivity.startsWith(".")){
							apkInfo.launchableActivity = apkInfo.pkg + apkInfo.launchableActivity;
						}
					}
				}
			}

			for (int i = 0; i < mapList.size(); i++) {
				String key = "application-icon-" + max_int_pix;
				if (mapList.get(i).containsKey(key)) {
					if (apkInfo.icon == null) {
						apkInfo.icon = mapList.get(i).get(key).replace("'", "");
						break;
					}
				}
			}
			if (apkInfo.icon != null) {
				apkInfo.logoUrl = aapt.dir_appinfo + File.separator + apkInfo.icon.replace("/", "_");
			} else {
				apkInfo.icon = "";
				apkInfo.logoUrl = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return apkInfo;
	}

	private static String convertToHexString(byte data[]) {
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			strBuffer.append(Integer.toHexString(0xff & data[i]));
		}
		return strBuffer.toString();
	}

	private static String getToken(File apkFile){
		String result = null;
		try {
			MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
			byte[] buffer = FileUtils.readFileToByteArray(apkFile);
			algorithm.update(buffer);
			byte resultData[] = algorithm.digest();
			result = convertToHexString(resultData); 	
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static ApkInfo getInstance(String apkFilePath, String dir_appinfo, String aapt) throws FileNotFoundException, FileNoExecutePermision{
		if(apkInfo == null){
			apkInfo = new ApkInfo(apkFilePath);
		}
		apkInfo.fileSize = String.format("%.2fM", new File(apkFilePath).length() / (1024.0 * 1024));
		apkInfo.hashValue = getToken(new File(apkFilePath));
		apkInfo.aapt = AAPT.getInstance(aapt, dir_appinfo);
		return apkInfo;
	}
}
