package org.apache.commons.jxpath;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Created by klemm0 on 2015-01-21.
 */
public class JXPathPropertyDescriptor extends PropertyDescriptor {
	private final String xmlName;

	public JXPathPropertyDescriptor(final String propertyName, final String xmlName, final Class<?> beanClass) throws IntrospectionException {
		super(propertyName, beanClass);
		this.xmlName = xmlName;
	}

	public JXPathPropertyDescriptor(final String propertyName, final String xmlName, final Class<?> beanClass, final String readMethodName, final String writeMethodName) throws IntrospectionException {
		super(propertyName, beanClass, readMethodName, writeMethodName);
		this.xmlName = xmlName;
	}

	public JXPathPropertyDescriptor(final String propertyName, final String xmlName, final Method readMethod, final Method writeMethod) throws IntrospectionException {
		super(propertyName, readMethod, writeMethod);
		this.xmlName = xmlName;
	}

	@Override
	public String getName() {
		return this.xmlName;
	}
}
