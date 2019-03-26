/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.compare.internal;


public class ReplaceWithPreviousEditionAction extends EditionAction {

	public ReplaceWithPreviousEditionAction() {
		super(true,
			"org.eclipse.compare.internal.ReplaceWithEditionAction");	//$NON-NLS-1$
		fPrevious= true;
	}
}
