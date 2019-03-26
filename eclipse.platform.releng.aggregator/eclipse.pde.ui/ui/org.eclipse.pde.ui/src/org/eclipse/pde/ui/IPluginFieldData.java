/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.pde.ui;

/**
 * In addition to field data from the 'New Project' wizard pages, this interface
 * provides choices made by the user that are unique to creating a new plug-in
 * project.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.0
 */
public interface IPluginFieldData extends IFieldData {
	/**
	 * The class name field.
	 *
	 * @return the name of the plug-in class
	 */
	String getClassname();

	/**
	 * UI plug-in selection.
	 *
	 * @return <code>true</code> if the plug-in contains UI code and
	 *         extensions, <code>false</code> otherwise.
	 */
	boolean isUIPlugin();

	/**
	 * Plug-in class generation selection.
	 *
	 * @return <code>true</code> if the plug-in class is to be generated by
	 *         the plug-in wizard, <code>false</code> otherwise.
	 */
	boolean doGenerateClass();

}
