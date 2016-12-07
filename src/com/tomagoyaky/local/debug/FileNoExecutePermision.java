package com.tomagoyaky.local.debug;

/**
 * 没有执行权限异常
 */
public class FileNoExecutePermision extends Exception {

	private static final long serialVersionUID = 1L;

	public FileNoExecutePermision(String message) {
		super(message);
	}
}
