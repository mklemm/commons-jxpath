package org.apache.commons.jxpath;

import java.beans.PropertyDescriptor;

import org.apache.commons.jxpath.ri.QName;

/**
 * Created by klemm0 on 2015-02-03.
 */
public class PropertyXMLMapping {
	private final PropertyDescriptor propertyDescriptor;
	private final String localName;
	private final boolean attribute;
	private final String namespaceUri;
	private final String preferredNamespacePrefix = "tns";

	public PropertyXMLMapping(final PropertyDescriptor propertyDescriptor, final String namespaceUri, final String localName, final boolean attribute) {
		this.propertyDescriptor = propertyDescriptor;
		this.localName = localName;
		this.attribute = attribute;
		this.namespaceUri = namespaceUri;
	}

	public String getModelPropertyName() {
		return this.propertyDescriptor.getName();
	}

	public String getName() {
		return this.localName;
	}

	public boolean isAttribute() {
		return this.attribute;
	}

	public String getNamespaceUri() {
		return this.namespaceUri;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof PropertyXMLMapping)) return false;

		final PropertyXMLMapping that = (PropertyXMLMapping) o;

		if (this.attribute != that.attribute) return false;
		if (!this.namespaceUri.equals(that.namespaceUri)) return false;
		if (!this.localName.equals(that.localName)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = this.localName.hashCode();
		result = 31 * result + (this.attribute ? 1 : 0);
		result = 31 * result + this.getNamespaceUri().hashCode();
		return result;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return this.propertyDescriptor;
	}

	public QName getQName() {
		return new QName(this.namespaceContext.getPrefix);
	}
}
