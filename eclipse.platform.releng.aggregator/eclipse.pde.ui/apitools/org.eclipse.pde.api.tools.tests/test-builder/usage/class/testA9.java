/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
package x.y.z;

import java.util.List;

import c.ClassUsageClass;

public class testA9 {

	public List<String> m1() {
		class inner extends ClassUsageClass {
			
		}
		new inner();
		return null;
	}
}
