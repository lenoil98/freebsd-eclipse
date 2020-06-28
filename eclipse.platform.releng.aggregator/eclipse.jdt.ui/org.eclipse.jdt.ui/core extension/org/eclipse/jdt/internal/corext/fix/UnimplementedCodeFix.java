/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
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
 *     Red Hat Inc. - modified to use UnimplementedCodeFixCore
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.text.edits.MultiTextEdit;

import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;

import org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels;
import org.eclipse.jdt.internal.corext.util.Messages;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;

public class UnimplementedCodeFix extends CompilationUnitRewriteOperationsFix {

	public static ICleanUpFix createCleanUp(CompilationUnit root, boolean addMissingMethod, boolean makeTypeAbstract, IProblemLocation[] problems) {
		Assert.isLegal(!addMissingMethod || !makeTypeAbstract);
		if (!addMissingMethod && !makeTypeAbstract)
			return null;

		if (problems.length == 0)
			return null;

		ArrayList<CompilationUnitRewriteOperationsFixCore.CompilationUnitRewriteOperation> operations= new ArrayList<>();

		for (IProblemLocation problem : problems) {
			if (addMissingMethod) {
				ASTNode typeNode= getSelectedTypeNode(root, problem);
				if (typeNode != null && !isTypeBindingNull(typeNode)) {
					operations.add(new AddUnimplementedMethodsOperation(typeNode));
				}
			} else {
				ASTNode typeNode= getSelectedTypeNode(root, problem);
				if (typeNode instanceof TypeDeclaration) {
					operations.add(new UnimplementedCodeFixCore.MakeTypeAbstractOperation((TypeDeclaration) typeNode));
				}
			}
		}

		if (operations.isEmpty())
			return null;

		String label;
		if (addMissingMethod) {
			label= CorrectionMessages.UnimplementedMethodsCorrectionProposal_description;
		} else {
			label= CorrectionMessages.UnimplementedCodeFix_MakeAbstractFix_label;
		}
		return new UnimplementedCodeFix(label, root, operations.toArray(new CompilationUnitRewriteOperationsFixCore.CompilationUnitRewriteOperation[operations.size()]));
	}

	public static IProposableFix createAddUnimplementedMethodsFix(final CompilationUnit root, IProblemLocation problem) {
		ASTNode typeNode= getSelectedTypeNode(root, problem);
		if (typeNode == null)
			return null;

		if (isTypeBindingNull(typeNode))
			return null;

		AddUnimplementedMethodsOperation operation= new AddUnimplementedMethodsOperation(typeNode);
		if (operation.getMethodsToImplement().length > 0) {
			return new UnimplementedCodeFix(CorrectionMessages.UnimplementedMethodsCorrectionProposal_description, root,
					new CompilationUnitRewriteOperationsFixCore.CompilationUnitRewriteOperation[] { operation });
		} else {
			return new IProposableFix() {
				@Override
				public CompilationUnitChange createChange(IProgressMonitor progressMonitor) throws CoreException {
					CompilationUnitChange change= new CompilationUnitChange(CorrectionMessages.UnimplementedMethodsCorrectionProposal_description, (ICompilationUnit) root.getJavaElement()) {
						@Override
						public Change perform(IProgressMonitor pm) throws CoreException {
							Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							String dialogTitle= CorrectionMessages.UnimplementedMethodsCorrectionProposal_description;
							IStatus status= getStatus();
							ErrorDialog.openError(shell, dialogTitle, CorrectionMessages.UnimplementedCodeFix_DependenciesErrorMessage, status);

							return new NullChange();
						}
					};
					change.setEdit(new MultiTextEdit());
					return change;
				}

				@Override
				public String getAdditionalProposalInfo() {
					return new String();
				}

				@Override
				public String getDisplayString() {
					return CorrectionMessages.UnimplementedMethodsCorrectionProposal_description;
				}

				@Override
				public IStatus getStatus() {
					return new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, CorrectionMessages.UnimplementedCodeFix_DependenciesStatusMessage);
				}
			};
		}
	}

	public static UnimplementedCodeFix createMakeTypeAbstractFix(CompilationUnit root, IProblemLocation problem) {
		ASTNode typeNode= getSelectedTypeNode(root, problem);
		if (!(typeNode instanceof TypeDeclaration))
			return null;

		TypeDeclaration typeDeclaration= (TypeDeclaration) typeNode;
		UnimplementedCodeFixCore.MakeTypeAbstractOperation operation= new UnimplementedCodeFixCore.MakeTypeAbstractOperation(typeDeclaration);

		String label= Messages.format(CorrectionMessages.ModifierCorrectionSubProcessor_addabstract_description, BasicElementLabels.getJavaElementName(typeDeclaration.getName().getIdentifier()));
		return new UnimplementedCodeFix(label, root, new CompilationUnitRewriteOperationsFixCore.CompilationUnitRewriteOperation[] { operation });
	}

	public static ASTNode getSelectedTypeNode(CompilationUnit root, IProblemLocation problem) {
		ASTNode selectedNode= problem.getCoveringNode(root);
		if (selectedNode == null)
			return null;

		if (selectedNode.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) { // bug 200016
			selectedNode= selectedNode.getParent();
		}

		if (selectedNode.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY) {
			selectedNode= selectedNode.getParent();
		}
		if (selectedNode.getNodeType() == ASTNode.SIMPLE_NAME && selectedNode.getParent() instanceof AbstractTypeDeclaration) {
			return selectedNode.getParent();
		} else if (selectedNode.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			return ((ClassInstanceCreation) selectedNode).getAnonymousClassDeclaration();
		} else if (selectedNode.getNodeType() == ASTNode.ENUM_CONSTANT_DECLARATION) {
			EnumConstantDeclaration enumConst= (EnumConstantDeclaration) selectedNode;
			if (enumConst.getAnonymousClassDeclaration() != null)
				return enumConst.getAnonymousClassDeclaration();
			return enumConst;
		} else {
			return null;
		}
	}

	private static boolean isTypeBindingNull(ASTNode typeNode) {
		if (typeNode instanceof AbstractTypeDeclaration) {
			AbstractTypeDeclaration abstractTypeDeclaration= (AbstractTypeDeclaration) typeNode;
			if (abstractTypeDeclaration.resolveBinding() == null)
				return true;

			return false;
		} else if (typeNode instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration anonymousClassDeclaration= (AnonymousClassDeclaration) typeNode;
			if (anonymousClassDeclaration.resolveBinding() == null)
				return true;

			return false;
		} else if (typeNode instanceof EnumConstantDeclaration) {
			return false;
		} else {
			return true;
		}
	}

	public UnimplementedCodeFix(String name, CompilationUnit compilationUnit, CompilationUnitRewriteOperationsFixCore.CompilationUnitRewriteOperation[] fixRewriteOperations) {
		super(name, compilationUnit, fixRewriteOperations);
	}
}
