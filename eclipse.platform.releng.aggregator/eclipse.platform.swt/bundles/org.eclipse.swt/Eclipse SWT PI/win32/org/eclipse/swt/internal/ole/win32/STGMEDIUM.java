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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.ole.win32;

public final class STGMEDIUM {
	public int tymed;
	/** @field accessor=hGlobal,cast=(HGLOBAL) */
	public long /*int*/ unionField;
	/** @field cast=(IUnknown *) */
	public long /*int*/ pUnkForRelease;
	public static final int sizeof = COM.STGMEDIUM_sizeof ();
}
