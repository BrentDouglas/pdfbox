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
package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.util.ImageIOUtil;

/**
 * An image class for JPegs.
 *
 * @author mathiak
 * @version $Revision: 1.5 $
 */
public class PDJpeg extends PDXObjectImage
{
    private BufferedImage image = null;

    private static final String JPG = "jpg";

    private static final List<String> DCT_FILTERS = new ArrayList<String>();

    private static final float DEFAULT_COMPRESSION_LEVEL = 0.75f;

    static
    {
        DCT_FILTERS.add( COSName.DCT_DECODE.getName() );
        DCT_FILTERS.add( COSName.DCT_DECODE_ABBREVIATION.getName() );
    }

    /**
     * Standard constructor.
     *
     * @param jpeg The COSStream from which to extract the JPeg
     */
    public PDJpeg(PDStream jpeg)
    {
        super(jpeg, JPG);
    }

    /**
     * Construct from a stream.
     *
     * @param doc The document to create the image as part of.
     * @param is The stream that contains the jpeg data.
     * @throws IOException If there is an error reading the jpeg data.
     */
    public PDJpeg( PDDocument doc, InputStream is ) throws IOException
    {
        super( new PDStream( doc, is, true ), JPG);
        COSDictionary dic = getCOSStream();
        dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
        dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
        dic.setItem( COSName.TYPE, COSName.XOBJECT );

        getRGBImage();
        if (image != null)
        {
            setBitsPerComponent( 8 );
            setColorSpace( PDDeviceRGB.INSTANCE );
            setHeight( image.getHeight() );
            setWidth( image.getWidth() );
        }

    }

