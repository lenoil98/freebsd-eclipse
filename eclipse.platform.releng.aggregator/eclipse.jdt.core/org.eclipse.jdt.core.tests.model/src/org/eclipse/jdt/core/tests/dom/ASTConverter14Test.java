/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

@SuppressWarnings("rawtypes")
public class ASTConverter14Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
	private static final String jclLib = "CONVERTER_JCL14_LIB";

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST14(), false);
		if (this.ast.apiLevel() == AST.JLS14 ) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
		}
	}

	public ASTConverter14Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter14Test.class);
	}

	static int getAST14() {
		return AST.JLS14;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}
	/*
	 * Test that a simple switch expression's return type holds the correct type
	 */
	public void _test0001() throws JavaModelException {
		String contents =
			"	public class X {\n" +
			"   enum Day\n" +
			"   {\n" +
			"   	SUNDAY, MONDAY, TUESDAY, WEDNESDAY,\n" +
			"   	THURSDAY, FRIDAY, SATURDAY;\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		Day day = Day.SUNDAY;\n" +
			"		int k = switch (day) {\n" +
			"    	case MONDAY  -> throw new NullPointerException();\n" +
			"    	case TUESDAY -> 1;\n" +
			"\n" +
			"	 	case WEDNESDAY -> {yield 10;}\n" +
			"    	default      -> {\n" +
			"        	int g = day.toString().length();\n" +
			"        	yield g;\n" +
			"   	}};\n" +
			"   	System.out.println(k);\n" +
			"	}\n" +
			"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		VariableDeclarationStatement vStmt1 = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(1);
		Type type = vStmt1.getType();
		IBinding binding = type.resolveBinding();
		assertTrue("null binding", binding != null);
		assertTrue("binding incorrect", binding.getName().equals("int"));
	}
	/*
	 * Test that a case statement with multiple cases is resolved correctly
	 * and has the correct source range
	 */
	public void _test0002() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}\n" +
			"	String aa(Day day) throws Exception {\n" +
			"		var today = \"\";\n" +
			"		switch (day) {\n" +
			"			case SATURDAY,SUNDAY ->\n" +
			"				today=\"Weekend\";\n" +
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->\n" +
			"				today=\"Working\";\n" +
			"			default ->\n" +
			"				throw new Exception(\"Invalid day: \" + day.name());\n" +
			"		}\n" +
			"		return today;\n" +
			"	}\n" +
			"	\n" +
			"	String bb(Day day) throws Exception {\n" +
			"		var today = \"\";\n" +
			"		switch (day) {\n" +
			"			case SATURDAY,SUNDAY:\n" +
			"				today = \"Weekend day\";\n" +
			"				break;\n" +
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:\n" +
			"				today = \"Working day\";\n" +
			"				break;\n" +
			"			default:\n" +
			"				throw new Exception(\"Invalid day: \" + day.name());\n" +
			"		}\n" +
			"		return today;\n" +
			"	}\n" +
			"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1, 1);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		SwitchStatement switchStatement = (SwitchStatement) node;
		checkSourceRange((Statement) switchStatement.statements().get(0), "case SATURDAY,SUNDAY ->", contents);
		checkSourceRange((Statement) switchStatement.statements().get(2), "case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->", contents);
	}

	/* test implicit break statement */

	public void _test0003() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}\n" +
			"	String aa(Day day) throws Exception {\n" +
			"		var today = \"\";\n" +
			"		switch (day) {\n" +
			"			case SATURDAY,SUNDAY ->\n" +
			"				today=\"Weekend\";\n" +
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->\n" +
			"				today=\"Working\";\n" +
			"			default ->\n" +
			"				throw new Exception(\"Invalid day: \" + day.name());\n" +
			"		}\n" +
			"		return today;\n" +
			"	}\n" +
			"	\n" +
			"	String bb(Day day) throws Exception {\n" +
			"		String today = \"\";\n" +
			"		today = switch (day) {\n" +
			"			case SATURDAY,SUNDAY:\n" +
			"				yield \"Weekend day\";\n" +
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:\n" +
			"				yield \"Week day\";\n" +
			"			default:\n" +
			"				yield \"Any day\";\n" +
			"		};\n" +
			"		return today;\n" +
			"	}\n" +
			"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1, 1);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		SwitchStatement switchStatement = (SwitchStatement) node;
		checkSourceRange((Statement) switchStatement.statements().get(0), "case SATURDAY,SUNDAY ->", contents);

	}

	public void _test0004() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}\n" +
				"	String bb(Day day) throws Exception {\n" +
				"		String today = switch (day) {\n" +
				"			case SATURDAY,SUNDAY:\n" +
				"				yield \"Weekend day\";\n" +
				"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:\n" +
				"				yield \"Week day\";\n" +
				"			default:\n" +
				"				yield \"Any day\";\n" +
				"		};\n" +
				"		return today;\n" +
				"	}\n" +
				"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 1, 0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
		Expression expression = ((SwitchExpression) initializer).getExpression();
		assertEquals("incorrect type", ASTNode.SIMPLE_NAME, expression.getNodeType());
		assertEquals("incorrect name", "day", ((SimpleName) expression).getFullyQualifiedName());
		List statements = ((SwitchExpression) initializer).statements();
		assertEquals("incorrect no of statements", 6, statements.size());
		YieldStatement brStmt = (YieldStatement) statements.get(1);
		Expression expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

		//default case:
		SwitchCase caseStmt = (SwitchCase) statements.get(4);
		assertTrue("not default", caseStmt.isDefault());
		brStmt = (YieldStatement) statements.get(5);
		expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

	}
	@Deprecated
	public void _test0005() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public String test001() {\n" +
				"		int i = 0;\n" +
				"		String ret = switch(i%2) {\n" +
				"		case 0 -> \"odd\";\n" +
				"		case 1 -> \"even\";\n" +
				"		default -> \"\";\n" +
				"		};\n" +
				"		return ret;\n" +
				"	}" +
				"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
		Expression expression = ((SwitchExpression) initializer).getExpression();
		assertEquals("incorrect type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
		List statements = ((SwitchExpression) initializer).statements();
		assertEquals("incorrect no of statements", 6, statements.size());
		BreakStatement brStmt = (BreakStatement) statements.get(1);
		Expression expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

				//default case:
		SwitchCase caseStmt = (SwitchCase) statements.get(4);
		assertTrue("not default", caseStmt.isDefault());
		brStmt = (BreakStatement) statements.get(5);
		expression2 = brStmt.getExpression();
		assertNotNull("should not null", expression2);
		assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

	}
	public void _test0006() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"	public String test001() {\n" +
						"		int i = 0;\n" +
						"		String ret = switch(i%2) {\n" +
						"		case 0 -> {return \"odd\"; }\n" +
						"		case 1 -> \"even\";\n" +
						"		default -> \"\";\n" +
						"		};\n" +
						"		return ret;\n" +
						"	}" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		List statements = ((SwitchExpression) initializer).statements();
		assertEquals("incorrect no of statements", 6, statements.size());
		Block block = (Block) statements.get(1);
		statements = block.statements();
		assertEquals("incorrect no of statements", 1, statements.size());
		Statement stmt = (Statement) statements.get(0);
		assertEquals("incorrect node type", ASTNode.RETURN_STATEMENT, stmt.getNodeType());

	}
	// Moved over from ASTConverter9Test
	public void _testBug531714_015() throws CoreException {
		// saw NPE in SwitchExpression.resolveType(SwitchExpression.java:423)
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		IJavaProject p =  createJavaProject("Foo", new String[] {"src"}, new String[] {jclLib}, "bin", "14"); // FIXME jcl12?
		try {
			String source =
				"import java.util.*;\n" +
				"public class X {\n" +
				"	void testForeach1(int i, List<String> list) {\n" +
				"		for (String s : switch(i) { case 1 -> list; default -> ; }) {\n" +
				"			\n" +
				"		}\n" +
				"		Throwable t = switch (i) {\n" +
				"			case 1 -> new Exception();\n" +
				"			case 2 -> new RuntimeException();\n" + // trigger !typeUniformAcrossAllArms
				"			default -> missing;\n" +
				"		};\n" +
				"	}\n" +
				"	void testForeach0(int i, List<String> list) {\n" + // errors in first arm
				"		for (String s : switch(i) { case 1 -> ; default -> list; }) {\n" +
				"			\n" +
				"		}\n" +
				"		Throwable t = switch (i) {\n" +
				"			case 0 -> missing;\n" +
				"			case 1 -> new Exception();\n" +
				"			default -> new RuntimeException();\n" + // trigger !typeUniformAcrossAllArms
				"		};\n" +
				"	}\n" +
				"	void testForeachAll(int i) {\n" + // only erroneous arms
				"		Throwable t = switch (i) {\n" +
				"			case 0 -> missing;\n" +
				"			default -> absent;\n" +
				"		};\n" +
				"	}\n" +
				"}\n";
			createFile("Foo/src/X.java", source);
			ICompilationUnit cuD = getCompilationUnit("/Foo/src/X.java");

			ASTParser parser = ASTParser.newParser(AST_INTERNAL_JLS14);
			parser.setProject(p);
			parser.setSource(cuD);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			org.eclipse.jdt.core.dom.CompilationUnit cuAST = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);
			IProblem[] problems = cuAST.getProblems();
			assertProblems("Unexpected problems",
					"1. ERROR in /Foo/src/X.java (at line 4)\n" +
					"	for (String s : switch(i) { case 1 -> list; default -> ; }) {\n" +
					"	                                                    ^^\n" +
					"Syntax error on token \"->\", Expression expected after this token\n" +
					"----------\n" +
					"2. ERROR in /Foo/src/X.java (at line 10)\n" +
					"	default -> missing;\n" +
					"	           ^^^^^^^\n" +
					"missing cannot be resolved to a variable\n" +
					"----------\n" +
					"3. ERROR in /Foo/src/X.java (at line 14)\n" +
					"	for (String s : switch(i) { case 1 -> ; default -> list; }) {\n" +
					"	                                   ^^\n" +
					"Syntax error on token \"->\", Expression expected after this token\n" +
					"----------\n" +
					"4. ERROR in /Foo/src/X.java (at line 18)\n" +
					"	case 0 -> missing;\n" +
					"	          ^^^^^^^\n" +
					"missing cannot be resolved to a variable\n" +
					"----------\n" +
					"5. ERROR in /Foo/src/X.java (at line 25)\n" +
					"	case 0 -> missing;\n" +
					"	          ^^^^^^^\n" +
					"missing cannot be resolved to a variable\n" +
					"----------\n" +
					"6. ERROR in /Foo/src/X.java (at line 26)\n" +
					"	default -> absent;\n" +
					"	           ^^^^^^\n" +
					"absent cannot be resolved to a variable\n" +
					"----------\n",
					problems, source.toCharArray());
		} finally {
			deleteProject(p);
		}
	}

	public void _test0007() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"	public String test001() {\n" +
						"		String s = \"\"\"\n" +
						"      	<html>\n" +
						"        <body>\n" +
						"            <p>Hello, world</p>\n" +
						"        </body>\n" +
						"    	</html>\n" +
						"    	\"\"\";\n" +
						"    	System.out.println(s);" +
						"		return s;\n" +
						"	}" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Text block statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertTrue("Initializer is not a TextBlock", initializer instanceof TextBlock);
			String escapedValue = ((TextBlock) initializer).getEscapedValue();

			assertTrue("String should not be empty", escapedValue.length() != 0);
		assertTrue("String should start with \"\"\"", escapedValue.startsWith("\"\"\""));

		String literal = ((TextBlock) initializer).getLiteralValue();
		assertEquals("literal value not correct",
				"      	<html>\n" +
				"        <body>\n" +
				"            <p>Hello, world</p>\n" +
				"        </body>\n" +
				"    	</html>\n" +
				"    	",
				literal);

	}
	public void _test0008() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"	public String test001() {\n" +
						"		String s = \"\"\"\n" +
						"      	<html>\n" +
						"        <body>\n" +
						"            <p>Hello, world</p>\n" +
						"        </body>\n" +
						"    	</html>\n" +
						"    	\"\"\";\n" +
						"    	System.out.println(s);" +
						"		return s;\n" +
						"	}" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		try {
		buildAST(
				contents,
				this.workingCopy);
		} catch(UnsupportedOperationException e) {
			fail("Should not throw UnsupportedOperationException");
		} catch(AssertionFailedError e) {
			e.printStackTrace();
			return;
		}
		fail("Compilation should not succeed");

	}
	public void _test0009() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"	public String test001() {\n" +
						"		String s = \"\"\"\n" +
						"      	<html>\n" +
						"        <body>\n" +
						"            <p>Hello, world</p>\n" +
						"        </body>\n" +
						"    	</html>\n" +
						"    	\"\"\";\n" +
						"    	System.out.println(s);" +
						"		return s;\n" +
						"	}" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Text block statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertTrue("Initializer is not a TextBlock", initializer instanceof TextBlock);
		ITypeBinding binding = initializer.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "java.lang.String", binding.getQualifiedName());

			String escapedValue = ((TextBlock) initializer).getEscapedValue();

			assertTrue("String should not be empty", escapedValue.length() != 0);
		assertTrue("String should start with \"\"\"", escapedValue.startsWith("\"\"\""));
		assertEquals("escaped value not correct",
				"\"\"\"\n" +
				"      	<html>\n" +
				"        <body>\n" +
				"            <p>Hello, world</p>\n" +
				"        </body>\n" +
				"    	</html>\n" +
				"    	\"\"\"",
				escapedValue);

		String literal = ((TextBlock) initializer).getLiteralValue();
		assertEquals("literal value not correct",
				"      	<html>\n" +
				"        <body>\n" +
				"            <p>Hello, world</p>\n" +
				"        </body>\n" +
				"    	</html>\n" +
				"    	",
				literal);
	}
	public void _test0010() throws JavaModelException {
		String contents =
				"public class test14 {\n" +
				"	public static void main(String[] args) {\n" +
				"		String s = \"\"\"\n" +
				"				nadknaks vgvh \n" +
				"				\"\"\";\n" +
				"\n" +
				"		int m = 10;\n" +
				"		m = m* 6;\n" +
				"		System.out.println(s);\n" +
				"	}\n" +
				"}" ;
		this.workingCopy = getWorkingCopy("/Converter14/src/test14.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("wrong line number", 3, compilationUnit.getLineNumber(node.getStartPosition()));
		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("wrong line number", 7, compilationUnit.getLineNumber(node.getStartPosition()));
		node = getASTNode(compilationUnit, 0, 0, 2);
		assertEquals("wrong line number", 8, compilationUnit.getLineNumber(node.getStartPosition()));
		node = getASTNode(compilationUnit, 0, 0, 3);
		assertEquals("wrong line number", 9, compilationUnit.getLineNumber(node.getStartPosition()));
	}

	public void _test0011() throws CoreException {
		// saw NPE in SwitchExpression.resolveType(SwitchExpression.java:423)
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String source =
			"public class Switch {\n" +
			"	public static void main(String[] args) {\n" +
			"		foo(Day.TUESDAY);\n" +
			"	}\n" +
			"\n" +
			"	private static void foo(Day day) {\n" +
			"		switch (day) {\n" +
			"		case SUNDAY, MONDAY, FRIDAY -> System.out.println(6);\n" +
			"		case TUESDAY -> System.out.println(7);\n" +
			"		case THURSDAY, SATURDAY -> System.out.println(8);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"enum Day {\n" +
			"	MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter14/src/Switch.java", true/*resolve*/);
		try {
		buildAST(
				source,
				this.workingCopy);
		} catch(UnsupportedOperationException e) {
			fail("Should not throw UnsupportedOperationException");
		} catch(AssertionFailedError e) {
			e.printStackTrace();
			return;
		}

	}

	public void testRecord001() throws CoreException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String contents =
			"public record X() {\n" +
			"		public X {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	/**
	 * Added for Bug 561193 - [14]record keyword inside method not colored correctly
	 * @throws CoreException
	 */
	public void testRecord002() throws CoreException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String contents =
			"public record X(int param1, int param2) {\n" +
			"		public X {\n" +
			"			if (param1 > 5) {\n" +
			"				System.out.println(\"error\");\n" +
			"			}\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testRecord003() throws CoreException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String contents =
			"public record X(int param1, int param2) {\n" +
			"		public X {\n" +
			"			if (param1 > 5) {\n" +
			"				System.out.println(\"error\");\n" +
			"			}\n" +
			"		}\n" +
			"\n" +
			"		public X(int a) {\n" +
			"			this.param1 = 6;\n" +
			"			this.param2 = 16;\n" +
			"			a = 6;\n" +
			"		}\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testRecord004() throws CoreException {
		if (!isJRE14) {
			System.err.println("Test " + getName() + " requires a JRE 14");
			return;
		}
		String contents = "public class X {\n" +
						  "	public static void main(String[] args) {\n" +
				          "		record R(int x,int y){}\n" +
						  "		R r = new R(100, 200);\n" +
				          "		System.out.println(r.x());\n" +
						  "	}\n" +
				          "}";
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/* resolve */);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			List<ASTNode> statements = methodDeclaration.getBody().statements();
			node = statements.get(0);
			assertEquals("Not a TypDeclaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
			TypeDeclarationStatement tdStmt = (TypeDeclarationStatement) node;
			node = tdStmt.getDeclaration();
			assertEquals("Not a RecordDeclaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testRecord005() throws CoreException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String contents = "public class X {\n" +
				  "	public static void main(String[] args) {\n" +
		          "		record R(int x,String y){}\n" +
				  "		R r = new R(100, \"Point\");\n" +
		          "		System.out.println(r.x());\n" +
				  "	}\n" +
		          "}";
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			List<ASTNode> statements = methodDeclaration.getBody().statements();
			node = statements.get(0);
			assertEquals("Not a TypDeclaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
			TypeDeclarationStatement tdStmt = (TypeDeclarationStatement) node;
			node = tdStmt.getDeclaration();
			assertEquals("Not a RecordDeclaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			RecordDeclaration record = (RecordDeclaration)node;
			List<SingleVariableDeclaration> recordComponents = record.recordComponents();
			assertEquals("There should be 2 record components", 2, recordComponents.size());
			SingleVariableDeclaration recordComponent = recordComponents.get(0);
			assertEquals("First record component name should be x","x" , recordComponent.getName().toString());
			assertEquals("First record component type is int" , "int", recordComponent.getType().toString());
			IVariableBinding resolveBinding = recordComponent.resolveBinding();
			assertEquals("First record component binding" , true, resolveBinding.isRecordComponent());
			recordComponent = recordComponents.get(1);
			assertEquals("Second record component name should be y","y" , recordComponent.getName().toString());
			assertEquals("Second record component type is String" , "String", recordComponent.getType().toString());
			resolveBinding = recordComponent.resolveBinding();
			assertEquals("Second record component binding" , true, resolveBinding.isRecordComponent());
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testRecord006() throws CoreException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String contents = "import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"record X(@MyAnnot int lo) {\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"@Target({ElementType.FIELD})\n" +
				"@interface MyAnnot {}";
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			RecordDeclaration record = (RecordDeclaration)node;
			List<SingleVariableDeclaration> recordComponents = record.recordComponents();
			assertEquals("There should be 1 record component", 1, recordComponents.size());
			SingleVariableDeclaration recordComponent = recordComponents.get(0);
			assertEquals("Record component name should be lo","lo" , recordComponent.getName().toString());
			assertEquals("Record component type is int" , "int", recordComponent.getType().toString());
			IVariableBinding resolveBinding = recordComponent.resolveBinding();
			assertEquals("Record component binding" , true, resolveBinding.isRecordComponent());
			MarkerAnnotation annotation = (MarkerAnnotation)recordComponent.modifiers().get(0);
			assertEquals("Record component annotation name should be MyAnnot","@MyAnnot" , annotation.toString());
			assertEquals("Record component binding should not have annotation",0 , resolveBinding.getAnnotations().length);


		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testRecord007() throws CoreException {
		if (!isJRE14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		String contents = "record X(int lo) {\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter14/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			RecordDeclaration record = (RecordDeclaration)node;
			List<SingleVariableDeclaration> recordComponents = record.recordComponents();
			assertEquals("There should be 1 record component", 1, recordComponents.size());
			SingleVariableDeclaration recordComponent = recordComponents.get(0);
			SimpleName recordComponentName = recordComponent.getName();
			assertEquals("Record component name should be lo","lo" , recordComponentName.toString());
			ITypeBinding resolveTypeBinding = recordComponentName.resolveTypeBinding();
			assertEquals("Record component type is int" , "int",resolveTypeBinding.getName() );
			IVariableBinding resolveBinding = (IVariableBinding)recordComponentName.resolveBinding();
			assertEquals("Record component binding" , true, resolveBinding.isRecordComponent());


		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

}
