/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ui.examples.javaeditor.java;


import java.text.MessageFormat;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Example Java completion processor.
 */
public class JavaCompletionProcessor implements IContentAssistProcessor {

	/**
	 * Simple content assist tip closer. The tip is valid in a range
	 * of 5 characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

		protected int fInstallOffset;

		@Override
		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		@Override
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			fInstallOffset= offset;
		}

		@Override
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	protected final static String[] fgProposals=
		{ "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while" }; //$NON-NLS-48$ //$NON-NLS-47$ //$NON-NLS-46$ //$NON-NLS-45$ //$NON-NLS-44$ //$NON-NLS-43$ //$NON-NLS-42$ //$NON-NLS-41$ //$NON-NLS-40$ //$NON-NLS-39$ //$NON-NLS-38$ //$NON-NLS-37$ //$NON-NLS-36$ //$NON-NLS-35$ //$NON-NLS-34$ //$NON-NLS-33$ //$NON-NLS-32$ //$NON-NLS-31$ //$NON-NLS-30$ //$NON-NLS-29$ //$NON-NLS-28$ //$NON-NLS-27$ //$NON-NLS-26$ //$NON-NLS-25$ //$NON-NLS-24$ //$NON-NLS-23$ //$NON-NLS-22$ //$NON-NLS-21$ //$NON-NLS-20$ //$NON-NLS-19$ //$NON-NLS-18$ //$NON-NLS-17$ //$NON-NLS-16$ //$NON-NLS-15$ //$NON-NLS-14$ //$NON-NLS-13$ //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	protected IContextInformationValidator fValidator= new Validator();

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		ICompletionProposal[] result= new ICompletionProposal[fgProposals.length];
		for (int i= 0; i < fgProposals.length; i++) {
			IContextInformation info= new ContextInformation(fgProposals[i], MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.Proposal.ContextInfo.pattern"), new Object[] { fgProposals[i] })); //$NON-NLS-1$
			result[i]= new CompletionProposal(fgProposals[i], documentOffset, 0, fgProposals[i].length(), null, fgProposals[i], info, MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.Proposal.hoverinfo.pattern"), new Object[] { fgProposals[i]})); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result= new IContextInformation[5];
		for (int i= 0; i < result.length; i++)
			result[i]= new ContextInformation(
				MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"), new Object[] { Integer.valueOf(i), Integer.valueOf(documentOffset) }),  //$NON-NLS-1$
				MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"), new Object[] { Integer.valueOf(i), Integer.valueOf(documentOffset - 5), Integer.valueOf(documentOffset + 5)})); //$NON-NLS-1$
		return result;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.', '(' };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '#' };
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}
}
