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
package org.apache.commons.jxpath;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.ri.QName;

/**
 * An implementation of JXPathBeanInfo based on JavaBeans' BeanInfo. Properties
 * advertised by JXPathBasicBeanInfo are the same as those advertised by
 * BeanInfo for the corresponding class.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 * @see java.beans.BeanInfo
 * @see java.beans.Introspector
 */
public class JXPathBasicBeanInfo implements JXPathBeanInfo {
	private static final long serialVersionUID = -3863803443111484155L;

	private static final Comparator<PropertyXMLMapping> PROPERTY_DESCRIPTOR_COMPARATOR = new Comparator<PropertyXMLMapping>() {
		@Override
		public int compare(final PropertyXMLMapping left, final PropertyXMLMapping right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private boolean atomic = false;
	private final Class clazz;
	private Class dynamicPropertyHandlerClass;
	private transient List<PropertyXMLMapping> propertyDescriptors;
	private transient Map<String, PropertyXMLMapping> propertyDescriptorMap;

	/**
	 * Create a new JXPathBasicBeanInfo.
	 *
	 * @param clazz bean class
	 */
	public JXPathBasicBeanInfo(final Class clazz) {
		this.clazz = clazz;
	}

	/**
	 * Create a new JXPathBasicBeanInfo.
	 *
	 * @param clazz  bean class
	 * @param atomic whether objects of this class are treated as atomic
	 *               objects which have no properties of their own.
	 */
	public JXPathBasicBeanInfo(final Class clazz, final boolean atomic) {
		this.clazz = clazz;
		this.atomic = atomic;
	}

	/**
	 * Create a new JXPathBasicBeanInfo.
	 *
	 * @param clazz                       bean class
	 * @param dynamicPropertyHandlerClass dynamic property handler class
	 */
	public JXPathBasicBeanInfo(final Class clazz, final Class dynamicPropertyHandlerClass) {
		this.clazz = clazz;
		this.atomic = false;
		this.dynamicPropertyHandlerClass = dynamicPropertyHandlerClass;
	}

	/**
	 * Returns true if objects of this class are treated as atomic
	 * objects which have no properties of their own.
	 *
	 * @return boolean
	 */
	public boolean isAtomic() {
		return this.atomic;
	}

	/**
	 * Return true if the corresponding objects have dynamic properties.
	 *
	 * @return boolean
	 */
	public boolean isDynamic() {
		return this.dynamicPropertyHandlerClass != null;
	}

	protected Comparator<PropertyXMLMapping> getPropertyOrderComparator() {
		return PROPERTY_DESCRIPTOR_COMPARATOR;
	}

	protected PropertyXMLMapping createMappingDescriptor(final PropertyDescriptor propertyDescriptor) {
		return new PropertyXMLMapping(propertyDescriptor, "", propertyDescriptor.getName(), false);
	}

	public synchronized List<PropertyXMLMapping> getPropertyDescriptors() {
		if (this.propertyDescriptors == null) {
			if (this.clazz == Object.class) {
				this.propertyDescriptors = Collections.emptyList();
			} else {
				try {
					BeanInfo bi = null;
					if (this.clazz.isInterface()) {
						bi = Introspector.getBeanInfo(this.clazz);
					} else {
						bi = Introspector.getBeanInfo(this.clazz, Object.class);
					}
					final List<PropertyXMLMapping> descriptors = new ArrayList<>(bi.getPropertyDescriptors().length);
					for(final PropertyDescriptor propertyDescriptor : bi.getPropertyDescriptors()) {
						descriptors.add(createMappingDescriptor(propertyDescriptor));
					}
					Collections.sort(this.propertyDescriptors, JXPathBasicBeanInfo.PROPERTY_DESCRIPTOR_COMPARATOR);
					this.propertyDescriptors = Collections.unmodifiableList(descriptors);
				} catch (final IntrospectionException ex) {
					ex.printStackTrace();
					return Collections.emptyList();
				}
			}
		}
		return this.propertyDescriptors;
	}

	public synchronized PropertyXMLMapping getPropertyDescriptor(final String propertyName) {
		if (this.propertyDescriptorMap == null) {
			this.propertyDescriptorMap = new HashMap<>();
			final List<PropertyXMLMapping> pds = getPropertyDescriptors();
			for (final PropertyXMLMapping pd : pds) {
				this.propertyDescriptorMap.put(pd.getName(), pd);
			}
		}
		return this.propertyDescriptorMap.get(propertyName);
	}



	@Override
	public PropertyXMLMapping getPropertyDescriptor(final QName xmlPropertyName, final boolean attribute) {
		return getPropertyDescriptor(xmlPropertyName.getName());
	}


	/**
	 * For a dynamic class, returns the corresponding DynamicPropertyHandler
	 * class.
	 *
	 * @return Class
	 */
	public Class getDynamicPropertyHandlerClass() {
		return this.dynamicPropertyHandlerClass;
	}


	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("BeanInfo [class = ");
		buffer.append(this.clazz.getName());
		if (isDynamic()) {
			buffer.append(", dynamic");
		}
		if (isAtomic()) {
			buffer.append(", atomic");
		}
		buffer.append(", properties = ");

		for (final PropertyXMLMapping jpd : getPropertyDescriptors()) {
			buffer.append("\n    ");
			buffer.append(jpd.getPropertyDescriptor().getPropertyType());
			buffer.append(": ");
			buffer.append(jpd.getName());
		}
		buffer.append("]");
		return buffer.toString();
	}
}
