/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaUnknownPortExceptionHelper.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public class CorbaUnknownPortExceptionHelper {
     // It is useless to have instances of this class
     private CorbaUnknownPortExceptionHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException that) {
    out.write_string(id());

	out.write_string(that.portName);
	out.write_string(that.message);
    }
    public static ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException read(org.omg.CORBA.portable.InputStream in) {
        ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException that = new ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException();
         // read and discard the repository id
        in.read_string();

	that.portName = in.read_string();
	that.message = in.read_string();
    return that;
    }
   public static ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
       int _memberCount = 2;
       org.omg.CORBA.StructMember[] _members = null;
          if (_tc == null) {
               _members= new org.omg.CORBA.StructMember[2];
               _members[0] = new org.omg.CORBA.StructMember(
                 "portName",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[1] = new org.omg.CORBA.StructMember(
                 "message",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_exception_tc(id(), "CorbaUnknownPortException", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:CorbaActor/CorbaUnknownPortException:1.0";
   }
}
