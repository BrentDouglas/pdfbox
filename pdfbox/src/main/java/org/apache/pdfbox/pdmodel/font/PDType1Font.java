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
package org.apache.pdfbox.pdmodel.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.afm.FontMetric;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.AFMEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.EncodingManager;
import org.apache.pdfbox.encoding.Type1Encoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This is implementation of the Type1 Font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.11 $
 */
public class PDType1Font extends PDSimpleFont
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDType1Font.class);

    private PDType1CFont type1CFont = null;
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_ROMAN = new PDType1Font( "Times-Roman" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_BOLD = new PDType1Font( "Times-Bold" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_ITALIC = new PDType1Font( "Times-Italic" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font TIMES_BOLD_ITALIC = new PDType1Font( "Times-BoldItalic" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA = new PDType1Font( "Helvetica" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_BOLD = new PDType1Font( "Helvetica-Bold" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_OBLIQUE = new PDType1Font( "Helvetica-Oblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font HELVETICA_BOLD_OBLIQUE = new PDType1Font( "Helvetica-BoldOblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER = new PDType1Font( "Courier" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_BOLD = new PDType1Font( "Courier-Bold" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_OBLIQUE = new PDType1Font( "Courier-Oblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font COURIER_BOLD_OBLIQUE = new PDType1Font( "Courier-BoldOblique" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font SYMBOL = new PDType1Font( "Symbol" );
    /**
     * Standard Base 14 Font.
     */
    public static final PDType1Font ZAPF_DINGBATS = new PDType1Font( "ZapfDingbats" );

    private static final Map<String, PDType1Font> STANDARD_14 = new HashMap<String, PDType1Font>();
    static
    {
        STANDARD_14.put( TIMES_ROMAN.getBaseFont(), TIMES_ROMAN );
        STANDARD_14.put( TIMES_BOLD.getBaseFont(), TIMES_BOLD );
        STANDARD_14.put( TIMES_ITALIC.getBaseFont(), TIMES_ITALIC );
        STANDARD_14.put( TIMES_BOLD_ITALIC.getBaseFont(), TIMES_BOLD_ITALIC );
        STANDARD_14.put( HELVETICA.getBaseFont(), HELVETICA );
        STANDARD_14.put( HELVETICA_BOLD.getBaseFont(), HELVETICA_BOLD );
        STANDARD_14.put( HELVETICA_OBLIQUE.getBaseFont(), HELVETICA_OBLIQUE );
        STANDARD_14.put( HELVETICA_BOLD_OBLIQUE.getBaseFont(), HELVETICA_BOLD_OBLIQUE );
        STANDARD_14.put( COURIER.getBaseFont(), COURIER );
        STANDARD_14.put( COURIER_BOLD.getBaseFont(), COURIER_BOLD );
        STANDARD_14.put( COURIER_OBLIQUE.getBaseFont(), COURIER_OBLIQUE );
        STANDARD_14.put( COURIER_BOLD_OBLIQUE.getBaseFont(), COURIER_BOLD_OBLIQUE );
        STANDARD_14.put( SYMBOL.getBaseFont(), SYMBOL );
        STANDARD_14.put( ZAPF_DINGBATS.getBaseFont(), ZAPF_DINGBATS );
    }

    private Font awtFont = null;

    /**
     * Constructor.
     */
    public PDType1Font()
    {
        super();
        font.setItem( COSName.SUBTYPE, COSName.TYPE1 );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType1Font( COSDictionary fontDictionary )
    {
        super( fontDictionary );
        PDFontDescriptor fd = getFontDescriptor();
        if (fd != null && fd instanceof PDFontDescriptorDictionary)
        {
            // a Type1 font may contain a Type1C font
            PDStream fontFile3 = ((PDFontDescriptorDictionary)fd).getFontFile3();
            if (fontFile3 != null)
            {
                try 
                {
                    type1CFont = new PDType1CFont( super.font );
                }
                catch (IOException exception) 
                {
                    log.info("Can't read the embedded type1C font " + fd.getFontName() );
                }
            }
        }
    }

    /**
     * Constructor.
     *
     * @param baseFont The base font for this font.
     */
    public PDType1Font( String baseFont )
    {
        this();
        setBaseFont( baseFont );
        setFontEncoding(new WinAnsiEncoding());
        setEncoding(COSName.WIN_ANSI_ENCODING);
    }

    /**
     * A convenience method to get one of the standard 14 font from name.
     *
     * @param name The name of the font to get.
     *
     * @return The font that matches the name or null if it does not exist.
     */
    public static PDType1Font getStandardFont( String name )
    {
        return (PDType1Font)STANDARD_14.get( name );
    }

    /**
     * This will get the names of the standard 14 fonts.
     *
     * @return An array of the names of the standard 14 fonts.
     */
    public static String[] getStandard14Names()
    {
        return (String[])STANDARD_14.keySet().toArray( new String[14] );
    }

    /**
     * {@inheritDoc}
     */
    public Font getawtFont() throws IOException
    {
        if( awtFont == null )
        {
            if (type1CFont != null)
            {
                awtFont = type1CFont.getawtFont();
            }
            else
            {
                String baseFont = getBaseFont();
                PDFontDescriptor fd = getFontDescriptor();
                if (fd != null && fd instanceof PDFontDescriptorDictionary)
                {
                    PDFontDescriptorDictionary fdDictionary = (PDFontDescriptorDictionary)fd;
                    if( fdDictionary.getFontFile() != null )
                    {
                        try 
                        {
                            // create a type1 font with the embedded data
                            awtFont = Font.createFont( Font.TYPE1_FONT, fdDictionary.getFontFile().createInputStream() );
                        } 
                        catch (FontFormatException e) 
                        {
                            log.info("Can't read the embedded type1 font " + fd.getFontName() );
                        }
                    }
                    if (awtFont == null)
                    {
                        // check if the font is part of our environment
                        awtFont = FontManager.getAwtFont(fd.getFontName());
                        if (awtFont == null)
                        {
                            log.info("Can't find the specified font " + fd.getFontName() );
                        }
                    }
                }
                else
                {
                    // check if the font is part of our environment
                    awtFont = FontManager.getAwtFont(baseFont);
                    if (awtFont == null) 
                    {
                        log.info("Can't find the specified basefont " + baseFont );
                    }
                }
            }
            if (awtFont == null)
            {
                // we can't find anything, so we have to use the standard font
                awtFont = FontManager.getStandardFont();
                log.info("Using font "+awtFont.getName()+ " instead");
            }
        }
        return awtFont;
    }

    protected void determineEncoding()
    {
        super.determineEncoding();
        Encoding fontEncoding = getFontEncoding();
        if(fontEncoding == null)
        {
            FontMetric metric = getAFM();
            if (metric != null)
            {
                fontEncoding = new AFMEncoding( metric );
            }
            setFontEncoding(fontEncoding);
        }
        getEncodingFromFont(getFontEncoding() == null);
    }
    
    /**
     * Tries to get the encoding for the type1 font.
     *
     */
    private void getEncodingFromFont(boolean extractEncoding)
    {
        // This whole section of code needs to be replaced with an actual type1 font parser!!
        // Get the font program from the embedded type font.
        PDFontDescriptor fontDescriptor = getFontDescriptor();
        if( fontDescriptor != null && fontDescriptor instanceof PDFontDescriptorDictionary)
        {
            PDStream fontFile = ((PDFontDescriptorDictionary)fontDescriptor).getFontFile();
            if( fontFile != null )
            {
                BufferedReader in = null;
                try 
                {
                    in = new BufferedReader(new InputStreamReader(fontFile.createInputStream()));
                    
                    // this section parses the font program stream searching for a /Encoding entry
                    // if it contains an array of values a Type1Encoding will be returned
                    // if it encoding contains an encoding name the corresponding Encoding will be returned
                    String line = "";
                    Type1Encoding encoding = null;
                    while( (line = in.readLine()) != null)
                    {
                        if (extractEncoding) 
                        {
                            if (line.startsWith("currentdict end")) {
                                if (encoding != null)
                                    setFontEncoding(encoding);
                                break;
                            }
                            if (line.startsWith("/Encoding")) 
                            {
                                if(line.contains("array")) 
                                {
                                    StringTokenizer st = new StringTokenizer(line);
                                    // ignore the first token
                                    st.nextElement();
                                    int arraySize = Integer.parseInt(st.nextToken());
                                    encoding = new Type1Encoding(arraySize);
                                }
                                // if there is already an encoding, we don't need to
                                // assign another one
                                else if (getFontEncoding() == null)
                                {
                                    StringTokenizer st = new StringTokenizer(line);
                                    // ignore the first token
                                    st.nextElement();
                                    String type1Encoding = st.nextToken();
                                    setFontEncoding(
                                        EncodingManager.INSTANCE.getEncoding(
                                                COSName.getPDFName(type1Encoding)));
                                    break;
                                }
                            }
                            else if (line.startsWith("dup")) {
                                StringTokenizer st = new StringTokenizer(line.replaceAll("/"," /"));
                                // ignore the first token
                                st.nextElement();
                                int index = Integer.parseInt(st.nextToken());
                                String name = st.nextToken();
                                if(encoding == null)
                                    log.warn("Unable to get character encoding.  Encoding defintion found without /Encoding line.");
                                else
                                    encoding.addCharacterEncoding(index, name.replace("/", ""));
                            }
                        }
                        // according to the pdf reference, all font matrices should be same, except for type 3 fonts.
                        // but obviously there are some type1 fonts with different matrix values, see pdf sample
                        // attached to PDFBOX-935
                        if (line.startsWith("/FontMatrix"))
                        {
                            String matrixValues = line.substring(line.indexOf("[")+1,line.lastIndexOf("]"));
                            StringTokenizer st = new StringTokenizer(matrixValues);
                            COSArray array = new COSArray();
                            if (st.countTokens() >= 6)
                            {
                                try 
                                {
                                    for (int i=0;i<6;i++)
                                    {
                                        COSFloat floatValue = new COSFloat(Float.parseFloat(st.nextToken()));
                                        array.add(floatValue);
                                    }
                                    fontMatrix = new PDMatrix(array);
                                }
                                catch (NumberFormatException exception)
                                {
                                    log.error("Can't read the fontmatrix from embedded font file!");
                                }
                            }
                        }
                    }
                }
                catch(IOException exception) 
                {
                    log.error("Error: Could not extract the encoding from the embedded type1 font.");
                }
                finally
                {
                    if (in != null)
                    {
                        try
                        {
                            in.close();
                        }
                        catch(IOException exception) 
                        {
                            log.error("An error occurs while closing the stream used to read the embedded type1 font.");
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(byte[] c, int offset, int length) throws IOException
    {
        if (type1CFont != null && getFontEncoding() == null)
        {
            return type1CFont.encode(c, offset, length);
        }
        else
        {
            return super.encode(c, offset, length);
        }
    }
    
    public int encodeToCID( byte[] c, int offset, int length ) throws IOException {
      if (type1CFont != null && getFontEncoding() == null)
      {
          return type1CFont.encodeToCID(c, offset, length);
      }
      else
      {
          return super.encodeToCID(c, offset, length);
      }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public PDMatrix getFontMatrix()
    {
        if (type1CFont != null)
        {
            return type1CFont.getFontMatrix();
        }
        else
        {
            return super.getFontMatrix();
        }
    }
}
