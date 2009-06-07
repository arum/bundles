/*
  uk.co.arum.osgi.amf3.sample 
  
  Copyright (C) 2008 - 2009 Arum Systems Ltd

  This file is part of the uk.co.arum.osgi.amf3.sample bundle.

  uk.co.arum.osgi.amf3.sample is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  uk.co.arum.osgi.amf3.sample is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.arum.osgi.amf3.sample;

public interface SimpleService {

	/**
	 * Simply return the given text
	 * 
	 * @param text
	 *            the text to echo
	 * @return the original text
	 */
	String echo(String text);

	/**
	 * Reverse the given text
	 * 
	 * @param text
	 *            the text to reverse
	 * @return a reversed version of the text
	 */
	String reverse(String text);

	/**
	 * Generate a random number between 0 and 1 inclusive.
	 * 
	 * @return a random number
	 */
	double random();

	/**
	 * Throw an exception with the given message.
	 * 
	 * @param message
	 *            the message
	 */
	void throwException(String message);

	/**
	 * Get sample object, has a single parent and two children.
	 */
	SampleObject getSampleObject();

	/**
	 * Accepts a sample object and multiplies its value by 2.
	 * 
	 * @param incoming
	 *            the incoming object
	 * @return the same object
	 */
	SampleObject sendSampleObject(SampleObject incoming);

	/**
	 * Accepts a very simple object and then returns it.
	 * 
	 * @param incoming
	 *            the simple object
	 * @return the object that was passed in
	 */
	VerySimpleObject sendVerySimpleObject(VerySimpleObject incoming);

}
