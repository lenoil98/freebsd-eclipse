/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.dialogs;


/**
 * IRepositoryManipulationHood defines callbacks that are called when the
 * UI is manipulating repositories.
 */
public interface IRepositoryManipulationHook {
	public void preManipulateRepositories();

	public void postManipulateRepositories();
}