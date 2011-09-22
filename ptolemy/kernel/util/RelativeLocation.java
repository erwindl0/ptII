/** An attribute used to store a relative visual location. */
package ptolemy.kernel.util;

import java.util.List;

import ptolemy.kernel.CompositeEntity;

/** An attribute used to store a relative visual location.
 *  The location is relative to an object specified by
 *  the <i>relativeTo</i> attribute, which gives the name
 *  of an object that is expected to be contained by the
 *  container of the container of this attribute.
 *  In addition, the <i>relativeToElementName</i> specifies
 *  what kind of object this is relative to (an "entity",
 *  "property" (attribute), "port", or "relation").
 *  
 @author Edward A. Lee, Christian Motika, Miro Spoenemann
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class RelativeLocation extends Location {

    /** Construct an instance.
     *  @param container The container (the object to have a relative location).
     *  @param name The name of this instance.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public RelativeLocation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        relativeTo = new StringAttribute(this, "relativeTo");
        relativeToElementName = new StringAttribute(this, "relativeToElementName");
        relativeToElementName.setExpression("entity");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the object this location is relative to. */
    public StringAttribute relativeTo;
    
    /** The element name of the object this location is relative to.
     *  This defaults to "entity".
     */
    public StringAttribute relativeToElementName;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the location in some cartesian coordinate system.
     *  This method returns the relative location of the object.
     *  @return The location.
     *  @see #setLocation(double[])
     */
    public double[] getLocation() {
        double[] offset = super.getLocation();
        String relativeToValue = relativeTo.getExpression();
        if (relativeToValue.equals("")) {
            return offset;
        }
        double[] relativeToLocation = _getRelativeToLocation(relativeToValue);
        if (relativeToLocation != null) {
            double[] result = new double[offset.length];
            for (int i = 0; i < offset.length; i++) {
                result[i] = offset[i] + relativeToLocation[i];
            }
            return result;
        }
        // FIXME: If we get to here, then the relativeTo
        // object is gone... Need to record the old value.
        return offset;
    }
    
    /** Get the relative location, relative to the <i>relativeTo</i>
     *  object, if there is one, and otherwise return the absolute
     *  location.
     *  @return The relative location.
     *  @see #setLocation(double[])
     */
    public double[] getRelativeLocation() {
        return super.getLocation();
    }

    /** Set the location in some cartesian coordinate system, and notify
     *  the container and any value listeners of the new location. Setting
     *  the location involves maintaining a local copy of the passed
     *  parameter. No notification is done if the location is the same
     *  as before. This method propagates the value to any derived objects.
     *  @param location The location.
     *  @exception IllegalActionException If throw when attributeChanged()
     *  is called.
     *  @see #getLocation()
     */
    public void setLocation(double[] location) throws IllegalActionException {
        String relativeToValue = relativeTo.getExpression();
        if (relativeToValue.equals("")) {
            super.setLocation(location);
        }
        double[] relativeToLocation = _getRelativeToLocation(relativeToValue);
        if (relativeToLocation != null) {
            double[] result = new double[location.length];
            for (int i = 0; i < location.length; i++) {
                result[i] = location[i] - relativeToLocation[i];
            }
            super.setLocation(result);
            return;
        }
        // FIXME: If we get to here, then the relativeTo
        // object is gone... Need to record the old value.
        super.setLocation(location);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** If the <i>relativeTo</i> object exists, return its location.
     *  Otherwise, return null.
     *  @param relativeToName The name of the relativeTo object.
     *  @return The location of the relativeTo object, or null if it
     *   does not exist.
     */
    private double[] _getRelativeToLocation(String relativeToName) {
        NamedObj container = getContainer();
        if (container != null) {
            NamedObj containersContainer = container.getContainer();
            if (containersContainer instanceof CompositeEntity) {
                // The relativeTo object to not necessarily an Entity.
                String elementName = relativeToElementName.getExpression();
                NamedObj relativeToNamedObj = ((CompositeEntity)containersContainer).getEntity(relativeToName);
                if (elementName.equals("property")) {
                    relativeToNamedObj = ((CompositeEntity)containersContainer).getAttribute(relativeToName);
                } else if (elementName.equals("port")) {
                    relativeToNamedObj = ((CompositeEntity)containersContainer).getPort(relativeToName);
                } else if (elementName.equals("relation")) {
                    relativeToNamedObj = ((CompositeEntity)containersContainer).getRelation(relativeToName);
                }
                if (relativeToNamedObj != null) {
                    List<Locatable> locatables = relativeToNamedObj.attributeList(Locatable.class);
                    if (locatables.size() > 0) {
                        return locatables.get(0).getLocation();
                    }
                }
            }
        }
        return null;
    }
}
