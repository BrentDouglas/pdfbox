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
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class PostScriptTable extends TTFTable
{
    private float formatType;
    private float italicAngle;
    private short underlinePosition;
    private short underlineThickness;
    private long isFixedPitch;
    private long minMemType42;
    private long maxMemType42;
    private long mimMemType1;
    private long maxMemType1;
    private String[] glyphNames = null;
    
    /**
     * The 258 standard mac glyph names a used in 'post' format 1 and 2.
     */
    private static final int NUMBER_OF_MAC_GLYPHS = 258;
    
    private static final String[] MAC_GLYPH_NAMES = new String[] 
    { 
        ".notdef",".null", "nonmarkingreturn", "space", "exclam", "quotedbl",
            "numbersign", "dollar", "percent", "ampersand", "quotesingle",
            "parenleft", "parenright", "asterisk", "plus", "comma", "hyphen",
            "period", "slash", "zero", "one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine", "colon", "semicolon", "less",
            "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash",
            "bracketright", "asciicircum", "underscore", "grave", "a", "b",
            "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
            "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft",
            "bar", "braceright", "asciitilde", "Adieresis", "Aring",
            "Ccedilla", "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute",
            "agrave", "acircumflex", "adieresis", "atilde", "aring",
            "ccedilla", "eacute", "egrave", "ecircumflex", "edieresis",
            "iacute", "igrave", "icircumflex", "idieresis", "ntilde", "oacute",
            "ograve", "ocircumflex", "odieresis", "otilde", "uacute", "ugrave",
            "ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling",
            "section", "bullet", "paragraph", "germandbls", "registered",
            "copyright", "trademark", "acute", "dieresis", "notequal", "AE",
            "Oslash", "infinity", "plusminus", "lessequal", "greaterequal",
            "yen", "mu", "partialdiff", "summation", "product", "pi",
            "integral", "ordfeminine", "ordmasculine", "Omega", "ae", "oslash",
            "questiondown", "exclamdown", "logicalnot", "radical", "florin",
            "approxequal", "Delta", "guillemotleft", "guillemotright",
            "ellipsis", "nonbreakingspace", "Agrave", "Atilde", "Otilde", "OE",
            "oe", "endash", "emdash", "quotedblleft", "quotedblright",
            "quoteleft", "quoteright", "divide", "lozenge", "ydieresis",
            "Ydieresis", "fraction", "currency", "guilsinglleft",
            "guilsinglright", "fi", "fl", "daggerdbl", "periodcentered",
            "quotesinglbase", "quotedblbase", "perthousand", "Acircumflex",
            "Ecircumflex", "Aacute", "Edieresis", "Egrave", "Iacute",
            "Icircumflex", "Idieresis", "Igrave", "Oacute", "Ocircumflex",
            "apple", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi",
            "circumflex", "tilde", "macron", "breve", "dotaccent", "ring",
            "cedilla", "hungarumlaut", "ogonek", "caron", "Lslash", "lslash",
            "Scaron", "scaron", "Zcaron", "zcaron", "brokenbar", "Eth", "eth",
            "Yacute", "yacute", "Thorn", "thorn", "minus", "multiply",
            "onesuperior", "twosuperior", "threesuperior", "onehalf",
            "onequarter", "threequarters", "franc", "Gbreve", "gbreve",
            "Idotaccent", "Scedilla", "scedilla", "Cacute", "cacute", "Ccaron",
            "ccaron", "dcroat" 
    };

    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "post";
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        MaximumProfileTable maxp = ttf.getMaximumProfile();
        formatType = data.read32Fixed();
        italicAngle = data.read32Fixed();
        underlinePosition = data.readSignedShort();
        underlineThickness = data.readSignedShort();
        isFixedPitch = data.readUnsignedInt();
        minMemType42 = data.readUnsignedInt();
        maxMemType42 = data.readUnsignedInt();
        mimMemType1 = data.readUnsignedInt();
        maxMemType1 = data.readUnsignedInt();
        
        if( formatType == 1.0f )
        {
            /*
             * This TrueType font file contains exactly the 258 glyphs in the standard 
             * Macintosh TrueType.
             */
            glyphNames = new String[NUMBER_OF_MAC_GLYPHS];
            System.arraycopy(MAC_GLYPH_NAMES, 0, glyphNames, 0, NUMBER_OF_MAC_GLYPHS);
        }
        else if( formatType == 2.0f )
        {
            int numGlyphs = data.readUnsignedShort();
            int[] glyphNameIndex = new int[numGlyphs];
            glyphNames = new String[ numGlyphs ];
            int maxIndex = Integer.MIN_VALUE;
            for( int i=0; i<numGlyphs; i++ )
            {
                int index = data.readUnsignedShort();
                glyphNameIndex[i] = index;
                // PDFBOX-808: Index numbers between 32768 and 65535 are
                // reserved for future use, so we should just ignore them
                if (index <= 32767) 
                {
                    maxIndex = Math.max( maxIndex, index );
                }
            }
            String[] nameArray = null;
            if( maxIndex >= NUMBER_OF_MAC_GLYPHS )
            {
                nameArray = new String[ maxIndex-NUMBER_OF_MAC_GLYPHS +1 ];
                for( int i=0; i<maxIndex-NUMBER_OF_MAC_GLYPHS+1; i++ )
                {
                    int numberOfChars = data.read();
                    nameArray[i]=data.readString( numberOfChars );
                }
            }
            for( int i=0; i<numGlyphs; i++ )
            {
                int index = glyphNameIndex[i];
                if( index < NUMBER_OF_MAC_GLYPHS )
                {
                    glyphNames[i] = MAC_GLYPH_NAMES[index];
                }
                else if( index >= NUMBER_OF_MAC_GLYPHS && index <= 32767 )
                {
                    glyphNames[i] = nameArray[index-NUMBER_OF_MAC_GLYPHS];
                }
                else
                {
                    // PDFBOX-808: Index numbers between 32768 and 65535 are
                    // reserved for future use, so we should just ignore them
                    glyphNames[i] = ".undefined";
                }
            }
        }
        else if( formatType == 2.5f )
        {
            int[] glyphNameIndex = new int[maxp.getNumGlyphs()];
            for( int i=0; i<glyphNameIndex.length; i++)
            {
                int offset = data.readSignedByte();
                glyphNameIndex[i] = i+1+offset;
            }
            glyphNames = new String[glyphNameIndex.length];
            for( int i=0; i<glyphNames.length; i++)
            {
                String name = MAC_GLYPH_NAMES[glyphNameIndex[i]];
                if( name != null )
                {
                    glyphNames[i] = name;
                }
            }
            
        }
        else if( formatType == 3.0f )
        {
            //no postscript information is provided.
        }
    }
    /**
     * @return Returns the formatType.
     */
    public float getFormatType()
    {
        return formatType;
    }
    /**
     * @param formatTypeValue The formatType to set.
     */
    public void setFormatType(float formatTypeValue)
    {
        this.formatType = formatTypeValue;
    }
    /**
     * @return Returns the isFixedPitch.
     */
    public long getIsFixedPitch()
    {
        return isFixedPitch;
    }
    /**
     * @param isFixedPitchValue The isFixedPitch to set.
     */
    public void setIsFixedPitch(long isFixedPitchValue)
    {
        this.isFixedPitch = isFixedPitchValue;
    }
    /**
     * @return Returns the italicAngle.
     */
    public float getItalicAngle()
    {
        return italicAngle;
    }
    /**
     * @param italicAngleValue The italicAngle to set.
     */
    public void setItalicAngle(float italicAngleValue)
    {
        this.italicAngle = italicAngleValue;
    }
    /**
     * @return Returns the maxMemType1.
     */
    public long getMaxMemType1()
    {
        return maxMemType1;
    }
    /**
     * @param maxMemType1Value The maxMemType1 to set.
     */
    public void setMaxMemType1(long maxMemType1Value)
    {
        this.maxMemType1 = maxMemType1Value;
    }
    /**
     * @return Returns the maxMemType42.
     */
    public long getMaxMemType42()
    {
        return maxMemType42;
    }
    /**
     * @param maxMemType42Value The maxMemType42 to set.
     */
    public void setMaxMemType42(long maxMemType42Value)
    {
        this.maxMemType42 = maxMemType42Value;
    }
    /**
     * @return Returns the mimMemType1.
     */
    public long getMimMemType1()
    {
        return mimMemType1;
    }
    /**
     * @param mimMemType1Value The mimMemType1 to set.
     */
    public void setMimMemType1(long mimMemType1Value)
    {
        this.mimMemType1 = mimMemType1Value;
    }
    /**
     * @return Returns the minMemType42.
     */
    public long getMinMemType42()
    {
        return minMemType42;
    }
    /**
     * @param minMemType42Value The minMemType42 to set.
     */
    public void setMinMemType42(long minMemType42Value)
    {
        this.minMemType42 = minMemType42Value;
    }
    /**
     * @return Returns the underlinePosition.
     */
    public short getUnderlinePosition()
    {
        return underlinePosition;
    }
    /**
     * @param underlinePositionValue The underlinePosition to set.
     */
    public void setUnderlinePosition(short underlinePositionValue)
    {
        this.underlinePosition = underlinePositionValue;
    }
    /**
     * @return Returns the underlineThickness.
     */
    public short getUnderlineThickness()
    {
        return underlineThickness;
    }
    /**
     * @param underlineThicknessValue The underlineThickness to set.
     */
    public void setUnderlineThickness(short underlineThicknessValue)
    {
        this.underlineThickness = underlineThicknessValue;
    }
    /**
     * @return Returns the glyphNames.
     */
    public String[] getGlyphNames()
    {
        return glyphNames;
    }
    /**
     * @param glyphNamesValue The glyphNames to set.
     */
    public void setGlyphNames(String[] glyphNamesValue)
    {
        this.glyphNames = glyphNamesValue;
    }
}
