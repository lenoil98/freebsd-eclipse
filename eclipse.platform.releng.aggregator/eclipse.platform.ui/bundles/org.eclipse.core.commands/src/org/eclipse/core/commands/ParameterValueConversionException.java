/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.core.commands;

import org.eclipse.core.commands.common.CommandException;

/**
 * Signals that a problem occurred while converting a command parameter value
 * from string to object, or object to string.
 *
 * @see AbstractParameterValueConverter
 * @since 3.2
 */
public class ParameterValueConversionException extends CommandException {

	/**
	 * Generated serial version UID for this class.
	 */
	private static final long serialVersionUID = 4703077729505066104L;

	/**
	 * Creates a new instance of this class with the specified detail message.
	 *
	 * @param message
	 *            the detail message; may be <code>null</code>.
	 */
	public ParameterValueConversionException(final String message) {
		super(message);
	}

	/**
	 * Creates a new instance of this class with the specified detail message
	 * and cause.
	 *
	 * @param message
	 *            the detail message; may be <code>null</code>.
	 * @param cause
	 *            the cause; may be <code>null</code>.
	 */
	public ParameterValueConversionException(final String message,
			final Throwable cause) {
		super(message, cause);
	}
}
