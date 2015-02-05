/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.axes;

import java.util.Iterator;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.NameAttributeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyOwnerPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;
import org.apache.commons.jxpath.util.PropertyIdentifier;

/**
 * EvalContext that checks predicates.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public class PredicateContext extends EvalContext {
	private final Expression expression;
	private boolean done = false;
	private Expression nameTestExpression = null;
	private PropertyPointer dynamicPropertyPointer = null;

	/**
	 * Create a new PredicateContext.
	 *
	 * @param parentContext parent context
	 * @param expression    compiled Expression
	 */
	public PredicateContext(final EvalContext parentContext, final Expression expression) {
		super(parentContext);
		this.expression = expression;
		if (expression instanceof NameAttributeTest) {
			this.nameTestExpression =
					((NameAttributeTest) expression).getNameTestExpression();
		}
	}

	public boolean nextNode() {
		if (this.done) {
			return false;
		}
		while (this.parentContext.nextNode()) {
			if (setupDynamicPropertyPointer()) {
				final Object pred = this.nameTestExpression.computeValue(this.parentContext);
				final String propertyName = InfoSetUtil.stringValue(pred);

				// At this point it would be nice to say:
				// dynamicPropertyPointer.setPropertyName(propertyName)
				// and then: dynamicPropertyPointer.isActual().
				// However some PropertyPointers, e.g. DynamicPropertyPointer
				// will declare that any property you ask for is actual.
				// That's not acceptable for us: we really need to know
				// if the property is currently declared. Thus,
				// we'll need to perform a search.
				PropertyIdentifier foundProperty = null;
				for (final PropertyIdentifier name : this.dynamicPropertyPointer.getPropertyNames()) {
					if (name.getLocalName().equals(propertyName)) {
						foundProperty = name;
					}
				}
				if (foundProperty != null) {
					this.dynamicPropertyPointer.setPropertyName(foundProperty);
					this.position++;
					return true;
				}
			} else {
				Object pred = this.expression.computeValue(this.parentContext);
				if (pred instanceof Iterator) {
					if (!((Iterator) pred).hasNext()) {
						return false;
					}
					pred = ((Iterator) pred).next();
				}

				if (pred instanceof NodePointer) {
					pred = ((NodePointer) pred).getNode();
				}

				if (pred instanceof Number) {
					final int pos = (int) InfoSetUtil.doubleValue(pred);
					this.position++;
					this.done = true;
					return this.parentContext.setPosition(pos);
				}
				if (InfoSetUtil.booleanValue(pred)) {
					this.position++;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Used for an optimized access to dynamic properties using the
	 * "map[@name_ = 'name']" syntax
	 *
	 * @return whether valid
	 */
	private boolean setupDynamicPropertyPointer() {
		if (this.nameTestExpression == null) {
			return false;
		}

		NodePointer parent = this.parentContext.getCurrentNodePointer();
		if (parent == null) {
			return false;
		}
		parent = parent.getValuePointer();
		if (!(parent instanceof PropertyOwnerPointer)) {
			return false;
		}
		this.dynamicPropertyPointer =
				(PropertyPointer) ((PropertyOwnerPointer) parent)
						.getPropertyPointer()
						.clone();
		return true;
	}

	public boolean setPosition(final int position) {
		if (this.nameTestExpression == null) {
			return setPositionStandard(position);
		} else {
			if (this.dynamicPropertyPointer == null && !setupDynamicPropertyPointer()) {
				return setPositionStandard(position);
			}
			if (position < 1
					|| position > this.dynamicPropertyPointer.getLength()) {
				return false;
			}
			this.dynamicPropertyPointer.setIndex(position - 1);
			return true;
		}
	}

	public NodePointer getCurrentNodePointer() {
		if (this.position == 0 && !setPosition(1)) {
			return null;
		}
		if (this.dynamicPropertyPointer != null) {
			return this.dynamicPropertyPointer.getValuePointer();
		}
		return this.parentContext.getCurrentNodePointer();
	}

	public void reset() {
		super.reset();
		this.parentContext.reset();
		this.done = false;
	}

	public boolean nextSet() {
		reset();
		return this.parentContext.nextSet();
	}

	/**
	 * Basic setPosition
	 *
	 * @param position to set
	 * @return whether valid
	 */
	private boolean setPositionStandard(final int position) {
		if (this.position > position) {
			reset();
		}

		while (this.position < position) {
			if (!nextNode()) {
				return false;
			}
		}
		return true;
	}
}
