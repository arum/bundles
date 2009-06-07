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

package uk.co.arum.osgi.amf3.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.granite.messaging.amf.AMF0Body;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.AMF3Object;
import org.granite.messaging.amf.io.AMF0Deserializer;
import org.granite.messaging.amf.io.AMF0Serializer;

import uk.co.arum.osgi.amf3.AMFFactory;

public class AMFProcessor {

	private final AMFFactory factory;

	public AMFProcessor(AMFFactory factory) {
		super();
		this.factory = factory;
	}

	public void process(InputStream in, OutputStream out) throws IOException {

		AMF0Deserializer des = new AMF0Deserializer(factory,
				new DataInputStream(in));

		AMF0Message requestMessage = des.getAMFMessage();

		AMF0Message responseMessage = new AMF0Message();
		responseMessage.setVersion(requestMessage.getVersion());

		for (AMF0Body requestBody : requestMessage) {

			Object value = requestBody.getValue();
			Object response = null;

			try {
				response = factory.process(value);
			} catch (Exception ex) {
				response = factory.handleException(ex);
				if (null == response) {
					response = ex;
				}
			}

			if (null != response) {
				AMF3Object data = new AMF3Object(response);
				String target = factory.getResponseTarget(response);
				responseMessage.addBody(new AMF0Body(requestBody.getResponse()
						+ "/" + target, "", data,
						AMF0Body.DATA_TYPE_AMF3_OBJECT));
			}

		}

		AMF0Serializer ser = new AMF0Serializer(factory, new DataOutputStream(
				out));
		ser.serializeMessage(responseMessage);
	}
}
