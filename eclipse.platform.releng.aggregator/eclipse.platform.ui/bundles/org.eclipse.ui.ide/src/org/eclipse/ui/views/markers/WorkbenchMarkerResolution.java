/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.views.markers.internal.Util;

/**
 * WorkbenchMarkerResolution is the resolution that can be grouped
 * with others that are similar to allow multi selection.
 * @since 3.2
 *
 */
public abstract class WorkbenchMarkerResolution implements IMarkerResolution2 {

	/**
	 * Iterate through the list of supplied markers. Return any that can also have
	 * the receiver applied to them.
	 * @param markers
	 * @return IMarker[]
	 *
	 * */
	public abstract IMarker[] findOtherMarkers(IMarker[] markers);

	/**
	 * Runs this resolution. Resolve all <code>markers</code>.
	 * <code>markers</code> must be a subset of the markers returned
	 * by <code>findOtherMarkers(IMarker[])</code>.
	 *
	 * @param markers The markers to resolve, not null
	 * @param monitor The monitor to report progress
	 */
	public void run(IMarker[] markers, IProgressMonitor monitor) {

		for (IMarker marker : markers) {
			monitor.subTask(Util.getProperty(IMarker.MESSAGE, marker));
			run(marker);
		}
	}
}
