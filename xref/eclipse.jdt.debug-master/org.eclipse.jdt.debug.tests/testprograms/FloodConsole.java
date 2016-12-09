/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/**
 * Print 10,000 lines of output
 */
public class FloodConsole {

	public static void main(String[] args) {
		for (int i = 0; i < 9999; i++) {
			System.out.println("0---------1--------2--------3-------4--------5--------6--------7--------8");
		}
		System.err.print(" - THE END - ");
	}
}
