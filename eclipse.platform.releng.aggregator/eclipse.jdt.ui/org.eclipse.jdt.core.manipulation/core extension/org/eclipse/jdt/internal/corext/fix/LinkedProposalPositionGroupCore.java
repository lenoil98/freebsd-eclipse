/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
 *     Red Hat Inc. - split from LinkedProposalPositionGroup
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.fix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedPosition;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.manipulation.CodeStyleConfiguration;

import org.eclipse.jdt.internal.core.manipulation.BindingLabelProviderCore;
import org.eclipse.jdt.internal.core.manipulation.JavaElementLabelsCore;

public class LinkedProposalPositionGroupCore {

	/**
	 * {@link LinkedProposalPositionGroupCore.PositionInformation} describes a position
	 * inside a position group. The information provided must be accurate
	 * after the document change to the proposal has been performed, but doesn't
	 * need to reflect the changed done by the linking mode.
	 */
	public static abstract class PositionInformation {
		public abstract int getOffset();
		public abstract int getLength();
		public abstract int getSequenceRank();
	}

	public static class ProposalCore {

		private String fDisplayString;
		private int fRelevance;

		public ProposalCore(String displayString, int relevance) {
			fDisplayString= displayString;
			fRelevance= relevance;
		}

		public String getDisplayString() {
			return fDisplayString;
		}

		public int getRelevance() {
			return fRelevance;
		}

		public String getAdditionalProposalInfo() {
			return null;
		}

		@SuppressWarnings("unused")
		public TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model) throws CoreException {
			return new ReplaceEdit(position.getOffset(), position.getLength(), fDisplayString);
		}
	}

	public static PositionInformation createPositionInformation(ITrackedNodePosition pos, int sequenceRank) {
		return new TrackedNodePosition(pos, sequenceRank);
	}

	private static class TrackedNodePosition extends PositionInformation {

		private final ITrackedNodePosition fPos;
		private final int fSequenceRank;

		public TrackedNodePosition(ITrackedNodePosition pos, int sequenceRank) {
			fPos= pos;
			fSequenceRank= sequenceRank;
		}

		@Override
		public int getOffset() {
			return fPos.getStartPosition();
		}

		@Override
		public int getLength() {
			return fPos.getLength();
		}

		@Override
		public int getSequenceRank() {
			return fSequenceRank;
		}
	}

	/**
	 * A position that contains all of the given tracked node positions.
	 *
	 * @since 3.7
	 */
	public static class TrackedNodesPosition extends PositionInformation {

		private final Collection<ITrackedNodePosition> fPos;

		/**
		 * A position that contains all of the given tracked node positions.
		 *
		 * @param pos the positions
		 */
		public TrackedNodesPosition(Collection<ITrackedNodePosition> pos) {
			fPos= pos;
		}

		@Override
		public int getOffset() {
			int minStart= Integer.MAX_VALUE;
			for (ITrackedNodePosition node : fPos) {
				minStart= Math.min(minStart, node.getStartPosition());
			}
			return minStart == Integer.MAX_VALUE ? -1 : minStart;
		}

		@Override
		public int getLength() {
			int minStart= Integer.MAX_VALUE;
			int maxEnd= 0;
			for (ITrackedNodePosition node : fPos) {
				minStart= Math.min(minStart, node.getStartPosition());
				maxEnd= Math.max(maxEnd, node.getStartPosition() + node.getLength());
			}
			return minStart == Integer.MAX_VALUE ? 0 : maxEnd - getOffset();
		}

		@Override
		public int getSequenceRank() {
			return 0;
		}
	}

	/**
	 * A position for the start of the given tracked node position.
	 *
	 * @since 3.7
	 */
	public static class StartPositionInformation extends PositionInformation {

		private ITrackedNodePosition fPos;

		/**
		 * A position for the start of the given tracked node position.
		 *
		 * @param pos the position
		 */
		public StartPositionInformation(ITrackedNodePosition pos) {
			fPos= pos;
		}

		@Override
		public int getOffset() {
			return fPos.getStartPosition();
		}

		@Override
		public int getLength() {
			return 0;
		}

		@Override
		public int getSequenceRank() {
			return 0;
		}
	}

	private static final class JavaLinkedModeProposalCore extends ProposalCore {
		private final ITypeBinding fTypeProposal;
		private final ICompilationUnit fCompilationUnit;

		public JavaLinkedModeProposalCore(ICompilationUnit unit, ITypeBinding typeProposal, int relevance) {
			super(BindingLabelProviderCore.getBindingLabel(typeProposal, JavaElementLabelsCore.ALL_DEFAULT | JavaElementLabelsCore.ALL_POST_QUALIFIED), relevance);
			fTypeProposal= typeProposal;
			fCompilationUnit= unit;
		}

		@Override
		public TextEdit computeEdits(int offset, LinkedPosition position, char trigger, int stateMask, LinkedModeModel model) throws CoreException {
			ImportRewrite impRewrite= CodeStyleConfiguration.createImportRewrite(fCompilationUnit, true);
			String replaceString= impRewrite.addImport(fTypeProposal);

			MultiTextEdit composedEdit= new MultiTextEdit();
			composedEdit.addChild(new ReplaceEdit(position.getOffset(), position.getLength(), replaceString));
			composedEdit.addChild(impRewrite.rewriteImports(null));
			return composedEdit;
		}
	}


	private final String fGroupId;
	private final List<PositionInformation> fPositions;
	private final List<ProposalCore> fProposals;


	public LinkedProposalPositionGroupCore(String groupID) {
		fGroupId= groupID;
		fPositions= new ArrayList<>();
		fProposals= new ArrayList<>();
	}

	public void addPosition(PositionInformation position) {
		fPositions.add(position);
	}

	public void addPosition(ITrackedNodePosition position, int sequenceRank) {
		addPosition(createPositionInformation(position, sequenceRank));
	}

	public void addPosition(ITrackedNodePosition position, boolean isFirst) {
		addPosition(position, isFirst ? 0 : 1);
	}


	public void addProposal(ProposalCore proposal) {
		fProposals.add(proposal);
	}

	public void addProposal(String displayString, int relevance) {
		addProposal(new ProposalCore(displayString, relevance));
	}

	public void addProposal(ITypeBinding type, ICompilationUnit cu, int relevance) {
		addProposal(new JavaLinkedModeProposalCore(cu, type, relevance));
	}

	public String getGroupId() {
		return fGroupId;
	}

	public PositionInformation[] getPositions() {
		return fPositions.toArray(new PositionInformation[fPositions.size()]);
	}

	public ProposalCore[] getProposals() {
		return fProposals.toArray(new ProposalCore[fProposals.size()]);
	}

}
