package org.apache.pdfbox.util;
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

import java.util.Comparator;

/**
 * This class is a comparator for TextPosition operators.  It handles
 * pages with text in different directions by grouping the text based
 * on direction and sorting in that direction. This allows continuous text
 * in a given direction to be more easily grouped together.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class SafeTextPositionComparator implements Comparator<TextPosition>
{
    public static final SafeTextPositionComparator INSTANCE = new SafeTextPositionComparator();

    /**
     * {@inheritDoc}
     */
    public int compare(TextPosition pos1, TextPosition pos2)
    {
        int retval;

        /* Only compare text that is in the same direction. */
        if (pos1.getDir() < pos2.getDir())
        {
            return -1;
        }
        else if (pos1.getDir() > pos2.getDir())
        {
            return 1;
        }

        // Get the text direction adjusted coordinates
        float pos1YBottom = pos1.getYDirAdj();
        float pos2YBottom = pos2.getYDirAdj();
        // note that the coordinates have been adjusted so 0,0 is in upper left

        float yDifference = Math.abs( pos1YBottom-pos2YBottom);
        //we will do a simple tolerance comparison.
        if (yDifference < .1)
        {
            float x1 = pos1.getXDirAdj();
            float x2 = pos2.getXDirAdj();

            float xDifference = Math.abs( x1-x2);

            if ( xDifference < .1)
            {
                float pos1Height = pos1.getHeightDir();
                float pos2Height = pos2.getHeightDir();
                float heightDifference = Math.abs(pos1Height - pos2Height);
                if (heightDifference < .1)
                {
                    float pos1Width = pos1.getWidthDirAdj();
                    float pos2Width = pos2.getWidthDirAdj();
                    float widthDifference = Math.abs(pos1Width - pos2Width);
                    if (widthDifference < .1)
                    {
                        retval = 0;
                    }
                    else
                    {
                        retval = pos1Width < pos2Width ? -1 : 1;
                    }
                }
                else
                {
                    retval = pos1Height < pos2Height ? -1 : 1;
                }
            }
            else
            {
                retval = x1 < x2 ? -1 : 1;
            }
        }
        else
        {
            retval = pos1YBottom < pos2YBottom ? -1 : 1;
        }

        return retval;
    }
}
