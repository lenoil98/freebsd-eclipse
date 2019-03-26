/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.cocoa;

public class NSDirectoryEnumerator extends NSEnumerator {

public NSDirectoryEnumerator() {
	super();
}

public NSDirectoryEnumerator(long /*int*/ id) {
	super(id);
}

public NSDirectoryEnumerator(id id) {
	super(id);
}

public void skipDescendents() {
	OS.objc_msgSend(this.id, OS.sel_skipDescendents);
}

}
