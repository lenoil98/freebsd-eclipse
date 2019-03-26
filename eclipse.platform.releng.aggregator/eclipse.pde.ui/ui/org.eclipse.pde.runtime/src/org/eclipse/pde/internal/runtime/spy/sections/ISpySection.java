/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Chris Aniszczyk <zx@us.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.runtime.spy.sections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.pde.internal.runtime.spy.SpyFormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @since 3.4
 */
public interface ISpySection {

	public void build(ScrolledForm form, SpyFormToolkit toolkit, ExecutionEvent event);

}
