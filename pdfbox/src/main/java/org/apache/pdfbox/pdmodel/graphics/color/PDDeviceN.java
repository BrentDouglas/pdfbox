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
package org.apache.pdfbox.pdmodel.graphics.color;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;

import java.io.IOException;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;

import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;

/**
 * This class represents a DeviceN color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDDeviceN extends PDColorSpace
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDDeviceN.class);

    private static final int COLORANT_NAMES = 1;

    private static final int ALTERNATE_CS = 2;

    private static final int TINT_TRANSFORM = 3;

    private static final int DEVICEN_ATTRIBUTES = 4;

    /**
     * The name of this color space.
     */
    public static final String NAME = "DeviceN";

    private COSArray array;

    /**
     * Constructor.
     */
    public PDDeviceN()
    {
        array = new COSArray();
        array.add( COSName.DEVICEN );
        array.add( COSName.getPDFName( "" ) );
    }

    /**
     * Constructor.
     *
     * @param separation The array containing all separation information.
     */
    public PDDeviceN( COSArray separation )
    {
        array = separation;
    }

    /**
     * This will return the name of the color space.  For a PDSeparation object
     * this will always return "Separation"
     *
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * This will get the number of components that this color space is made up of.
     *
     * @return The number of components in this color space.
     *
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        return getColorantNames().size();
    }

    /**
     * Create a Java colorspace for this colorspace.
     *
     * @return A color space that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color space.
     */
    protected ColorSpace createColorSpace() throws IOException
    {
        try
        {
            PDColorSpace alt = getAlternateColorSpace();
            return alt.getJavaColorSpace();
        }
        catch (IOException ioexception)
        {
            LOG.error(ioexception, ioexception);
            throw ioexception;
        }
        catch (Exception exception)
        {
            LOG.error(exception, exception);
            throw new IOException("Failed to Create ColorSpace");
        }
    }

    /**
     * Create a Java color model for this colorspace.
     *
     * @param bpc The number of bits per component.
     *
     * @return A color model that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color model.
     */
    public ColorModel createColorModel( int bpc ) throws IOException
    {
        LOG.info("About to create ColorModel for " + getAlternateColorSpace().toString());
        return getAlternateColorSpace().createColorModel(bpc);
    }

    /**
     * This will get the colorant names.  A list of string objects.
     *
     * @return A list of colorants
     */
    public List<COSBase> getColorantNames()
    {
        COSArray names = (COSArray)array.getObject( COLORANT_NAMES );
        return COSArrayList.convertCOSNameCOSArrayToList( names );
    }

    /**
     * This will set the list of colorants.
     *
     * @param names The list of colorant names.
     */
    public void setColorantNames( List<COSBase> names )
    {
        COSArray namesArray = COSArrayList.convertStringListToCOSNameCOSArray( names );
        array.set( COLORANT_NAMES, namesArray );
    }

    /**
     * This will get the alternate color space for this separation.
     *
     * @return The alternate color space.
     *
     * @throws IOException If there is an error getting the alternate color space.
     */
    public PDColorSpace getAlternateColorSpace() throws IOException
    {
        COSBase alternate = array.getObject( ALTERNATE_CS );
        return PDColorSpaceFactory.createColorSpace( alternate );
    }

    /**
     * This will set the alternate color space.
     *
     * @param cs The alternate color space.
     */
    public void setAlternateColorSpace( PDColorSpace cs )
    {
        COSBase space = null;
        if( cs != null )
        {
            space = cs.getCOSObject();
        }
        array.set( ALTERNATE_CS, space );
    }

    /**
     * This will get the tint transform function.
     *
     * @return The tint transform function.
     *
     * @throws IOException if there is an error creating the function.
     */
    public PDFunction getTintTransform() throws IOException
    {
        return  PDFunction.create( array.getObject( TINT_TRANSFORM ) );
    }

    /**
     * This will set the tint transform function.
     *
     * @param tint The tint transform function.
     */
    public void setTintTransform( PDFunction tint )
    {
        array.set( TINT_TRANSFORM, tint );
    }

    /**
     * This will get the attributes that are associated with the deviceN
     * color space.
     *
     * @return The DeviceN attributes.
     */
    public PDDeviceNAttributes getAttributes()
    {
        PDDeviceNAttributes retval = null;
        if( array.size() <= DEVICEN_ATTRIBUTES )
        {
            retval = new PDDeviceNAttributes();
            setAttributes( retval );
        }
        return retval;
    }

    /**
     * This will set the color space attributes.  If null is passed in then
     * all attribute will be removed.
     *
     * @param attributes The color space attributes.
     */
    public void setAttributes( PDDeviceNAttributes attributes )
    {
        if( attributes == null )
        {
            array.remove( DEVICEN_ATTRIBUTES );
        }
        else
        {
            //make sure array is large enough
            while( array.size() <= DEVICEN_ATTRIBUTES )
            {
                array.add( COSNull.NULL );
            }
            array.set( DEVICEN_ATTRIBUTES, attributes.getCOSDictionary() );
        }
    }

    /**
     * Returns the components of the color in the alternate colorspace for the given tint value.
     * @param tintValues a list containing the tint values
     * @return COSArray with the color components
     * @throws IOException If the tint function is not supported
     */
    public COSArray calculateColorValues(List<COSBase> tintValues) throws IOException
    {
        PDFunction tintTransform = getTintTransform();
        COSArray tint = new COSArray();
        tint.addAll(tintValues);
        return tintTransform.eval(tint);
    }

}
