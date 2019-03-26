/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
package org.eclipse.jsp;

import org.eclipse.core.indexsearch.IIndexQuery;
import org.eclipse.core.indexsearch.ISearchResultCollector;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;


public class JspSearchEngine {

	public static void search(final IJavaSearchResultCollector collector, final IIndexQuery query, IProgressMonitor pm) {
		
		System.out.println("JspSearchEngine.search: " + query); //$NON-NLS-1$
									
		JspUIPlugin.getDefault().search(
			query,
			new ISearchResultCollector() {
				@Override
				public void accept(IResource resource, int start, int length) throws CoreException {
					System.out.println("  accept: " + resource); //$NON-NLS-1$
					collector.accept(resource, start, start+length, null, 999);
				}
			},
			pm
		);
	}
}
