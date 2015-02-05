package org.apache.commons.jxpath.util;

import java.beans.PropertyDescriptor;

/**
 * Created by klemm0 on 2015-02-03.
 */
public class JXPathPropertyDescriptor {
	private final PropertyIdentifier id;
	private final PropertyDescriptor propertyDescriptor;

	public JXPathPropertyDescriptor(final PropertyIdentifier id, final PropertyDescriptor propertyDescriptor) {
		this.id = id;
		this.propertyDescriptor = propertyDescriptor;
	}

	public PropertyIdentifier getId() {
		return this.id;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return this.propertyDescriptor;
	}

	@Override
	public String toString() {
		return (id == null ? "null:PropertyIdentifier" : id.toString()) + " -> " + (propertyDescriptor == null ? "null:PropertyDescriptor" : propertyDescriptor.getName());
	}
}
