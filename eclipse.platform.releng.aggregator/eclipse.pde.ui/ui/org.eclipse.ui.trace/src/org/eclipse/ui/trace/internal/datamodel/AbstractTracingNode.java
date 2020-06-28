/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
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
package org.eclipse.ui.trace.internal.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A abstract base class implementation of the {@link TracingNode} interface.
 */
public abstract class AbstractTracingNode implements TracingNode {

	/**
	 * Constructor to create the empty list of children
	 */
	public AbstractTracingNode() {
		children = new ArrayList<>();
	}

	protected abstract void populateChildren();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.trace.internal.datamodel.TracingNode#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.trace.internal.datamodel.TracingNode#getParent()
	 */
	@Override
	public TracingNode getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.trace.internal.datamodel.TracingNode#getChildren()
	 */
	@Override
	public TracingNode[] getChildren() {
		initialize();
		return children.toArray(new TracingNode[children.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.trace.internal.datamodel.TracingNode#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		initialize();
		return children != null && children.size() > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.trace.internal.datamodel.TracingNode#addChild(org.eclipse.ui.trace.internal.datamodel.TracingNode)
	 */
	@Override
	public void addChild(final TracingNode childNode) {
		if (!children.contains(childNode)) {
			children.add(childNode);
		}
	}

	/**
	 * Populate the list of children for this node if it has not been initialized yet.
	 */
	public void initialize() {
		if (!childrenInitialized) {
			populateChildren();
			childrenInitialized = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.trace.internal.datamodel.TracingNode#setLabel(java.lang.String)
	 */
	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.trace.internal.datamodel.TracingNode#setParent(org.eclipse.ui.trace.internal.datamodel.TracingNode)
	 */
	@Override
	public void setParent(final TracingNode parent) {
		if (this.parent == null) {
			this.parent = parent;
			if (this.parent != null) {
				// since a parent is being set then it should also be added as a child
				this.parent.addChild(this);
			}
		}
	}

	/** This nodes parent node */
	protected TracingNode parent = null;

	/** The label for this node */
	protected String label = null;

	/** The list of child nodes for this node */
	protected List<TracingNode> children = null;

	/** A flag to determine if the children have been initialized for this node */
	private boolean childrenInitialized = false;
}