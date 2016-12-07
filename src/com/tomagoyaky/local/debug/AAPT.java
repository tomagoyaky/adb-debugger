package com.tomagoyaky.local.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AAPT extends Executor{

	private String file_AAPT;
	public String dir_appinfo;

	private static AAPT aapt;
	public static AAPT getInstance(String file_AAPT, String dir_appinfo) throws FileNotFoundException, FileNoExecutePermision {
		if(aapt == null){
			aapt = new AAPT(file_AAPT, dir_appinfo);
		}
		return aapt;
	}
	
	public AAPT(String file_AAPT, String dir_appinfo) throws FileNoExecutePermision, FileNotFoundException{
		this.dir_appinfo = dir_appinfo;
		this.setAAPT(file_AAPT);

		if(!new File(file_AAPT).exists()) throw new FileNotFoundException(file_AAPT);
		if(new File(file_AAPT).canExecute() == false) throw new FileNoExecutePermision(file_AAPT);
	}

	public void setAAPT(String file_AAPT){
		this.file_AAPT = file_AAPT;
	}
	
	public String getAAPT() throws FileNotFoundException{
		if(!new File(file_AAPT).exists()) throw new FileNotFoundException(file_AAPT);
		return file_AAPT;
	}
	
	public String badging(String apkFilePath) throws FileNoExecutePermision, IOException {
		String retVal = execute(getAAPT() + " d badging \"" + apkFilePath + "\"", this.dir_appinfo, 0, 0);
		// Logger.Logger.debug(retVal.replace("\r\r\n", "\n"));
		return retVal;
	}
}
