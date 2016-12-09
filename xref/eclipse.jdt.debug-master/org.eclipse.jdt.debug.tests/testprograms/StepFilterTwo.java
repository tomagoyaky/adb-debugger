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

public class StepFilterTwo {

	private StepFilterThree sf3;

	public StepFilterTwo() {
		sf3 = new StepFilterThree();
	}

	protected void go() {
		sf3.go();
	}
	
	void test() {
		for (int i = 0; i < 10; i++);
	}
	
	/**
	 * This test method should only be called by the contributed step filter tests
	 * @see TestContributedStepFilter
	 */
	void contributed() {
	}
}

