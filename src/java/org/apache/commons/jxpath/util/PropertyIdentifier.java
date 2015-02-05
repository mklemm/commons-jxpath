/*
 * MIT License
 *
 * Copyright (c) 2014 Klemm Software Consulting, Mirko Klemm
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.apache.commons.jxpath.util;

import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;

/**
* @author Mirko Klemm 2015-02-04
*/
public class PropertyIdentifier {
	public static final PropertyIdentifier WILDCARD = createUnqualified("*");
	private final String namespaceUri;
	private final String localName;
	private final String prefix;
	private final boolean attribute;

	public PropertyIdentifier(final String namespaceUri, final String localName, final boolean attribute) {
		this(namespaceUri, null, localName, attribute);
	}

	protected PropertyIdentifier(final String namespaceUri, final String prefix, final String localName, final boolean attribute) {
		this.namespaceUri = namespaceUri;
		this.prefix = prefix;
		this.localName = localName;
		this.attribute = attribute;
	}

	public String getNamespaceUri() {
		return this.namespaceUri;
	}

	public String getLocalName() {
		return this.localName;
	}

	public boolean isAttribute() {
		return this.attribute;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public QName toQName(final NamespaceResolver namespaceResolver) {
		if(namespaceResolver == null) {
			return new QName(this.localName);
		} else {
			final String prefix = namespaceResolver.getPrefix(this.namespaceUri);
			return new QName(prefix, this.localName);
		}
	}

	public static PropertyIdentifier create(final String namespaceUri, final String prefix, final String localName, final boolean attribute) {
		return new PropertyIdentifier(namespaceUri, prefix, localName, attribute);
	}

	public static PropertyIdentifier create(final String namespaceUri, final String localName, final boolean attribute) {
		return new PropertyIdentifier(namespaceUri, localName, attribute);
	}

	public static PropertyIdentifier fromQName(final NamespaceResolver namespaceResolver, final QName qName, final boolean attribute) {
		if(namespaceResolver == null) {
			return new PropertyIdentifier(null, qName.getPrefix(), qName.getName(), attribute);
		} else {
			final String namespaceUri = qName.getPrefix() == null ? null : namespaceResolver.getNamespaceURI(qName.getPrefix());
			return new PropertyIdentifier(namespaceUri, qName.getPrefix(), qName.getName(), attribute);
		}
	}

	public static PropertyIdentifier fromQName(final String namespaceUri, final QName qName, final boolean attribute) {
		return new PropertyIdentifier(namespaceUri, qName.getPrefix(), qName.getName(), attribute);
	}

	public static PropertyIdentifier createUnqualified(final String name) {
		return new PropertyIdentifier(null, name, false);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof PropertyIdentifier)) return false;

		final PropertyIdentifier that = (PropertyIdentifier) o;

		if (this.attribute != that.attribute) return false;
		if (!this.localName.equals(that.localName)) return false;
		if (this.namespaceUri != null ? !this.namespaceUri.equals(that.namespaceUri) : that.namespaceUri != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = this.namespaceUri != null ? this.namespaceUri.hashCode() : 0;
		result = 31 * result + this.localName.hashCode();
		result = 31 * result + (this.attribute ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return (this.namespaceUri == null ? "" : "{" + this.namespaceUri + "}") + (this.attribute ? "@" : "") + this.localName;
	}
}
