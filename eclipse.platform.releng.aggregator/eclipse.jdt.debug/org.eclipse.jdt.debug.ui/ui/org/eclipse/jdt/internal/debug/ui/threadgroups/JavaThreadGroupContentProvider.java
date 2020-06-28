/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.internal.debug.ui.threadgroups;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaThreadGroup;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaElementContentProvider;

/**
 * @since 3.3
 */
public class JavaThreadGroupContentProvider extends JavaElementContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	@Override
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		int count = 0;
		if (element instanceof IJavaThreadGroup) {
			IJavaThreadGroup group = (IJavaThreadGroup) element;
			if (isAvailable(group)) {
				count += group.getThreadGroups().length;
				count += group.getThreads().length;
			}
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		if (parent instanceof IJavaThreadGroup) {
			return getElements(getChildren((IJavaThreadGroup) parent), index, length);
		}
		return EMPTY;
	}

	protected Object[] getChildren(IJavaThreadGroup group) throws CoreException {
		if (isAvailable(group)) {
			IJavaThreadGroup[] threadGroups = group.getThreadGroups();
			IJavaThread[] threads = group.getThreads();
			Object[] kids = new Object[threadGroups.length + threads.length];
			int index = 0;
			for (IJavaThread thread : threads) {
				kids[index]= thread;
				index++;
			}
			for (IJavaThreadGroup threadGroup : threadGroups) {
				kids[index] = threadGroup;
				index++;
			}
			return kids;
		}
		return EMPTY;
	}

	protected boolean isAvailable(IJavaThreadGroup group) {
		IDebugTarget debugTarget = group.getDebugTarget();
		return !(debugTarget.isTerminated() || debugTarget.isDisconnected());
	}

}
