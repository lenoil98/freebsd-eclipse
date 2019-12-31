/*******************************************************************************
 * Copyright (c) 2017 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class DialogSettingsCustomizationTest extends UITestCase {

	// See AbstractUIPlugin
	private static final String KEY_DEFAULT_DIALOG_SETTINGS_ROOTURL = "default_dialog_settings_rootUrl"; //$NON-NLS-1$
	private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; //$NON-NLS-1$

	private Path dialogSettingsPath;
	private Field settingsField;
	private TestPlugin testPlugin;
	private Path dialogSettingsPathBackup;
	private String rootUrlValue;

	public DialogSettingsCustomizationTest(String name) {
		super(name);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		testPlugin = TestPlugin.getDefault();
		rootUrlValue = "platform:/plugin/" + testPlugin.getBundle().getSymbolicName() + "/data/dialog_settings_root";
		dialogSettingsPath = testPlugin.getStateLocation().append(FN_DIALOG_SETTINGS).toFile().toPath();
		dialogSettingsPathBackup = testPlugin.getStateLocation().append(FN_DIALOG_SETTINGS + ".back").toFile().toPath();
		if (Files.exists(dialogSettingsPath)) {
			Files.deleteIfExists(dialogSettingsPathBackup);
			Files.move(dialogSettingsPath, dialogSettingsPathBackup);
		}
		settingsField = AbstractUIPlugin.class.getDeclaredField("dialogSettings");
		settingsField.setAccessible(true);
		settingsField.set(testPlugin, null);
	}

	@Override
	protected void doTearDown() throws Exception {
		Files.deleteIfExists(dialogSettingsPath);
		if (Files.exists(dialogSettingsPathBackup)) {
			Files.move(dialogSettingsPathBackup, dialogSettingsPath);
		}
		settingsField.set(testPlugin, null);
		super.doTearDown();
	}

	public void testDialogSettingsContributedByBundle() throws Exception {
		assertDefaultBundleValueIsSet();
		setPreference(PlatformUI.getPreferenceStore(), KEY_DEFAULT_DIALOG_SETTINGS_ROOTURL, rootUrlValue);
		assertCustomValueIsSet();
	}

	public void testDialogSettingsContributedByFileUrl() throws Exception {
		String rootUrl = FileLocator.toFileURL(new URL(rootUrlValue)).toString();
		assertDefaultBundleValueIsSet();
		setPreference(PlatformUI.getPreferenceStore(), KEY_DEFAULT_DIALOG_SETTINGS_ROOTURL, rootUrl);
		assertCustomValueIsSet();
	}

	private void assertDefaultBundleValueIsSet() {
		IDialogSettings section = testPlugin.getDialogSettings().getSection("DialogSettingsCustomizationTest");
		assertNotNull(section);
		assertEquals("defaultBundleValue", section.get("testKey"));
	}

	private void assertCustomValueIsSet() throws Exception {
		// delete previously read value to enforce settings load
		settingsField.set(testPlugin, null);

		IDialogSettings section = testPlugin.getDialogSettings().getSection("DialogSettingsCustomizationTest");
		assertNotNull(section);
		assertEquals("testValue", section.get("testKey"));
	}

}
