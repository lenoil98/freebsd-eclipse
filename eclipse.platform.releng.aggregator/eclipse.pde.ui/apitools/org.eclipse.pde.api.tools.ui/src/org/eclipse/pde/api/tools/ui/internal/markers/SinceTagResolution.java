/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
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
package org.eclipse.pde.api.tools.ui.internal.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.api.tools.internal.problems.ApiProblemFactory;
import org.eclipse.pde.api.tools.internal.provisional.IApiMarkerConstants;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.progress.UIJob;

/**
 * This resolution helps users to pick a default API profile when the tooling
 * has been set up but there is no default profile
 *
 * @since 1.0.0
 */
public class SinceTagResolution implements IMarkerResolution2 {
	int kind;
	String newVersionValue;

	public SinceTagResolution(IMarker marker) {
		this.kind = ApiProblemFactory.getProblemKind(marker.getAttribute(IApiMarkerConstants.MARKER_ATTR_PROBLEM_ID, 0));
		this.newVersionValue = marker.getAttribute(IApiMarkerConstants.MARKER_ATTR_VERSION, null);
	}

	@Override
	public String getDescription() {
		if (IApiProblem.SINCE_TAG_INVALID == this.kind) {
			return NLS.bind(MarkerMessages.SinceTagResolution_change_since_tag, this.newVersionValue);
		} else if (IApiProblem.SINCE_TAG_MALFORMED == this.kind) {
			return NLS.bind(MarkerMessages.SinceTagResolution_change_since_tag, this.newVersionValue);
		} else {
			return NLS.bind(MarkerMessages.SinceTagResolution_add_since_tag, this.newVersionValue);
		}
	}

	@Override
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAVADOCTAG);
	}

	@Override
	public String getLabel() {
		if (IApiProblem.SINCE_TAG_INVALID == this.kind) {
			return NLS.bind(MarkerMessages.SinceTagResolution_change_since_tag, this.newVersionValue);
		} else if (IApiProblem.SINCE_TAG_MALFORMED == this.kind) {
			return NLS.bind(MarkerMessages.SinceTagResolution_change_since_tag, this.newVersionValue);
		} else {
			return NLS.bind(MarkerMessages.SinceTagResolution_add_since_tag, this.newVersionValue);
		}
	}

	@Override
	public void run(final IMarker marker) {
		String title = null;
		if (IApiProblem.SINCE_TAG_INVALID == this.kind) {
			title = NLS.bind(MarkerMessages.SinceTagResolution_change_since_tag, this.newVersionValue);
		} else if (IApiProblem.SINCE_TAG_MALFORMED == this.kind) {
			title = NLS.bind(MarkerMessages.SinceTagResolution_change_since_tag, this.newVersionValue);
		} else {
			title = NLS.bind(MarkerMessages.SinceTagResolution_add_since_tag, this.newVersionValue);
		}
		UIJob job = new UIJob(title) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				UpdateSinceTagOperation updateSinceTagOperation = new UpdateSinceTagOperation(marker, SinceTagResolution.this.kind, SinceTagResolution.this.newVersionValue);
				updateSinceTagOperation.run(monitor);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
}
