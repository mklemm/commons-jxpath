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

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.PropertyIdentifier;

/**
 * Iterates property values of an object pointed at with a {@link PropertyOwnerPointer}.
 * Examples of such objects are JavaBeans and objects with Dynamic Properties.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public class PropertyIterator implements NodeIterator {
	private final boolean reverse;
	private final PropertyIdentifier name;
	private final PropertyPointer propertyNodePointer;
	private boolean empty = false;
	private int startIndex = 0;
	private boolean targetReady = false;
	private int position = 0;
	private int startPropertyIndex = 0;

	private boolean includeStart = false;

	/**
	 * Create a new PropertyIterator.
	 *
	 * @param pointer   owning pointer
	 * @param name      property name
	 * @param reverse   iteration order
	 * @param startWith beginning pointer
	 */
	public PropertyIterator(
			final PropertyOwnerPointer pointer,
			final PropertyIdentifier name,
			final boolean reverse,
			NodePointer startWith) {
		this.propertyNodePointer =
				(PropertyPointer) pointer.getPropertyPointer().clone();
		this.propertyNodePointer.setAttribute(name != null && name.isAttribute());
		this.propertyNodePointer.setPropertyName(name);
		this.name = name;
		this.reverse = reverse;
		this.includeStart = true;
		if (reverse) {
			this.startPropertyIndex = PropertyPointer.UNSPECIFIED_PROPERTY;
			this.startIndex = -1;
		}
		if (startWith != null) {
			while (startWith != null
					&& startWith.getImmediateParentPointer() != pointer) {
				startWith = startWith.getImmediateParentPointer();
			}
			if (startWith == null) {
				throw new JXPathException(
						"PropertyIterator startWith parameter is "
								+ "not a child of the supplied parent");
			}
			this.startPropertyIndex =
					((PropertyPointer) startWith).getPropertyIndex();
			this.startIndex = startWith.getIndex();
			if (this.startIndex == NodePointer.WHOLE_COLLECTION) {
				this.startIndex = 0;
			}
			this.includeStart = false;
			if (reverse && this.startIndex == -1) {
				this.includeStart = true;
			}
		}
	}

	/**
	 * Get the property pointer.
	 *
	 * @return NodePointer
	 */
	protected NodePointer getPropertyPointer() {
		return this.propertyNodePointer;
	}

	/**
	 * Reset property iteration.
	 */
	public void reset() {
		this.position = 0;
		this.targetReady = false;
	}

	public NodePointer getNodePointer() {
		if (this.position == 0) {
			if (this.name != null) {
				if (!this.targetReady) {
					prepareForIndividualProperty(this.name);
				}
				// If there is no such property - return null
				if (this.empty) {
					return null;
				}
			} else {
				if (!setPosition(1)) {
					return null;
				}
				reset();
			}
		}
		try {
			return this.propertyNodePointer.getValuePointer();
		} catch (final Throwable t) {
			this.propertyNodePointer.handle(t);
			final NullPropertyPointer npp =
					new NullPropertyPointer(
							this.propertyNodePointer.getImmediateParentPointer());
			npp.setPropertyName(this.propertyNodePointer.getPropertyName());
			npp.setIndex(this.propertyNodePointer.getIndex());
			return npp.getValuePointer();
		}
	}

	public int getPosition() {
		return this.position;
	}

	public boolean setPosition(final int position) {
		return this.name == null ? setPositionAllProperties(position) : setPositionIndividualProperty(position);
	}

	/**
	 * Set position for an individual property.
	 *
	 * @param position int position
	 * @return whether this was a valid position
	 */
	private boolean setPositionIndividualProperty(final int position) {
		this.position = position;
		if (position < 1) {
			return false;
		}

		if (!this.targetReady) {
			prepareForIndividualProperty(this.name);
		}

		if (this.empty) {
			return false;
		}

		final int length = getLength();
		int index;
		if (!this.reverse) {
			index = position + this.startIndex;
			if (!this.includeStart) {
				index++;
			}
			if (index > length) {
				return false;
			}
		} else {
			int end = this.startIndex;
			if (end == -1) {
				end = length - 1;
			}
			index = end - position + 2;
			if (!this.includeStart) {
				index--;
			}
			if (index < 1) {
				return false;
			}
		}
		this.propertyNodePointer.setIndex(index - 1);
		return true;
	}

	/**
	 * Set position for all properties
	 *
	 * @param position int position
	 * @return whether this was a valid position
	 */
	private boolean setPositionAllProperties(final int position) {
		this.position = position;
		if (position < 1) {
			return false;
		}

		int offset;
		final int count = this.propertyNodePointer.getPropertyCount();
		if (!this.reverse) {
			int index = 1;
			for (int i = this.startPropertyIndex; i < count; i++) {
				this.propertyNodePointer.setPropertyIndex(i);
				int length = getLength();
				if (i == this.startPropertyIndex) {
					length -= this.startIndex;
					if (!this.includeStart) {
						length--;
					}
					offset = this.startIndex + position - index;
					if (!this.includeStart) {
						offset++;
					}
				} else {
					offset = position - index;
				}
				if (index <= position && position < index + length) {
					this.propertyNodePointer.setIndex(offset);
					return true;
				}
				index += length;
			}
		} else {
			int index = 1;
			int start = this.startPropertyIndex;
			if (start == PropertyPointer.UNSPECIFIED_PROPERTY) {
				start = count - 1;
			}
			for (int i = start; i >= 0; i--) {
				this.propertyNodePointer.setPropertyIndex(i);
				int length = getLength();
				if (i == this.startPropertyIndex) {
					int end = this.startIndex;
					if (end == -1) {
						end = length - 1;
					}
					length = end + 1;
					offset = end - position + 1;
					if (!this.includeStart) {
						offset--;
						length--;
					}
				} else {
					offset = length - (position - index) - 1;
				}

				if (index <= position && position < index + length) {
					this.propertyNodePointer.setIndex(offset);
					return true;
				}
				index += length;
			}
		}
		return false;
	}

	/**
	 * Prepare for an individual property.
	 *
	 * @param name property name
	 */
	protected void prepareForIndividualProperty(final PropertyIdentifier name) {
		this.targetReady = true;
		this.empty = true;

		final PropertyIdentifier[] names = this.propertyNodePointer.getPropertyNames();
		if (!this.reverse) {
			if (this.startPropertyIndex == PropertyPointer.UNSPECIFIED_PROPERTY) {
				this.startPropertyIndex = 0;
			}
			if (this.startIndex == NodePointer.WHOLE_COLLECTION) {
				this.startIndex = 0;
			}
			for (int i = this.startPropertyIndex; i < names.length; i++) {
				if (names[i].equals(name)) {
					this.propertyNodePointer.setPropertyIndex(i);
					if (i != this.startPropertyIndex) {
						this.startIndex = 0;
						this.includeStart = true;
					}
					this.empty = false;
					break;
				}
			}
		} else {
			if (this.startPropertyIndex == PropertyPointer.UNSPECIFIED_PROPERTY) {
				this.startPropertyIndex = names.length - 1;
			}
			if (this.startIndex == NodePointer.WHOLE_COLLECTION) {
				this.startIndex = -1;
			}
			for (int i = this.startPropertyIndex; i >= 0; i--) {
				if (names[i].equals(name)) {
					this.propertyNodePointer.setPropertyIndex(i);
					if (i != this.startPropertyIndex) {
						this.startIndex = -1;
						this.includeStart = true;
					}
					this.empty = false;
					break;
				}
			}
		}
	}

	/**
	 * Computes length for the current pointer - ignores any exceptions.
	 *
	 * @return length
	 */
	private int getLength() {
		int length;
		try {
			length = this.propertyNodePointer.getLength(); // TBD: cache length
		} catch (final Throwable t) {
			this.propertyNodePointer.handle(t);
			length = 0;
		}
		return length;
	}
}
