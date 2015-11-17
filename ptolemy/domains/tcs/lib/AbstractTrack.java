/* A model of a track in Train control systems.
 
 Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.domains.tcs.lib;



import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.domains.tcs.kernel.TCSDirector;
import ptolemy.domains.tcs.kernel.Rejecting;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;
import ptolemy.data.type.Type;

/** A model of a track in Train  control systems.
 *  This track can have no more than one Train in transit.
 *  If there is one in transit, then it rejects all inputs.
 *  @author Maryam Bagheri
 */
public class AbstractTrack extends  TypedAtomicActor implements Rejecting{

    public AbstractTrack(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
       
        
        input = new TypedIOPort(this, "input", true, false);
                
        output=new TypedIOPort(this, "output", false, true); 
        output.setTypeEquals(new RecordType(_lables, _types));
        
        
        trackId= new Parameter(this, "trackId");
        trackId.setTypeEquals(BaseType.INT);
        trackId.setExpression("-1");
        
        lineSymbol= new Parameter(this, "lineSymbol");
        lineSymbol.setTypeEquals(BaseType.STRING);
       
        broken=new Parameter(this, "broken");
        broken.setTypeEquals(BaseType.BOOLEAN);

        // Create an icon for this Track node.
        EditorIcon node_icon = new EditorIcon(this, "_icon");
        
        //icon for the broken zone
        _circle = new EllipseAttribute(node_icon, "_circleShap");
        _circle.centered.setToken("true");
        _circle.width.setToken("40");
        _circle.height.setToken("40");
        _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _circle.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        
        
        //icon of train
        _shape = new ResizablePolygonAttribute(node_icon, "_trainShape");
        _shape.centered.setToken("true");
        _shape.width.setToken("50");
        _shape.height.setToken("30");
        _shape.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _shape.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        
        //icon of track 
        _rectangle=new RectangleAttribute(node_icon, "_trackShape");
        _rectangle.centered.setToken("true");
        _rectangle.width.setToken("30");
        _rectangle.height.setToken("10");
        _rectangle.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
               
    }
    
    public TypedIOPort input;
    public TypedIOPort output;
    public Parameter trackId, lineSymbol, broken;
    
    @Override
    public boolean reject(Token token, IOPort port) {
        boolean unAvailable=(_inTransit != null || ((BooleanToken)_isBroken).booleanValue());
        return unAvailable;
    }
    
