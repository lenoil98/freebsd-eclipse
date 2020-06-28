/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Robert M. Fuhrer (rfuhrer@watson.ibm.com), IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets;

import java.util.Iterator;

import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

public class TypeSetUnion extends TypeSet {
	private TypeSet fLHS;
	private TypeSet fRHS;

	public TypeSetUnion(TypeSet lhs, TypeSet rhs) {
		super(lhs.getTypeSetEnvironment());
		fLHS= lhs;
		fRHS= rhs;
	}

	@Override
	public boolean isUniverse() {
		if (fLHS.isUniverse() || fRHS.isUniverse())
			return true;
		if (fLHS.isSingleton() && fRHS.isSingleton())
			return false;
		throw new IllegalStateException("unimplemented"); //$NON-NLS-1$
	}

	@Override
	public TypeSet makeClone() {
		return new TypeSetUnion(fLHS.makeClone(), fRHS.makeClone());
	}

	@Override
	public boolean isEmpty() {
		return fLHS.isEmpty() && fRHS.isEmpty();
	}

	@Override
	public boolean contains(TType t) {
		return fLHS.contains(t) || fRHS.contains(t);
	}

	@Override
	public boolean containsAll(TypeSet s) {
		return fLHS.containsAll(s) || fRHS.containsAll(s);
	}

	@Override
	public TType anyMember() {
		return fLHS.anyMember();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TypeSetUnion) {
			TypeSetUnion other= (TypeSetUnion) o;
			return other.fLHS.equals(fLHS) && other.fRHS.equals(fRHS);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return fLHS.hashCode() * 37 + fRHS.hashCode();
	}

	@Override
	public TypeSet upperBound() {
		throw new IllegalStateException("unimplemented"); //$NON-NLS-1$
	}

	@Override
	public TypeSet lowerBound() {
		throw new IllegalStateException("unimplemented"); //$NON-NLS-1$
	}

	@Override
	public Iterator<TType> iterator() {
		throw new IllegalStateException("unimplemented"); //$NON-NLS-1$
	}
	@Override
	public boolean isSingleton() {
		return fLHS.isSingleton() && fRHS.isSingleton() && fLHS.anyMember().equals(fRHS.anyMember());
	}

	@Override
	public boolean hasUniqueLowerBound() {
		return false;
	}

	@Override
	public boolean hasUniqueUpperBound() {
		return false;
	}

	@Override
	public TType uniqueLowerBound() {
		return null;
	}

	@Override
	public TType uniqueUpperBound() {
		return null;
	}

	@Override
	public EnumeratedTypeSet enumerate() {
		EnumeratedTypeSet result= fLHS.enumerate();

		result.addAll(fRHS.enumerate());
		return result;
	}

	@Override
	public String toString() {
		return "<" + fID + ": union(" + fLHS + "," + fRHS + ")>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
