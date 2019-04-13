/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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

import internal.x.y.z.Iinternal;

/**
 * 
 */
public interface Etest8 extends Iinternal {
	interface inner {
		
	}
	interface inner2 {
		interface inner extends Iinternal {
			
		}
	}
	interface inner3 extends Iinternal {
		
	}
}

interface outer {
	interface inner extends Iinternal {
		
	}
}

interface outer2 {
	interface inner {
		interface inner2 extends Iinternal {
			
		}
	}
}