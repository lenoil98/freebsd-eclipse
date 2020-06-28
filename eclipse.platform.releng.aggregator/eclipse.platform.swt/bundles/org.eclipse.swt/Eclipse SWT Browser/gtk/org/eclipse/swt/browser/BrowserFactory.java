/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
package org.eclipse.swt.browser;

import org.eclipse.swt.internal.gtk.*;

class BrowserFactory {

WebBrowser createWebBrowser (int style) {
	if (OS.IsWin32) return null;
	if (GTK.GTK4) return null;
	boolean webkitInstalled = WebKit.IsInstalled ();
	if (!webkitInstalled) return null;

	return new WebKit ();
}

}
