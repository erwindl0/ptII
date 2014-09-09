package org.terraswarm.gdp;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * <i>native declaration : /usr/include/stdlib.h:13</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class lldiv_t extends Structure {
	public long quot;
	public long rem;
	public lldiv_t() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("quot", "rem");
	}
	public lldiv_t(long quot, long rem) {
		super();
		this.quot = quot;
		this.rem = rem;
	}
	public lldiv_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends lldiv_t implements Structure.ByReference {
		
	};
	public static class ByValue extends lldiv_t implements Structure.ByValue {
		
	};
}
