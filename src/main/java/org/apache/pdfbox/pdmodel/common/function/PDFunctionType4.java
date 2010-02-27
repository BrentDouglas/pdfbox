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
package org.apache.pdfbox.pdmodel.common.function;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.cos.COSArray;
import java.io.IOException;

/**
 * This class represents a type 4 function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFunctionType4 extends PDStreamFunction
{

    /**
     * Constructor to create a new blank type 4 function.
     *
     * @param doc The document that the function will be part of.
     */
    protected PDFunctionType4( PDDocument doc )
    {
        super( doc, 4 );
    }

    /**
     * Constructor.
     *
     * @param functionDictionary The prepopulated function dictionary.
     */
    public PDFunctionType4( PDStream functionDictionary )
    {
        super( functionDictionary );
    }
    
    
    /**
    * {@inheritDoc}
    */
    public COSArray Eval(COSArray input) throws IOException
    {
        //Implementation here will require evaluation of PostScript functions.
        //See section 3.9.4 of the PDF Reference.
        throw new IOException("Not Implemented");
    }
}
