package com.tomagoyaky.local.debug;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.exec.ExecuteException;

public class Runntime extends Executor{

	public static int getpid(ADB adb, String pkg) throws ExecuteException, IOException {
		String retStr = adb.shell("ps | grep " + pkg, System.getProperty("user.dir"), 0, 0);
		// u0_a80    11695 178   871556 33828 ffffffff 00000000 S com.bradzhao.crackme
		String[] items = retStr.split(" ");
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < items.length; i++) {
			String item = items[i];
			if(!item.equals("")){
				list.add(item);
			}
		}
		return Integer.parseInt(list.get(1));
	}

}
