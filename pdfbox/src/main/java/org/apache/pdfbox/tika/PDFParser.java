/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.tika;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.CloseShieldInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.metadata.Property;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Tika parser for PDF documents.
 * <p>
 * This parser can process also encrypted PDF documents if the required
 * password is given as a part of the input metadata associated with a
 * document. If no password is given, then this parser will try decrypting
 * the document using the empty password that's often used with PDFs.
 *
 * @since Apache PDFBox 1.7.0
 */
public class PDFParser extends AbstractParser {

    /** Serial version UID */
    private static final long serialVersionUID = -752276948656079347L;

    // True if we let PDFBox "guess" where spaces should go:
    private boolean enableAutoSpace = true;

    // True if we let PDFBox remove duplicate overlapping text:
    private boolean suppressDuplicateOverlappingText;

    // True if we extract annotation text ourselves
    // (workaround for PDFBOX-1143):
    private boolean extractAnnotationText = true;

    // True if we should sort text tokens by position
    // (necessary for some PDFs, but messes up other PDFs):
    private boolean sortByPosition = false;

    /**
     * Metadata key for giving the document password to the parser.
     */
    public static final String PASSWORD = "org.apache.pdfbox.tika.password";

    /**
     * Metadata key for giving the document password to the parser.
     *
     * @since Apache Tika 0.5
     */
    private static final String OLD_PASSWORD =
            "org.apache.tika.parser.pdf.password";

    private static final Set<MediaType> SUPPORTED_TYPES =
        Collections.singleton(MediaType.application("pdf"));

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

    public void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        PDDocument pdfDocument =
            PDDocument.load(new CloseShieldInputStream(stream), true);
        try {
            if (pdfDocument.isEncrypted()) {
                try {
                    String password = metadata.get(PASSWORD);
                    if (password == null) {
                        password = metadata.get(OLD_PASSWORD);
                    }
                    if (password == null) {
                        password = "";
                    }
                    pdfDocument.decrypt(password);
                } catch (Exception e) {
                    // Ignore
                }
            }
            metadata.set(Metadata.CONTENT_TYPE, "application/pdf");
            extractMetadata(pdfDocument, metadata);
            PDF2XHTML.process(pdfDocument, handler, metadata,
                    extractAnnotationText, enableAutoSpace,
                    suppressDuplicateOverlappingText, sortByPosition);
        } finally {
            pdfDocument.close();
        }
    }

    private void extractMetadata(PDDocument document, Metadata metadata)
            throws TikaException {
        PDDocumentInformation info = document.getDocumentInformation();
        metadata.set(PagedText.N_PAGES, document.getNumberOfPages());
        addMetadata(metadata, Metadata.TITLE, info.getTitle());
        addMetadata(metadata, Metadata.AUTHOR, info.getAuthor());
        addMetadata(metadata, Metadata.CREATOR, info.getCreator());
        addMetadata(metadata, Metadata.KEYWORDS, info.getKeywords());
        addMetadata(metadata, "producer", info.getProducer());
        addMetadata(metadata, Metadata.SUBJECT, info.getSubject());
        addMetadata(metadata, "trapped", info.getTrapped());
        try {
            addMetadata(metadata, "created", info.getCreationDate());
            addMetadata(metadata, Metadata.CREATION_DATE, info.getCreationDate());
        } catch (IOException e) {
            // Invalid date format, just ignore
        }
        try {
            Calendar modified = info.getModificationDate(); 
            addMetadata(metadata, Metadata.LAST_MODIFIED, modified);
        } catch (IOException e) {
            // Invalid date format, just ignore
        }

        // All remaining metadata is custom
        // Copy this over as-is
        List<String> handledMetadata = Arrays.asList(
                "Author", "Creator", "CreationDate", "ModDate",
                "Keywords", "Producer", "Subject", "Title", "Trapped");
        for (COSName key : info.getDictionary().keySet()) {
            String name = key.getName();
            if (!handledMetadata.contains(name)) {
                addMetadata(
                        metadata, name,
                        info.getDictionary().getDictionaryObject(key));
            }
        }
    }

    private void addMetadata(Metadata metadata, String name, String value) {
        if (value != null) {
            metadata.add(name, value);
        }
    }

    private void addMetadata(Metadata metadata, String name, Calendar value) {
        if (value != null) {
            metadata.set(name, value.getTime().toString());
        }
    }

    private void addMetadata(
            Metadata metadata, Property property, Calendar value) {
        if (value != null) {
            metadata.set(property, value.getTime());
        }
    }

    /**
     * Used when processing custom metadata entries, as PDFBox won't do
     * the conversion for us in the way it does for the standard ones
     */
    private void addMetadata(Metadata metadata, String name, COSBase value) {
        if (value instanceof COSArray) {
            for (COSBase v : ((COSArray)value).toList()) {
                addMetadata(metadata, name, v);
            }
        } else if (value instanceof COSString) {
            addMetadata(metadata, name, ((COSString) value).getString());
        } else {
            addMetadata(metadata, name, value.toString());
        }
    }

    /**
     *  If true (the default), the parser should estimate
     *  where spaces should be inserted between words.  For
     *  many PDFs this is necessary as they do not include
     *  explicit whitespace characters.
     */
    public void setEnableAutoSpace(boolean v) {
        enableAutoSpace = v;
    }

    /** @see #setEnableAutoSpace. */
    public boolean getEnableAutoSpace() {
        return enableAutoSpace;
    }

    /**
     * If true (the default), text in annotations will be
     * extracted.
     */
    public void setExtractAnnotationText(boolean v) {
        extractAnnotationText = v;
    }

    /**
     * If true, text in annotations will be extracted.
     */
    public boolean getExtractAnnotationText() {
        return extractAnnotationText;
    }

    /**
     *  If true, the parser should try to remove duplicated
     *  text over the same region.  This is needed for some
     *  PDFs that achieve bolding by re-writing the same
     *  text in the same area.  Note that this can
     *  slow down extraction substantially (PDFBOX-956) and
     *  sometimes remove characters that were not in fact
     *  duplicated (PDFBOX-1155).  By default this is disabled.
     */
    public void setSuppressDuplicateOverlappingText(boolean v) {
        suppressDuplicateOverlappingText = v;
    }

    /** @see #setSuppressDuplicateOverlappingText. */
    public boolean getSuppressDuplicateOverlappingText() {
        return suppressDuplicateOverlappingText;
    }

    /**
     *  If true, sort text tokens by their x/y position
     *  before extracting text.  This may be necessary for
     *  some PDFs (if the text tokens are not rendered "in
     *  order"), while for other PDFs it can produce the
     *  wrong result (for example if there are 2 columns,
     *  the text will be interleaved).  Default is false.
     */
    public void setSortByPosition(boolean v) {
        sortByPosition = v;
    }

    /** @see #setSortByPosition. */
    public boolean getSortByPosition() {
        return sortByPosition;
    }

}
