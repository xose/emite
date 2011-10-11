/*
 * ((e)) emite: A pure Google Web Toolkit XMPP library
 * Copyright (c) 2008-2011 The Emite development team
 * 
 * This file is part of Emite.
 *
 * Emite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Emite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with Emite.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.calclab.emite.core.client.events;

import com.calclab.emite.core.client.conn.ConnectionStatus;
import com.google.web.bindery.event.shared.Event;

public class ConnectionStatusChangedEvent extends Event<ConnectionStatusChangedEvent.Handler> {

	public interface Handler {
		void onConnectionStatusChanged(ConnectionStatusChangedEvent event);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final ConnectionStatus status;
	private final String description;
	private final int value;

	public ConnectionStatusChangedEvent(final ConnectionStatus status) {
		this(status, null, 0);
	}

	public ConnectionStatusChangedEvent(final ConnectionStatus status, final String description) {
		this(status, description, 0);
	}

	public ConnectionStatusChangedEvent(final ConnectionStatus status, final String description, final int value) {
		assert status != null : "status can't be null in ConnectionStatusChangedEvent";
		this.status = status;
		this.description = description;
		this.value = value;
	}

	public ConnectionStatus getStatus() {
		return status;
	}

	public String getDescription() {
		return description;
	}

	public int getValue() {
		return value;
	}

	public boolean is(final ConnectionStatus status) {
		return this.status.equals(status);
	}

	@Override
	public String toDebugString() {
		final String desc = description != null ? "(" + description + ")" : "";
		return super.toDebugString() + " " + status + desc + " value: " + value;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(final Handler handler) {
		handler.onConnectionStatusChanged(this);
	}

}
