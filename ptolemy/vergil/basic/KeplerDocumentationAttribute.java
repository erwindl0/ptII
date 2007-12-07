/* A documentation attribute for Kepler.

 Copyright (c) 2007 The Regents of the University of California.
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

 */

package ptolemy.vergil.basic;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 A Documentation attribute for actors.
 This class is used by Kepler so that the DocViewer can access kepler
 specfic actor metadata based documentation.
 @author Chad Berkley
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)

 */
public class KeplerDocumentationAttribute extends Attribute implements
        Configurable {

    /** Construct a Kepler documentation attribute.  */
    public KeplerDocumentationAttribute() {
        super();
    }

    /**
     * Construct a Kepler documentation attribute.
     *
     *@param container The container.
     *@param name The name of the Kepler documentation attribute.
     *@exception IllegalActionException If thrown by the superclass.
     *@exception NameDuplicationException  If thrown by the superclass.
     */
    public KeplerDocumentationAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Construct a Kepler documentation attribute.
     *
     *@param workspace The workspace in which the object is created.
     */
    public KeplerDocumentationAttribute(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Populates the members of KeplerDocumentationAttribute from
     * another given KeplerDccumentationAtttribute.
     * @param da The DocumentationAttribute from which to copy attributes.
     */
    public void createInstanceFromExisting(KeplerDocumentationAttribute da) {
        //System.out.println("da att list: " + da.attributeList());
        //need to get: description, author, version, userleveldocumentation, ports, properties
        Iterator itt = da.attributeList().iterator();
        while (itt.hasNext()) {
            ConfigurableAttribute att = (ConfigurableAttribute) itt.next();
            String attName = att.getName();
            if (attName.equals("description")) {
                this.description = att.getConfigureText();
            } else if (attName.equals("author")) {
                this.author = att.getConfigureText();
            } else if (attName.equals("version")) {
                this.version = att.getConfigureText();
            } else if (attName.equals("userLevelDocumentation")) {
                this.userLevelDocumentation = att.getConfigureText();
            } else if (attName.indexOf("port:") != -1) { //add to the port hash
                String portName = attName.substring(attName.indexOf(":") + 1,
                        attName.length());
                String portDesc = att.getConfigureText();
                portHash.put(portName, portDesc);
            } else if (attName.indexOf("prop:") != -1) { //add to the prop hash
                String propName = attName.substring(attName.indexOf(":") + 1,
                        attName.length());
                String propDesc = att.getConfigureText();
                propertyHash.put(propName, propDesc);
            }
        }
    }

    /** Write a MoML description of this object with the specified
     *  indentation depth and with the specified name substituting
     *  for the name of this object.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        createInstanceFromExisting(this);
        StringBuffer sb = new StringBuffer();
        sb.append("<property name=\"" + name + "\" class=\"" + getClassName());
        sb.append("\">\n");
        //description
        sb
                .append("<property name=\"description\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">");
        sb.append("<configure>" + description + "</configure>");
        sb.append("</property>\n");

        sb
                .append("<property name=\"author\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">");
        sb.append("<configure>" + author + "</configure>");
        sb.append("</property>\n");

        sb
                .append("<property name=\"version\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">");
        sb.append("<configure>" + version + "</configure>");
        sb.append("</property>\n");

        sb
                .append("<property name=\"userLevelDocumentation\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">");
        sb.append("<configure>" + userLevelDocumentation + "</configure>");
        sb.append("</property>\n");

        Enumeration portKeys = portHash.keys();
        while (portKeys.hasMoreElements()) {
            String key = (String) portKeys.nextElement();
            String val = (String) portHash.get(key);
            sb
                    .append("<property name=\"port:"
                            + key
                            + "\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">");
            sb.append("<configure>" + val + "</configure>");
            sb.append("</property>\n");
        }

        Enumeration propKeys = propertyHash.keys();
        while (propKeys.hasMoreElements()) {
            String key = (String) propKeys.nextElement();
            String val = (String) propertyHash.get(key);
            sb
                    .append("<property name=\"prop:"
                            + key
                            + "\" class=\"ptolemy.kernel.util.ConfigurableAttribute\">");
            sb.append("<configure>" + val + "</configure>");
            sb.append("</property>\n");
        }

        sb.append("</property>");
        output.write(sb.toString());
    }

    /**
     * Exports this documentation attribute as docML.
     * @return The docML
     */
    public String toDocML() {
        createInstanceFromExisting(this);
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" standalone=\"yes\"?>\n");
        sb.append("<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML 1//EN\"");
        sb
                .append("\"http://ptolemy.eecs.berkeley.edu/xml/dtd/DocML_1.dtd\">\n");
        sb
                .append("<doc name=\"" + docName + "\" class=\"" + docClass
                        + "\">\n");
        sb.append("<description>\n" + userLevelDocumentation
                + "\n</description>\n");
        sb.append("<author>" + author + "</author>\n");

        Enumeration portItt = portHash.keys();
        while (portItt.hasMoreElements()) {
            String name = (String) portItt.nextElement();
            String desc = (String) portHash.get(name);
            sb.append("<port name=\"" + name + "\">");
            sb.append(desc).append("</port>\n");
        }

        Enumeration propItt = propertyHash.keys();
        while (propItt.hasMoreElements()) {
            String name = (String) propItt.nextElement();
            String desc = (String) propertyHash.get(name);
            sb.append("<property name=\"" + name + "\">");
            sb.append(desc).append("</property>\n");
        }

        sb.append("</doc>\n");
        return sb.toString();
    }

    /**
     * Return a docAttribute with the available kepler documentation.
     * Returns null if an error prevents the doc attribute from being
     * created.
     * @param target The container for the DocAttribute
     * @return The DocAttribute.
     */
    public DocAttribute getDocAttribute(NamedObj target) {
        createInstanceFromExisting(this);
        try {
            DocAttribute da = new DocAttribute(target.workspace());
            da.setContainer(target);
            //da.setName("keplerFormattedPTIIDocumentation");
            da.author = new StringAttribute(da, "author");
            da.author.setExpression(author);
            da.version = new StringAttribute(da, "version");
            da.version.setExpression(version);
            da.since = new StringAttribute(da, "since");
            da.since.setExpression("");
            da.description = new StringParameter(da, "description");
            da.description.setExpression(userLevelDocumentation);

            //add ports and params
            Enumeration portItt = portHash.keys();
            while (portItt.hasMoreElements()) {
                String name = (String) portItt.nextElement();
                String desc = (String) portHash.get(name);
                StringAttribute sa = new StringAttribute(da, name + " (port)");
                sa.setExpression(desc);
            }

            Enumeration propItt = propertyHash.keys();
            while (propItt.hasMoreElements()) {
                String name = (String) propItt.nextElement();
                String desc = (String) propertyHash.get(name);
                StringParameter sp = new StringParameter(da, name
                        + " (parameter)");
                sp.setExpression(desc);
            }

            return da;
        } catch (Exception e) {
            System.out
                    .println("Error creating docAttribute: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Method for configurable.
     * In this class, we do nothing.
     */
    public void updateContent() throws InternalErrorException {
        //do nothing
    }

    ////////////////////////////////////////////////////////////////////////
    ///////////////////// Getters and Setters //////////////////////////////

    /** Set the name of this document.
     *  @param name The name of this document.
     */
    public void setDocName(String name) {
        this.docName = name;
    }

    /** Set the name of this docClass.
     *  @param className The name of this docClass.
     */
    public void setDocClass(String className) {
        this.docClass = className;
    }

    /** Set the description.
     *  @param description The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Set the author.
     *  @param author The author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /** Set the version.
     *  @param version The version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /** Set the user level documentation.
     *  @param userLevelDocumentation The user level documentation.
     */
    public void setUserLevelDocumentation(String userLevelDocumentation) {
        this.userLevelDocumentation = userLevelDocumentation;
    }

    /** Set the port hash.
     *  @param portHash The port hash.
     */
    public void setPortHash(Hashtable portHash) {
        this.portHash = portHash;
    }

    /** Add port to the port hashtable.
     *  @param name The name of the port.
     *  @param value A String representing the port.
     */
    public void addPort(String name, String value) {
        portHash.put(name, value);
    }

    /** Set the property hashtable.
     *  @param propertyHash The property hashtable.
     */
    public void setPropertyHash(Hashtable propertyHash) {
        this.propertyHash = propertyHash;
    }

    /** Add a property to the property hashtable.
     *  @param name The name of the property.
     *  @param value A string representing the propety.
     */
    public void addProperty(String name, String value) {
        propertyHash.put(name, value);
    }

    /** Configure this documentation attribute.
     *  @param base Currently ignored.
     *  @param source The source of this configuration.
     *  @param text The configuration text.
     */
    public void configure(java.net.URL base, String source, String text) {
        this.source = source;
        this.text = text;
    }

    /** Get the configuration source.
     *  @return The configuration source.
     */
    public String getConfigureSource() {
        return source;
    }

    /** Get the configuration text.
     *  @return The configuration text
     */
    public String getConfigureText() {
        return text;
    }

    //////////////////////////////////////////////////////////////////////
    ///////////                    Private Members                ////////

    //members for Configurable
    private String source;

    private String text;

    //members for DocumenationAttribute
    private String docName;

    private String docClass;

    private String description;

    private String author;

    private String version;

    private String userLevelDocumentation;

    private Hashtable portHash = new Hashtable();

    private Hashtable propertyHash = new Hashtable();
}
