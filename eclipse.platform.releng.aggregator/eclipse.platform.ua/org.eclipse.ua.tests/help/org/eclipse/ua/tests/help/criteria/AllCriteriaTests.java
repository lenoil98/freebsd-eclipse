/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
package org.eclipse.ua.tests.help.criteria;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests help keyword index functionality.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ParseTocWithCriteria.class,
	CriteriaUtilitiesTest.class,
	CriteriaDefinitionTest.class,
	ParseCriteriaDefinition.class,
	TestCriteriaProvider.class
})
public class AllCriteriaTests {
}
