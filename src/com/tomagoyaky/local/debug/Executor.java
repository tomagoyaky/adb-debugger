package com.tomagoyaky.local.debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

public class Executor extends Logger{

	public static OutputStream nullOS = new OutputStream() {
		@Override
		public void write(int b) throws IOException {}
	};

	/**
	 * <p> 执行命令并返回布尔类型结果 </p>
	 * @param cmdlineStr	命令行
	 * @param cwd	当前执行目录
	 * @param exitValue	结束值
	 * @param timeout	超时，当值为0时，表示不设置超时
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @return boolean
	 * */
	public static boolean execute(String cmdlineStr, String cwd, int exitValue, OutputStream os, int timeout) throws ExecuteException, IOException{
		
		logd("[CMD]" + cmdlineStr);
		CommandLine cmdLine = CommandLine.parse(cmdlineStr);
		DefaultExecutor	executor = new DefaultExecutor();
		PumpStreamHandler streamHandler = new PumpStreamHandler(os); 
		executor.setStreamHandler(streamHandler);
		executor.setExitValue(exitValue);
		executor.setWorkingDirectory(new File(cwd));
		if(timeout != 0){
			executor.setWatchdog(new ExecuteWatchdog(timeout));
		}
		executor.execute(cmdLine);
		return true;
	}
	
	public static boolean execute(String cmdlineStr, String cwd, OutputStream os, int timeout){

//		log("[CMD]" + cmdlineStr);
		CommandLine cmdLine = CommandLine.parse(cmdlineStr);
		DefaultExecutor	executor = new DefaultExecutor();
		PumpStreamHandler streamHandler = new PumpStreamHandler(os); 
		executor.setStreamHandler(streamHandler);
		executor.setWorkingDirectory(new File(cwd));
		if(timeout != 0){
			executor.setWatchdog(new ExecuteWatchdog(timeout));
		}
		try {
			executor.execute(cmdLine);
		} catch (ExecuteException e) {
		} catch (IOException e) {
		}
		return true;
	}
	
	/**
	 * <p> 执行命令并返回字符串类型结果 </p>
	 * @param cmdlineStr	命令行
	 * @param cwd	当前执行目录
	 * @param exitValue	结束值
	 * @param timeout	超时，当值为0时，表示不设置超时
	 * @throws IOException 
	 * @throws ExecuteException 
	 * @return String
	 * */
	public static String execute(String cmdlineStr, String cwd, int exitValue, int timeout) throws ExecuteException, IOException{
		
//		log("[CMD]" + cmdlineStr);
		CommandLine cmdLine = CommandLine.parse(cmdlineStr);
		DefaultExecutor	executor = new DefaultExecutor();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream errorStream = new ByteArrayOutputStream(); 
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream); 
		executor.setStreamHandler(streamHandler);
		executor.setExitValue(exitValue);
		executor.setWorkingDirectory(new File(cwd));
		if(timeout != 0){
			executor.setWatchdog(new ExecuteWatchdog(timeout));
		}
		executor.execute(cmdLine);
		String out = outputStream.toString("utf-8");
		String error = errorStream.toString("utf-8");
		return out + error;
	}
}
