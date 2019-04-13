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
package org.eclipse.pde.api.tools.tests;

import org.eclipse.core.runtime.Plugin;

/**
 * Test Plug-in Class
 */
public class ApiTestsPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.pde.api.tools.tests";//$NON-NLS-1$

	private static ApiTestsPlugin fgDefault = null;

	public ApiTestsPlugin() {
		fgDefault = this;
	}

	/**
	 * Returns the test plug-in.
	 *
	 * @return the test plug-in
	 */
	public static ApiTestsPlugin getDefault() {
		return fgDefault;
	}

}