/*******************************************************************************
 * Copyright (c) 2011-2016 Igor Fedorenko
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Igor Fedorenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.launching.sourcelookup.advanced;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.launching.sourcelookup.advanced.AdvancedSourceLookupSupport;
import org.eclipse.jdt.internal.launching.sourcelookup.advanced.IJDIHelpers;

/**
 * Static methods for implementing advanced source lookup.
 *
 * @since 3.10
 * @provisional This is part of work in progress and can be changed, moved or removed without notice
 */
public class AdvancedSourceLookup {
	private static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	/**
	 * Returns {@code true} if the given project has sources folders, {@code false} otherwise.
	 */
	public static boolean isSourceProject(IJavaProject project) throws JavaModelException {
		for (IPackageFragmentRoot fragment : project.getPackageFragmentRoots()) {
			if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
				return true;
			}
		}
		return false;
	}

	public static Map<File, IPackageFragmentRoot> getClasspath(IJavaProject project) throws JavaModelException {
		final Map<File, IPackageFragmentRoot> classpath = new LinkedHashMap<>();
		for (IPackageFragmentRoot fragment : project.getPackageFragmentRoots()) {
			if (fragment.getKind() == IPackageFragmentRoot.K_BINARY) {
				File classpathLocation = null;
				if (fragment.isExternal()) {
					classpathLocation = fragment.getPath().toFile();
				} else {
					IResource resource = fragment.getResource();
					if (resource != null) {
						IPath location = resource.getLocation();
						if (location != null) {
							classpathLocation = location.toFile();
						}
					}
				}
				if (classpathLocation != null) {
					classpath.put(classpathLocation, fragment);
				}
			}
		}
		return classpath;
	}

	public static Set<File> getOutputDirectories(IJavaProject project) throws JavaModelException {
		final Set<File> locations = new LinkedHashSet<>();
		addWorkspaceLocation(locations, project.getOutputLocation());
		for (IClasspathEntry cpe : project.getRawClasspath()) {
			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE && cpe.getOutputLocation() != null) {
				addWorkspaceLocation(locations, cpe.getOutputLocation());
			}
		}
		return locations;
	}

	private static void addWorkspaceLocation(Collection<File> locations, IPath workspacePath) {
		IResource resource = root.findMember(workspacePath);
		if (resource == null) {
			return;
		}
		IPath location = resource.getLocation();
		if (location == null) {
			return;
		}
		locations.add(location.toFile());
	}

	/**
	 * Returns {@code -javaagent} jvm launch argument.
	 */
	public static String getJavaagentString() {
		return AdvancedSourceLookupSupport.getJavaagentString();
	}

	/**
	 * Returns filesystem classes location that corresponds to the given debug element, or {@code null} if the location cannot be determined.
	 */
	public static File getClassesLocation(Object fElement) throws DebugException {
		return IJDIHelpers.INSTANCE.getClassesLocation(fElement);
	}

	/**
	 * Creates and returns new {@link IPersistableSourceLocator} of the specified type and with the provided configuration.
	 */
	public static IPersistableSourceLocator createSourceLocator(String type, ILaunchConfiguration configuration) throws CoreException {
		return AdvancedSourceLookupSupport.createSourceLocator(type, configuration);
	}
}
