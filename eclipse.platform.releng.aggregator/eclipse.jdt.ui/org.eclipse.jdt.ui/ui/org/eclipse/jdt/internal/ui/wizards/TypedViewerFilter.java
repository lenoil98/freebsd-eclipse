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
package org.eclipse.jdt.internal.ui.wizards;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter used in selection dialogs.
 */
public class TypedViewerFilter extends ViewerFilter {

	private Class<?>[] fAcceptedTypes;
	private Object[] fRejectedElements;

	/**
	 * Creates a filter that only allows elements of gives types.
	 * @param acceptedTypes The types of accepted elements
	 */
	public TypedViewerFilter(Class<?>[] acceptedTypes) {
		this(acceptedTypes, null);
	}

	/**
	 * Creates a filter that only allows elements of gives types, but not from a
	 * list of rejected elements.
	 * @param acceptedTypes Accepted elements must be of this types
	 * @param rejectedElements Element equals to the rejected elements are
	 * filtered out
	 */
	public TypedViewerFilter(Class<?>[] acceptedTypes, Object[] rejectedElements) {
		Assert.isNotNull(acceptedTypes);
		fAcceptedTypes= acceptedTypes;
		fRejectedElements= rejectedElements;
	}

	/**
	 * @see ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (fRejectedElements != null) {
			for (Object rejectedelement : fRejectedElements) {
				if (element.equals(rejectedelement)) {
					return false;
				}
			}
		}
		for (Class<?> type : fAcceptedTypes) {
			if (type.isInstance(element)) {
				return true;
			}
		}
		return false;
	}

}
