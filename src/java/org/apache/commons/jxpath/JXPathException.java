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

/**
 * Thrown in various situations by JXPath; may contain a nested exception.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */

public class JXPathException extends RuntimeException {
	private static final long serialVersionUID = 4306409701468017766L;

	public JXPathException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JXPathException() {
	}

	public JXPathException(final String message) {
		super(message);
	}

	public JXPathException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JXPathException(final Throwable t) {
		super(t.getMessage(), t);
		this.setStackTrace(t.getStackTrace());
	}


	public String toString() {
		final StringBuilder temp = new StringBuilder("JXPathException: ");
		if(this.getCause() != null) {
			temp.append(this.getCause().getClass().getName());
		}
		final String message = this.getLocalizedMessage();
		if (message != null) {
			temp.append(": ").append(message);
		}

		return temp.toString();
	}


}
