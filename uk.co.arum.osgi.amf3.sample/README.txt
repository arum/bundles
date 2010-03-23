
AMF for OSGi Sample Application

	Queries and feedback to: 
		Christopher Brind (chris.brind@arum.co.uk)
		Senior Consultant / Developer
		Arum Sytems Ltd
		http://www.arum.co.uk/
	
License:

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

General Requirements:
* An OSGi 4 compliant container, e.g.
** Felix
** Equinox
** ProSyst mBedded

Required Bundles:
* OSGi LogService implementation
* OSGi HttpService implementation
* OSGi SCR implementation
* From bundles folder:
** uk.co.arum.osgi.amf3
** uk.co.arum.osgi.amf3.http
** uk.co.arum.osgi.amf3.remoting
** uk.co.arum.osgi.amf3.sample

Optional Bundles:
* OSGi EventAdmin implementation

Notes:
* uk.co.arum.osgi.amf3.http registers a Servlet with the HTTP container on the mapping /amf3osgi/
* uk.co.arum.osgi.amf3.sample registers resources at the URI /sample/

Installation:
* Confirm the above bundles (or implementations thereof) are available and started.
* Install and start uk.co.arum.osgi.amf3
* Install and start uk.co.arum.osgi.amf3.http
* Install and start uk.co.arum.osgi.amf3.remoting
* Install and start uk.co.arum.osgi.amf3.sample
* Access http://localhost:8080/sample/index.html replacing localhost:8080 with your server name and port.

Sample App Usage:
* Enter text in to the field and use one of the following buttons:
** Echo, uses the server to echo the text
** Reverse, uses the server to reverse the text
** Send, "produces" a message sent to any other open clients
** Exception, have the server thrown RuntimeException with the given text
* Get Random Number, uses the server to get a random number
* Get Sample Object, retrieves a "SampleObject" from the server
* Send Very Simple Object, sends and retrieves a "VerySimpleObject" from the server
* Send Sample Object, send and receive a "SampleObject"
* Open multiple clients
** New clients "produce" a hello message which is then displayed by other clients
** Type text and hit send to send a message to the other clients using the server as the broker (Flex producer/consumer)

