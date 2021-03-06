/*******************************************************************************
 * Copyright (c) May 28, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.targets;

public class HcrClass7 {

	public void run() {
		class Local {
			class Inner {
				public void run() {
					String s = new String("Local$Inner#run()");
				}
			}
			
			public void run() {
				new Inner().run();
			}
		}
		new Local().run();
	}
	
	public static void main(String[] args) {
		HcrClass7 clazz = new HcrClass7();
		clazz.run();
	}
}