    @Override
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        Director director=getDirector();
        if (attribute == broken) {
             if(broken.getToken()!=null){
                _isBroken=broken.getToken();
                
                //change color of the storm zone
                if(((BooleanToken)_isBroken).booleanValue()==true)
                    _circle.fillColor.setToken("{1.0,0.2,0.2,1.0}");
                else
                    _circle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
                //
                ((TCSDirector)director).handleTrackAttributeChanged(this);
            }
        } else if(attribute==lineSymbol && lineSymbol.getToken()!=null){
            _symbol=((StringToken)lineSymbol.getToken()).stringValue();
            if(_symbol.length()>1)
                throw new IllegalActionException("Inappropriate line symbol");
            _color=((TCSDirector)director).getColor(_symbol);
            _rectangle.fillColor.setToken(_color);
            _rectangle.lineColor.setToken(_color);
        }
        else {
            super.attributeChanged(attribute);
        }
    }
    
    @Override
 public void declareDelayDependency() throws IllegalActionException {
     _declareDelayDependency(input, output, 0.0);         
 }
    
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = _director.getModelTime();
        if (currentTime.equals(_transitExpires) && _inTransit != null) {
            try{
                    output.send(0, _inTransit);
            }
            catch (NoRoomException ex){
             // Token rejected by the destination.
                if (!(_director instanceof TCSDirector)) {
                    throw new IllegalActionException(this, "Track must be used with an TCSDirector.");
                }
                double additionalDelay = ((TCSDirector)_director).handleRejectionWithDelay(this);
                if (additionalDelay < 0.0) {
                    throw new IllegalActionException(this, "Unable to handle rejection.");
                }
                _transitExpires = _transitExpires.add(additionalDelay);
                _director.fireAt(this, _transitExpires);
                return;
            }
            // Token has been sent successfully
            _inTransit = null;
            _changeIcon(_symbol);
            return; 
        }
        // Handle any input that have been accepted.
       
            if(input.hasNewToken(0))
            {
                // This if is for chacking safety. Instead of throwing exception we can write a record to the file.
                if(_inTransit!=null)
                {
                    throw new IllegalActionException("two train in one track");
                }
                //
                _inTransit=input.get(0);
                _setIconForTrain(((RecordToken)_inTransit).get("trainId"));
                double movingTime=((TCSDirector)_director).movingTimeOfTrain(_inTransit,_id);
                if(movingTime<=0.0)
                    throw new IllegalActionException("Minstake in calculating moving time of Train");
                _transitExpires=currentTime.add(movingTime);
                _director.fireAt(this, _transitExpires);
    }
    }
    
   

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _director=getDirector();
        _inTransit = null;
        _id=trackId.getToken();
        _isBroken=broken.getToken();
        if(_isBroken==null)
            _isBroken=(Token)(new BooleanToken(false));
       ((TCSDirector)_director).handleInitializedTrack(this);
	   if(lineSymbol.getToken()==null)
           _symbol="";
       else
           _symbol=((StringToken)lineSymbol.getToken()).stringValue();
		   
       _changeIcon(_symbol);
    }
    
  /** Change icon of the track from train to a rectangle.
     *  @param symbol The symbol of the line .
     *  @throws IllegalActionException
     */
    protected void _changeIcon(String symbol) throws IllegalActionException {
        _shape.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _shape.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        if(symbol.equals(""))
        {
            _rectangle.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
            _rectangle.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        }
        else{
            _color=((TCSDirector)_director).getColor(symbol);
            _rectangle.fillColor.setToken(_color);
            _rectangle.lineColor.setToken(_color);
        }
    }
    
	/** Change icon of the track from rectangle to icon of the train.
	*  @param idOfTrain The id of the train .
	*  @throws IllegalActionException
	 */
    private void _setIconForTrain(Token idOfTrain) throws IllegalActionException {
        int id=((IntToken)idOfTrain).intValue();
        _rectangle.fillColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _rectangle.lineColor.setToken("{0.0, 0.0, 0.0, 0.0}");
        _shape.fillColor.setToken(_setIcon(id));
        _shape.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
    }
    
    /** Determine the color of the track/train .
     *  @param id The train ID or -1 to indicate no train.
     *  @throws IllegalActionException
     */
    protected ArrayToken _setIcon(int id) throws IllegalActionException {
        ArrayToken color = _noTrainColor;
            if (id > -1) {
                Director _director=getDirector();
                color = ((TCSDirector)_director).handleTrainColor(id);
                if(color==null)
                    throw new IllegalActionException("Color for the train "+id+" has not been set");
            } 
        return color;
    }
    
    private DoubleToken _one = new DoubleToken(1.0);
    private Token[] _white = {_one, _one, _one, _one};
    private ArrayToken _noTrainColor = new ArrayToken(_white);
    
    protected ResizablePolygonAttribute _shape;
    private RectangleAttribute _rectangle;
    private EllipseAttribute _circle;
  //  private AttributeValueAttribute _value;
    private ArrayToken _color;
    
   

    private Token _inTransit;
    private Time _transitExpires;
    private Token _isBroken;
    private String _symbol;
    private Token _id;
    private Director _director;
    private String[] _lables={"trainId","trainSymbol","movingMap","trainSpeed","fuel","arrivalTimeToStation","dipartureTimeFromStation"};
    private Type[] _types={BaseType.INT,BaseType.STRING,new ArrayType(BaseType.STRING), BaseType.INT,BaseType.DOUBLE,BaseType.DOUBLE,BaseType.DOUBLE};
    
}
