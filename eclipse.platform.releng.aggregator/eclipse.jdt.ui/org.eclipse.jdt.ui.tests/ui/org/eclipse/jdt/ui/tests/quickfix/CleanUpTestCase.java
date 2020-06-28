/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.ui.tests.quickfix;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.jdt.internal.core.manipulation.CodeTemplateContextType;
import org.eclipse.jdt.internal.core.manipulation.StubUtility;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpPreferenceUtil;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jdt.ui.cleanup.ICleanUp;
import org.eclipse.jdt.ui.tests.core.rules.ProjectTestSetup;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.preferences.cleanup.CleanUpProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.Profile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileStore;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CleanUpStressTest.class,
	CleanUpTest.class,
	CleanUpTest1d5.class,
	CleanUpTest1d7.class,
	CleanUpTest1d8.class,
	CleanUpTest1d10.class,
	CleanUpAnnotationTest.class,
	SaveParticipantTest.class,
	CleanUpActionTest.class,
	NullAnnotationsCleanUpTest1d8.class
})
public class CleanUpTestCase extends QuickFixTest {
	protected static final String FIELD_COMMENT= "/* Test */";

	protected IPackageFragmentRoot fSourceFolder;
	protected IJavaProject fJProject1;

	private CustomProfile fProfile;

	@Before
	public void setUp() throws Exception {
		Hashtable<String, String> options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");

		JavaCore.setOptions(options);

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, true);
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "//TODO\n${body_statement}", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.OVERRIDECOMMENT_ID, "/* comment */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.FIELDCOMMENT_ID, FIELD_COMMENT, null);

		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");

		fJProject1= getProject();

		fSourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		Map<String, String> settings= new Hashtable<>();
		fProfile= new ProfileManager.CustomProfile("testProfile", settings, CleanUpProfileVersioner.CURRENT_VERSION, CleanUpProfileVersioner.PROFILE_KIND);
		InstanceScope.INSTANCE.getNode(JavaUI.ID_PLUGIN).put(CleanUpConstants.CLEANUP_PROFILE, fProfile.getID());
		InstanceScope.INSTANCE.getNode(JavaUI.ID_PLUGIN).put(CleanUpConstants.SAVE_PARTICIPANT_PROFILE, fProfile.getID());

		disableAll();
	}

	@After
	public void tearDown() throws Exception {
		JavaProjectHelper.clear(fJProject1, getDefaultClasspath());
		disableAll();
		fJProject1= null;
		fSourceFolder= null;
		fProfile= null;
	}

	protected IJavaProject getProject() {
		return ProjectTestSetup.getProject();
	}

	protected IClasspathEntry[] getDefaultClasspath() throws CoreException {
		return ProjectTestSetup.getDefaultClasspath();
	}

	private void disableAll() throws CoreException {
		Map<String, String> settings= fProfile.getSettings();
		CleanUpOptions options= JavaPlugin.getDefault().getCleanUpRegistry().getDefaultOptions(CleanUpConstants.DEFAULT_CLEAN_UP_OPTIONS);
		for (String key : options.getKeys()) {
			settings.put(key, CleanUpOptions.FALSE);
		}
		commitProfile();
	}

	protected void disable(String key) throws CoreException {
		fProfile.getSettings().put(key, CleanUpOptions.FALSE);
		commitProfile();
	}

	protected void enable(String key) throws CoreException {
		fProfile.getSettings().put(key, CleanUpOptions.TRUE);
		commitProfile();
	}

	private void commitProfile() throws CoreException {
		List<Profile> profiles= CleanUpPreferenceUtil.getBuiltInProfiles();
		profiles.add(fProfile);

		CleanUpProfileVersioner versioner= new CleanUpProfileVersioner();
		ProfileStore profileStore= new ProfileStore(CleanUpConstants.CLEANUP_PROFILES, versioner);
		profileStore.writeProfiles(profiles, InstanceScope.INSTANCE);

		CleanUpPreferenceUtil.saveSaveParticipantOptions(InstanceScope.INSTANCE, fProfile.getSettings());
	}

	protected RefactoringStatus assertRefactoringResultAsExpected(ICompilationUnit[] cus, String[] expected) throws CoreException {
		RefactoringStatus status= performRefactoring(cus);

		String[] previews= new String[cus.length];
		for (int i= 0; i < cus.length; i++) {
			ICompilationUnit cu= cus[i];
			previews[i]= cu.getBuffer().getContents();
		}

		assertEqualStringsIgnoreOrder(previews, expected);

		return status;
	}

	protected void assertRefactoringResultAsExpectedIgnoreHashValue(ICompilationUnit[] cus, String[] expected) throws CoreException {
		performRefactoring(cus);

		Pattern regex= Pattern.compile("long serialVersionUID = .*L;");

		String[] previews= new String[cus.length];
		for (int i= 0; i < cus.length; i++) {
			ICompilationUnit cu= cus[i];
			previews[i]= cu.getBuffer().getContents().replaceAll(regex.pattern(), "long serialVersionUID = 1L;");
		}

		assertEqualStringsIgnoreOrder(previews, expected);
	}

	protected RefactoringStatus assertRefactoringHasNoChange(ICompilationUnit[] cus) throws CoreException {
		String[] expected= new String[cus.length];
		for (int i= 0; i < cus.length; i++) {
			expected[i]= cus[i].getBuffer().getContents();
		}
		return assertRefactoringResultAsExpected(cus, expected);
	}

	protected final RefactoringStatus performRefactoring(ICompilationUnit[] cus) throws CoreException {
		final CleanUpRefactoring ref= new CleanUpRefactoring();
		ref.setUseOptionsFromProfile(true);
		ICleanUp[] cleanUps= JavaPlugin.getDefault().getCleanUpRegistry().createCleanUps();

		return performRefactoring(ref, cus, cleanUps);
	}

	protected RefactoringStatus performRefactoring(final CleanUpRefactoring ref, ICompilationUnit[] cus, ICleanUp[] cleanUps) throws CoreException {
		for (ICompilationUnit cu : cus) {
			ref.addCompilationUnit(cu);
		}
		for (ICleanUp cleanUp : cleanUps) {
			ref.addCleanUp(cleanUp);
		}

		IUndoManager undoManager= getUndoManager();
		final CreateChangeOperation create= new CreateChangeOperation(
			new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS),
			RefactoringStatus.FATAL);

		final PerformChangeOperation perform= new PerformChangeOperation(create);
		perform.setUndoManager(undoManager, ref.getName());

		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		executePerformOperation(perform, workspace);

		RefactoringStatus status= create.getConditionCheckingStatus();
		if (status.hasFatalError()) {
			throw new CoreException(new StatusInfo(status.getSeverity(), status.getMessageMatchingSeverity(status.getSeverity())));
		}

		assertTrue("Change wasn't executed", perform.changeExecuted());

		Change undo= perform.getUndoChange();
		assertNotNull("Undo doesn't exist", undo);
		assertTrue("Undo manager is empty", undoManager.anythingToUndo());

		return status;
	}

	private IUndoManager getUndoManager() {
		IUndoManager undoManager= RefactoringCore.getUndoManager();
		undoManager.flush();
		return undoManager;
	}

	private void executePerformOperation(final PerformChangeOperation perform, IWorkspace workspace) throws CoreException {
		workspace.run(perform, new NullProgressMonitor());
	}
}
