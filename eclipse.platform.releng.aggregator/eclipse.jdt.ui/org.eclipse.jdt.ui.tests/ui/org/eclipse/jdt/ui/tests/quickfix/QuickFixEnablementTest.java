/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.ui.tests.core.rules.ProjectTestSetup;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

@RunWith(JUnit4.class)
public class QuickFixEnablementTest extends QuickFixTest {

	@Rule
    public ProjectTestSetup projectsetup = new ProjectTestSetup();

	private IJavaProject fJProject1;
	private IPackageFragmentRoot fSourceFolder;

	@Before
	public void setUp() throws Exception {
		fJProject1= ProjectTestSetup.getProject();

		fSourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}


	@After
	public void tearDown() throws Exception {
		TestOptions.initializeProjectOptions(fJProject1);
		JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
	}


	@Test
	public void testContributedQuickFix1() throws Exception {

		HashMap<String, String> options= new HashMap<>();
		JavaModelUtil.setComplianceOptions(options, JavaCore.VERSION_1_5);
		fJProject1.setOptions(options);

		// quick fix is contributed only for files with name 'A.java' in a 1.5 project
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int x= 9999999999999999999999;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		CompilationUnit astRoot= getASTRoot(cu);
		ArrayList<IJavaCompletionProposal> proposals= collectCorrections(cu, astRoot);
		assertNumberOfProposals(proposals, 1);
		assertCorrectLabels(proposals);

		String[] previewContents= getPreviewContents(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int x= 0;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();
		assertEqualString(previewContents[0], expected);
	}


	@Test
	public void testContributedQuickFix2() throws Exception {
		// quick fix is contributed only for files with name 'A.java' in a 1.5 project
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int x= 9999999999999999999999;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("A.java", buf.toString(), false, null);

		HashMap<String, String> options= new HashMap<>();
		JavaModelUtil.setComplianceOptions(options, JavaCore.VERSION_1_6);
		fJProject1.setOptions(options);

		assertNumberOfProposals(collectCorrections(cu, getASTRoot(cu)), 1); // ok

		options= new HashMap<>();
		JavaModelUtil.setComplianceOptions(options, JavaCore.VERSION_1_4);
		fJProject1.setOptions(options);

		assertNumberOfProposals(collectCorrections(cu, getASTRoot(cu)), 0); // wrong version

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("    public void foo() {\n");
		buf.append("        int x= 9999999999999999999999;\n");
		buf.append("    }\n");
		buf.append("}\n");
		cu= pack1.createCompilationUnit("B.java", buf.toString(), false, null);

		options= new HashMap<>();
		JavaModelUtil.setComplianceOptions(options, JavaCore.VERSION_1_5);
		fJProject1.setOptions(options);

		assertNumberOfProposals(collectCorrections(cu, getASTRoot(cu)), 0); // wrong name
	}

}
