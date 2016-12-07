package com.tomagoyaky.local.debug;

public class IDAHook extends Executor{

	private static IDAHook idaHook;
	public static IDAHook getInstance(String itemValue) {
		if(idaHook == null){
			idaHook = new IDAHook();
		}
		return null;
	}
	public void waitForAttach(int ida_pid) {
		// TODO Auto-generated method stub
		
	}

}
