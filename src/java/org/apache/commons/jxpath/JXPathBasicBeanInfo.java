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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jxpath.util.JXPathPropertyDescriptor;
import org.apache.commons.jxpath.util.PropertyIdentifier;

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

	private static final Comparator<JXPathPropertyDescriptor> PROPERTY_DESCRIPTOR_COMPARATOR = new Comparator<JXPathPropertyDescriptor>() {
		@Override
		public int compare(final JXPathPropertyDescriptor left, final JXPathPropertyDescriptor right) {
			return left.getPropertyDescriptor().getName().compareTo(right.getPropertyDescriptor().getName());
		}
	};

	private boolean atomic = false;
	protected final Class<?> clazz;
	private Class<?> dynamicPropertyHandlerClass = null;
	private transient List<JXPathPropertyDescriptor> propertyDescriptors = null;
	private transient Map<String, JXPathPropertyDescriptor> descriptorsByPropertyName = null;
	private transient Map<String, JXPathPropertyDescriptor> descriptorsByXmlName = null;

	private void initCollections() {
		if (this.propertyDescriptors == null) {
					if (this.clazz == Object.class) {
						this.propertyDescriptors = Collections.emptyList();
					} else {
						try {
							final BeanInfo bi;
							if (this.clazz.isInterface()) {
								bi = Introspector.getBeanInfo(this.clazz);
							} else {
								bi = Introspector.getBeanInfo(this.clazz, Object.class);
							}
							final List<JXPathPropertyDescriptor> descriptors = new ArrayList<>(bi.getPropertyDescriptors().length);
							for(final PropertyDescriptor propertyDescriptor : bi.getPropertyDescriptors()) {
								descriptors.add(createMappingDescriptor(propertyDescriptor));
							}
							Collections.sort(descriptors, getPropertyOrderComparator());
							final Map<String,JXPathPropertyDescriptor> descriptorsByPropertyName = new LinkedHashMap<>(descriptors.size());
							final Map<String,JXPathPropertyDescriptor> descriptorsByXmlName = new LinkedHashMap<>(descriptors.size());
							for(final JXPathPropertyDescriptor propertyDescriptor:descriptors) {
								descriptorsByXmlName.put(createKey(propertyDescriptor.getId()), propertyDescriptor);
								descriptorsByPropertyName.put(propertyDescriptor.getPropertyDescriptor().getName(), propertyDescriptor);
							}
							this.propertyDescriptors = Collections.unmodifiableList(descriptors);
							this.descriptorsByPropertyName = Collections.unmodifiableMap(descriptorsByPropertyName);
							this.descriptorsByXmlName = Collections.unmodifiableMap(descriptorsByXmlName);
						} catch (final IntrospectionException ex) {
							throw new JXPathException(ex);
						}
					}
				}
	}
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

	protected Comparator<JXPathPropertyDescriptor> getPropertyOrderComparator() {
		return JXPathBasicBeanInfo.PROPERTY_DESCRIPTOR_COMPARATOR;
	}

	protected JXPathPropertyDescriptor createMappingDescriptor(final PropertyDescriptor propertyDescriptor) {
		return new JXPathPropertyDescriptor(new PropertyIdentifier(null, propertyDescriptor.getName(), false), propertyDescriptor);
	}


	public synchronized List<JXPathPropertyDescriptor> getPropertyDescriptors() {
		initCollections();
		return this.propertyDescriptors;
	}

	@Override
	public JXPathPropertyDescriptor getPropertyDescriptor(final String propertyName) {
		initCollections();
		return this.descriptorsByPropertyName.get(propertyName);
	}

	@Override
	public JXPathPropertyDescriptor getPropertyDescriptor(final PropertyIdentifier propertyIdentifier) {
		initCollections();
		return this.descriptorsByXmlName.get(createKey(propertyIdentifier));
	}

	protected String createKey(final PropertyIdentifier propertyIdentifier) {
		return propertyIdentifier.getLocalName();
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

	@Override
	public String getTargetNamespace() {
		return null;
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

		for (final JXPathPropertyDescriptor jpd : getPropertyDescriptors()) {
			buffer.append("\n    ");
			buffer.append(jpd.getPropertyDescriptor().getPropertyType());
			buffer.append(": ");
			buffer.append(jpd.getPropertyDescriptor().getName());
		}
		buffer.append("]");
		return buffer.toString();
	}

}
