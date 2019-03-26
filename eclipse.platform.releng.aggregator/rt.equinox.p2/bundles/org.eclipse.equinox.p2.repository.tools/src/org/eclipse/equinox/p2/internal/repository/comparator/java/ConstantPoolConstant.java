/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
package org.eclipse.equinox.p2.internal.repository.comparator.java;

public interface ConstantPoolConstant {

	int CONSTANT_Class = 7;
	int CONSTANT_Fieldref = 9;
	int CONSTANT_Methodref = 10;
	int CONSTANT_InterfaceMethodref = 11;
	int CONSTANT_String = 8;
	int CONSTANT_Integer = 3;
	int CONSTANT_Float = 4;
	int CONSTANT_Long = 5;
	int CONSTANT_Double = 6;
	int CONSTANT_NameAndType = 12;
	int CONSTANT_Utf8 = 1;

	int CONSTANT_Methodref_SIZE = 5;
	int CONSTANT_Class_SIZE = 3;
	int CONSTANT_Double_SIZE = 9;
	int CONSTANT_Fieldref_SIZE = 5;
	int CONSTANT_Float_SIZE = 5;
	int CONSTANT_Integer_SIZE = 5;
	int CONSTANT_InterfaceMethodref_SIZE = 5;
	int CONSTANT_Long_SIZE = 9;
	int CONSTANT_String_SIZE = 3;
	int CONSTANT_Utf8_SIZE = 3;
	int CONSTANT_NameAndType_SIZE = 5;

}
