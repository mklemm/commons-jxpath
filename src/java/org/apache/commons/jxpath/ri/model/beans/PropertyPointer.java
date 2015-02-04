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
package org.apache.commons.jxpath.ri.model.beans;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * A pointer allocated by a PropertyOwnerPointer to represent the value of
 * a property of the parent object.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public abstract class PropertyPointer extends NodePointer {
	public static final int UNSPECIFIED_PROPERTY = Integer.MIN_VALUE;

	/**
	 * property index
	 */
	protected int propertyIndex = PropertyPointer.UNSPECIFIED_PROPERTY;

	/**
	 * owning object
	 */
	protected Object bean;

	/**
	 * Takes a javabean, a descriptor of a property of that object and
	 * an offset within that property (starting with 0).
	 *
	 * @param parent parent pointer
	 */
	public PropertyPointer(final NodePointer parent) {
		super(parent);
	}

	/**
	 * Get the property index.
	 *
	 * @return int index
	 */
	public int getPropertyIndex() {
		return this.propertyIndex;
	}

	/**
	 * Set the property index.
	 *
	 * @param index property index
	 */
	public void setPropertyIndex(final int index) {
		if (this.propertyIndex != index) {
			this.propertyIndex = index;
			setIndex(NodePointer.WHOLE_COLLECTION);
		}
	}

	/**
	 * Get the parent bean.
	 *
	 * @return Object
	 */
	public Object getBean() {
		if (this.bean == null) {
			this.bean = getImmediateParentPointer().getNode();
		}
		return this.bean;
	}

	public QName getName() {
		return getPropertyName();
	}

	/**
	 * Get the property name.
	 *
	 * @return String property name.
	 */
	public abstract QName getPropertyName();

	/**
	 * Set the property name.
	 *
	 * @param propertyName property name to set.
	 */
	public abstract void setPropertyName(QName propertyName);

	/**
	 * Count the number of properties represented.
	 *
	 * @return int
	 */
	public abstract int getPropertyCount();

	/**
	 * Get the names of the included properties.
	 *
	 * @return String[]
	 */
	public abstract QName[] getPropertyNames();

	/**
	 * Learn whether this pointer references an actual property.
	 *
	 * @return true if actual
	 */
	protected abstract boolean isActualProperty();

	public boolean isActual() {
		return isActualProperty() && super.isActual();
	}

	private static final Object UNINITIALIZED = new Object();

	private Object value = PropertyPointer.UNINITIALIZED;

	public Object getImmediateNode() {
		if (this.value == PropertyPointer.UNINITIALIZED) {
			this.value = this.index == NodePointer.WHOLE_COLLECTION ? ValueUtils.getValue(getBaseValue())
					: ValueUtils.getValue(getBaseValue(), this.index);
		}
		return this.value;
	}

	public boolean isCollection() {
		final Object value = getBaseValue();
		return value != null && ValueUtils.isCollection(value);
	}

	public boolean isLeaf() {
		final Object value = getNode();
		return value == null || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
	}

	/**
	 * If the property contains a collection, then the length of that
	 * collection, otherwise - 1.
	 *
	 * @return int length
	 */
	public int getLength() {
		final Object baseValue = getBaseValue();
		return baseValue == null ? 1 : ValueUtils.getLength(baseValue);
	}

	/**
	 * Returns a NodePointer that can be used to access the currently
	 * selected property value.
	 *
	 * @return NodePointer
	 */
	public NodePointer getImmediateValuePointer() {
		return NodePointer.newChildNodePointer(
				(NodePointer) this.clone(),
				getName(),
				getImmediateNode());
	}

	public NodePointer createPath(final JXPathContext context) {
		if (getImmediateNode() == null) {
			final AbstractFactory factory = getAbstractFactory(context);
			final int inx = (this.index == NodePointer.WHOLE_COLLECTION ? 0 : this.index);
			final boolean success =
					factory.createObject(
							context,
							this,
							getBean(),
							getPropertyName().toString(),
							inx);
			if (!success) {
				throw new JXPathAbstractFactoryException("Factory " + factory
						+ " could not create an object for path: " + asPath());
			}
		}
		return this;
	}

	public NodePointer createPath(final JXPathContext context, final Object value) {
		// If neccessary, expand collection
		if (this.index != NodePointer.WHOLE_COLLECTION && this.index >= getLength()) {
			createPath(context);
		}
		setValue(value);
		return this;
	}

	public NodePointer createChild(
			final JXPathContext context,
			final QName name,
			final int index,
			final Object value) {
		final PropertyPointer prop = (PropertyPointer) clone();
		if (name != null) {
			prop.setPropertyName(name);
		}
		prop.setIndex(index);
		return prop.createPath(context, value);
	}

	public NodePointer createChild(
			final JXPathContext context,
			final QName name,
			final int index) {
		final PropertyPointer prop = (PropertyPointer) clone();
		if (name != null) {
			prop.setPropertyName(name);
		}
		prop.setIndex(index);
		return prop.createPath(context);
	}

	public int hashCode() {
		return getImmediateParentPointer().hashCode() + this.propertyIndex + this.index;
	}

	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof PropertyPointer)) {
			return false;
		}

		final PropertyPointer other = (PropertyPointer) object;
		if (this.parent != other.parent && (this.parent == null || !this.parent.equals(other.parent))) {
			return false;
		}

		if (getPropertyIndex() != other.getPropertyIndex()
				|| !getPropertyName().equals(other.getPropertyName())) {
			return false;
		}

		final int iThis = (this.index == NodePointer.WHOLE_COLLECTION ? 0 : this.index);
		final int iOther = (other.index == NodePointer.WHOLE_COLLECTION ? 0 : other.index);
		return iThis == iOther;
	}

	public int compareChildNodePointers(
			final NodePointer pointer1,
			final NodePointer pointer2) {
		return getValuePointer().compareChildNodePointers(pointer1, pointer2);
	}

}
