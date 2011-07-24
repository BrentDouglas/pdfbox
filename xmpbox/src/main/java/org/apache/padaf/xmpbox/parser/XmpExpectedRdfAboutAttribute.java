/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.padaf.xmpbox.parser;

/**
 * This exception is thrown when a rdf:Description not contains attribute
 * rdf:about
 * 
 * @author a183132
 * 
 */
public class XmpExpectedRdfAboutAttribute extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 737516114298078255L;

	/**
	 * Build This exception with specified error message
	 * 
	 * @param message
	 *            a description of the encountered problem
	 */
	public XmpExpectedRdfAboutAttribute(String message) {
		super(message);

	}

	/**
	 * Build This exception with specified error message and the original cause
	 * 
	 * @param message
	 *            a description of the encountered problem
	 * @param cause
	 *            Original Cause of this exception
	 */
	public XmpExpectedRdfAboutAttribute(String message, Throwable cause) {
		super(message, cause);

	}

}
