/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.jface.operation;

/**
 * A thread listener is an object that is interested in receiving notifications
 * of thread changes.  For example, a thread listener can be used to notify a
 * runnable of the thread that will execute it, allowing the runnable to transfer
 * thread-local state from the calling thread before control passes to the new thread.
 *
 * @since 3.1
 */
public interface IThreadListener {
	/**
	 * Notification that a thread change is occurring.
	 *
	 * @param thread The new thread
	 */
	public void threadChange(Thread thread);
}
