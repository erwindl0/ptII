package org.terraswarm.gdp;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * Scheduling paramters<br>
 * <i>native declaration : /usr/include/sched.h:5</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class sched_param extends Structure {
	public int sched_priority;
	/** C type : char[4] */
	public byte[] __opaque = new byte[4];
	public sched_param() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("sched_priority", "__opaque");
	}
	/** @param __opaque C type : char[4] */
	public sched_param(int sched_priority, byte __opaque[]) {
		super();
		this.sched_priority = sched_priority;
		if ((__opaque.length != this.__opaque.length)) 
			throw new IllegalArgumentException("Wrong array size !");
		this.__opaque = __opaque;
	}
	public sched_param(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends sched_param implements Structure.ByReference {
		
	};
	public static class ByValue extends sched_param implements Structure.ByValue {
		
	};
}
