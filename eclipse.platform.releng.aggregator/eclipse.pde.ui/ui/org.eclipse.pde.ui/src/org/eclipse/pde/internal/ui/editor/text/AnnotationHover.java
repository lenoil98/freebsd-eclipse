/*******************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
package org.eclipse.pde.internal.ui.editor.text;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class AnnotationHover implements IAnnotationHover {

	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		String[] messages = getMessagesForLine(sourceViewer, lineNumber);

		if (messages.length == 0)
			return null;

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < messages.length; i++) {
			buffer.append(messages[i]);
			if (i < messages.length - 1)
				buffer.append(System.lineSeparator());
		}
		return buffer.toString();
	}

	private String[] getMessagesForLine(ISourceViewer viewer, int line) {
		IDocument document = viewer.getDocument();
		IAnnotationModel model = viewer.getAnnotationModel();

		if (model == null)
			return new String[0];

		ArrayList<String> messages = new ArrayList<>();

		Iterator<?> iter = model.getAnnotationIterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof MarkerAnnotation) {
				MarkerAnnotation annotation = (MarkerAnnotation) object;
				if (compareRulerLine(model.getPosition(annotation), document, line)) {
					IMarker marker = annotation.getMarker();
					String message = marker.getAttribute(IMarker.MESSAGE, (String) null);
					if (message != null && message.trim().length() > 0) {
						// if version change marker, also put the description in
						// hover
						String problemKind = marker.getAttribute("version", null); //$NON-NLS-1$
						if (problemKind != null) {
							String descr = marker.getAttribute("description", null); //$NON-NLS-1$ //
							message = NLS.bind(PDEUIMessages.AnnotationHover_version_change,
									new String[] { message, descr });

						}
						messages.add(message);
					}
				}
			}
		}
		return messages.toArray(new String[messages.size()]);
	}

	private boolean compareRulerLine(Position position, IDocument document, int line) {

		try {
			if (position.getOffset() > -1 && position.getLength() > -1) {
				return document.getLineOfOffset(position.getOffset()) == line;
			}
		} catch (BadLocationException e) {
		}
		return false;
	}
}
