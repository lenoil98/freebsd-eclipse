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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;


/**
 * Model object that represents a single entry in a filter table.
 */
public class Filter {

	private String fName;
	private boolean fChecked;

	public Filter(String name, boolean checked) {
		setName(name);
		setChecked(checked);
	}

	public String getName() {
		return fName;
	}

	public void setName(String name) {
		fName = name;
	}

	public boolean isChecked() {
		return fChecked;
	}

	public void setChecked(boolean checked) {
		fChecked = checked;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Filter) {
			Filter other = (Filter) o;
			if (getName().equals(other.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}
}
