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

package org.apache.pdfbox.preflight.font.descriptor;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_CID_DAMAGED;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_KEY_CIDSET;

import java.io.ByteArrayInputStream;

import org.apache.fontbox.ttf.CIDFontType2Parser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptorDictionary;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.FontValidator;
import org.apache.pdfbox.preflight.font.container.CIDType2Container;
import org.apache.pdfbox.preflight.utils.COSUtils;

public class CIDType2DescriptorHelper extends FontDescriptorHelper<CIDType2Container>
{

    public CIDType2DescriptorHelper(PreflightContext context, PDFont font, CIDType2Container fontContainer)
    {
        super(context, font, fontContainer);
    }

    /**
     * If the embedded font is a subset, the CIDSet entry is mandatory and must be a Stream. If the CIDSet entry doesn't
     * respects conditions, the FontContainer is updated.
     * 
     * @param pfDescriptor
     */
    protected void checkCIDSet(PDFontDescriptorDictionary pfDescriptor)
    {
        if (FontValidator.isSubSet(pfDescriptor.getFontName()))
        {
            COSDocument cosDocument = context.getDocument().getDocument();
            COSBase cidset = pfDescriptor.getCOSDictionary().getItem(COSName.getPDFName(FONT_DICTIONARY_KEY_CIDSET));
            if (cidset == null || !COSUtils.isStream(cidset, cosDocument))
            {
                this.fContainer.push(new ValidationResult.ValidationError(ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET,
                        "The CIDSet entry is missing for the Composite Subset"));
            }
        }
    }

    @Override
    public PDStream extractFontFile(PDFontDescriptorDictionary fontDescriptor)
    {
        PDStream ff2 = fontDescriptor.getFontFile2();
        if (ff2 != null)
        {
            /*
             * Stream validation should be done by the StreamValidateHelper. Process font specific check
             */
            COSStream stream = ff2.getStream();
            if (stream == null)
            {
                this.fContainer.push(new ValidationError(ERROR_FONTS_FONT_FILEX_INVALID, "The FontFile is missing for "
                        + fontDescriptor.getFontName()));
                this.fContainer.notEmbedded();
            }
        }
        checkCIDSet(fontDescriptor);
        return ff2;
    }

    @Override
    protected void processFontFile(PDFontDescriptorDictionary fontDescriptor, PDStream fontFile)
    {
        /*
         * try to load the font using the java.awt.font object. if the font is invalid, an exception will be thrown
         */
        TrueTypeFont ttf = null;
        try
        {
            /*
             * According to PDF Reference, CIDFontType2 is a TrueType font. Remark : Java.awt.Font throws exception when
             * a CIDFontType2 is parsed even if it is valid.
             */
            ttf = new CIDFontType2Parser(true).parseTTF(new ByteArrayInputStream(fontFile.getByteArray()));
            this.fContainer.setTtf(ttf);
        }
        catch (Exception e)
        {
            /*
             * Exceptionally, Exception is catched Here because of damaged font can throw NullPointer Exception...
             */
            this.fContainer.push(new ValidationError(ERROR_FONTS_CID_DAMAGED, "The FontFile can't be read"));
        }
    }

}
