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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author Franck WOLFF
 */
public class MethodProperty extends Property {

	private final Method setter;
	private final Method getter;
	private final Type type;

	public MethodProperty(String name, Method setter, Method getter) {
		super(name);
		this.setter = setter;
		this.getter = getter;
		this.type = getter != null ? getter.getGenericReturnType()
				: setter.getParameterTypes()[0];
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void setProperty(Object instance, Object value, boolean convert) {

		/*
		 * Basically, what is happening here is that if a property that is
		 * supposedly an AMF Object is being set to null, it is coming through
		 * as a type 0x9 element (array) with a single null value, rather than a
		 * type 0x1 (null).
		 * 
		 * Can't see how granite handles this, or if there is something more
		 * fundamentally wrong with the changes I have made to deserialisation.
		 */
		if (convert && value != null) {
			if (!value.getClass().equals(type)) {
				if (value instanceof Object[]) {
					Object[] array = (Object[]) value;
					if (array.length == 1 && array[0] == null) {
						value = null;
					}
				}

			}
		}

		try {
			Object[] params = new Object[] { value };
			setter.invoke(instance, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getProperty(Object instance) {
		try {
			return getter.invoke(instance, new Object[0]);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
