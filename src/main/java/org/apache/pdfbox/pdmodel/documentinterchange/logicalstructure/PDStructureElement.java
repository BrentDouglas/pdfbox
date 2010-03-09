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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import java.util.Iterator;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

/**
 * A structure element.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDStructureElement extends PDStructureNode
{
    public static final String TYPE = "StructElem";


    /**
     * Constructor with required values.
     *
     * @param structureType the structure type
     * @param parent the parent structure node
     */
    public PDStructureElement(String structureType, PDStructureNode parent)
    {
        super(TYPE);
        this.setStructureType(structureType);
        this.setParent(parent);
    }

    /**
     * Constructor for an existing structure element.
     *
     * @param dic The existing dictionary.
     */
    public PDStructureElement( COSDictionary dic )
    {
        super(dic);
    }


    /**
     * Returns the structure type (S).
     * 
     * @return the structure type
     */
    public String getStructureType()
    {
        return this.getCOSDictionary().getNameAsString("S");
    }

    /**
     * Sets the structure type (S).
     * 
     * @param structureType the structure type
     */
    public void setStructureType(String structureType)
    {
        this.getCOSDictionary().setName("S", structureType);
    }

    /**
     * Returns the parent in the structure hierarchy (P).
     * 
     * @return the parent in the structure hierarchy
     */
    public PDStructureNode getParent()
    {
        COSDictionary p = (COSDictionary) this.getCOSDictionary()
            .getDictionaryObject(COSName.P);
        if (p == null)
        {
            return null;
        }
        return PDStructureNode.create((COSDictionary) p);
    }

    /**
     * Sets the parent in the structure hierarchy (P).
     * 
     * @param structureNode the parent in the structure hierarchy
     */
    public void setParent(PDStructureNode structureNode)
    {
        this.getCOSDictionary().setItem(COSName.P, structureNode);
    }

    /**
     * Returns the element identifier (ID).
     * 
     * @return the element identifier
     */
    public String getElementIdentifier()
    {
        return this.getCOSDictionary().getString("ID");
    }

    /**
     * Sets the element identifier (ID).
     * 
     * @param id the element identifier
     */
    public void setElementIdentifier(String id)
    {
        this.getCOSDictionary().setString("ID", id);
    }

    /**
     * Returns the page on which some or all of the content items designated by
     *  the K entry shall be rendered (Pg).
     * 
     * @return the page on which some or all of the content items designated by
     *  the K entry shall be rendered
     */
    public PDPage getPage()
    {
        COSDictionary pageDic = (COSDictionary) this.getCOSDictionary()
            .getDictionaryObject("Pg");
        if (pageDic == null)
        {
            return null;
        }
        return new PDPage(pageDic);
    }

    /**
     * Sets the page on which some or all of the content items designated by
     *  the K entry shall be rendered (Pg).
     * @param page the page on which some or all of the content items designated
     *  by the K entry shall be rendered.
     */
    public void setPage(PDPage page)
    {
        this.getCOSDictionary().setItem("Pg", page);
    }

    /**
     * Returns the class names together with their revision numbers (C).
     * 
     * @return the class names
     */
    public Revisions<String> getClassNames()
    {
        String key = "C";
        Revisions<String> classNames = new Revisions<String>();
        COSBase c = this.getCOSDictionary().getDictionaryObject(key);
        if (c instanceof COSName)
        {
            classNames.addObject(((COSName) c).getName(), 0);
        }
        if (c instanceof COSArray)
        {
            COSArray array = (COSArray) c;
            Iterator<COSBase> it = array.iterator();
            String className = null;
            while (it.hasNext())
            {
                COSBase item = it.next();
                if (item instanceof COSName)
                {
                    className = ((COSName) item).getName();
                    classNames.addObject(className, 0);
                }
                else if (item instanceof COSInteger)
                {
                    classNames.setRevisionNumber(className,
                        ((COSInteger) item).intValue());
                }
            }
        }
        return classNames;
    }

    /**
     * Sets the class names together with their revision numbers (C).
     * 
     * @param classNames the class names
     */
    public void setClassNames(Revisions<String> classNames)
    {
        String key = "C";
        if ((classNames.size() == 1) && (classNames.getRevisionNumber(0) == 0))
        {
            String className = classNames.getObject(0);
            this.getCOSDictionary().setName(key, className);
            return;
        }
        COSArray array = new COSArray();
        for (int i = 0; i < classNames.size(); i++)
        {
            String className = classNames.getObject(i);
            int revisionNumber = classNames.getRevisionNumber(i);
            if (revisionNumber < 0)
            {
                // TODO throw Exception because revision number must be > -1?
            }
            array.add(COSName.getPDFName(className));
            array.add(COSInteger.get(revisionNumber));
        }
        this.getCOSDictionary().setItem(key, array);
    }

    /**
     * Adds a class name.
     * 
     * @param className the class name
     */
    public void addClassName(String className)
    {
        String key = "C";
        COSBase c = this.getCOSDictionary().getDictionaryObject(key);
        COSArray array = null;
        if (c instanceof COSArray)
        {
            array = (COSArray) c;
        }
        else
        {
            array = new COSArray();
            if (c != null)
            {
                array.add(c);
                array.add(COSInteger.get(0));
            }
        }
        this.getCOSDictionary().setItem(key, array);
        array.add(COSName.getPDFName(className));
        array.add(COSInteger.get(this.getRevisionNumber()));
    }

    /**
     * Removes a class name.
     * 
     * @param className the class name
     */
    public void removeClassName(String className)
    {
        String key = "C";
        COSBase c = this.getCOSDictionary().getDictionaryObject(key);
        COSName name = COSName.getPDFName(className);
        if (c instanceof COSArray)
        {
            COSArray array = (COSArray) c;
            array.remove(name);
            if ((array.size() == 2) && (array.getInt(1) == 0))
            {
                this.getCOSDictionary().setItem(key, array.getObject(0));
            }
        }
        else
        {
            COSBase directC = c;
            if (c instanceof COSObject)
            {
                directC = ((COSObject) c).getObject();
            }
            if (name.equals(directC))
            {
                this.getCOSDictionary().setItem(key, null);
            }
        }
    }

    /**
     * Returns the revision number (R).
     * 
     * @return the revision number
     */
    public int getRevisionNumber()
    {
        return this.getCOSDictionary().getInt(COSName.R, 0);
    }

    /**
     * Sets the revision number (R).
     * 
     * @param revisionNumber the revision number
     */
    public void setRevisionNumber(int revisionNumber)
    {
        this.getCOSDictionary().setInt(COSName.R, revisionNumber);
    }

    /**
     * Returns the title (T).
     * 
     * @return the title
     */
    public String getTitle()
    {
        return this.getCOSDictionary().getString("T");
    }

    /**
     * Sets the title (T).
     * 
     * @param title the title
     */
    public void setTitle(String title)
    {
        this.getCOSDictionary().setString("T", title);
    }

    /**
     * Returns the language (Lang).
     * 
     * @return the language
     */
    public String getLanguage()
    {
        return this.getCOSDictionary().getString("Lang");
    }

    /**
     * Sets the language (Lang).
     * 
     * @param language the language
     */
    public void setLanguage(String language)
    {
        this.getCOSDictionary().setString("Lang", language);
    }

    /**
     * Returns the alternate description (Alt).
     * 
     * @return the alternate description
     */
    public String getAlternateDescription()
    {
        return this.getCOSDictionary().getString("Alt");
    }

    /**
     * Sets the alternate description (Alt).
     * 
     * @param alternateDescription the alternate description
     */
    public void setAlternateDescription(String alternateDescription)
    {
        this.getCOSDictionary().setString("Alt", alternateDescription);
    }

    /**
     * Returns the expanded form (E).
     * 
     * @return the expanded form
     */
    public String getExpandedForm()
    {
        return this.getCOSDictionary().getString("E");
    }

    /**
     * Sets the expanded form (E).
     * 
     * @param expandedForm the expanded form
     */
    public void setExpandedForm(String expandedForm)
    {
        this.getCOSDictionary().setString("E", expandedForm);
    }

    /**
     * Returns the actual text (ActualText).
     * 
     * @return the actual text
     */
    public String getActualText()
    {
        return this.getCOSDictionary().getString("ActualText");
    }

    /**
     * Sets the actual text (ActualText).
     * 
     * @param actualText the actual text
     */
    public void setActualText(String actualText)
    {
        this.getCOSDictionary().setString("ActualText", actualText);
    }

    /**
     * Returns the standard structure type, the actual structure type is mapped
     * to in the role map.
     * 
     * @return the standard structure type
     */
    public String getStandardStructureType()
    {
        String type = this.getStructureType();
        String mappedType;
        while (true)
        {
            mappedType = this.getRoleMap().get(type);
            if ((mappedType == null) || type.equals(mappedType))
            {
                break;
            }
            type = mappedType;
        }
        return type;
    }

    /**
     * Appends a marked-content sequence kid.
     * 
     * @param markedContent the marked-content sequence
     */
    public void appendKid(PDMarkedContent markedContent)
    {
        this.appendKid(COSInteger.get(markedContent.getMCID()));
    }

    /**
     * Appends a marked-content reference kid.
     * 
     * @param markedContentReference the marked-content reference
     */
    public void appendKid(PDMarkedContentReference markedContentReference)
    {
        this.appendObjectableKid(markedContentReference);
    }

    /**
     * Appends an object reference kid.
     * 
     * @param objectReference the object reference
     */
    public void appendKid(PDObjectReference objectReference)
    {
        this.appendObjectableKid(objectReference);
    }

    /**
     * Inserts a marked-content identifier kid before a reference kid.
     * 
     * @param markedContentIdentifier the marked-content identifier
     * @param refKid the reference kid
     */
    public void insertBefore(COSInteger markedContentIdentifier, Object refKid)
    {
        this.insertBefore(markedContentIdentifier, refKid);
    }

    /**
     * Inserts a marked-content reference kid before a reference kid.
     * 
     * @param markedContentReference the marked-content reference
     * @param refKid the reference kid
     */
    public void insertBefore(PDMarkedContentReference markedContentReference, Object refKid)
    {
        this.insertBefore(markedContentReference, refKid);
    }

    /**
     * Inserts an object reference kid before a reference kid.
     * 
     * @param objectReference the object reference
     * @param refKid the reference kid
     */
    public void insertBefore(PDObjectReference objectReference, Object refKid)
    {
        this.insertBefore(objectReference, refKid);
    }

    /**
     * Removes a marked-content identifier kid.
     * 
     * @param markedContentIdentifier the marked-content identifier
     */
    public void removeKid(COSInteger markedContentIdentifier)
    {
        this.removeKid((COSBase) markedContentIdentifier);
    }

    /**
     * Removes a marked-content reference kid.
     * 
     * @param markedContentReference the marked-content reference
     */
    public void removeKid(PDMarkedContentReference markedContentReference)
    {
        this.removeObjectableKid(markedContentReference);
    }

    /**
     * Removes an object reference kid.
     * 
     * @param objectReference the object reference
     */
    public void removeKid(PDObjectReference objectReference)
    {
        this.removeObjectableKid(objectReference);
    }


    /**
     * Returns the structure tree root.
     * 
     * @return the structure tree root
     */
    private PDStructureTreeRoot getStructureTreeRoot()
    {
        PDStructureNode parent = this.getParent();
        while (parent instanceof PDStructureElement)
        {
            parent = ((PDStructureElement) parent).getParent();
        }
        if (parent instanceof PDStructureTreeRoot)
        {
            return (PDStructureTreeRoot) parent;
        }
        return null;
    }

    /**
     * Returns the role map.
     * 
     * @return the role map
     */
    private Map<String, String> getRoleMap()
    {
        PDStructureTreeRoot root = this.getStructureTreeRoot();
        if (root != null)
        {
            return root.getRoleMap();
        }
        return null;
    }

}
