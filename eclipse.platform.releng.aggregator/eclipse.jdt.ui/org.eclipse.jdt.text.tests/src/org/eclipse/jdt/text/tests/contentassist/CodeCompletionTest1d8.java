/*******************************************************************************
 * Copyright (c) 2014, 2020 GK Software AG, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann <stephan.herrmann@berlin.de> - initial API and implementation - https://bugs.eclipse.org/425183
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.text.tests.contentassist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Hashtable;

import org.junit.Rule;
import org.junit.Test;

import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.NullTestUtils;
import org.eclipse.jdt.testplugin.TestOptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.jdt.internal.core.manipulation.CodeTemplateContextType;
import org.eclipse.jdt.internal.core.manipulation.StubUtility;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.tests.core.rules.Java1d8ProjectTestSetup;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * Those tests are made to run on Java Spider 1.8 .
 */
public class CodeCompletionTest1d8 extends AbstractCompletionTest {
	@Rule
	public Java1d8ProjectTestSetup j18s= new Java1d8ProjectTestSetup();

	private IJavaProject fJProject1;

	@Override
	public void setUp() throws Exception {
		fJProject1= Java1d8ProjectTestSetup.getProject();

		Hashtable<String, String> options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		options.put(JavaCore.CODEASSIST_FIELD_PREFIXES, "f");
		JavaCore.setOptions(options);

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, true);
		store.setValue(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, false);
		store.setValue(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.OVERRIDECOMMENT_ID, "/* (non-Javadoc)\n * ${see_to_overridden}\n */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.DELEGATECOMMENT_ID, "/* (non-Javadoc)\n * ${see_to_target}\n */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "//TODO\n${body_statement}", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID, "/**\n * Constructor.\n */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODCOMMENT_ID, "/**\n * Method.\n */", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.CONSTRUCTORSTUB_ID, "//TODO\n${body_statement}", null);
		StubUtility.setCodeTemplate(CodeTemplateContextType.GETTERCOMMENT_ID, "/**\n * @return the ${bare_field_name}\n */", fJProject1);
		StubUtility.setCodeTemplate(CodeTemplateContextType.SETTERCOMMENT_ID, "/**\n * @param ${param} the ${bare_field_name} to set\n */", fJProject1);
	}

	@Override
	public void tearDown() throws Exception {
		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setToDefault(PreferenceConstants.CODEGEN_ADD_COMMENTS);
		store.setToDefault(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS);
		store.setToDefault(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS);
		closeAllEditors();
		JavaProjectHelper.clear(fJProject1, Java1d8ProjectTestSetup.getDefaultClasspath());
	}

