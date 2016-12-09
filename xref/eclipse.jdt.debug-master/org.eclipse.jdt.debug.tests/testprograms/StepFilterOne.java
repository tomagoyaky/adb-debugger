/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class StepFilterOne {

	public static void main(String[] args) {
		StepFilterOne sf1 = new StepFilterOne();
		sf1.go();
	}
	
	private void go() {
		StepFilterTwo sf2 = new StepFilterTwo();
		sf2.test();
		sf2.go();
		sf2.test();
		sf2.go();
	}
}

