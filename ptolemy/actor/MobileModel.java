/* A composite actor that apply models dynamically.

 Copyright (c) 2003 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Const;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Entity;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.domains.de.kernel.DEDirector;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;

//////////////////////////////////////////////////////////////////////////
//// MobileModel
/**
This actor extends the TypedCompositeActor. It contains another model
that is defined as a Ptolemy composite actor to its inputs. Rather than
specified before executing, the inside model can be dynamically changed
either locally or remotely. Currently, the model that dynamically applied
to this actor is specified by a moml string from the <i>modelString<i>
input.

Currently, it only accepts models with one input and one output, and
requires the model name its input port as "input", output port as "output".

@author Yang Zhao
@version $Id$
@since Ptolemy II 3.0
*/
public class MobileModel extends TypedCompositeActor {

    /** Construct an actor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public MobileModel(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        input = new TypedIOPort(this, "input", true, false);
        modelString = new TypedIOPort(this, "modelString", true, false);
        modelString.setTypeEquals(BaseType.STRING);
        defaultValue = new Parameter(this, "defaultValue",
                new IntToken(0));
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(defaultValue);
        new DEDirector(this, "director");
        getMoMLInfo().className = "ptolemy.actor.MobileModel";
    }

    /** Construct an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MobileModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        modelString = new TypedIOPort(this, "modelString", true, false);
        modelString.setTypeEquals(BaseType.STRING);
        defaultValue = new Parameter(this, "defaultValue",
                new IntToken(0));
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(defaultValue);
        new DEDirector(this, "director");
        getMoMLInfo().className = "ptolemy.actor.MobileModel";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    /** The input port for incoming data to the inside model.
     */
    public TypedIOPort input;

    /** The input port for model changing request of the inside model.
     *  The type is string.
     */
    public TypedIOPort modelString;

    /** The output port for the result after firing the inside model
     * upon the incoming data. Notice that the type is determined by
     * the type of the <i>defultValue<i> parameter.
     */
    public TypedIOPort output;