    /**
     * Construct from a buffered image.
     * The default compression level of 0.75 will be used.
     *
     * @param doc The document to create the image as part of.
     * @param bi The image to convert to a jpeg
     * @throws IOException If there is an error processing the jpeg data.
     */
    public PDJpeg( PDDocument doc, BufferedImage bi ) throws IOException
    {
        super( new PDStream( doc ) , JPG);
        createImageStream(doc, bi, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Construct from a buffered image.
     *
     * @param doc The document to create the image as part of.
     * @param bi The image to convert to a jpeg
     * @param compressionQuality The quality level which is used to compress the image
     * @throws IOException If there is an error processing the jpeg data.
     */
    public PDJpeg( PDDocument doc, BufferedImage bi, float compressionQuality ) throws IOException
    {
        super( new PDStream( doc ), JPG);
        createImageStream(doc, bi, compressionQuality);
    }

    private void createImageStream(PDDocument doc, BufferedImage bi, float compressionQuality) throws IOException
    {
        BufferedImage alpha = null;
        if (bi.getColorModel().hasAlpha())
        {
            alpha = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = alpha.createGraphics();
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, bi.getWidth(), bi.getHeight());
            g.setColor(Color.WHITE);
            g.drawImage(bi, 0, 0, null);
            int alphaHeight = alpha.getHeight();
            int alphaWidth = alpha.getWidth();
            int whiteRGB = (Color.WHITE).getRGB();
            for(int y = 0; y < alphaHeight; y++)
            {
                for(int x = 0; x < alphaWidth; x++)
                {
                    int colorValues = alpha.getRGB(x, y);
                    // TODO check condition PDFBOX-626
                    if( ((colorValues >> 16) & 0xFF) != 0 
                            && ((colorValues >> 8) & 0xFF) != 0 
                            && ((colorValues >> 0) & 0xFF) != 0 )
                    {
                        alpha.setRGB(x, y, whiteRGB);
                    }
                }
            }
            image = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
            g = image.createGraphics();
            g.drawImage(bi, 0, 0, null);
            bi = image;
        }

        java.io.OutputStream os = getCOSStream().createFilteredStream();
        try
        {
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(JPG);
            if (iter.hasNext())
            {
                writer = iter.next();
            }
            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);

            // Set the compression quality
            JPEGImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
            iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwparam.setCompressionQuality(compressionQuality);

            // Write the image
            writer.write(null, new IIOImage(bi, null, null), iwparam);

            writer.dispose();

            COSDictionary dic = getCOSStream();
            dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
            dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
            dic.setItem( COSName.TYPE, COSName.XOBJECT );
            PDXObjectImage alphaPdImage = null;
            if(alpha != null)
            {
                alphaPdImage = new PDJpeg(doc, alpha, compressionQuality);
                dic.setItem(COSName.SMASK, alphaPdImage);
            }
            setBitsPerComponent( 8 );
            if (bi.getColorModel().getNumComponents() == 3)
            {
                setColorSpace( PDDeviceRGB.INSTANCE );
            }
            else
            {
                if (bi.getColorModel().getNumComponents() == 1)
                {
                    setColorSpace( new PDDeviceGray() );
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            setHeight( bi.getHeight() );
            setWidth( bi.getWidth() );
        }
        finally
        {
            os.close();
        }
    }

    /**
     * Returns an image of the JPeg, or null if JPegs are not supported. (They should be. )
     * {@inheritDoc}
     */
    public BufferedImage getRGBImage() throws IOException
    {   
        if (image != null)
        {
            return image;
        }
        
        BufferedImage bi = null;
        boolean readError = false;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        removeAllFiltersButDCT(os);
        os.close();
        byte[] img = os.toByteArray();
        
        PDColorSpace cs = getColorSpace();
        try 
        {
            if (cs instanceof PDDeviceCMYK 
                    || (cs instanceof PDICCBased && cs.getNumberOfComponents() == 4))
            {
                // create BufferedImage based on the converted color values
                bi = convertCMYK2RGB(readImage(img), cs);

            }
            else if (cs instanceof PDSeparation)
            {
                // create BufferedImage based on the converted color values
                bi = processTintTransformation(readImage(img), 
                        ((PDSeparation)cs).getTintTransform(), cs.getJavaColorSpace());
            }
            else if (cs instanceof PDDeviceN)
            {
                // create BufferedImage based on the converted color values
                bi = processTintTransformation(readImage(img), 
                        ((PDDeviceN)cs).getTintTransform(), cs.getJavaColorSpace());
            }
            else 
            {
                ByteArrayInputStream bai = new ByteArrayInputStream(img);
                bi = ImageIO.read(bai);
            }
                
        }
        catch(IIOException exception) 
        {
            readError = true;
        }
        // 2. try to read jpeg again. some jpegs have some strange header containing
        //    "Adobe " at some place. so just replace the header with a valid jpeg header.
        // TODO : not sure if it works for all cases
        if (bi == null && readError)
        {
            byte[] newImage = replaceHeader(img);
            ByteArrayInputStream bai = new ByteArrayInputStream(newImage);
            bi = ImageIO.read(bai);
        }

        // If there is a 'soft mask' image then we use that as a transparency mask.
        PDXObjectImage smask = getSMaskImage();
        if (smask != null)
        {
            BufferedImage smaskBI = smask.getRGBImage();

            COSArray decodeArray = smask.getDecode();
            CompositeImage compositeImage = new CompositeImage(bi, smaskBI);
            BufferedImage rgbImage = compositeImage.createMaskedImage(decodeArray);

            image = rgbImage;
        }
        else
        {
            // But if there is no soft mask, use the unaltered image.
            image = bi;
        }
        return image;
    }

    /**
     * This writes the JPeg to out.
     * {@inheritDoc}
     */
    public void write2OutputStream(OutputStream out) throws IOException
    {
        getRGBImage();
        if (image != null) 
        {
            ImageIOUtil.writeImage(image, JPG, out);
        }

    }

    private void removeAllFiltersButDCT(OutputStream out) throws IOException
    {
        InputStream data = getPDStream().getPartiallyFilteredStream( DCT_FILTERS );
        byte[] buf = new byte[1024];
        int amountRead = -1;
        while( (amountRead = data.read( buf )) != -1 )
        {
            out.write( buf, 0, amountRead );
        }
    }

    private int getHeaderEndPos(byte[] imageAsBytes)
    {
        for (int i = 0; i < imageAsBytes.length; i++)
        {
            byte b = imageAsBytes[i];
            if (b == (byte) 0xDB)
            {
                // TODO : check for ff db
                return i -2;
            }
        }
        return 0;
    }

    private byte[] replaceHeader(byte[] imageAsBytes)
    {
        // get end position of wrong header respectively startposition of "real jpeg data"
        int pos = getHeaderEndPos(imageAsBytes);

        // simple correct header
        byte[] header = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, (byte) 0x00,
                (byte) 0x10, (byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46, (byte) 0x00, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00};

        // concat
        byte[] newImage = new byte[imageAsBytes.length - pos + header.length - 1];
        System.arraycopy(header, 0, newImage, 0, header.length);
        System.arraycopy(imageAsBytes, pos + 1, newImage, header.length, imageAsBytes.length - pos - 1);

        return newImage;
    }

    private Raster readImage(byte[] bytes) throws IOException 
    {
        ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        if (readers == null || !readers.hasNext()) 
        {
            throw new RuntimeException("No ImageReaders found");
        }

        // read the raster information only
        // avoid to access the meta information
        ImageReader reader = (ImageReader) readers.next();
        reader.setInput(input);
        Raster raster = reader.readRaster(0, reader.getDefaultReadParam());
        if (input != null) 
        {
            input.close();
        }
        reader.dispose();
        return raster;
    }

    // CMYK jpegs are not supported by JAI, so that we have to do the conversion on our own
    private BufferedImage convertCMYK2RGB(Raster raster, PDColorSpace colorspace) throws IOException 
    {
        // create a java color space to be used for conversion
        ColorSpace cs = colorspace.getJavaColorSpace();
        int width = raster.getWidth();
        int height = raster.getHeight();
        byte[] rgb = new byte[width * height * 3]; 
        int rgbIndex = 0;
        for (int i = 0; i < height; i++) 
        {
            for (int j = 0; j < width; j++)
            {
                // get the source color values
                float[] srcColorValues = raster.getPixel(j,i, (float[])null);
                // convert values from 0..255 to 0..1
                for (int k = 0; k < 4; k++)
                {
                    srcColorValues[k] /= 255f; 
                }
                // convert CMYK to RGB
                float[] rgbValues = cs.toRGB(srcColorValues);
                // convert values from 0..1 to 0..255
                for (int k = 0; k < 3; k++)
                {
                    rgb[rgbIndex+k] = (byte)(rgbValues[k] * 255); 
                }
                rgbIndex +=3;
            }
        }
        // create a RGB color model
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 
                false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        // create the target raster
        WritableRaster writeableRaster = cm.createCompatibleWritableRaster(width, height);
        // get the data buffer of the raster
        DataBufferByte buffer = (DataBufferByte)writeableRaster.getDataBuffer();
        byte[] bufferData = buffer.getData();
        // copy all the converted data to the raster buffer
        System.arraycopy( rgb, 0,bufferData, 0,rgb.length );
        // create an image using the converted color values
        return new BufferedImage(cm, writeableRaster, true, null);
    }

    // Separation and DeviceN colorspaces are using a tint transform function to convert color values 
    private BufferedImage processTintTransformation(Raster raster, PDFunction function, ColorSpace colorspace) 
    throws IOException 
    {
        int numberOfInputValues = function.getNumberOfInputParameters();
        int numberOfOutputValues = function.getNumberOfOutputParameters();
        int width = raster.getWidth();
        int height = raster.getHeight();
        byte[] sourceBuffer = new byte[width * height * numberOfOutputValues]; 
        int bufferIndex = 0;
        for (int i = 0; i < height; i++) 
        {
            for (int j = 0; j < width; j++)
            {
                // get the source color values
                float[] srcColorValues = raster.getPixel(j,i, (float[])null);
                // convert values from 0..255 to 0..1
                for (int k = 0; k < numberOfInputValues; k++)
                {
                    srcColorValues[k] /= 255f; 
                }
                // transform the color values using the tint function
                float[] convertedValues = function.eval(srcColorValues);
                // convert values from 0..1 to 0..255
                for (int k = 0; k < numberOfOutputValues; k++)
                {
                    sourceBuffer[bufferIndex+k] = (byte)(convertedValues[k] * 255); 
                }
                bufferIndex +=numberOfOutputValues;
            }
        }
        // create a target color model
        ColorModel cm = new ComponentColorModel(colorspace, 
                false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        // create the target raster
        WritableRaster writeableRaster = cm.createCompatibleWritableRaster(width, height);
        // get the data buffer of the raster
        DataBufferByte buffer = (DataBufferByte)writeableRaster.getDataBuffer();
        byte[] bufferData = buffer.getData();
        // copy all the converted data to the raster buffer
        System.arraycopy( sourceBuffer, 0,bufferData, 0,sourceBuffer.length );
        // create an image using the converted color values
        return new BufferedImage(cm, writeableRaster, true, null);
    }

}

