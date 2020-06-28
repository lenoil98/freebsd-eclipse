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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.debug.core.model.IBreakpoint;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;

import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;

import org.eclipse.jdt.ui.tests.core.rules.ProjectTestSetup;


@RunWith(JUnit4.class)
public class CleanUpAnnotationTest extends CleanUpTestCase {

	@Rule
    public ProjectTestSetup projectsetup = new ProjectTestSetup();

	private void assertMarker(long markerId, ICompilationUnit unit, int expectedLineNumber, int expectedCharStart, int expectedCharEnd) throws CoreException {
		IFile file= (IFile)unit.getResource();

		IMarker marker= file.findMarker(markerId);

		assertNotNull(marker);
		assertTrue(marker.exists());

		assertEquals(expectedLineNumber, ((Integer)marker.getAttribute(IMarker.LINE_NUMBER)).intValue());
		assertEquals(expectedCharStart, ((Integer)marker.getAttribute(IMarker.CHAR_START)).intValue());
		assertEquals(expectedCharEnd, ((Integer)marker.getAttribute(IMarker.CHAR_END)).intValue());
	}

	private IMarker addMarker(String markerType, ICompilationUnit unit, int lineNumber) throws CoreException, BadLocationException {
		IFile file= (IFile)unit.getResource();

		IMarker marker= file.createMarker(markerType);
		marker.setAttribute(IMarker.LINE_NUMBER, Integer.valueOf(lineNumber));

		IDocument document= new Document(unit.getBuffer().getContents());
		int offset= document.getLineOffset(lineNumber - 1);
		marker.setAttribute(IMarker.CHAR_START, Integer.valueOf(offset));
		int lenght= offset + document.getLineLength(lineNumber - 1) - 1;
		marker.setAttribute(IMarker.CHAR_END, Integer.valueOf(lenght));

		assertMarker(marker.getId(), unit, lineNumber, offset, lenght);

		return marker;
	}

	@Test
	public void testSortMembersTask() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void a() {}\n");
		buf.append("}\n");
		ICompilationUnit cu1= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);

		addMarker(IMarker.TASK, cu1, 4).getId();

		enable(CleanUpConstants.SORT_MEMBERS);
		enable(CleanUpConstants.SORT_MEMBERS_ALL);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void a() {}\n");
		buf.append("\n");
		buf.append("    public void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		RefactoringStatus status= assertRefactoringResultAsExpected(new ICompilationUnit[] {
			cu1
		}, new String[] {
			expected1
		});

		assertTrue(status.hasWarning());
	}

	@Test
	public void testSortMembersBookmarks() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void a() {}\n");
		buf.append("}\n");
		ICompilationUnit cu1= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);

		addMarker(IMarker.BOOKMARK, cu1, 4).getId();

		enable(CleanUpConstants.SORT_MEMBERS);
		enable(CleanUpConstants.SORT_MEMBERS_ALL);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void a() {}\n");
		buf.append("\n");
		buf.append("    public void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		RefactoringStatus status= assertRefactoringResultAsExpected(new ICompilationUnit[] {
			cu1
		}, new String[] {
			expected1
		});

		assertTrue(status.hasWarning());
	}

	@Test
	public void testSortMembersBreakpoints() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void a() {}\n");
		buf.append("}\n");
		ICompilationUnit cu1= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);

		addMarker(IBreakpoint.LINE_BREAKPOINT_MARKER, cu1, 4).getId();

		enable(CleanUpConstants.SORT_MEMBERS);
		enable(CleanUpConstants.SORT_MEMBERS_ALL);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void a() {}\n");
		buf.append("\n");
		buf.append("    public void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		RefactoringStatus status= assertRefactoringResultAsExpected(new ICompilationUnit[] {
			cu1
		}, new String[] {
			expected1
		});

		assertTrue(status.hasWarning());
	}

	@Test
	public void testSortMembersProblemMarker() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    private void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    public void a() {}\n");
		buf.append("}\n");
		ICompilationUnit cu1= pack1.createCompilationUnit("E1.java", buf.toString(), false, null);

		Hashtable<String, String> options= JavaCore.getOptions();
		options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.WARNING);
		JavaCore.setOptions(options);

		CompilationUnit ast= SharedASTProviderCore.getAST(cu1, SharedASTProviderCore.WAIT_YES, null);
		assertTrue(ast.getProblems().length > 0);

		enable(CleanUpConstants.SORT_MEMBERS);
		enable(CleanUpConstants.SORT_MEMBERS_ALL);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E1 {\n");
		buf.append("    public void a() {}\n");
		buf.append("\n");
		buf.append("    private void x() {\n");
		buf.append("        System.out.println();\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected1= buf.toString();

		RefactoringStatus status= assertRefactoringResultAsExpected(new ICompilationUnit[] {
			cu1
		}, new String[] {
			expected1
		});

		assertTrue(status.toString(), status.isOK());
	}
}