    /** The default output token when there is no inside model
     *  defined. The default value is 0, and the default type is
     *  int. Notice that the type of the output port
     *  is determined by the type of this parameter.
     */
    public Parameter defaultValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        MobileModel newObject = (MobileModel)super.clone(workspace);
        // Set the type constraint.
        newObject.output.setTypeAtLeast(newObject.defaultValue);
        return newObject;
    }

    /** Save the model here if there is a new model to apply. and then call
     *  super.fire().
     *  @exception IllegalActionException If there is no director, or if
     *  the director's fire() method throws it, or if the actor is not
     *  opaque.
     */
    public void fire() throws IllegalActionException  {
        if (_debugging) {
            _debug("Invoking fire");
        }
        for (int i = 0; i < modelString.getWidth(); i++) {
            if (modelString.hasToken(i)) {
                StringToken str = null;
                try {
                    str = (StringToken) modelString.get(0);
                    //URL url = new URL(str.stringValue());
                    _model = (CompositeActor) _parser.parse(str.stringValue());
                    break;
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Problem parsing " + str.stringValue());
                }
            }
        }
        super.fire();
    }

    /** Initialize this actor. create a new moml parser for passing
     *  the applied model to it.
     *  @exception IllegalActionException If there is no director, or
     *  if the director's initialize() method throws it, or if the
     *  actor is not opaque.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Invoking init");
        }
        _model = null;

        try {
            _parser = new MoMLParser();
            _parser.setMoMLFilters(BackwardCompatibility.allFilters());
            //     _parser.addMoMLFilter(new RemoveGraphicalClasses());

            // When no model applied, output the default value.
            Const constActor = new Const(this, "Const");
            constActor.value.setExpression(defaultValue.getToken().toString());
            connect(input, constActor.trigger);
            connect(constActor.output, output);

        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "initialize() failed");
        }
        //connect(input, output);
        super.initialize();
    }

    /** Return true.
     */
    public boolean isOpaque() {
        return true;
    }

    /** Update the model here to achieve consistency.
     *  @exception IllegalActionException If there is no director,
     *  or if the director's postfire() method throws it, or if this actor
     *  is not opaque.
     */
    public boolean postfire() throws IllegalActionException {
        if (!_stopRequested && _model != null) {
            //remove the old model inside first, if there is one.
            String delete = _requestToRemoveAll(this);
            MoMLChangeRequest removeRequest = new MoMLChangeRequest(
                    this,            // originator
                    this,            // context
                    delete,          // MoML code
                    null);
            requestChange(removeRequest);

            // Add the entity represented by the new model string.

            // FIXME: the reason I do the change by two change request
            // is because when I tried to group them in one, I got a
            // parser error...

 //            MoMLChangeRequest request = new MoMLChangeRequest(
//                     this,            // originator
//                     this,          // context
//                     _model.exportMoML(), // MoML code
//                     null);
//             requestChange(request);

            //connect the model.
            StringWriter writer = new StringWriter();
            try {
                _model.exportMoML(writer, 1);
            } catch (Exception ex) {
                // FIXME: don't ignore?
            }

            String modelMoML =  writer.toString();
            String moml = "<group>\n" + modelMoML + "<relation name=\"newR1\" "
                    + "class=\"ptolemy.actor.TypedIORelation\">\n"
                    + "</relation>\n"
                    + "<relation name=\"newR2\" "
                    + "class=\"ptolemy.actor.TypedIORelation\">\n"
                    + "</relation>\n"
                    + "<link port=\"input\" relation=\"newR1\"/>\n"
                    + "<link port=\"" + _model.getName()
                    + ".input\" relation=\"newR1\"/>\n"
                    + "<link port=\"" + _model.getName()
                    + ".output\" relation=\"newR2\"/>\n"
                    + "<link port=\"output\" relation=\"newR2\"/>\n"
                    + "</group>";
            System.out.println("moml = " + moml);
            MoMLChangeRequest request2 = new MoMLChangeRequest(
                    this,            // originator
                    this,          // context
                    moml, // MoML code
                    null);
            requestChange(request2);
            if (_debugging) {
                _debug("issues change request to modify the model");
            }
            _model = null;
        }
        return super.postfire();
    }

    /** Return true if the actor either of its input port has token.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Invoking prefire");
        }
        if (input.hasToken(0) || modelString.hasToken(0)) {
            return super.prefire();
        }
        return false;
    }

    /** Clean up tha changes that have been made.
     *  @exception IllegalActionException If there is no director,
     *  or if the director's wrapup() method throws it, or if this
     *  actor is not opaque.
     */
    public void wrapup() throws IllegalActionException {
        // clean the inside content.
        String delete = _requestToRemoveAll(this);
        MoMLChangeRequest removeRequest = new MoMLChangeRequest(
                this,            // originator
                this,            // context
                delete,          // MoML code
                null);
        requestChange(removeRequest);
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Export the moml description of this.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            attribute.exportMoML(output, depth);
        }
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            port.exportMoML(output, depth);
        }
        output.write(exportLinks(depth, null));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    /** Construct a modl string for the composite actor to delete
     *  of its entities and relations.
     *  @param actor The composite actor.
     */
    private String _requestToRemoveAll(CompositeActor actor) {
        StringBuffer delete = new StringBuffer("<group>");
        Iterator entities = actor.entityList().iterator();
        while (entities.hasNext()) {
            Entity entity = (Entity) entities.next();
            delete.append("<deleteEntity name=\"" + entity.getName()
                    + "\" class=\"" + entity.getClass().getName() + "\"/>");
        }
        Iterator relations = actor.relationList().iterator();
        while (relations.hasNext()) {
            IORelation relation = (IORelation) relations.next();
            delete.append("<deleteRelation name=\"" + relation.getName()
                    + "\" class=\"ptolemy.actor.TypedIORelation\"/>");
        }
        delete.append("</group>");
        return delete.toString();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The inside model that contained by this actor.
     *
     */
    private CompositeActor _model;

    /** The moml parser for parsing the moml string of the inside model.
     *
     */
    private MoMLParser _parser;
}
