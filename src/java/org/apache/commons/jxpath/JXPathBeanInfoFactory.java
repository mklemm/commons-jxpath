package org.apache.commons.jxpath;

import javax.xml.namespace.NamespaceContext;

/**
 * Bean Info factory contract
 */
public interface JXPathBeanInfoFactory {
	JXPathBeanInfo createBeanInfo(final NamespaceContext namespaceContext, final Class clazz);
}
