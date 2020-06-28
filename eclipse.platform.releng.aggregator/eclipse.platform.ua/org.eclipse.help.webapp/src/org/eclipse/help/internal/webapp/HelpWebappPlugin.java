/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     George Suaridze <suag@1c.ru> (1C-Soft LLC) - Bug 560168
 *******************************************************************************/
package org.eclipse.help.internal.webapp;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Welp web application plug-in.
 */
public class HelpWebappPlugin extends Plugin {
	public final static String PLUGIN_ID = "org.eclipse.help.webapp"; //$NON-NLS-1$

	// debug options
	public static boolean DEBUG = false;

	public static boolean DEBUG_WORKINGSETS = false;

	protected static HelpWebappPlugin plugin;

	/**
	 * Logs an Error message with an exception.
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = ""; //$NON-NLS-1$
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK,
				message, ex);
		HelpWebappPlugin.getDefault().getLog().log(errorStatus);
	}

	/**
	 * Logs a Warning message with an exception. Note that the message should
	 * already be localized to proper locale. ie: WebappResources.getString()
	 * should already have been called
	 */
	public static synchronized void logWarning(String message) {
		if (HelpWebappPlugin.DEBUG) {
			if (message == null)
				message = ""; //$NON-NLS-1$
			Status warningStatus = new Status(IStatus.WARNING, PLUGIN_ID,
					IStatus.OK, message, null);
			HelpWebappPlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * @return the singleton instance of the help webapp plugin
	 */
	public static HelpWebappPlugin getDefault() {
		return plugin;
	}

	private static BundleContext bundleContext;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
		// Setup debugging options
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_WORKINGSETS = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.help.webapp/debug/workingsets")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		//bundleContext = null;
		super.stop(context);
	}

	public static BundleContext getContext() {
		return bundleContext;
	}
}
