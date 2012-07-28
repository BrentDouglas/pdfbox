 /*****************************************************************************
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

package org.apache.padaf.xmpbox.schema;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DublinCoreTest extends AbstractSchemaTester {

	protected DublinCoreSchema schema = null;
	
	public DublinCoreSchema getSchema () {
		return schema;
	}
	
	@Before
	public void before() throws Exception {
		super.before();
		schema = xmp.createAndAddDublinCoreSchema();
	}

	public DublinCoreTest(String fieldName, String type, Cardinality card) {
		super(fieldName, type, card);
	}

	@Parameters
	public static Collection<Object[]> initializeParameters() throws Exception {
    	Collection<Object[]> result = new ArrayList<Object[]>();

    	result.add(new Object [] {"contributor","ProperName",Cardinality.Bag});
    	result.add(new Object [] {"coverage","Text",Cardinality.Simple});
    	result.add(new Object [] {"creator","ProperName",Cardinality.Seq});
    	result.add(new Object [] {"date","Date",Cardinality.Seq});
    	// description TODO TEST lang alt
    	result.add(new Object [] {"format","MIMEType",Cardinality.Simple});
    	result.add(new Object [] {"identifier","Text",Cardinality.Simple});
    	result.add(new Object [] {"language","Locale",Cardinality.Bag});
    	result.add(new Object [] {"publisher","ProperName",Cardinality.Bag});
    	result.add(new Object [] {"relation","Text",Cardinality.Bag});
    	// rights TODO TEST lang alt
    	result.add(new Object [] {"source","Text",Cardinality.Simple});
    	result.add(new Object [] {"subject","Text",Cardinality.Bag});
    	// title TODO TEST lang alt
    	result.add(new Object [] {"type","Text",Cardinality.Bag});
    	
    	
//		List<Object[]> data = new ArrayList<Object[]>();
//		data.add(wrapProperty("contributor", "bag Text", new String[] {
//				"contri 1", "contri 2" }));
//		data.add(wrapProperty("coverage", "Text", "scope of the resource"));
//		data.add(wrapProperty("creator", "seq Text", new String[] {
//				"creator 1", "creator 2", "creator 3" }));
//		data.add(wrapProperty("date", "seq Date", new Calendar[] { Calendar
//				.getInstance() }));
//
//		Map<String, String> desc = new HashMap<String, String>(2);
//		desc.put("fr", "en français");
//		desc.put("en", "in english");
//		data.add(wrapProperty("description", "Lang Alt", desc));
//
//		data.add(wrapProperty("format", "Text", "text/html"));
//		data.add(wrapProperty("identifier", "Text", "my id"));
//		data.add(wrapProperty("language", "bag Text", new String[] { "fr",
//				"en", "es" }));
//		data.add(wrapProperty("publisher", "bag Text", new String[] { "pub1",
//				"pub2" }));
//		data.add(wrapProperty("relation", "bag Text", new String[] { "rel1",
//				"relation 2" }));
//
//		Map<String, String> rights = new HashMap<String, String>(2);
//		rights.put("fr", "protégé");
//		rights.put("en", "protected");
//		data.add(wrapProperty("rights", "Lang Alt", rights));
//
//		data.add(wrapProperty("source", "Text", "my source"));
//		data.add(wrapProperty("subject", "bag Text", new String[] { "subj1",
//				"subj2" }));
//
//		Map<String, String> title = new HashMap<String, String>(2);
//		title.put("fr", "essai");
//		title.put("en", "test");
//		title.put("es", "prueba");
//		data.add(wrapProperty("title", "Lang Alt", title));
//
//		data.add(wrapProperty("type", "bag Text", new String[] { "text",
//				"test", "dummy" }));
//
//		return data;
    	
    	return result;
	}
//
//	public DublinCoreTest(String property, String type, Object value) {
//		super(property, type, value);
//	}

}
