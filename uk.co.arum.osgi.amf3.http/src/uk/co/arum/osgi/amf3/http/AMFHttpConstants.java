/*
  uk.co.arum.osgi.amf3.http 
  
  Copyright (C) 2008 - 2009 Arum Systems Ltd

  This file is part of the uk.co.arum.osgi.amf3.http bundle.

  uk.co.arum.osgi.amf3.http is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  uk.co.arum.osgi.amf3.http is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.arum.osgi.amf3.http;

public interface AMFHttpConstants {

	/** 5 minute session expiry. * */
	public static final int SESSION_MAX_INTERVAL = 60 * 5;

	/**
	 * The name of the variable put in session to handle expiry so that an event
	 * can be dispatched.
	 */
	public static final String OSGI_SESSION_ATTRIBUTE_NAME = "___OSGI_SESSION___";

}
