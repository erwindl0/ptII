/*
 Class containing helper methods used by the ptserver and/or Homer.
 
 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

///////////////////////////////////////////////////////////////////
//// ServerUtility

/**
 * Class containing helper methods used by the ptserver and/or Homer.
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ServerUtility {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Create and initialize a new MoMLParser.
     * The parser would have BackwardCompatiblity and RemoveGraphicalClasses filters.
     * The RemoteGraphicalClasses would filter out only classes that are known not to be
     * portable to be portable to Android.
     * @return new MoMLParser with BackwardCompatibility and RemoveGraphicalClasses filters.
     */
    public static MoMLParser createMoMLParser() {
        MoMLParser parser = new MoMLParser(new Workspace());
        parser.resetAll();
        // TODO: is this thread safe?
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        // TODO either fork RemoveGraphicalClasses or make its hashmap non-static (?)
        RemoveGraphicalClasses filter = new RemoveGraphicalClasses();
        filter.remove("ptolemy.actor.lib.gui.ArrayPlotter");
        filter.remove("ptolemy.actor.lib.gui.SequencePlotter");
        filter.remove("ptolemy.actor.lib.gui.Display");
        filter.remove("ptolemy.actor.gui.style.CheckBoxStyle");
        filter.remove("ptolemy.actor.gui.style.ChoiceStyle");
        MoMLParser.addMoMLFilter(filter);
        return parser;
    }

    /**
     * Return the deep attribute list of the container.
     * @param container the container to process.
     * @return the deep attribute list of the container.
     */
    public static List<Attribute> deepAttributeList(NamedObj container) {
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        _deepAttributeList(container, attributeList);
        return attributeList;
    }

    /**
     * Find all remote attributes of the model and add them to the
     * remoteAttributeMap.
     * @param attributeList the attribute list to search
     * @param remoteAttributeMap the map where the attributes need to be added.
     */
    public static void findRemoteAttributes(List<Attribute> attributeList,
            HashMap<String, Settable> remoteAttributeMap) {
        for (Attribute attribute : attributeList) {
            if (ServerUtility.isRemoteAttribute(attribute)) {
                remoteAttributeMap.put(attribute.getFullName(),
                        (Settable) attribute);
            }
        }
    }

    /**
     * Return true if the attribute is marked as remote attribute, false otherwise.
     * @param attribute the child attribute of the attribute to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isRemoteAttribute(Attribute attribute) {
        if (attribute instanceof Settable) {
            Attribute isRemoteAttribute = attribute
                    .getAttribute(ProxyModelBuilder.REMOTE_OBJECT_TAG);
            if (isRemoteAttribute instanceof Parameter) {
                if (((Parameter) isRemoteAttribute).getExpression().equals(
                        ProxyModelBuilder.REMOTE_ATTRIBUTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return true if the attribute is marked as remote sink, false otherwise.
     * @param attribute the child attribute the source actor to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isTargetProxySink(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            if (parameter.getExpression().equals(
                    ProxyModelBuilder.PROXY_SINK_ATTRIBUTE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the attribute is marked as remote source, false otherwise.
     * @param attribute the child attribute of the source actor to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isTargetProxySource(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            if (parameter.getExpression().equals(
                    ProxyModelBuilder.PROXY_SOURCE_ATTRIBUTE)) {
                return true;
            }
        }
        return false;
    }

    public static CompositeEntity mergeModelWithLayout(CompositeEntity model,
            CompositeEntity layout, HashSet<Class<Attribute>> classesToMerge)
            throws IllegalActionException, NameDuplicationException {

        // Traverse all elements in the layout.
        for (ComponentEntity entity : (List<ComponentEntity>) layout
                .deepEntityList()) {
            _mergeElements(entity, model, classesToMerge);
        }
        _mergeElements(layout, model, classesToMerge);

        return model;
    }

    public static CompositeEntity mergeModelWithLayout(URL modelURL,
            URL layoutURL, HashSet<Class<Attribute>> classesToMerge)
            throws IllegalActionException, NameDuplicationException {
        CompositeEntity model = openModelFile(modelURL);
        CompositeEntity layout = openModelFile(layoutURL);
        return mergeModelWithLayout(model, layout, classesToMerge);
    }

    public static CompositeEntity mergeModelWithLayout(String modelURL,
            String layoutURL, HashSet<Class<Attribute>> classesToMerge)
            throws MalformedURLException, IllegalActionException,
            NameDuplicationException {
        return mergeModelWithLayout(new URL(modelURL), new URL(layoutURL),
                classesToMerge);
    }

    /** Open a MoML file, parse it, and the parsed model.
     * 
     *  @param url The url of the model.
     *  @return The parsed model.
     *  @exception IllegalActionException If the parsing failed.
     */
    public static CompositeEntity openModelFile(URL url)
            throws IllegalActionException {
        CompositeEntity topLevel = null;
        MoMLParser parser = new MoMLParser(new Workspace());
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        try {
            topLevel = (CompositeEntity) parser.parse(null, url);
        } catch (Exception e) {
            throw new IllegalActionException(null, e, "Unable to parse url: "
                    + url);
        }

        return topLevel;
    }

    /** Strips the first part of a compound element name, including the
     *  "." at the beginning.
     * 
     * @param fullName The compound name of an element.
     * @return The stripped name of the element, where the first part of
     * the compound name is removed, including the "." at the beginning.
     */
    public static String stripFullName(String fullName) {
        if (fullName.indexOf(".") == -1 || fullName.length() < 2) {
            return fullName;
        }
        return fullName.substring(fullName.substring(1).indexOf(".") + 2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     * Recursively find all attributes of the container.
     * @param container the container to check.
     * @param attributeList the attributeList that would contain attributes.
     */
    private static void _deepAttributeList(NamedObj container,
            List<Attribute> attributeList) {
        for (Object attributeObject : container.attributeList()) {
            Attribute attribute = (Attribute) attributeObject;
            attributeList.add(attribute);
            _deepAttributeList(attribute, attributeList);
        }
    }

    /** Merge the source object and all its deeply contained attributes with the target model.
     *  If an entity does not exists in the target model it will not be added, but each
     *  existing object's attributes that are not in the target model will be added.
     *  
     *  @param source The source object will potentially extra attributes that are not contained
     *  in the target model.
     *  @param targetModel The target model to be updated.
     *  @param classesToMerge Contains the classes of attributes to be included when merging. If
     *  this is null, every attribute not present in the target model will be added.
     *  @exception IllegalActionException If an attribute could not be added to the target model.
     */
    private static void _mergeElements(NamedObj source,
            CompositeEntity targetModel,
            HashSet<Class<Attribute>> classesToMerge)
            throws IllegalActionException {
        // Check if source and model is available.
        if (source == null || targetModel == null) {
            return;
        }

        // Check if source is either an entity or an attribute. Merging is only done
        // on those two types.
        if (!(source instanceof ComponentEntity || source instanceof Attribute)) {
            return;
        }

        // If the source is an entity, but is not originally in the target model, the merge
        // skips it.
        if (source instanceof ComponentEntity
                && targetModel.getEntity(stripFullName(source.getFullName())) == null) {
            return;
        }

        // At this point the source is either an entity that is also in the target model
        // or it's an attribute. In both cases the the merge will add all attributes that are
        // not present in the target model.
        List<Attribute> attributeList = ServerUtility.deepAttributeList(source);
        for (Attribute attribute : attributeList) {
            if (!(classesToMerge == null || classesToMerge.contains(attribute
                    .getClass()))) {
                return;
            }

            // Insert attribute into the target model. The attribute will no longer be
            // available in the source.
            try {
                // Get read and write access from the source to the target.
                source.workspace().getReadAccess();
                targetModel.workspace().getWriteAccess();

                if (source instanceof ComponentEntity) {
                    attribute.setContainer(targetModel
                            .getEntity(stripFullName(source.getFullName())));
                } else if (source instanceof Attribute) {
                    attribute.setContainer(targetModel
                            .getAttribute(stripFullName(source.getFullName())));
                }
            } catch (NameDuplicationException e) {
                // The attribute already exists. Since deepAttributeList returns all deeply
                // nested attributes too, the merge will look into attributes in lower levels
                // of the model. No need to do anything here.
            } finally {
                // Remove the accesses from the workspaces.
                targetModel.workspace().doneWriting();
                source.workspace().doneReading();
            }
        }
    }

}
