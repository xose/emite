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

package com.calclab.emite.core.client.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.calclab.emite.core.client.conn.bosh.StreamSettings;
import com.calclab.emite.core.client.conn.ConnectionStatus;
import com.calclab.emite.core.client.conn.XmppConnection;
import com.calclab.emite.core.client.events.AuthorizationResultEvent;
import com.calclab.emite.core.client.events.BeforeStanzaSentEvent;
import com.calclab.emite.core.client.events.ConnectionStatusChangedEvent;
import com.calclab.emite.core.client.events.IQReceivedEvent;
import com.calclab.emite.core.client.events.MessageReceivedEvent;
import com.calclab.emite.core.client.events.PresenceReceivedEvent;
import com.calclab.emite.core.client.events.SessionStatusChangedEvent;
import com.calclab.emite.core.client.events.StanzaReceivedEvent;
import com.calclab.emite.core.client.packet.IPacket;
import com.calclab.emite.core.client.session.sasl.SASLManager;
import com.calclab.emite.core.client.stanzas.IQ;
import com.calclab.emite.core.client.stanzas.Message;
import com.calclab.emite.core.client.stanzas.Presence;
import com.calclab.emite.core.client.stanzas.XmppURI;
import com.calclab.emite.core.client.stanzas.IQ.Type;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Default XmppSession logic implementation. You should use XmppSession
 * interface instead.
 * 
 * @see XmppSession
 */
@Singleton
public class XmppSessionImpl implements XmppSession, ConnectionStatusChangedEvent.Handler, AuthorizationResultEvent.Handler, StanzaReceivedEvent.Handler {

	private static final Logger logger = Logger.getLogger(XmppSessionImpl.class.getName());

	private final EventBus eventBus;
	private final XmppConnection connection;
	private final SASLManager saslManager;
	
	private int iqId;
	private final Map<String, IQCallback> iqHandlers;
	private final List<IPacket> queuedStanzas;
	private SessionStatus status;
	private Credentials credentials;
	private XmppURI userUri;
	
	@Inject
	public XmppSessionImpl(@Named("emite") final EventBus eventBus, final XmppConnection connection, final SASLManager saslManager) {
		this.eventBus = eventBus;
		this.connection = connection;
		this.saslManager = saslManager;
		
		iqHandlers = new HashMap<String, IQCallback>();
		queuedStanzas = new ArrayList<IPacket>();
		status = SessionStatus.disconnected;

		connection.addConnectionStatusChangedHandler(this);
		connection.addStanzaReceivedHandler(this);
		saslManager.addAuthorizationResultHandler(this);
	}
	
	@Override
	public void onConnectionStatusChanged(final ConnectionStatusChangedEvent event) {
		if (event.is(ConnectionStatus.error)) {
			logger.severe("Connection error: " + event.getDescription());
			setStatus(SessionStatus.error);
		} else if (event.is(ConnectionStatus.disconnected)) {
			setStatus(SessionStatus.disconnected);
		}
	}
	
	@Override
	public void onStanzaReceived(final StanzaReceivedEvent event) {
		final IPacket stanza = event.getStanza();
		final String name = stanza.getName();
		if (name.equals("message")) {
			eventBus.fireEventFromSource(new MessageReceivedEvent(new Message(stanza)), this);
		} else if (name.equals("presence")) {
			eventBus.fireEventFromSource(new PresenceReceivedEvent(new Presence(stanza)), this);
		} else if (name.equals("iq")) {
			final String type = stanza.getAttribute("type");
			if ("get".equals(type) || "set".equals(type)) {
				eventBus.fireEventFromSource(new IQReceivedEvent(new IQ(stanza)), this);
			} else {
				final IQCallback handler = iqHandlers.remove(stanza.getAttribute("id"));
				if (handler != null)
					handler.onIQ(new IQ(stanza));
			}
		} else if (credentials != null && ("stream:features".equals(name) || "features".equals(name)) && stanza.hasChild("mechanisms")) {
			setStatus(SessionStatus.connecting);
			saslManager.sendAuthorizationRequest(credentials);
			credentials = null;
		}
	}
	
	@Override
	public void onAuthorizationResult(final AuthorizationResultEvent event) {
		if (event.isSuccess()) {
			setStatus(SessionStatus.authorized);
			connection.restartStream();
			bindResource(event.getXmppUri().getResource());
		} else {
			setStatus(SessionStatus.notAuthorized);
			connection.disconnect();
		}
	}
	
