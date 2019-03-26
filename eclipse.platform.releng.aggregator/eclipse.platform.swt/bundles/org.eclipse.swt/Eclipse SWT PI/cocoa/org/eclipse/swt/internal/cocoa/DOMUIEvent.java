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

public class DOMUIEvent extends DOMEvent {

public DOMUIEvent() {
	super();
}

public DOMUIEvent(long /*int*/ id) {
	super(id);
}

public DOMUIEvent(id id) {
	super(id);
}

public int detail() {
	return (int)/*64*/OS.objc_msgSend(this.id, OS.sel_detail);
}

}
