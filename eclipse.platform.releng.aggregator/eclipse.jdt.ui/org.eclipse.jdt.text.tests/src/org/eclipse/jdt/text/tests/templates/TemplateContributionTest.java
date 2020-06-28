/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.text.tests.templates;

import java.util.Iterator;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.template.java.JavaDocContextType;
import org.eclipse.jdt.internal.corext.template.java.SWTContextType;

import org.eclipse.jdt.internal.ui.JavaPlugin;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Template contribution tests.
 *
 * @since 3.4
 */
public class TemplateContributionTest extends TestCase {

	public static Test suite() {
		return new TestSuite(TemplateContributionTest.class);
	}

	private void checkContribution(String resolverContextTypeId, String contextTypeId) throws TemplateException {
		ContextTypeRegistry registry= JavaPlugin.getDefault().getTemplateContextRegistry();
		TemplateContextType context= registry.getContextType(resolverContextTypeId);

		for (Template template : JavaPlugin.getDefault().getTemplateStore().getTemplates(contextTypeId)) {
			TemplateTranslator translator= new TemplateTranslator();
			for (TemplateVariable variable : translator.translate(template).getVariables()) {
				if (!variable.getType().equals(variable.getName())) {
					assertTrue("No resolver found for variable '" + variable.getType() + "' in template '" + template.getName() + "'\n\n" + template.getPattern(), canHandle(context, variable));
				}
			}
		}
	}

	public void testJavaContribution() throws Exception {
		checkContribution(JavaContextType.ID_ALL, JavaContextType.ID_ALL);
		checkContribution(JavaContextType.ID_ALL, JavaContextType.ID_MEMBERS);
		checkContribution(JavaContextType.ID_ALL, JavaContextType.ID_STATEMENTS);
		checkContribution(JavaContextType.ID_MEMBERS, JavaContextType.ID_MEMBERS);
		checkContribution(JavaContextType.ID_STATEMENTS, JavaContextType.ID_STATEMENTS);
	}

	public void testJavaDocContribution() throws Exception {
		checkContribution(JavaDocContextType.ID, JavaDocContextType.ID);
	}

	public void testSWTContributionAll() throws Exception {
		checkContribution(SWTContextType.ID_ALL, SWTContextType.ID_ALL);
		checkContribution(SWTContextType.ID_ALL, SWTContextType.ID_MEMBERS);
		checkContribution(SWTContextType.ID_ALL, SWTContextType.ID_STATEMENTS);
		checkContribution(SWTContextType.ID_MEMBERS, SWTContextType.ID_MEMBERS);
		checkContribution(SWTContextType.ID_STATEMENTS, SWTContextType.ID_STATEMENTS);
	}

	private boolean canHandle(TemplateContextType context, TemplateVariable variable) {
		for (Iterator<TemplateVariableResolver> iterator= context.resolvers(); iterator.hasNext();) {
			TemplateVariableResolver resolver= iterator.next();
			if (variable.getType().equals(resolver.getType()))
				return true;
		}
		return false;
	}

}
