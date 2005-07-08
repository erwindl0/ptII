package ptolemy.actor.corba.util;

/**
 * ptolemy/actor/corba/util/CorbaUnknownPortException.java
 * Generated by the IDL-to-Java compiler (portable), version "3.0"
 * from CorbaActor.idl
 * Thursday, January 18, 2001 7:07:58 PM PST
 */
public final class CorbaUnknownPortException extends
        org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    public String portName = null;

    public String message = null;

    public CorbaUnknownPortException() {
    } // ctor

    public CorbaUnknownPortException(String _portName, String _message) {
        portName = _portName;
        message = _message;
    } // ctor
} // class CorbaUnknownPortException
