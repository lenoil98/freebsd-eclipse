/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

package org.eclipse.ui.internal.activities;

import java.util.List;

public interface IActivityRegistry {

    void addActivityRegistryListener(
            IActivityRegistryListener activityRegistryListener);

	List<ActivityRequirementBindingDefinition> getActivityRequirementBindingDefinitions();

	List<ActivityDefinition> getActivityDefinitions();

	List<ActivityPatternBindingDefinition> getActivityPatternBindingDefinitions();

	List<CategoryActivityBindingDefinition> getCategoryActivityBindingDefinitions();

	List<CategoryDefinition> getCategoryDefinitions();

	List<String> getDefaultEnabledActivities();

    void removeActivityRegistryListener(
            IActivityRegistryListener activityRegistryListener);
}
