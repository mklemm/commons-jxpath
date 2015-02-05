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
package org.apache.commons.jxpath.ri.model.dynamic;

import java.util.Arrays;
import java.util.Map;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;
import org.apache.commons.jxpath.util.PropertyIdentifier;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * Pointer pointing to a property of an object with dynamic properties.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public class DynamicPropertyPointer extends PropertyPointer {

	private static final long serialVersionUID = -5720585681149150822L;

	private DynamicPropertyHandler handler;
	private String name;
	private String[] names;
	private String requiredPropertyName;

	/**
	 * Create a new DynamicPropertyPointer.
	 *
	 * @param parent  pointer
	 * @param handler DynamicPropertyHandler
	 */
	public DynamicPropertyPointer(final NodePointer parent,
	                              final DynamicPropertyHandler handler) {
		super(parent);
		this.handler = handler;
	}

	/**
	 * This type of node is auxiliary.
	 *
	 * @return true
	 */
	public boolean isContainer() {
		return true;
	}

	/**
	 * Number of the DP object's properties.
	 *
	 * @return int
	 */
	public int getPropertyCount() {
		return getPropertyNames().length;
	}

	/**
	 * Names of all properties, sorted alphabetically.
	 *
	 * @return String[]
	 */
	public PropertyIdentifier[] getPropertyNames() {
		if (this.names == null) {
			String[] allNames = this.handler.getPropertyNames(getBean());
			this.names = new String[allNames.length];
			for (int i = 0; i < this.names.length; i++) {
				this.names[i] = allNames[i];
			}
			Arrays.sort(this.names);
			if (this.requiredPropertyName != null) {
				final int inx = Arrays.binarySearch(this.names, this.requiredPropertyName);
				if (inx < 0) {
					allNames = this.names;
					this.names = new String[allNames.length + 1];
					this.names[0] = this.requiredPropertyName;
					System.arraycopy(allNames, 0, this.names, 1, allNames.length);
					Arrays.sort(this.names);
				}
			}
		}
		final PropertyIdentifier[] propertyIdentifiers = new PropertyIdentifier[this.names.length];
		int i = 0;
		for (final String name : this.names) {
			propertyIdentifiers[i++] = PropertyIdentifier.createUnqualified(name);
		}
		return propertyIdentifiers;
	}

	/**
	 * Returns the name of the currently selected property or "*"
	 * if none has been selected.
	 *
	 * @return String
	 */
	public PropertyIdentifier getPropertyName() {
		if (this.name == null) {
			final PropertyIdentifier[] names = getPropertyNames();
			this.name = this.propertyIndex >= 0 && this.propertyIndex < names.length ? names[this.propertyIndex].getLocalName() : "*";
		}
		return PropertyIdentifier.createUnqualified(this.name);
	}

	/**
	 * Select a property by name.  If the supplied name is
	 * not one of the object's existing properties, it implicitly
	 * adds this name to the object's property name list. It does not
	 * set the property value though. In order to set the property
	 * value, call setValue().
	 *
	 * @param propertyName to set
	 */
	public void setPropertyName(final PropertyIdentifier propertyName) {
		setPropertyIndex(PropertyPointer.UNSPECIFIED_PROPERTY);
		this.name = propertyName != null ? propertyName.getLocalName() : null;
		this.requiredPropertyName = this.name;
		if (this.names != null && Arrays.binarySearch(this.names, this.name) < 0) {
			this.names = null;
		}
	}

	/**
	 * Index of the currently selected property in the list of all
	 * properties sorted alphabetically.
	 *
	 * @return int
	 */
	public int getPropertyIndex() {
		if (this.propertyIndex == PropertyPointer.UNSPECIFIED_PROPERTY) {
			final PropertyIdentifier[] names = getPropertyNames();
			for (int i = 0; i < names.length; i++) {
				if (names[i].getLocalName().equals(this.name)) {
					setPropertyIndex(i);
					break;
				}
			}
		}
		return super.getPropertyIndex();
	}

	/**
	 * Index a property by its index in the list of all
	 * properties sorted alphabetically.
	 *
	 * @param index to set
	 */
	public void setPropertyIndex(final int index) {
		if (this.propertyIndex != index) {
			super.setPropertyIndex(index);
			this.name = null;
		}
	}

	/**
	 * Returns the value of the property, not an element of the collection
	 * represented by the property, if any.
	 *
	 * @return Object
	 */
	public Object getBaseValue() {
		return this.handler.getProperty(getBean(), getPropertyName().getLocalName());
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
		final Object value;
		if (this.index == NodePointer.WHOLE_COLLECTION) {
			value = ValueUtils.getValue(this.handler.getProperty(
					getBean(),
					getPropertyName().getLocalName()));
		} else {
			value = ValueUtils.getValue(this.handler.getProperty(
					getBean(),
					getPropertyName().getLocalName()), this.index);
		}
		return value;
	}

	/**
	 * A dynamic property is always considered actual - all keys are apparently
	 * existing with possibly the value of null.
	 *
	 * @return boolean
	 */
	protected boolean isActualProperty() {
		return true;
	}

	/**
	 * If index == WHOLE_COLLECTION, change the value of the property, otherwise
	 * change the value of the index'th element of the collection
	 * represented by the property.
	 *
	 * @param value to set
	 */
	public void setValue(final Object value) {
		if (this.index == NodePointer.WHOLE_COLLECTION) {
			this.handler.setProperty(getBean(), getPropertyName().getLocalName(), value);
		} else {
			ValueUtils.setValue(
					this.handler.getProperty(getBean(), getPropertyName().getLocalName()),
					this.index,
					value);
		}
	}

	public NodePointer createPath(final JXPathContext context) {
		// Ignore the name passed to us, use our own data
		Object collection = getBaseValue();
		if (collection == null) {
			final AbstractFactory factory = getAbstractFactory(context);
			final boolean success =
					factory.createObject(
							context,
							this,
							getBean(),
							getPropertyName().getLocalName(),
							0);
			if (!success) {
				throw new JXPathAbstractFactoryException(
						"Factory could not create an object for path: " + asPath());
			}
			collection = getBaseValue();
		}

		if (this.index != NodePointer.WHOLE_COLLECTION) {
			if (this.index < 0) {
				throw new JXPathInvalidAccessException("Index is less than 1: "
						+ asPath());
			}

			if (this.index >= getLength()) {
				collection = ValueUtils.expandCollection(collection, this.index + 1);
				this.handler.setProperty(getBean(), getPropertyName().getLocalName(), collection);
			}
		}

		return this;
	}

	public NodePointer createPath(final JXPathContext context, final Object value) {
		if (this.index == NodePointer.WHOLE_COLLECTION) {
			this.handler.setProperty(getBean(), getPropertyName().getLocalName(), value);
		} else {
			createPath(context);
			ValueUtils.setValue(getBaseValue(), this.index, value);
		}
		return this;
	}

	public void remove() {
		if (this.index == NodePointer.WHOLE_COLLECTION) {
			removeKey();
		} else if (isCollection()) {
			final Object collection = ValueUtils.remove(getBaseValue(), this.index);
			this.handler.setProperty(getBean(), getPropertyName().getLocalName(), collection);
		} else if (this.index == 0) {
			removeKey();
		}
	}

	/**
	 * Remove the current property.
	 */
	private void removeKey() {
		final Object bean = getBean();
		if (bean instanceof Map) {
			((Map) bean).remove(getPropertyName().getLocalName());
		} else {
			this.handler.setProperty(bean, getPropertyName().getLocalName(), null);
		}
	}

	public String asPath() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(getImmediateParentPointer().asPath());
		if (buffer.length() == 0) {
			buffer.append("/.");
		} else if (buffer.charAt(buffer.length() - 1) == '/') {
			buffer.append('.');
		}
		buffer.append("[@name_='");
		buffer.append(escape(getPropertyName().getLocalName()));
		buffer.append("']");
		if (this.index != NodePointer.WHOLE_COLLECTION && isCollection()) {
			buffer.append('[').append(this.index + 1).append(']');
		}
		return buffer.toString();
	}

}
