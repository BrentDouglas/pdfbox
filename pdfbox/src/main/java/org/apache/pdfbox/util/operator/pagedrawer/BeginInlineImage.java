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
package org.apache.pdfbox.util.operator.pagedrawer;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDInlinedImage;
import org.apache.pdfbox.util.ImageParameters;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.operator.OperatorProcessor;

/**
 * Implementation of content stream operator for page drawer.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class BeginInlineImage extends OperatorProcessor
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(BeginInlineImage.class);

    /**
     * process : BI : begin inline image.
     * @param operator The operator that is being executed.
     * @param arguments List
     * @throws IOException If there is an error displaying the inline image.
     */
    public void process(PDFOperator operator, List<COSBase> arguments)  throws IOException
    {
        PageDrawer drawer = (PageDrawer)context;
        PDPage page = drawer.getPage();
        //begin inline image object
        ImageParameters params = operator.getImageParameters();
        PDInlinedImage image = new PDInlinedImage();
        image.setImageParameters( params );
        image.setImageData( operator.getImageData() );
        BufferedImage awtImage = image.createImage( context.getColorSpaces() );

        if (awtImage == null) 
        {
            log.warn("BeginInlineImage.process(): createImage returned NULL");
            return;
        }
        int imageWidth = awtImage.getWidth();
        int imageHeight = awtImage.getHeight();
        double pageHeight = drawer.getPageSize().getHeight();
        
        Matrix ctm = drawer.getGraphicsState().getCurrentTransformationMatrix();
        int pageRotation = page.findRotation();

        AffineTransform ctmAT = ctm.createAffineTransform();
        ctmAT.scale(1f/imageWidth, 1f/imageHeight);
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setFromAffineTransform( ctmAT );
        // calculate the inverse rotation angle
        // scaleX = m00 = cos
        // shearX = m01 = -sin
        // tan = sin/cos
        double angle = Math.atan(ctmAT.getShearX()/ctmAT.getScaleX());
        Matrix translationMatrix = null;
        if (pageRotation == 0 || pageRotation == 180) 
        {
            translationMatrix = Matrix.getTranslatingInstance((float)(Math.sin(angle)*ctm.getXScale()), (float)(pageHeight-2*ctm.getYPosition()-Math.cos(angle)*ctm.getYScale())); 
        }
        else if (pageRotation == 90 || pageRotation == 270) 
        {
            translationMatrix = Matrix.getTranslatingInstance((float)(Math.sin(angle)*ctm.getYScale()), (float)(pageHeight-2*ctm.getYPosition())); 
        }
        rotationMatrix = rotationMatrix.multiply(translationMatrix);
        rotationMatrix.setValue(0, 1, (-1)*rotationMatrix.getValue(0, 1));
        rotationMatrix.setValue(1, 0, (-1)*rotationMatrix.getValue(1, 0));
        AffineTransform at = new AffineTransform(
                rotationMatrix.getValue(0,0),rotationMatrix.getValue(0,1),
                rotationMatrix.getValue(1,0), rotationMatrix.getValue( 1, 1),
                rotationMatrix.getValue(2,0),rotationMatrix.getValue(2,1)
                );
        drawer.drawImage(awtImage, at);
    }
}