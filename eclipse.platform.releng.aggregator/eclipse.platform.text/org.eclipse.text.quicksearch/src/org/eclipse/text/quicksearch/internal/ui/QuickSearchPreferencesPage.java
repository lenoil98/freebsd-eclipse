/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.quicksearch.internal.core.preferences.QuickSearchPreferences;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class QuickSearchPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public QuickSearchPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(QuickSearchActivator.getDefault().getPreferenceStore());
		QuickSearchPreferences.initializeDefaults();
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	private static final String[] prefsKeys = {
			QuickSearchPreferences.IGNORED_EXTENSIONS,
			QuickSearchPreferences.IGNORED_PREFIXES,
			QuickSearchPreferences.IGNORED_NAMES
	};

	private static final String[] fieldLabels = {
			Messages.QuickSearchPreferencesPage_Ignored_Extensions, Messages.QuickSearchPreferencesPage_Ignored_Prefixes, Messages.QuickSearchPreferencesPage_Ignored_Names
	};

	private static final String[] toolTips = {
			Messages.QuickSearchPreferencesPage_Tooltip_Extensions
		,
			Messages.QuickSearchPreferencesPage_Tooltip_Prefixes
		,
			Messages.QuickSearchPreferencesPage_Tooltip_Names
	};

	@Override
	protected void createFieldEditors() {
		IntegerFieldEditor fieldMaxLineLen = new IntegerFieldEditor(QuickSearchPreferences.MAX_LINE_LEN, Messages.QuickSearchPreferencesPage_MaxLineLength, getFieldEditorParent());
		fieldMaxLineLen.getTextControl(getFieldEditorParent()).setToolTipText(
				Messages.QuickSearchPreferencesPage_Tooltip_MaxLineLength);
		addField(fieldMaxLineLen);

		for (int i = 0; i < fieldLabels.length; i++) {
			final String tooltip = toolTips[i];
			StringFieldEditor field = new StringFieldEditor(prefsKeys[i], fieldLabels[i], StringFieldEditor.UNLIMITED, 5, StringFieldEditor.VALIDATE_ON_FOCUS_LOST, getFieldEditorParent()) {
				@Override
				protected Text createTextWidget(Composite parent) {
					Text w = super.createTextWidget(parent);
					w.setToolTipText(tooltip);
					return w;
				}

				@Override
				protected void doFillIntoGrid(Composite parent, int numColumns) {
					super.doFillIntoGrid(parent, numColumns);
					Text text = getTextControl();
					GridData layout = (GridData) text.getLayoutData();
					layout.widthHint = 400;
					layout.minimumWidth = 100;
				}
			};
			addField(field);
		}
	}
}
