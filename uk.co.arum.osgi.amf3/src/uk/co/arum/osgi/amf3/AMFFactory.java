/*
  uk.co.arum.osgi.amf3 
  
  Copyright (C) 2008 - 2009 Arum Systems Ltd

  This file is part of the uk.co.arum.osgi.amf3 bundle.

  uk.co.arum.osgi.amf3 is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  uk.co.arum.osgi.amf3 is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.arum.osgi.amf3;

import java.io.ObjectInput;
import java.io.ObjectOutput;

public interface AMFFactory {

	/**
	 * Create an instance of the class with the given name
	 * 
	 * @param name
	 *            the name of the class to load
	 * @return the class
	 */
	Object newInstance(String name);

	/**
	 * Load the class with the given name
	 * 
	 * @param name
	 *            the name of the class
	 * 
	 * @return the class
	 */
	Class<?> loadClass(String name);

	/**
	 * Return true if this factory is the externalizer for the given class.
	 * 
	 * @param name
	 *            the name of the externalizable class
	 * 
	 * @return true if this factory is the externalizer for the given class.
	 */
	boolean isExternaliser(String name);

	/**
	 * Create an instance of the given class. This will only be called if this
	 * class returned true to the {@link #isExternaliser(String)} method.
	 * 
	 * @param name
	 *            the name of the class
	 * 
	 * @param deserializer
	 */
	Object readExternal(String name, ObjectInput deserializer);

	/**
	 * Get a description of this factory.
	 * 
	 * @return a description
	 */
	String getDescription();

	/**
	 * Process an object.
	 * 
	 * @param o
	 *            the object to process
	 * @return the response
	 */
	Object process(Object o);

	/**
	 * Get the response target for a given response object.
	 * 
	 * @param response
	 *            the response.
	 * @return the target, e.g. "onStatus" or "onResult"
	 */
	String getResponseTarget(Object response);

	/**
	 * Write an object using custom externalisation. This will only be called if
	 * this class returned true to the {@link #isExternaliser(String)} method.
	 * 
	 * @param o
	 *            the object
	 * @param serialiser
	 *            the data output stream
	 * @return true if this factory wrote the object
	 */
	boolean writeExternal(Object o, ObjectOutput serialiser);

	/**
	 * Get the class to use for this object, for introspection, etc.
	 * 
	 * @param o
	 *            the object
	 * @return the class
	 */
	Class<?> getObjectClass(Object o);

	/**
	 * Handle the given given exception.
	 * 
	 * @param ex
	 *            the exception
	 * @return an object that represents the exception
	 */
	Object handleException(Exception ex);

}
