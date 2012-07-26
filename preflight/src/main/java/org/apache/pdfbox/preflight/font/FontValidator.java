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

package org.apache.pdfbox.preflight.font;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.font.descriptor.FontDescriptorHelper;

public abstract class FontValidator <T extends FontContainer> {
	
	protected T fontContainer;
	protected PreflightContext context;
	protected PDFont font;
	protected FontDescriptorHelper<T> descriptorHelper;

	private static final String SUB_SET_PATTERN = "^[A-Z]{6}\\+.*";

	public FontValidator(PreflightContext context, PDFont font, T fContainer) {
		super();
		this.context = context;
		this.font = font;
		this.fontContainer = fContainer;
		this.context.addFontContainer(font.getCOSObject(), fContainer);
	}

  public static boolean isSubSet(String fontName) {
    return fontName.matches(SUB_SET_PATTERN);
  }

  public static String getSubSetPatternDelimiter() {
    return "\\+";
  }
 	
	public abstract void validate() throws ValidationException;
	
	protected void checkEncoding() {
		// nothing to check for PDF/A-1b
	}

	protected void checkToUnicode() {
		// nothing to check for PDF/A-1b
	}

	public T getFontContainer() {
		return fontContainer;
	}
	
}
