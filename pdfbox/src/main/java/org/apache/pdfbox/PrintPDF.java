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
package org.apache.pdfbox;

import java.awt.print.PrinterJob;

import javax.print.PrintService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageable;

import java.io.File;

/**
 * This is a command line program that will print a PDF document.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PrintPDF
{

    private static final String PASSWORD     = "-password";
    private static final String SILENT       = "-silentPrint";
    private static final String PRINTER_NAME = "-printerName";

    /**
     * private constructor.
    */
    private PrintPDF()
    {
        //static class
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        String password = "";
        String pdfFile = null;
        boolean silentPrint = false;
        String printerName = null;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else if( args[i].equals( PRINTER_NAME ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                printerName = args[i];
            }
            else if( args[i].equals( SILENT ) )
            {
                silentPrint = true;
            }
            else
            {
                pdfFile = args[i];
            }
        }

        if( pdfFile == null )
        {
            usage();
        }

        PDDocument document = null;
        try
        {
            document = PDDocument.load( pdfFile );

            if( document.isEncrypted() )
            {
                document.decrypt( password );
            }

            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setJobName(new File(pdfFile).getName());

            if(printerName != null )
            {
                PrintService[] printService = PrinterJob.lookupPrintServices();
                boolean printerFound = false;
                for(int i = 0; !printerFound && i < printService.length; i++)
                {
                    if(printService[i].getName().indexOf(printerName) != -1)
                    {
                        printJob.setPrintService(printService[i]);
                        printerFound = true;
                    }
                }
            }

            printJob.setPageable(new PDPageable(document, printJob));
            if( silentPrint || printJob.printDialog())
            {
                printJob.print();
            }
        }
        finally
        {
            if( document != null )
            {
                document.close();
            }
        }
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java -jar pdfbox-app-x.y.z.jar PrintPDF [OPTIONS] <PDF file>\n" +
            "  -password  <password>        Password to decrypt document\n" +
            "  -silentPrint                 Print without prompting for printer info\n"
            );
        System.exit( 1 );
    }
}
