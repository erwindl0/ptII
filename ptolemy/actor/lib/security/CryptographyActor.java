/* A base class for cryptographic actors.

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

@ProposedRating Yellow (rnreddy@ptolemy.eecs.berkeley.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CryptographyActor
/**
This is a base class that implements general and helper functions used by
cryptographic actors. Actors extending this class take in an unsigned byte
array at the <i>input</i>, process the the data based on the
<i>algorithm</i> parameter and send an unsigned byte array on the <i>output</i>.
These transformations include ciphers and signatures implemented by JCE.
The algorithms that maybe implemented are limited those that are
implemented by "providers" following the JCE specifications and installed on
the machine being run.  In case a provider specific instance of an algorithm
is needed, the provider may be specified in the <i>provider</i> parameter.
This class takes care of basic initialization of the subclasses. The
<i>keySize</i> also allows implementations of algorithms using various key
sizes.

This class and its subclasses rely on the Java Cryptography Extension (JCE)
and Java Cryptography Architecture(JCA).

TODO: add link talking about basics of crypto or create a readme.txt

@author Rakesh Reddy
@version $Id$
@since Ptolemy II 3.1
*/

public class CryptographyActor extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CryptographyActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));

        algorithm = new Parameter(this, "algorithm");
        algorithm.setTypeEquals(BaseType.STRING);
        algorithm.setExpression("");

        provider = new Parameter(this, "provider");
        provider.setTypeEquals(BaseType.STRING);
        provider.setExpression("SystemDefault");

        keySize = new Parameter(this, "keySize");
        keySize.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Specify the algorithm to be used to process data.  The algorithm is
     *  specified as a string. The algorithms are limited to those
     *  implemented by providers using the Java JCE which are found on the
     *  system.
     */
    public Parameter algorithm;

    /** Specify a provider for the given algorithm.  Takes the algorithm name
     *  as a string. The default value is "SystemDefault" which allows the
     *  system chooses the provider based on the JCE architecture.
     */
    public Parameter provider;

    /** Specify the size of the key to be created.  This is an int value
     *  representing the number of bits. Refer to the readme.txt for a
     *  list of possible key sizes for certain algorithms.
     */
    public Parameter keySize;

    /** This port takes in an UnsignedByteArray and processes the data.
     */
    public SDFIOPort input;

    /** This port sends out the processed data received from <i>input</i> in
     *  the form of an UnsignedByteArray.
     */
    public SDFIOPort output;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method takes the data from the <i>input</i> and processes the
     *  data based on the <i>algorithm</i>, and <i>provider</i>.  The
     *  transformed data is then sent on the <i>output</i>.
     *
     * @exception IllegalActionException if thrown by the base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        try{
            if(input.hasToken(0)){
                byte[] dataBytes =
                    _arrayTokenToUnsignedByteArray((ArrayToken)input.get(0));
                dataBytes=_process(dataBytes);
                output.send(0, _unsignedByteArrayToArrayToken(dataBytes));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }

    }

    /** This method retrieves the <i>algorithm</i>, <i>provider</i>,
     *  and, <i>keySize</i>.
     *
     * @exception IllegalActionException if exception below is thrown.
     * @exception NoSuchAlgorihmException when the algorithm is not found.
     * @exception NoSuchPaddingException when the padding scheme is illegal
     *     for the given algorithm.
     * @exception NoSuchProviderException if the specified provider does not
     *     exist.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _algorithm = ((StringToken)algorithm.getToken()).stringValue();
        _provider = ((StringToken)provider.getToken()).stringValue();
        _keySize = ((IntToken)keySize.getToken()).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Protected Methods                 ////

    /** Convert an ArrayToken to an array of unsigned bytes.
     *
     * @param dataArrayToken to be converted to a unsigned byte array.
     * @return dataBytes the resulting unsigned byte array.
     */
    protected byte[] _arrayTokenToUnsignedByteArray(ArrayToken dataArrayToken){
        byte[] dataBytes = new byte[dataArrayToken.length()];
        for (int j = 0; j < dataArrayToken.length(); j++) {
            UnsignedByteToken dataToken =
                (UnsignedByteToken)dataArrayToken.getElement(j);
            dataBytes[j] = (byte)dataToken.byteValue();
        }
        return dataBytes;
    }

    /** Convert a byte array to a key Object using an ObjectStream.
     *  The byte array must be the result of the ObjectOutputStream of a Key.
     *
     * @param key the object whose byte array value is determined.
     * @return the byte array of the key object.
     * @throws IllegalActionException if IOException or ClassNotFound
     *  exceptions occurs.
     */
    protected Key _bytesToKey(byte keyBytes[])throws IllegalActionException{
        try{
            ByteArrayInputStream bais;
            ObjectInputStream ois;
            Key key;
            bais = new ByteArrayInputStream(keyBytes);
            ois = new ObjectInputStream(bais);
            key =(Key)ois.readObject();
            return key;
        } catch (IOException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }
    }

    /** Create a pair of keys to be used for asymmetric algorithms.
     *
     * @throws IllegalActionException if algorithm or provider is not found.
     *
     */
    protected KeyPair _createAsymmetricKeys()throws IllegalActionException{
        try{
            KeyPairGenerator keyPairGen;
            if(_provider.equalsIgnoreCase("SystemDefault")){
                keyPairGen = KeyPairGenerator.getInstance(_keyAlgorithm);
            } else {
                keyPairGen = KeyPairGenerator.getInstance(_keyAlgorithm, _provider);
            }

            keyPairGen.initialize(_keySize, new SecureRandom());
            return keyPairGen.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }
    }

    /** Create a symmetric secret key for symmetric algorithms.
     *
     * @throws IllegalActionException if algorithm or provider is not found.
     */
    protected Key _createSymmetricKey() throws IllegalActionException{
        try{
            KeyGenerator keyGen;
            if(_provider.equalsIgnoreCase("SystemDefault")){
                keyGen = KeyGenerator.getInstance(_keyAlgorithm);
            } else {
                keyGen = KeyGenerator.getInstance(_keyAlgorithm, _provider);
            }
            keyGen.init(_keySize, new SecureRandom());
            return keyGen.generateKey();

        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }catch (NoSuchProviderException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }
    }

    /** Convert a key Object to a byte array using an ObjectStream.
     *
     * @param key the object whose byte array value is determined.
     * @return the byte array of the key object.
     * @throws IllegalActionException if IOException occurs.
     */
    protected byte[] _keyToBytes(Key key) throws IllegalActionException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(key);
            return baos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
            throw new IllegalActionException(this.getName() + e.getMessage());
        }
    }

    /** Processes the data based on parameter specifications.  This class returns
     *  the data in its original form.  Subclasses should process the data using
     *  one of the signature or cipher classes provided in the JCE or JCA.
     *
     * @param dataBytes the data to be processed.
     * @return dataBytes the data unchanged.
     * @throws IlligalActionException if subclass throws exception.
     */
    protected byte[] _process(byte [] dataBytes)throws IllegalActionException{
        return dataBytes;
    }

    /** Take an array of unsigned bytes and convert it to an ArrayToken.
     *
     * @param dataBytes data to be converted to an ArrayToken.
     * @return dataArrayToken the resulting ArrayToken.
     * @throws IllegalActionException if ArrayTOken can not be created.
     */
    protected ArrayToken _unsignedByteArrayToArrayToken( byte[] dataBytes)
            throws IllegalActionException{
        int bytesAvailable = dataBytes.length;
        Token[] dataArrayToken = new Token[bytesAvailable];
        for (int j = 0; j < bytesAvailable; j++) {
            dataArrayToken[j] = new UnsignedByteToken(dataBytes[j]);
        }
        return new ArrayToken(dataArrayToken);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The name of the algorithm to be used.
    protected String _algorithm;

    /* The algorithm to be used for generating the key.  This is the
     * same as the _algorithm for Ciphers but needs to be specified
     * separately for Signatures
     */
    protected String _keyAlgorithm;

    // The key size to be used when processing information.
    protected int _keySize;

    // The provider to be used for a provider specific implementation.
    protected String _provider;



}
