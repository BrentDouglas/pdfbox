/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.fontbox.cff;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CFFFontROS extends CFFFont {
	private String registry;
	private String ordering;
	private int supplement;

	private List<Map<String, Object>> fontDictionaries = new LinkedList<Map<String,Object>>();
	private List<Map<String, Object>> privateDictionaries = new LinkedList<Map<String,Object>>();
	private CIDKeyedFDSelect fdSelect = null;

	/**
	 * Returns the registry value.
	 * @return the registry
	 */
	public String getRegistry() {
		return registry;
	}

	/**
	 * Sets the registry value.
	 * 
	 * @param registry the registry to set
	 */
	public void setRegistry(String registry) {
		this.registry = registry;
	}

	/**
	 * Returns the ordering value.
	 * 
	 * @return the ordering
	 */
	public String getOrdering() {
		return ordering;
	}

	/**
	 * Sets the ordering value.
	 * 
	 * @param ordering the ordering to set
	 */
	public void setOrdering(String ordering) {
		this.ordering = ordering;
	}

	/**
	 * Returns the supplement value.
	 * 
	 * @return the supplement
	 */
	public int getSupplement() {
		return supplement;
	}

	/**
	 * Sets the supplement value.
	 * 
	 * @param supplement the supplement to set
	 */
	public void setSupplement(int supplement) {
		this.supplement = supplement;
	}

	/**
	 * Returns the font dictionaries.
	 * 
	 * @return the fontDict
	 */
	public List<Map<String, Object>> getFontDict() {
		return fontDictionaries;
	}

	/**
	 * Sets the font dictionaries.
	 * 
	 * @param fontDict the fontDict to set
	 */
	public void setFontDict(List<Map<String, Object>> fontDict) {
		this.fontDictionaries = fontDict;
	}

	/**
	 * Returns the private dictionary.
	 * 
	 * @return the privDict
	 */
	public List<Map<String, Object>> getPrivDict() {
		return privateDictionaries;
	}

	/**
	 * Sets the private dictionary.
	 * 
	 * @param privDict the privDict to set
	 */
	public void setPrivDict(List<Map<String, Object>> privDict) {
		this.privateDictionaries = privDict;
	}

	/**
	 * Returns the fdSelect value.
	 * 
	 * @return the fdSelect
	 */
	public CIDKeyedFDSelect getFdSelect() {
		return fdSelect;
	}

	/**
	 * Sets the fdSelect value.
	 * 
	 * @param fdSelect the fdSelect to set
	 */
	public void setFdSelect(CIDKeyedFDSelect fdSelect) {
		this.fdSelect = fdSelect;
	}

	/**
	 * Returns the Width value of the given Glyph identifier
	 * 
	 * @param CID
	 * @return -1 if the SID is missing from the Font.
	 * @throws IOException
	 */
	public int getWidth(int CID) throws IOException {
		// ---- search the right FDArray index in the FDSelect according to the Character identifier
		// 		this index will be used to access the private dictionary which contains useful values 
		//		to compute width.
		int fdArrayIndex = this.fdSelect.getFd(CID);
		if (fdArrayIndex == -1 && CID == 0 ) { // --- notdef char
			return super.getWidth(CID);
		} else if (fdArrayIndex == -1) {
			return 1000;
		}
		
		Map<String, Object> fontDict = this.fontDictionaries.get(fdArrayIndex);
		Map<String, Object> privDict = this.privateDictionaries.get(fdArrayIndex);

		int nominalWidth = privDict.containsKey("nominalWidthX") ? ((Number)privDict.get("nominalWidthX")).intValue() : 0;
		int defaultWidth = privDict.containsKey("defaultWidthX") ? ((Number)privDict.get("defaultWidthX")).intValue() : 1000 ;

		for (Mapping m : getMappings() ){
			if (m.getSID() == CID) {

				CharStringRenderer csr = null;
				Number charStringType = (Number)getProperty("CharstringType");
				if ( charStringType.intValue() == 2 ) {
					List<Object> lSeq = m.toType2Sequence();
					csr = new CharStringRenderer(false);
					csr.render(lSeq);
				} else {
					List<Object> lSeq = m.toType1Sequence();
					csr = new CharStringRenderer();
					csr.render(lSeq);
				}

				// ---- If the CharString has a Width nominalWidthX must be added, 
				//	    otherwise it is the default width.
				return csr.getWidth() != 0 ? csr.getWidth() + nominalWidth : defaultWidth;
			}
		}

		// ---- CID Width not found, return the notdef width
		return getNotDefWidth(defaultWidth, nominalWidth);
	}
}
