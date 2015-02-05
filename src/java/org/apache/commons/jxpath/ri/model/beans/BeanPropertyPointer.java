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

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.util.List;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.JXPathPropertyDescriptor;
import org.apache.commons.jxpath.util.PropertyIdentifier;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * Pointer pointing to a property of a JavaBean.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public class BeanPropertyPointer extends PropertyPointer {
	private static final long serialVersionUID = -6008991447676468786L;

	private static final Object UNINITIALIZED = new Object();

	private PropertyIdentifier propertyName = null;
	private final JXPathBeanInfo beanInfo;
	private Object baseValue = BeanPropertyPointer.UNINITIALIZED;
	private Object value = BeanPropertyPointer.UNINITIALIZED;
	private transient PropertyIdentifier[] names = null;
	private transient List<JXPathPropertyDescriptor> propertyDescriptors = null;
	private transient JXPathPropertyDescriptor propertyDescriptor = null;

	/**
	 * Create a new BeanPropertyPointer.
	 *
	 * @param parent   parent pointer
	 * @param beanInfo describes the target property/ies.
	 */
	public BeanPropertyPointer(final NodePointer parent, final JXPathBeanInfo beanInfo) {
		super(parent);
		this.beanInfo = beanInfo;
	}

	/**
	 * This type of node is auxiliary.
	 *
	 * @return true
	 */
	public boolean isContainer() {
		return true;
	}

	public int getPropertyCount() {
		if (this.beanInfo.isAtomic()) {
			return 0;
		}
		return getPropertyDescriptors().size();
	}

	/**
	 * Get the names of all properties, sorted alphabetically
	 *
	 * @return String[]
	 */
	public PropertyIdentifier[] getPropertyNames() {
		if (this.names == null) {
			final List<JXPathPropertyDescriptor> pds = getPropertyDescriptors();
			this.names = new PropertyIdentifier[pds.size()];
			int i = 0;
			for (final JXPathPropertyDescriptor pd : pds) {
				this.names[i++] = pd.getId();
			}
		}
		return this.names;
	}

	/**
	 * Selects a property by its offset in the alphabetically sorted list.
	 *
	 * @param index property index
	 */
	public void setPropertyIndex(final int index) {
		if (this.propertyIndex != index) {
			super.setPropertyIndex(index);
			this.propertyName = null;
			this.propertyDescriptor = null;
			this.baseValue = BeanPropertyPointer.UNINITIALIZED;
			this.value = BeanPropertyPointer.UNINITIALIZED;
		}
	}

	/**
	 * Get the value of the currently selected property.
	 *
	 * @return Object value
	 */
	public Object getBaseValue() {
		if (this.baseValue == BeanPropertyPointer.UNINITIALIZED) {
			final PropertyDescriptor pd = getPropertyDescriptor();
			if (pd == null) {
				return null;
			}
			this.baseValue = ValueUtils.getValue(getBean(), pd);
		}
		return this.baseValue;
	}

	public void setIndex(final int index) {
		if (this.index == index) {
			return;
		}
		// When dealing with a scalar, index == 0 is equivalent to
		// WHOLE_COLLECTION, so do not change it.
		if (this.index != NodePointer.WHOLE_COLLECTION
				|| index != 0
				|| isCollection()) {
			super.setIndex(index);
			this.value = BeanPropertyPointer.UNINITIALIZED;
		}
	}

	/**
	 * If index == WHOLE_COLLECTION, the value of the property, otherwise
	 * the value of the index'th element of the collection represented by the
	 * property. If the property is not a collection, index should be zero
	 * and the value will be the property itself.
	 *
	 * @return Object
	 */
	public Object getImmediateNode() {
		if (this.value == BeanPropertyPointer.UNINITIALIZED) {
			if (this.index == NodePointer.WHOLE_COLLECTION) {
				this.value = ValueUtils.getValue(getBaseValue());
			} else {
				final PropertyDescriptor pd = getPropertyDescriptor();
				if (pd == null) {
					this.value = null;
				} else {
					this.value = ValueUtils.getValue(getBean(), pd, this.index);
				}
			}
		}
		return this.value;
	}

	protected boolean isActualProperty() {
		return getPropertyDescriptor() != null;
	}

	public boolean isCollection() {
		final PropertyDescriptor pd = getPropertyDescriptor();
		if (pd == null) {
			return false;
		}

		if (pd instanceof IndexedPropertyDescriptor) {
			return true;
		}

		final int hint = ValueUtils.getCollectionHint(pd.getPropertyType());
		if (hint == -1) {
			return false;
		}
		if (hint == 1) {
			return true;
		}

		final Object value = getBaseValue();
		return value != null && ValueUtils.isCollection(value);
	}

	/**
	 * If the property contains a collection, then the length of that
	 * collection, otherwise - 1.
	 *
	 * @return int length
	 */
	public int getLength() {
		final PropertyDescriptor pd = getPropertyDescriptor();
		if (pd == null) {
			return 1;
		}

		if (pd instanceof IndexedPropertyDescriptor) {
			return ValueUtils.getIndexedPropertyLength(
					getBean(),
					(IndexedPropertyDescriptor) pd);
		}

		final int hint = ValueUtils.getCollectionHint(pd.getPropertyType());
		if (hint == -1) {
			return 1;
		}
		return super.getLength();
	}

	/**
	 * If index == WHOLE_COLLECTION, change the value of the property, otherwise
	 * change the value of the index'th element of the collection
	 * represented by the property.
	 *
	 * @param value value to set
	 */
	public void setValue(final Object value) {
		final PropertyDescriptor pd = getPropertyDescriptor();
		if (pd == null) {
			throw new JXPathInvalidAccessException(
					"Cannot set property: " + asPath() + " - no such property");
		}

		if (this.index == NodePointer.WHOLE_COLLECTION) {
			ValueUtils.setValue(getBean(), pd, value);
		} else {
			ValueUtils.setValue(getBean(), pd, this.index, value);
		}
		this.value = value;
	}

	public NodePointer createPath(final JXPathContext context) {
		if (getImmediateNode() == null) {
			super.createPath(context);
			this.baseValue = BeanPropertyPointer.UNINITIALIZED;
			this.value = BeanPropertyPointer.UNINITIALIZED;
		}
		return this;
	}

	public void remove() {
		if (this.index == NodePointer.WHOLE_COLLECTION) {
			setValue(null);
		} else if (isCollection()) {
			final Object o = getBaseValue();
			final Object collection = ValueUtils.remove(getBaseValue(), this.index);
			if (collection != o) {
				ValueUtils.setValue(getBean(), getPropertyDescriptor(), collection);
			}
		} else if (this.index == 0) {
			this.index = NodePointer.WHOLE_COLLECTION;
			setValue(null);
		}
	}

	/**
	 * Get the name of the currently selected property.
	 *
	 * @return String property name
	 */
	public PropertyIdentifier getPropertyName() {
		if (this.propertyName == null) {
			final JXPathPropertyDescriptor pd = getDescriptor();
			if (pd != null) {
				this.propertyName = pd.getId();
			}
		}
		return this.propertyName != null ? this.propertyName : PropertyIdentifier.WILDCARD;
	}

	/**
	 * Select a property by name.
	 *
	 * @param propertyName String name
	 */
	public void setPropertyName(final PropertyIdentifier propertyName) {
		setPropertyIndex(PropertyPointer.UNSPECIFIED_PROPERTY);
		this.propertyName = propertyName;
	}

	/**
	 * Finds the property descriptor corresponding to the current property
	 * index.
	 *
	 * @return PropertyDescriptor
	 */
	private JXPathPropertyDescriptor getDescriptor() {
		if (this.propertyDescriptor == null) {
			final int inx = getPropertyIndex();
			if (inx == PropertyPointer.UNSPECIFIED_PROPERTY) {
				this.propertyDescriptor = this.beanInfo.getPropertyDescriptor(this.propertyName);
				if(this.propertyDescriptor != null) {
					setAttribute(this.propertyDescriptor.getId().isAttribute());
				}
			} else {
				final List<JXPathPropertyDescriptor> propertyDescriptors =
						getPropertyDescriptors();
				if (inx >= 0 && inx < propertyDescriptors.size()) {
					this.propertyDescriptor = propertyDescriptors.get(inx);
					setAttribute(this.propertyDescriptor.getId().isAttribute());
				} else {
					this.propertyDescriptor = null;
				}
			}
		}
		return this.propertyDescriptor;
	}

	private PropertyDescriptor getPropertyDescriptor() {
		final JXPathPropertyDescriptor descriptor = getDescriptor();
		return descriptor == null ? null : descriptor.getPropertyDescriptor();
	}

	/**
	 * Get all PropertyDescriptors.
	 *
	 * @return PropertyDescriptor[]
	 */
	protected synchronized List<JXPathPropertyDescriptor> getPropertyDescriptors() {
		if (this.propertyDescriptors == null) {
			this.propertyDescriptors = this.beanInfo.getPropertyDescriptors();
		}
		return this.propertyDescriptors;
	}
}
