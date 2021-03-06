/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

/**
 * Print 10,000 lines of output
 */
public class FloodConsole {

	public static void main(String[] args) {
		for (int i = 0; i < 9999; i++) {
			System.out.println("0---------1--------2--------3-------4--------5--------6--------7--------8");
		}
		System.err.print(" - THE END - ");
	}
}
