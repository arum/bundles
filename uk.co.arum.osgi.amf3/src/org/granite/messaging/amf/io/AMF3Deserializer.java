/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2008 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.messaging.amf.io;

import java.io.DataInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.AMF3Constants;
import org.granite.messaging.amf.io.util.ActionScriptClassDescriptor;
import org.granite.messaging.amf.io.util.DefaultActionScriptClassDescriptor;
import org.granite.util.XMLUtil;
import org.w3c.dom.Document;

import uk.co.arum.osgi.amf3.AMFFactory;

/**
 * @author Franck WOLFF
 */
public class AMF3Deserializer extends DataInputStream implements ObjectInput,
		AMF3Constants {

	// /////////////////////////////////////////////////////////////////////////
	// Fields.

	protected final List<String> storedStrings = new ArrayList<String>();
	protected final List<Object> storedObjects = new ArrayList<Object>();
	protected final List<ActionScriptClassDescriptor> storedClassDescriptors = new ArrayList<ActionScriptClassDescriptor>();

	protected final XMLUtil xmlUtil = new XMLUtil();

	protected final AMFFactory factory;
	
	// /////////////////////////////////////////////////////////////////////////
	// Constructor.

	public AMF3Deserializer(AMFFactory factory, InputStream in) {
		super(in);
		this.factory = factory;
	}

	// /////////////////////////////////////////////////////////////////////////
	// ObjectInput implementation.

	public Object readObject() throws IOException {

		int type = readAMF3Integer();
		return readObject(type);
	}

	// /////////////////////////////////////////////////////////////////////////
	// AMF3 deserialization.

	protected Object readObject(int type) throws IOException {

		switch (type) {
		case AMF3_UNDEFINED: // 0x00;
		case AMF3_NULL: // 0x01;
			return null;
		case AMF3_BOOLEAN_FALSE: // 0x02;
			return Boolean.FALSE;
		case AMF3_BOOLEAN_TRUE: // 0x03;
			return Boolean.TRUE;
		case AMF3_INTEGER: // 0x04;
			return Integer.valueOf(readAMF3Integer());
		case AMF3_NUMBER: // 0x05;
			return readAMF3Double();
		case AMF3_STRING: // 0x06;
			return readAMF3String();
		case AMF3_XML: // 0x07;
			return readAMF3Xml();
		case AMF3_DATE: // 0x08;
			return readAMF3Date();
		case AMF3_ARRAY: // 0x09;
			return readAMF3Array();
		case AMF3_OBJECT: // 0x0A;
			return readAMF3Object();
		case AMF3_XMLSTRING: // 0x0B;
			return readAMF3XmlString();
		case AMF3_BYTEARRAY: // 0x0C;
			return readAMF3ByteArray();
		default:
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

    protected int readAMF3Integer() throws IOException {
        int result = 0;

        int n = 0;
        int b = readUnsignedByte();
        while ((b & 0x80) != 0 && n < 3) {
            result <<= 7;
            result |= (b & 0x7f);
            b = readUnsignedByte();
            n++;
        }
        if (n < 3) {
            result <<= 7;
            result |= b;
        } else {
            result <<= 8;
            result |= b;
            if ((result & 0x10000000) != 0)
                result |= 0xe0000000;
        }

        return result;
    }

	protected Double readAMF3Double() throws IOException {
		double d = readDouble();
		Double result = (Double.isNaN(d) ? null : Double.valueOf(d));

		return result;
	}

    protected String readAMF3String() throws IOException {
        String result = null;

        int type = readAMF3Integer();
        if ((type & 0x01) == 0) // stored string
            result = getFromStoredStrings(type >> 1);
        else {
            int length = type >> 1;

            if (length > 0) {

                byte[] utfBytes = new byte[length];
                char[] utfChars = new char[length];

                readFully(utfBytes);

                int c, c2, c3, iBytes = 0, iChars = 0;
                while (iBytes < length) {
                    c = utfBytes[iBytes++] & 0xFF;
                    if (c <= 0x7F)
                        utfChars[iChars++] = (char)c;
                    else {
                        switch (c >> 4) {
                        case 12: case 13:
                            c2 = utfBytes[iBytes++];
                            if ((c2 & 0xC0) != 0x80)
                                throw new UTFDataFormatException("Malformed input around byte " + (iBytes-2));
                            utfChars[iChars++] = (char)(((c & 0x1F) << 6) | (c2 & 0x3F));
                            break;
                        case 14:
                            c2 = utfBytes[iBytes++];
                            c3 = utfBytes[iBytes++];
                            if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80))
                                throw new UTFDataFormatException("Malformed input around byte " + (iBytes-3));
                            utfChars[iChars++] = (char)(((c & 0x0F) << 12) | ((c2 & 0x3F) << 6) | ((c3 & 0x3F) << 0));
                            break;
                        default:
                            throw new UTFDataFormatException("Malformed input around byte " + (iBytes-1));
                        }
                    }
                }
                result = new String(utfChars, 0, iChars);


                addToStoredStrings(result);
            } else
                result = "";
        }


        return result;
    }

	protected Date readAMF3Date() throws IOException {
		Date result = null;

		int type = readAMF3Integer();
		if ((type & 0x01) == 0) // stored Date
			result = (Date) getFromStoredObjects(type >> 1);
		else {
			result = new Date((long) readDouble());
			addToStoredObjects(result);
		}

		return result;
	}

	protected Object readAMF3Array() throws IOException {
		Object result = null;

		int type = readAMF3Integer();
		if ((type & 0x01) == 0) // stored array.
			result = getFromStoredObjects(type >> 1);
		else {
			final int size = type >> 1;

			String key = readAMF3String();
			if (key.length() == 0) {
				Object[] objects = new Object[size];
				addToStoredObjects(objects);

				for (int i = 0; i < size; i++) {
					objects[i] = readObject();
				}

				result = objects;
			} else {
				Map<Object, Object> map = new HashMap<Object, Object>();
				addToStoredObjects(map);

				while (key.length() > 0) {
					map.put(key, readObject());
					key = readAMF3String();
				}
				for (int i = 0; i < size; i++)
					map.put(Integer.valueOf(i), readObject());

				result = map;
			}
		}

		return result;
	}

	protected Object readAMF3Object() throws IOException {
		Object result = null;

		int type = readAMF3Integer();

		if ((type & 0x01) == 0) {
			// stored object.
			return getFromStoredObjects(type >> 1);
		}

		boolean inlineClassDef = (((type >> 1) & 0x01) != 0);

		// read class decriptor.
		ActionScriptClassDescriptor desc = null;
		if (inlineClassDef) {
			desc = readClassDescriptor(type);
		} else {
			desc = getFromStoredClassDescriptors(type >> 2);
		}

		int objectEncoding = desc.getEncoding();

		// read object content...
		if ((objectEncoding & 0x01) != 0) {
			return readExternalisedObject(desc);
		}

		if (null == desc.getType() || desc.getType().trim().length() == 0) {
			result = new HashMap<String, Object>();
		} else {
			result = factory.newInstance(desc.getType());
		}

		addToStoredObjects(result);

		// defined values...
		for (int i = 0; i < desc.getPropertiesCount(); i++) {
			byte vType = readByte();
			Object value = readObject(vType);
			desc.setPropertyValue(i, result, value);
		}

		// dynamic values...
		if (objectEncoding == 0x02) {
			while (true) {
				String name = readAMF3String();
				if (name.length() == 0)
					break;
				byte vType = readByte();
				Object value = readObject(vType);
				desc.setPropertyValue(name, result, value);
			}
		}

		return result;
	}

	private ActionScriptClassDescriptor readClassDescriptor(int type)
			throws IOException {
		ActionScriptClassDescriptor desc;
		int propertiesCount = type >> 4;

		byte encoding = (byte) ((type >> 2) & 0x03);

		String className = readAMF3String();

		desc = new DefaultActionScriptClassDescriptor(factory, className,
				encoding);

		addToStoredClassDescriptors(desc);

		for (int i = 0; i < propertiesCount; i++) {
			String name = readAMF3String();
			desc.defineProperty(name);
		}
		return desc;
	}

	private Object readExternalisedObject(ActionScriptClassDescriptor desc)
			throws IOException {
		Object result;
		Integer index = null;

		if (factory.isExternaliser(desc.getType())) {

			result = factory.readExternal(desc.getType(), this);
			index = addToStoredObjects(result);

		} else {

			result = factory.newInstance(desc.getType());
			index = addToStoredObjects(result);
			if (result instanceof Externalizable) {
				try {
					((Externalizable) result).readExternal(this);

				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			} else {
				index = null;
			}

		}

		if (null == index) {
			throw new RuntimeException("The ActionScript3 class bound to "
					+ desc.getType() + " (ie: [RemoteClass(alias=\""
					+ desc.getType() + "\")])"
					+ " implements flash.utils.IExternalizable but"
					+ " the Java class is not Externalizable and"
					+ " no factory is capable of reading this class");
		}
		return result;
	}

	protected Document readAMF3Xml() throws IOException {
		String xml = readAMF3XmlString();
		Document result = xmlUtil.buildDocument(xml);
		return result;
	}

	protected String readAMF3XmlString() throws IOException {
		String result = null;

		int type = readAMF3Integer();
		if ((type & 0x01) == 0) // stored String
			result = getFromStoredStrings(type >> 1);
		else {
			byte[] bytes = readBytes(type >> 1);
			result = new String(bytes, "UTF-8");
			addToStoredStrings(result);
		}

		return result;
	}

	protected byte[] readAMF3ByteArray() throws IOException {
		byte[] result = null;

		int type = readAMF3Integer();
		if ((type & 0x01) == 0) // stored object.
			result = (byte[]) getFromStoredObjects(type >> 1);
		else {
			result = readBytes(type >> 1);
			addToStoredObjects(result);
		}

		return result;
	}

	// /////////////////////////////////////////////////////////////////////////
	// Cached objects methods.

	protected int addToStoredStrings(String s) {
		int index = storedStrings.size();
		storedStrings.add(s);
		return index;
	}

	protected String getFromStoredStrings(int index) {
		String s = storedStrings.get(index);
		return s;
	}

	protected int addToStoredObjects(Object o) {
		int index = storedObjects.size();
		storedObjects.add(o);
		return index;
	}

	protected void setStoredObject(int index, Object o) {
		storedObjects.set(index, o);
	}

	protected Object getFromStoredObjects(int index) {
		return storedObjects.get(index);
	}

	protected void addToStoredClassDescriptors(ActionScriptClassDescriptor desc) {
		storedClassDescriptors.add(desc);
	}

	protected ActionScriptClassDescriptor getFromStoredClassDescriptors(
			int index) {
		ActionScriptClassDescriptor desc = storedClassDescriptors.get(index);
		return desc;
	}

	// /////////////////////////////////////////////////////////////////////////
	// Utilities.

	protected byte[] readBytes(int count) throws IOException {
		byte[] bytes = new byte[count];
		readFully(bytes);
		return bytes;
	}
}
