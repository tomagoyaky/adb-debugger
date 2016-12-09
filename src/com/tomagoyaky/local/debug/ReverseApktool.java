package com.tomagoyaky.local.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

public class ReverseApktool extends Executor {

	/**
	 * <p>功能:使用apktool进行反编译</p>
	 * @author tomagoyaky
	 * @param jar_apktool 	apktool.jar
	 * @param apkFilePath	apk文件路径
	 * @param dir_decompile	反编译目录
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static boolean decompile(String jar_apktool, String apkFilePath, String dir_decompile) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		// java -jar %ResourcePath%\apktool.jar d -f %APK_pro% -o %apktoolTempDir%
		return execute("java -jar \"" + jar_apktool + "\" d -f \"" + apkFilePath + "\" -o \"" + dir_decompile + "\"" , 
				System.getProperty("user.dir"), 0, System.out, 0);
	}

	/**
	 * <p>功能:使用apktool进行回编译</p>
	 * @author tomagoyaky
	 * @param jar_apktool 	apktool.jar
	 * @param dir_decompile	反编译目录
	 * @param apkFilePath	生成的apk文件路径
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static boolean bacompile(String jar_apktool, String dir_decompile, String apkFilePath) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		// java -jar %ResourcePath%\apktool.jar b %apktoolTempDir% -o %APK_tmp%
		return execute("java -jar \"" + jar_apktool + "\" b \"" + dir_decompile + "\" -o \"" + apkFilePath + "\"" , 
				System.getProperty("user.dir"), 0, System.out, 0);
	}

	/**
	 * <p>功能:使用AXMLPrinter对AndroidManifest.xml进行解码</p>
	 * @author tomagoyaky
	 * @param androidManifest_xml	加密文本
	 * @param androidManifest_txt	明文
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @category java -jar %ResourcePath%\AXMLPrinter2.jar .\UnzipPackage\AndroidManifest.xml > .\DismPackage\AndroidManifest.txt
	 */
	public static void DecodeAndroidManifest(String jar_AXMLPrinter, String androidManifest_xml, String androidManifest_txt) throws IOException, ParserConfigurationException, SAXException {
		String resultdata = execute("java -jar \"" + jar_AXMLPrinter + "\" \"" + androidManifest_xml + "\"" , 
				System.getProperty("user.dir"), 0, 0);
		FileUtils.write(new File(androidManifest_txt), resultdata);
	}
	
	/**
	 * <p>功能:使用smali库反编译</p>
	 * @author tomagoyaky
	 * @param jar_baksmali 		baksmali.jar
	 * @param dir_decompile 	反汇编目录
	 * @param dexFilePath		dex文件路径
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @category 示例:{java -jar %ResourcePath%\baksmali-2.0.3.jar -a 17 %DEX% -o %DismDir%} 
	 * */
	public static boolean baksmali(String jar_baksmali, String dir_decompile, String dexFilePath) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		return execute("java -jar \"" + jar_baksmali + "\" -a 19 \"" + dexFilePath + "\" -o \"" + dir_decompile + "\"",
				System.getProperty("user.dir"), 0, System.out, 0);
	}
	/**
	 * <p>功能:使用smali库反编译(支持定制)</p>
	 * @author tomagoyaky
	 * @param jar_baksmali 		baksmali.jar
	 * @param dir_decompile 	反汇编目录
	 * @param dexFilePath		dex文件路径
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @category 示例:{java -jar %ResourcePath%\baksmali-2.0.3.jar -a 17 %DEX% -o %DismDir%} 
	 * */
	public static boolean baksmali(String jar_baksmali, String dir_decompile, String dexFilePath, String cwd, int retval, OutputStream os) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		return execute("java -jar \"" + jar_baksmali + "\" -a 19 \"" + dexFilePath + "\" -o \"" + dir_decompile + "\"" , 
				cwd, retval, os, 0);
	}
	
	/**
	 * <p>功能:使用smali库回编译</p>
	 * @author tomagoyaky
	 * @param jar_smali 	smali.jar
	 * @param dir_decompile	反汇编的目录
	 * @param targetDexFilePath	生成的目标dex文件的路径
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @category 示例:{java -jar %ResourcePath%\smali-2.0.3.jar -a 17 -o %Output_DEX% %Smali_Dir%}
	 * */
	public static boolean smali(String jar_smali, String dir_decompile, String targetDexFilePath) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		return smali(jar_smali, dir_decompile, targetDexFilePath, System.getProperty("user.dir"), 0, System.out);
	}

	/**
	 * <p>功能:使用smali库回编译(支持定制)</p>
	 * @author tomagoyaky
	 * @param jar_smali 	smali.jar
	 * @param dir_decompile	反汇编的目录
	 * @param targetFilePath	生成的目标dex文件的路径
	 * @param cwd	当前的工作目录
	 * @param retval	CMD结束值
	 * @param os	异步输出流
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @category 示例:{java -jar %ResourcePath%\smali-2.0.3.jar -a 17 -o %Output_DEX% %Smali_Dir%}
	 * */
	public static boolean smali(String jar_smali, String dir_decompile, String targetDexFilePath, String cwd, int retval, OutputStream os) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		return execute("java -jar \"" + jar_smali + "\" -a 19  -o \"" + dir_decompile + "\" " + "\"" + targetDexFilePath + "\"" , 
				cwd, retval, os, 0);
	}

	/**
	 * <p>功能:对目录下的多个dex文件进行反汇编</p>
	 * @author tomagoyaky
	 * @param jar_baksmali 		baksmali.jar
	 * @param dexFilePathList 	dex文件列表
	 * @param dir_workplace 	反汇编目录的父目录
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * */ 
	public static void baksmaliMultiDex(String jar_baksmali, ArrayList<String> dexFilePathList, String dir_workplace) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		if(!new File(jar_baksmali).exists()) throw new FileNotFoundException(jar_baksmali);
		if(!new File(dir_workplace).exists()) throw new FileNotFoundException(dir_workplace);
		for (int i = 0; i < dexFilePathList.size(); i++) {
			String filePath = dexFilePathList.get(i);
			String dir_decompile = dir_workplace + File.separator + "disassembly_" + String.format("%02d", i + 1);
			if(!new File(filePath).exists()) throw new FileNotFoundException(filePath);
			execute("java -jar \"" + jar_baksmali + "\" -a 19 \"" + filePath + "\" -o \"" + dir_decompile + "\"" , 
					System.getProperty("user.dir"), 0, System.out, 0);
		}
	}
	/**
	 * <p>功能:对目录下的多个dex文件进行反汇编(支持定制)</p>
	 * @author tomagoyaky
	 * @param jar_baksmali 		baksmali.jar
	 * @param dexFilePathList 	dex文件列表
	 * @param dir_workplace 	反汇编目录的父目录
	 * @param cwd	当前的工作目录
	 * @param retval	CMD结束值
	 * @param os	异步输出流
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * */ 
	public static void baksmaliMultiDex(String jar_baksmali, ArrayList<String> dexFilePathList, String dir_workplace, String cwd, int retval, OutputStream os) throws ExecuteException, IOException, ParserConfigurationException, SAXException {
		for (int i = 0; i < dexFilePathList.size(); i++) {
			String filePath = dexFilePathList.get(i);
			String dir_decompile = dir_workplace + File.separator + "disassembly_" + String.format("%02d", i + 1);
			execute("java -jar \"" + jar_baksmali + "\" -a 19 \"" + filePath + "\" -o \"" + dir_decompile + "\"" , 
					cwd, retval, os, 0);
		}
	}
}
