/* A Factory to create useful objects from XMLElements.

 Copyright (c) 1998-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;

import ptolemy.kernel.util.*;
import java.util.*;
import collections.*;
import java.io.*;
import ptolemy.schematic.xml.*;
import java.net.*;

//////////////////////////////////////////////////////////////////////////
//// XMLObjectFactory
/**
An XMLObjectFactory supports the creation of useful objects (IconLibraries, 
DomainLibraries, etc.) from XMLElements that represent the root elements
of the correspinding PTML file.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PTMLObjectFactory {

    /** 
     * Create the root EntityLibrary from an XMLElement that was parsed from 
     * the root EntityLibrary.  
     * @exception IllegalActionException If the XML element does 
     * not have a type of "entitylibrary".
     * @exception NameDuplicationException If the XML element contains two
     * named objects with the same name.
     */
    public static EntityLibrary createEntityLibrary(XMLElement e, 
            IconLibrary iconroot) 
            throws IllegalActionException, NameDuplicationException {

        _checkElement(e, "entitylibrary");

        EntityLibrary entitylibrary = new EntityLibrary();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("entity")) {
                // if it's an Entity, then create it, 
                // and add it to the list of entitys.
                entitylibrary.addEntity(
                        _createEntityTemplate(child, iconroot));
            } else if(etype.equals("sublibrary")) {
                // if it's a sublibrary, then add it to the 
                // list of sublibraries.

                String url = "";
                try {
                    String offset = child.getAttribute("url");
                    XMLElement sublibtree = _parseSubURL(e, offset);
                    EntityLibrary sublib = 
                        createEntityLibrary(sublibtree, iconroot); 
                    entitylibrary.addSubLibrary(sublib);
                }
                catch (Exception ex) {
                    System.out.println("Couldn't parse entitylibrary " +
                            "from url " + url);
                    System.out.println(ex.getMessage());
                }

            } else if(etype.equals("description")) {
                entitylibrary.setDocumentation(child.getPCData());
            } else {
                _unknownElementType(child, "entitylibrary");
            }
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    entitylibrary.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(e, n);
            }
        }
        return entitylibrary;
            
    }

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception IllegalActionException If the XML element does not 
     * have a type of "iconlibrary"
     * @exception NameDuplicationException If the XML element contains two
     * named objects with the same name.
     */
    public static IconLibrary createIconLibrary(XMLElement e) 
            throws IllegalActionException, NameDuplicationException {

        _checkElement(e, "iconlibrary");

        IconLibrary iconlibrary = new IconLibrary();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("icon")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
                iconlibrary.addIcon(_createIcon(child));
            } else if(etype.equals("sublibrary")) {
                // if it's a sublibrary, then add it to the 
                // list of sublibraries.

                String url = "";
                try {
                    String offset = child.getAttribute("url");
                    XMLElement sublibtree = _parseSubURL(e, offset);
                    IconLibrary sublib = createIconLibrary(sublibtree); 
                    iconlibrary.addSubLibrary(sublib);
                }
                catch (Exception ex) {
                    System.out.println("Couldn't parse iconlibrary from url "+
                            url);
                    System.out.println(ex.getMessage());
                }

            } else if(etype.equals("description")) {
                iconlibrary.setDocumentation(child.getPCData());
            } else if(etype.equals("terminalstyle")) {
                iconlibrary.addTerminalStyle(_createTerminalStyle(child));
            } else {
                _unknownElementType(child, "iconlibrary");
            }
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    iconlibrary.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(e, n);
            }
        }

        return iconlibrary;
            
    }

    /** 
     * Create a Schematic from the given XML element and entity library.  
     * @exception IllegalActionException If the XML element does not 
     * have a type of "schematic"
     * @exception NameDuplicationException If the XML element contains two
     * named objects with the same name.
     */
    public static Schematic createSchematic(XMLElement e,
            EntityLibrary lib) 
            throws IllegalActionException, NameDuplicationException {

        _checkElement(e, "schematic");
        Schematic schematic = new Schematic();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement) children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("port")) {
                // if it's a port, then create it, 
                // and add it to the list of ports.
                // FIXME
                //schematic.addPort(_createSchematicPort(child));
            } else if(etype.equals("terminal")) {
                // if it's a terminal, then create it, 
                // and add it to the list of terminals.
                schematic.addTerminal(_createSchematicTerminal(child, null));
            } else if(etype.equals("entity")) {
                // if it's a entity, then create it, 
                // and add it to the list of entities.
                schematic.addEntity(_createSchematicEntity(child, lib));
            } else if(etype.equals("relation")) {
                // if it's a relation, then create it, 
                // and add it to the list of relations.
                schematic.addRelation(
                        _createSchematicRelation(child, schematic));
            } else if(etype.equals("description")) {
                schematic.setDocumentation(child.getPCData());
            } else {
                _unknownElementType(child, "iconlibrary");
            }
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    schematic.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(e, n);
            }
        }

        return schematic;            
    }

    /** 
     * Parse an Icon Library from the given url string. 
     * Create a PTMLParser and point it at the URL, then call createIconLibrary
     * on the returned tree of XMLElements.
     */
    public static IconLibrary parseIconLibrary(URL url) 
            throws Exception {

        String urlstring = url.toString();        
        XMLElement root = _parser.parse(urlstring);
        return PTMLObjectFactory.createIconLibrary(root);
    }

    /** 
     * Parse an entity library from the given url string. 
     * Point the PTMLParser at the URL, then call createIconLibrary
     * on the returned tree of XMLElements.  All Icons and TerminalStyles
     * should be contained within the given IconLibrary, which will be used
     * to resolve references.
     */
    public static EntityLibrary parseEntityLibrary(URL url, 
            IconLibrary iconLib) 
            throws Exception {

        String urlstring = url.toString();        
        XMLElement root = _parser.parse(urlstring);
        return PTMLObjectFactory.createEntityLibrary(root, iconLib);
    }

    /** 
     * Parse an schematic from the given url string. 
     * Point the PTMLParser at the URL, then call createSchematic
     * on the returned tree of XMLElements.  All entities
     * should be contained within the given EntityLibrary, which will be used
     * to resolve references.
     */
    public static Schematic parseSchematic(URL url, 
            EntityLibrary entityLib) 
            throws Exception {

        String urlstring = url.toString();        
        XMLElement root = _parser.parse(urlstring);
        return PTMLObjectFactory.createSchematic(root, entityLib);
    }

    /** 
     * Create the root IconLibrary from an XMLElement that was parsed from 
     * the root IconLibrary.  
     * @exception If the XML element does not have a type of "iconlibrary"
     */
    /*    public static DomainLibrary createDomainLibrary(XMLElement e)
    throws IllegalActionException {
        
    }*/
   
    /**
     * Check the validity of the XML element.  This method is used to 
     * check external inputs to this class, which should not be flagged
     * as fatal errors (although the calling code may interpret them as such).
     *
     * @exception IllegalActionException If the element is null, or its
     * type is not equal to the given type.
     */
    private static void _checkElement(XMLElement e, String elementtype) 
            throws IllegalActionException {
        if(e == null) {
            throw new IllegalActionException("_checkElement: " +
                    "Received null XMLElement");
        }
        if(!e.getElementType().equals(elementtype)) {
            throw new IllegalActionException("createIconLibrary: " +
                    "Element type " + e.getElementType() + 
                    "differs from expected " + elementtype + ".");
        }
    }

    private static EntityTemplate _createEntityTemplate(XMLElement e, 
            IconLibrary iconroot)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "entity");

        EntityTemplate entity = new EntityTemplate();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("description")) {
                entity.setDocumentation(child.getPCData());
            } else {
                _unknownElementType(child, "entity");
            }    
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                entity.setName(_getString(e, n));
            } else if (n.equals("icon")) {
                Icon icon = iconroot.findIcon(_getString(e, n));
                entity.setIcon(icon);
            } else if (n.equals("terminalstyle")) {
                TerminalStyle terminalstyle =
                    iconroot.findTerminalStyle(_getString(e, n));
                entity.setTerminalStyle(terminalstyle);
            } else {
                _unknownAttribute(e, n);
            }
        }
        return entity;
    }

    private static GraphicElement _createGraphicElement(XMLElement e)
        throws IllegalActionException {

        String name = e.getElementType();
        GraphicElement graphic = new GraphicElement(name);
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(child, "GraphicElement");
        }
        
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            String v = e.getAttribute(n);        
            graphic.setAttribute(n, v);
        }

        graphic.setAttribute("content", e.getPCData());
        return graphic;
    }

    private static Icon _createIcon(XMLElement e)
        throws IllegalActionException {

        _verifyElement(e, "icon");

        Icon icon = new Icon();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("tclscript")) {
                // if it's an Icon, then create it, 
                // and add it to the list of icons.
            } else if(etype.equals("description")) {
                icon.setDocumentation(child.getPCData());
            } else if(etype.equals("terminal")) {
            } else if(etype.equals("xmlgraphic")) {
                Enumeration graphics = child.childElements();
                while(graphics.hasMoreElements()) {
                    XMLElement graphic = (XMLElement)graphics.nextElement();
                    String gtype = graphic.getElementType();                
                    GraphicElement g = _createGraphicElement(graphic);
                    icon.addGraphicElement(g);
                }
            } else {
                _unknownElementType(child, "icon");
            }    
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                try {
                    icon.setName(_getString(e, n));
                } catch (Exception ex) {};
            } else {
                _unknownAttribute(e, n);
            }
        }
        return icon;
    }

    private static SchematicEntity _createSchematicEntity(XMLElement e,
            EntityLibrary entityLib)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "entity");

        EntityTemplate template = null;
        if(e.hasAttribute("template")) {
            String templateString = _getString(e, "template");
            template = entityLib.findEntityTemplate(templateString);
        } else {
            throw new IllegalActionException(
                    "SchematicEntity has no template.");
        }

        SchematicEntity entity = 
            new SchematicEntity(template.getName(), template);
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(child, "entity");
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                entity.setName(_getString(e, n));
            } else if (n.equals("x")) {
                entity.setX(_getDouble(e, n));
            } else if (n.equals("y")) {
                entity.setY(_getDouble(e, n));
            } else if (n.equals("template")) {
                //ignore
            } else {
                _unknownAttribute(e, n);
            }
        }
        return entity;
    }

    private static SchematicLink _createSchematicLink(XMLElement e, 
            Schematic schematic)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "link");

        SchematicLink entity = new SchematicLink();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(child, "entity");
        }

        // FIXME hmm... I bet it is possible for the terminal that 
        // Schematic Terminal returns to not be in this relation.  This is
        // Probably bad, but we don't check for it.
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("to")) {
                entity.setTo(
                        schematic.findSchematicTerminal(_getString(e, n)));
            } else if (n.equals("from")) {
                entity.setFrom(
                        schematic.findSchematicTerminal(_getString(e, n)));
            } else {
                _unknownAttribute(e, n);
            }
        }
        return entity;
    }

    private static SchematicRelation _createSchematicRelation(XMLElement e, 
            Schematic schematic)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "relation");

        SchematicRelation relation = new SchematicRelation();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("link")) {
                relation.addLink(_createSchematicLink(child, schematic));
            } else if(etype.equals("terminal")) {
                relation.addTerminal(_createSchematicTerminal(child, null));
            } else 
                _unknownElementType(child, "relation");
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                relation.setName(_getString(e, n));
            } else if (n.equals("width")) {
                relation.setWidth(_getInt(e, n));
            } else {
                _unknownAttribute(e, n);
            }
        }
        return relation;
    }

    //FIXME do we need this terminalstyle?
    private static SchematicTerminal _createSchematicTerminal(XMLElement e,
            TerminalStyle terminalStyle)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "terminal");

        SchematicTerminal terminal = new SchematicTerminal("terminal", null);
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(child, "terminal");
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                terminal.setName(_getString(e, n));
            } else if (n.equals("x")) {
                terminal.setX(_getDouble(e, n));
            } else if (n.equals("y")) {
                terminal.setY(_getDouble(e, n));
            } else {
                _unknownAttribute(e, n);
            }
        }
        return terminal;
    }

    private static Terminal _createTerminal(XMLElement e)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "terminal");

        Terminal terminal = new Terminal();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            _unknownElementType(child, "terminal");
        }

        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                terminal.setName(_getString(e, n));
            } else if (n.equals("x")) {
                terminal.setX(_getDouble(e, n));
            } else if (n.equals("y")) {
                terminal.setY(_getDouble(e, n));
            } else {
                _unknownAttribute(e, n);
            }
        }
        return terminal;
    }

    private static TerminalStyle _createTerminalStyle(XMLElement e)
        throws IllegalActionException, NameDuplicationException {

        _verifyElement(e, "terminalstyle");

        TerminalStyle terminalStyle = new TerminalStyle();
        Enumeration children = e.childElements();
        while(children.hasMoreElements()) {
            XMLElement child = (XMLElement)children.nextElement();
            String etype = child.getElementType();
            if(etype.equals("terminal")) {
                terminalStyle.addTerminal(_createTerminal(child));
            } else {
                _unknownElementType(child, "terminalstyle");
            }    
        }
        Enumeration attributes = e.attributeNames();
        while(attributes.hasMoreElements()) {
            String n = (String) attributes.nextElement();
            if (n.equals("name")) {
                terminalStyle.setName(_getString(e, n));
            } else {
                _unknownAttribute(e, n);
            }
        }
        return terminalStyle;
    }

    /** Return a boolean corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     * @throws IllegalActionException If the value of the XML attribute
     *  is not "true" or "false"
     */
    private static boolean _getBoolean(XMLElement e, String name) 
        throws IllegalActionException {
        String v = e.getAttribute(name);
        if(v == "true") 
            return true;
        else if(v == "false")
            return false;
        else throw new IllegalActionException(
                "Attribute " + name + " with value " + v + 
                    " does not represent a valid boolean.");
    }
   
    /** Return a double corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     * @throws IllegalActionException If the value of the XML attribute
     *  does not represent a valid double
     */
    private static double _getDouble(XMLElement el, String name) 
        throws IllegalActionException {
        String v = el.getAttribute(name);
        try {
            Double d = new Double(v);
            return d.doubleValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(
                    "Attribute " + name + " with value " + v + 
                    " does not represent a valid double.");
        }
    }

   /** Return an integer corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     * @throws IllegalActionException If the value of the XML attribute
     *  does not represent a valid int
     */
    private static int _getInt(XMLElement el, String name) 
        throws IllegalActionException {
        String v = el.getAttribute(name);
        try {
            Integer d = new Integer(v);
            return d.intValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(
                    "Attribute " + name + " with value " + v + 
                    " does not represent a valid double.");
        }
    }

    /** Return a string corresponding to the value of the attribute with
     * the given name in the given XMLElement.
     */
    private static String _getString(XMLElement el, String name) {
        String v = el.getAttribute(name);
        return v;
    }

    /** Parse the xml file that is at a relative location to the location 
     * of the given XMLElement given by the urloffset.
     */
    private static XMLElement _parseSubURL(XMLElement e, String urloffset) 
            throws Exception {
        URL baseurl = new URL(e.getXMLFileLocation());        
        URL newurl = new URL(baseurl, urloffset);
        String url = newurl.toString();
        
        XMLElement sublibtree = _parser.parse(url);
        return sublibtree;
    }

    /** 
     * Print a message about the unknown element.
     */
    private static void _unknownElementType(XMLElement el, String parent) {
            String etype = el.getElementType();
                System.out.println("Unrecognized element type = " +
                    etype + " found in " + parent);
    }
        
    /** 
     * Print a message about the unknown element
     */
    private static void _unknownAttribute(XMLElement el, String name) {
        System.out.println("Unrecognized attribute (" + name + "=" +
                _getString(el, name) + ") found in " + el.getElementType());
    }
        
    /**
     * Check the validity of the XML element.   This method is very similar 
     * to _checkElement, except that it is used internally to check state
     * that should already be true, unless the code is broken.
     *
     * @exception InternalErrorException If the element is null, or its
     * type is not equal to the given type.
     */
     private static void _verifyElement(XMLElement e, String elementtype) {
        if(e == null) {
            throw new InternalErrorException("_checkElement: " +
                    "Received null XMLElement");
        }
        if(!e.getElementType().equals(elementtype)) {
            throw new InternalErrorException("createIconLibrary: " +
                    "Element type " + e.getElementType() + 
                    " differs from expected " + elementtype + ".");
        }
     }

    private static PTMLParser _parser = new PTMLParser();
}

