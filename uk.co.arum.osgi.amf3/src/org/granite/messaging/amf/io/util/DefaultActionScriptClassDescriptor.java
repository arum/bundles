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

package org.granite.messaging.amf.io.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import uk.co.arum.osgi.amf3.AMFFactory;

/**
 * @author Franck WOLFF
 */
public class DefaultActionScriptClassDescriptor extends
		ActionScriptClassDescriptor {

	private final AMFFactory factory;

	public DefaultActionScriptClassDescriptor(AMFFactory factory, String type,
			byte encoding) {
		super(type, encoding);
		this.factory = factory;
	}

	@Override
	public void defineProperty(String name) {

		if (type.length() == 0) {
			properties.add(new MapProperty(name));
		} else {
			try {
				Class<?> clazz = factory.loadClass(type);
				if (null == clazz) {
					throw new RuntimeException("Factory could not load class ["
							+ type + "]");
				}

				// Try to find public getter/setter.
				BeanInfo info = Introspector.getBeanInfo(clazz);
				PropertyDescriptor[] props = info.getPropertyDescriptors();
				for (PropertyDescriptor prop : props) {
					if (name.equals(prop.getName())
							&& prop.getWriteMethod() != null
							&& prop.getReadMethod() != null) {
						properties.add(new MethodProperty(name, prop
								.getWriteMethod(), prop.getReadMethod()));
						return;
					}
				}

				// Try to find public field.
				Field field = clazz.getField(name);
				if (!Modifier.isStatic(field.getModifiers())
						&& !Modifier.isTransient(field.getModifiers())) {
					properties.add(new FieldProperty(field));
				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
