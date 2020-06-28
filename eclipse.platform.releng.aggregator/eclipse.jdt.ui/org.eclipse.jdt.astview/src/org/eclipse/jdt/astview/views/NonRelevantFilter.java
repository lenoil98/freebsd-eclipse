/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

package org.eclipse.jdt.astview.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class NonRelevantFilter extends ViewerFilter {

	private boolean fShowNonRelevant;

	public boolean isShowNonRelevant() {
		return fShowNonRelevant;
	}

	public void setShowNonRelevant(boolean showNonRelevant) {
		fShowNonRelevant= showNonRelevant;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (fShowNonRelevant)
			return true;

		if (element instanceof Binding) {
			return ((Binding) element).isRelevant();
		}
		if (element instanceof BindingProperty) {
			return ((BindingProperty) element).isRelevant();
		}
		return true;
	}


}