	public static void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site= editor.getSite()) != null && (page= site.getPage()) != null)
			page.closeEditor(editor, false);
	}

	public static void closeAllEditors() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorReference : page.getEditorReferences()) {
					closeEditor(editorReference.getEditor(false));
				}
			}
		}
	}

	private CompletionProposalCollector createCollector(ICompilationUnit cu, int offset) throws PartInitException, JavaModelException {
		CompletionProposalCollector collector= new CompletionProposalCollector(cu);
		collector.setInvocationContext(createContext(offset, cu));
		return collector;
	}

	private JavaContentAssistInvocationContext createContext(int offset, ICompilationUnit cu) throws PartInitException, JavaModelException {
		JavaEditor editor= (JavaEditor) JavaUI.openInEditor(cu);
		ISourceViewer viewer= editor.getViewer();
		return new JavaContentAssistInvocationContext(viewer, offset, editor);
	}

	private void codeComplete(ICompilationUnit cu, int offset, CompletionProposalCollector collector) throws JavaModelException {
		cu.codeComplete(offset, collector, new NullProgressMonitor());
	}

	@Test
	public void testBug425183_comment8() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");

		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		String contents=
			"package test1\n" +
			"public class X {\n" +
			"    void foo() {\n" +
			"        java.util.Comparator.reverseOrder().  // content assist after '.' => NPE\n" +
			"    }\n" +
			"}\n";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", contents, false, null);


		String str= "java.util.Comparator.reverseOrder().";

		int offset= contents.indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);

		collector.setReplacementLength(0);

		codeComplete(cu, offset, collector);

		ICompletionProposal proposal= null;

		for (IJavaCompletionProposal curr : collector.getJavaCompletionProposals()) {
			if (curr.getDisplayString().startsWith("thenComparingLong")) {
				assertNull("more than one proposal for thenComparingLong()", proposal);
				proposal= curr;
			}
		}
		assertNotNull("no proposal for thenComparingLong()", proposal);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposal.apply(doc);

		String expectedContents=
				"package test1\n" +
				"public class X {\n" +
				"    void foo() {\n" +
				"        java.util.Comparator.reverseOrder().thenComparingLong()  // content assist after '.' => NPE\n" +
				"    }\n" +
				"}\n";
		assertEquals(expectedContents, doc.get());
	}

	@Test
	public void testOverride1() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Main {\n");
		buf.append("    private static class Cls implements Interface {\n");
		buf.append("        hello\n");
		buf.append("    }\n");
		buf.append("    private static interface Interface {\n");
		buf.append("        default void hello() {\n");
		buf.append("            System.out.println(\"Hello\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Main.java", buf.toString(), false, null);

		String str= "hello";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(1, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Main {\n");
		buf.append("    private static class Cls implements Interface {\n");
		buf.append("        /* (non-Javadoc)\n");
		buf.append("         * @see test1.Main.Interface#hello()\n");
		buf.append("         */\n");
		buf.append("        @Override\n");
		buf.append("        public void hello() {\n");
		buf.append("            //TODO\n");
		buf.append("            Interface.super.hello();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private static interface Interface {\n");
		buf.append("        default void hello() {\n");
		buf.append("            System.out.println(\"Hello\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride2() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Main {\n");
		buf.append("    private static interface Bar extends Foo {\n");
		buf.append("        hello\n");
		buf.append("    }\n");
		buf.append("    private static interface Foo {\n");
		buf.append("        default void hello() {\n");
		buf.append("            System.out.println(\"Hello\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("Main.java", buf.toString(), false, null);

		String str= "hello";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(1, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class Main {\n");
		buf.append("    private static interface Bar extends Foo {\n");
		buf.append("        /* (non-Javadoc)\n");
		buf.append("         * @see test1.Main.Foo#hello()\n");
		buf.append("         */\n");
		buf.append("        @Override\n");
		buf.append("        default void hello() {\n");
		buf.append("            //TODO\n");
		buf.append("            Foo.super.hello();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private static interface Foo {\n");
		buf.append("        default void hello() {\n");
		buf.append("            System.out.println(\"Hello\");\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride3() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface I {\n");
		buf.append("    default int getSize(String name) {\n");
		buf.append("        return name.length();\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface I2 extends I {\n");
		buf.append("    getSize\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I.java", buf.toString(), false, null);

		String str= "getSize";
		int offset= buf.toString().lastIndexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(1, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public interface I {\n");
		buf.append("    default int getSize(String name) {\n");
		buf.append("        return name.length();\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface I2 extends I {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see test1.I#getSize(java.lang.String)\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    default int getSize(String name) {\n");
		buf.append("        //TODO\n");
		buf.append("        return I.super.getSize(name);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride4() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("abstract class X implements Collection<Integer> {\n");
		buf.append("    parallelS\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		String str= "parallelS";
		int offset= buf.toString().lastIndexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(2, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.Collection;\n");
		buf.append("import java.util.stream.Stream;\n");
		buf.append("abstract class X implements Collection<Integer> {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see java.util.Collection#parallelStream()\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    public Stream<Integer> parallelStream() {\n");
		buf.append("        //TODO\n");
		buf.append("        return Collection.super.parallelStream();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride5() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I1 {\n");
		buf.append("    equals\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I1.java", buf.toString(), false, null);

		String str= "equals";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(1, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I1 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see java.lang.Object#equals(java.lang.Object)\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    boolean equals(Object arg0);\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride6() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I2 {\n");
		buf.append("    hashCode\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I2.java", buf.toString(), false, null);

		String str= "hashCode";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(1, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I2 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see java.lang.Object#hashCode()\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    int hashCode();\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride7() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I3 {\n");
		buf.append("    toString\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I3.java", buf.toString(), false, null);

		String str= "toString";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(1, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I3 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see java.lang.Object#toString()\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    String toString();\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride8() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I4 {\n");
		buf.append("    clone\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I4.java", buf.toString(), false, null);

		String str= "clone";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(3, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[2].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I4 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see java.lang.Object#clone()\n");
		buf.append("     */\n");
		buf.append("    Object clone() throws CloneNotSupportedException;\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride9() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I5 {\n");
		buf.append("    finalize\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I5.java", buf.toString(), false, null);

		String str= "finalize";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(2, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[1].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I5 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see java.lang.Object#finalize()\n");
		buf.append("     */\n");
		buf.append("    void finalize() throws Throwable;\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride10() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I6 extends A {\n");
		buf.append("    myAbs\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface A {\n");
		buf.append("    void myAbstractM();\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("I6.java", buf.toString(), false, null);

		String str= "myAbs";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(2, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
		proposals[0].apply(doc);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("public interface I6 extends A {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see test1.A#myAbstractM()\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    default void myAbstractM() {\n");
		buf.append("        //TODO\n");
		buf.append("        \n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("interface A {\n");
		buf.append("    void myAbstractM();\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride11() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("pp", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pp;\n");
		buf.append("\n");
		buf.append("public class CC implements I2 {\n");
		buf.append("    dispose\n");
		buf.append("}\n");
		buf.append("interface I2 {\n");
		buf.append("    default void dispose2() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("CC.java", buf.toString(), false, null);

		String str= "dispose";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(2, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());

		proposals[0].apply(doc);
		buf= new StringBuffer();
		buf.append("package pp;\n");
		buf.append("\n");
		buf.append("public class CC implements I2 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see pp.I2#dispose2()\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    public void dispose2() {\n");
		buf.append("        //TODO\n");
		buf.append("        I2.super.dispose2();\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface I2 {\n");
		buf.append("    default void dispose2() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride12() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("pp", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pp;\n");
		buf.append("\n");
		buf.append("public class CC extends S1 {\n");
		buf.append("    dispose\n");
		buf.append("}\n");
		buf.append("class S1 {\n");
		buf.append("    void dispose1() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("CC.java", buf.toString(), false, null);

		String str= "dispose";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(2, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());

		proposals[0].apply(doc);
		buf= new StringBuffer();
		buf.append("package pp;\n");
		buf.append("\n");
		buf.append("public class CC extends S1 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see pp.S1#dispose1()\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    void dispose1() {\n");
		buf.append("        //TODO\n");
		buf.append("        super.dispose1();\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class S1 {\n");
		buf.append("    void dispose1() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}

	@Test
	public void testOverride13() throws CoreException {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		IPackageFragment pack1= sourceFolder.createPackageFragment("pp", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pp;\n");
		buf.append("\n");
		buf.append("public class CC extends S1 {\n");
		buf.append("    dispose\n");
		buf.append("}\n");
		buf.append("class S1 implements I1 {\n");
		buf.append("    void dispose1() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface I1 {\n");
		buf.append("    default void dispose() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("CC.java", buf.toString(), false, null);

		String str= "dispose";
		int offset= buf.toString().indexOf(str) + str.length();

		CompletionProposalCollector collector= createCollector(cu, offset);
		collector.setReplacementLength(0);
		codeComplete(cu, offset, collector);

		IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();
		assertEquals(2, proposals.length);
		IEditorPart part= JavaUI.openInEditor(cu);
		IDocument doc= JavaUI.getDocumentProvider().getDocument(part.getEditorInput());

		proposals[1].apply(doc);
		buf= new StringBuffer();
		buf.append("package pp;\n");
		buf.append("\n");
		buf.append("public class CC extends S1 {\n");
		buf.append("    /* (non-Javadoc)\n");
		buf.append("     * @see pp.I1#dispose()\n");
		buf.append("     */\n");
		buf.append("    @Override\n");
		buf.append("    public void dispose() {\n");
		buf.append("        //TODO\n");
		buf.append("        super.dispose();\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class S1 implements I1 {\n");
		buf.append("    void dispose1() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface I1 {\n");
		buf.append("    default void dispose() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEquals(buf.toString(), doc.get());
	}
	private static void assertNumberOf(String name, int is, int expected) {
		assertEquals("Wrong number of " + name + ", is: " + is + ", expected: " + expected, expected, is);
	}

	@Test
	public void testBug528871() throws Exception {
		IPackageFragmentRoot sourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		NullTestUtils.prepareNullTypeAnnotations(sourceFolder);
		try {
			IPackageFragment pack1= sourceFolder.createPackageFragment("test1", false, null);
			StringBuffer buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("\n");
			buf.append("import annots.NonNull;\n");
			buf.append("import annots.NonNullByDefault;\n");
			buf.append("\n");
			buf.append("interface A {\n");
			buf.append("    @NonNull String m();\n");
			buf.append("}\n");
			buf.append("\n");
			buf.append("@NonNullByDefault\n");
			buf.append("public class Test {\n");
			buf.append("    void f() {\n");
			buf.append("        new A()\n");
			buf.append("    }\n");
			buf.append("}\n");
			buf.append("");
			String contents= buf.toString();

			ICompilationUnit cu= pack1.createCompilationUnit("A.java", contents, false, null);

			String str= "new A(";

			int offset= contents.indexOf(str) + str.length();

			CompletionProposalCollector collector= createCollector(cu, offset);
			collector.setReplacementLength(0);

			codeComplete(cu, offset, collector);

			IJavaCompletionProposal[] proposals= collector.getJavaCompletionProposals();

			assertNumberOf("proposals", proposals.length, 1);

			IDocument doc= new Document(contents);

			proposals[0].apply(doc);

			buf= new StringBuffer();
			buf.append("package test1;\n");
			buf.append("\n");
			buf.append("import annots.NonNull;\n");
			buf.append("import annots.NonNullByDefault;\n");
			buf.append("\n");
			buf.append("interface A {\n");
			buf.append("    @NonNull String m();\n");
			buf.append("}\n");
			buf.append("\n");
			buf.append("@NonNullByDefault\n");
			buf.append("public class Test {\n");
			buf.append("    void f() {\n");
			buf.append("        new A() {\n");
			buf.append("            \n");
			buf.append("            @Override\n");
			buf.append("            public String m() {\n");
			buf.append("                //TODO\n");
			buf.append("                return null;\n");
			buf.append("            }\n");
			buf.append("        }\n");
			buf.append("    }\n");
			buf.append("}\n");
			buf.append("");
			assertEquals(buf.toString(), doc.get());
		} finally {
			NullTestUtils.disableAnnotationBasedNullAnalysis(sourceFolder);
		}
	}
}
