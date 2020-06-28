/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval;

import org.eclipse.osgi.util.NLS;

public class EvaluationMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.debug.eval.EvaluationMessages";//$NON-NLS-1$

	public static String LocalEvaluationEngine_Evaluation_in_context_of_inner_type_not_supported__19;
	public static String LocalEvaluationEngine_Evaluation_failed___unable_to_determine_receiving_type_context__18;
	public static String LocalEvaluationEngine_Evaluation_failed___internal_error_retreiving_result__17;
	public static String LocalEvaluationEngine_Evaluation_failed___unable_to_instantiate_code_snippet_class__11;
	public static String LocalEvaluationEngine__0__occurred_deploying_class_file_for_evaluation_9;
	public static String LocalEvaluationEngine_Evaluation_failed___evaluation_thread_must_be_suspended__8;
	public static String LocalEvaluationEngine_Evaluation_failed___evaluation_context_has_been_disposed__7;
	public static String LocalEvaluationEngine_Evaluation_failed___unable_to_initialize_local_variables__6;
	public static String LocalEvaluationEngine_Evaluation_failed___unable_to_initialize___this___context__5;
	public static String LocalEvaluationEngine_Evaluation_failed___unable_to_initialize_local_variables__4;
	public static String RemoteEvaluationEngine_Evaluation_failed___unable_to_instantiate_snippet_class;
	public static String RemoteEvaluationEngine_Evaluation_failed___unable_to_find_injected_class;
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, EvaluationMessages.class);
	}
}