	@Override
	public HandlerRegistration addBeforeStanzaSentHandler(final BeforeStanzaSentEvent.Handler handler) {
		return eventBus.addHandlerToSource(BeforeStanzaSentEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addIQReceivedHandler(final IQReceivedEvent.Handler handler) {
		return eventBus.addHandlerToSource(IQReceivedEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addMessageReceivedHandler(final MessageReceivedEvent.Handler handler) {
		return eventBus.addHandlerToSource(MessageReceivedEvent.TYPE, this, handler);
	}

	@Override
	public HandlerRegistration addPresenceReceivedHandler(final PresenceReceivedEvent.Handler handler) {
		return eventBus.addHandlerToSource(PresenceReceivedEvent.TYPE, this, handler);
	}
	
	@Override
	public HandlerRegistration addSessionStatusChangedHandler(final boolean sendCurrent, final SessionStatusChangedEvent.Handler handler) {
		if (sendCurrent) {
			handler.onSessionStatusChanged(new SessionStatusChangedEvent(getStatus()));
		}
		
		return eventBus.addHandlerToSource(SessionStatusChangedEvent.TYPE, this, handler);
	}
	
	@Override
	public SessionStatus getStatus() {
		return status;
	}

	@Override
	public boolean isStatus(final SessionStatus expectedStatus) {
		return status.equals(expectedStatus);
	}

	@Override
	public void setStatus(final SessionStatus newStatus) {
		if (SessionStatus.ready.equals(newStatus)) {
			sendQueuedStanzas();
		} else if (SessionStatus.disconnected.equals(newStatus)) {
			userUri = null;
		}
		if (!status.equals(newStatus)) {
			status = newStatus;
			eventBus.fireEventFromSource(new SessionStatusChangedEvent(newStatus), this);
		}
	}

	@Override
	public XmppURI getCurrentUserURI() {
		return userUri;
	}

	@Override
	public boolean isReady() {
		return userUri != null;
	}

	@Override
	public void login(final Credentials credentials) {
		logger.info("XmppSessionLogic - login");
		if (getStatus() == SessionStatus.disconnected) {
			setStatus(SessionStatus.connecting);
			connection.connect();
			this.credentials = credentials;
		}
	}
	
	@Override
	public void login(final XmppURI uri, final String password) {
		login(new Credentials(uri, password));
	}

	@Override
	public void logout() {
		if (getStatus() != SessionStatus.disconnected && userUri != null) {
			// TODO : To be reviewed, preventing unvailable presences to be sent
			// so that only the 'terminate' is sent
			// Unvailabel are handled automatically by the server
			setStatus(SessionStatus.loggingOut);
			userUri = null;
			connection.disconnect();
			setStatus(SessionStatus.disconnected);
		}
	}

	@Override
	public StreamSettings pause() {
		return connection.pause();
	}

	@Override
	public void resume(final XmppURI userURI, final StreamSettings settings) {
		userUri = userURI;
		setStatus(SessionStatus.resume);
		connection.resume(settings);
		setStatus(SessionStatus.ready);
	}

	@Override
	public void send(final IPacket packet) {
		// Added a condition to check the connection is not retrying...
		if (connection.hasErrors() || userUri == null) {
			logger.finer("session queuing stanza" + packet);
			queuedStanzas.add(packet);
		} else {
			packet.setAttribute("from", userUri.toString());
			eventBus.fireEventFromSource(new BeforeStanzaSentEvent(packet), this);
			connection.send(packet);
		}
	}

	@Override
	public void sendIQ(final String category, final IQ iq, final IQCallback handler) {
		if (!iq.getType().equals(Type.result)) {
			final String key = category + "_" + iqId++;
			iqHandlers.put(key, handler);
			iq.setAttribute("id", key);
			send(iq);
		}
	}

	@Override
	public String toString() {
		return "Session " + userUri + " in " + getStatus().toString() + " " + queuedStanzas.size() + " queued stanzas con=" + connection.toString();
	}

	private void sendQueuedStanzas() {
		logger.finer("Sending queued stanzas....");
		for (final IPacket packet : queuedStanzas) {
			send(packet);
		}
		queuedStanzas.clear();
	}

	private void setLoggedIn(final XmppURI userURI) {
		userUri = userURI;
		logger.info("SESSION LOGGED IN");
		setStatus(SessionStatus.loggedIn);
	}
	
	private void bindResource(final String resource) {
		final IQ iq = new IQ(IQ.Type.set);
		iq.setId("bind-resource");
		iq.addChild("bind", "urn:ietf:params:xml:ns:xmpp-bind").addChild("resource", null).setText(resource);

		// FIXME? Can't use sendIQ
		iqHandlers.put("bind-resource", new IQCallback() {
			@Override
			public void onIQ(IQ iq) {
				setStatus(SessionStatus.binded);
				
				requestSession(XmppURI.uri(iq.getFirstChild("bind").getFirstChild("jid").getText()));
			}
		});
		
		connection.send(iq);
	}
	
	private void requestSession(final XmppURI uri) {
		final IQ iq = new IQ(IQ.Type.set, uri.getHostURI()).From(uri);
		iq.setId("session-request");
		iq.addChild("session", "urn:ietf:params:xml:ns:xmpp-session");

		// FIXME? Can't use sendIQ
		iqHandlers.put("session-request", new IQCallback() {
			@Override
			public void onIQ(IQ iq) {
				// TODO: disconnect() on failure
				setLoggedIn(XmppURI.uri(iq.getAttribute("to")));
			}
		});
		
		connection.send(iq);
	}
	
}