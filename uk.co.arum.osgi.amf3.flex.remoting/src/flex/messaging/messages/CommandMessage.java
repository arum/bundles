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

package flex.messaging.messages;

/**
 * @author Franck WOLFF
 */
public class CommandMessage extends AsyncMessage {

	private static final long serialVersionUID = 1L;

	public static final String SELECTOR_HEADER = "DSSelector";

	public static final int SUBSCRIBE_OPERATION = 0;
	public static final int UNSUBSCRIBE_OPERATION = 1;
	public static final int POLL_OPERATION = 2;
	public static final int CLIENT_SYNC_OPERATION = 4;
	public static final int CLIENT_PING_OPERATION = 5;
	public static final int CLUSTER_REQUEST_OPERATION = 7;
	public static final int LOGIN_OPERATION = 8;
	public static final int LOGOUT_OPERATION = 9;
	public static final int SESSION_INVALIDATE_OPERATION = 10;
	public static final int UNKNOWN_OPERATION = 10000;

	private int operation;

	public CommandMessage() {
		super();
	}

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public static String getReadableOperation(int operation) {
		switch (operation) {
		case SUBSCRIBE_OPERATION:
			return "SUBSCRIBE";
		case UNSUBSCRIBE_OPERATION:
			return "UNSUBSCRIBE";
		case POLL_OPERATION:
			return "POLL";
		case CLIENT_SYNC_OPERATION:
			return "CLIENT_SYNC";
		case CLIENT_PING_OPERATION:
			return "CLIENT_PING";
		case CLUSTER_REQUEST_OPERATION:
			return "CLUSTER_REQUEST";
		case LOGIN_OPERATION:
			return "LOGIN";
		case LOGOUT_OPERATION:
			return "LOGOUT";
		case SESSION_INVALIDATE_OPERATION:
			return "SESSION_INVALIDATE";
		case UNKNOWN_OPERATION:
			return "UNKNOWN";

		default:
			return "REALLY UNKNOWN: 0x" + Integer.toBinaryString(operation);
		}
	}
}
