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
package org.apache.commons.jxpath.ri.model.dynabeans;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.jxpath.JXPathTypeConversionException;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;
import org.apache.commons.jxpath.util.PropertyIdentifier;
import org.apache.commons.jxpath.util.TypeUtils;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * Pointer pointing to a property of a {@link DynaBean}. If the target DynaBean is
 * Serializable, so should this instance be.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public class DynaBeanPropertyPointer extends PropertyPointer {
	private static final String CLASS = "class";
	private static final long serialVersionUID = 2094421509141267239L;
	private final DynaBean dynaBean;
	private PropertyIdentifier name = null;
	private PropertyIdentifier[] names = null;

	/**
	 * Create a new DynaBeanPropertyPointer.
	 *
	 * @param parent   pointer
	 * @param dynaBean pointed
	 */
	public DynaBeanPropertyPointer(final NodePointer parent, final DynaBean dynaBean) {
		super(parent);
		this.dynaBean = dynaBean;
	}

	public Object getBaseValue() {
		return this.dynaBean.get(getPropertyName().getLocalName());
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
		return getPropertyNames().length;
	}

	public PropertyIdentifier[] getPropertyNames() {
	    /* @todo do something about the sorting - LIKE WHAT? - MJB */
		if (this.names == null) {
			final DynaClass dynaClass = this.dynaBean.getDynaClass();
			final DynaProperty[] dynaProperties = dynaClass.getDynaProperties();
			final List<PropertyIdentifier> properties = new ArrayList<>(dynaProperties.length);
			for (int i = 0; i < dynaProperties.length; i++) {
				final PropertyIdentifier name = PropertyIdentifier.createUnqualified(dynaProperties[i].getName());
				if (!DynaBeanPropertyPointer.CLASS.equals(name.getLocalName())) {
					properties.add(name);
				}
			}
			this.names = properties.toArray(new PropertyIdentifier[properties.size()]);
		}
		return this.names;
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
			this.name = this.propertyIndex >= 0 && this.propertyIndex < names.length ? names[this.propertyIndex] : PropertyIdentifier.createUnqualified("*");
		}
		return this.name;
	}

	/**
	 * Select a property by name.
	 *
	 * @param propertyName to select
	 */
	public void setPropertyName(final PropertyIdentifier propertyName) {
		setPropertyIndex(PropertyPointer.UNSPECIFIED_PROPERTY);
		this.name = propertyName;
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
				if (names[i].equals(this.name)) {
					this.propertyIndex = i;
					this.name = null;
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
	 * If index == WHOLE_COLLECTION, the value of the property, otherwise
	 * the value of the index'th element of the collection represented by the
	 * property. If the property is not a collection, index should be zero
	 * and the value will be the property itself.
	 *
	 * @return Object
	 */
	public Object getImmediateNode() {
		final PropertyIdentifier name = getPropertyName();
		if (name.equals(PropertyIdentifier.WILDCARD)) {
			return null;
		}

		Object value;
		if (this.index == NodePointer.WHOLE_COLLECTION) {
			value = ValueUtils.getValue(this.dynaBean.get(name.getLocalName()));
		} else if (isIndexedProperty()) {
			// DynaClass at this point is not based on whether
			// the property is indeed indexed, but rather on
			// whether it is an array or List. Therefore
			// the indexed set may fail.
			try {
				value = ValueUtils.getValue(this.dynaBean.get(name.getLocalName(), this.index));
			} catch (final ArrayIndexOutOfBoundsException ex) {
				value = null;
			} catch (final IllegalArgumentException ex) {
				value = this.dynaBean.get(name.getLocalName());
				value = ValueUtils.getValue(value, this.index);
			}
		} else {
			value = this.dynaBean.get(name.getLocalName());
			if (ValueUtils.isCollection(value)) {
				value = ValueUtils.getValue(value, this.index);
			} else if (this.index != 0) {
				value = null;
			}
		}
		return value;
	}

	/**
	 * Returns true if the bean has the currently selected property.
	 *
	 * @return boolean
	 */
	protected boolean isActualProperty() {
		final DynaClass dynaClass = this.dynaBean.getDynaClass();
		return dynaClass.getDynaProperty(getPropertyName().getLocalName()) != null;
	}

	/**
	 * Learn whether the property referenced is an indexed property.
	 *
	 * @return boolean
	 */
	protected boolean isIndexedProperty() {
		final DynaClass dynaClass = this.dynaBean.getDynaClass();
		final DynaProperty property = dynaClass.getDynaProperty(this.name.getLocalName());
		return property.isIndexed();
	}

	/**
	 * If index == WHOLE_COLLECTION, change the value of the property, otherwise
	 * change the value of the index'th element of the collection
	 * represented by the property.
	 *
	 * @param value to set
	 */
	public void setValue(final Object value) {
		setValue(this.index, value);
	}

	public void remove() {
		if (this.index == NodePointer.WHOLE_COLLECTION) {
			this.dynaBean.set(getPropertyName().getLocalName(), null);
		} else if (isIndexedProperty()) {
			this.dynaBean.set(getPropertyName().getLocalName(), this.index, null);
		} else if (isCollection()) {
			final Object collection = ValueUtils.remove(getBaseValue(), this.index);
			this.dynaBean.set(getPropertyName().getLocalName(), collection);
		} else if (this.index == 0) {
			this.dynaBean.set(getPropertyName().getLocalName(), null);
		}
	}

	/**
	 * Set an indexed value.
	 *
	 * @param index to change
	 * @param value to set
	 */
	private void setValue(final int index, final Object value) {
		if (index == NodePointer.WHOLE_COLLECTION) {
			this.dynaBean.set(getPropertyName().getLocalName(), convert(value, false));
		} else if (isIndexedProperty()) {
			this.dynaBean.set(getPropertyName().getLocalName(), index, convert(value, true));
		} else {
			final Object baseValue = this.dynaBean.get(getPropertyName().getLocalName());
			ValueUtils.setValue(baseValue, index, value);
		}
	}


	/**
	 * Convert a value to the appropriate property type.
	 *
	 * @param value   to convert
	 * @param element whether this should be a collection element.
	 * @return conversion result
	 */
	private Object convert(final Object value, final boolean element) {
		final DynaClass dynaClass = this.dynaBean.getDynaClass();
		final DynaProperty property = dynaClass.getDynaProperty(getPropertyName().getLocalName());
		Class type = property.getType();
		if (element) {
			if (type.isArray()) {
				type = type.getComponentType();
			} else {
				return value; // No need to convert
			}
		}

		try {
			return TypeUtils.convert(value, type);
		} catch (final Exception ex) {
			final String string = value == null ? "null" : value.getClass().getName();
			throw new JXPathTypeConversionException(
					"Cannot convert value of class " + string + " to type "
							+ type, ex);
		}
	}
}
