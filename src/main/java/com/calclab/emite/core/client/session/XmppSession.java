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

import com.calclab.emite.core.client.conn.bosh.StreamSettings;
import com.calclab.emite.core.client.events.BeforeStanzaSentEvent;
import com.calclab.emite.core.client.events.IQReceivedEvent;
import com.calclab.emite.core.client.events.MessageReceivedEvent;
import com.calclab.emite.core.client.events.PresenceReceivedEvent;
import com.calclab.emite.core.client.events.SessionStatusChangedEvent;
import com.calclab.emite.core.client.packet.IPacket;
import com.calclab.emite.core.client.stanzas.IQ;
import com.calclab.emite.core.client.stanzas.XmppURI;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * The most important object in Xmpp emite module. You can login, send and
 * receive stanzas. It also allows you to pause and resume the session.
 */
public interface XmppSession {
	/**
	 * Add a handler to know when a stanza is going to be send. Allows to modify
	 * a stanza before is sent to the server.
	 * 
	 * @param handler
	 * @return a way to remove the handler
	 */
	public HandlerRegistration addBeforeStanzaSentHandler(BeforeStanzaSentEvent.Handler handler);

	/**
	 * Add a handler to know when a IQ has been received
	 * 
	 * @param handler
	 * @return a way to remove the handler
	 */
	public HandlerRegistration addIQReceivedHandler(IQReceivedEvent.Handler handler);

	/**
	 * Add a handler to know when a Message has been received
	 * 
	 * @param handler
	 * @return a way to remove the handler
	 */
	public HandlerRegistration addMessageReceivedHandler(MessageReceivedEvent.Handler handler);

	/**
	 * Add a handler to know when a Presence has been received
	 * 
	 * @param handler
	 * @return a way to remove the handler
	 */
	public HandlerRegistration addPresenceReceivedHandler(PresenceReceivedEvent.Handler handler);

	/**
	 * Add a handler to track session status changes. If sendCurrent is true, the
	 * handler will receive the current session status just after been added.
	 * 
	 * @param sendCurrent
	 *            if true, the current session status will be sent to the handler
	 *            just after addition
	 * @param handler
	 *            the handler itself
	 * 
	 * @return a way to remove the handler
	 */
	public HandlerRegistration addSessionStatusChangedHandler(boolean sendCurrent, SessionStatusChangedEvent.Handler handler);

	/**
	 * Returns the current user xmpp uri
	 * 
	 * @return the current user xmpp uri
	 */
	public XmppURI getCurrentUserURI();

	/**
	 * Returns the current status
	 * 
	 * @return The current status as enum
	 */
	public SessionStatus getStatus();

	/**
	 * Set the current session's status
	 * 
	 * @param status
	 *            the current session status
	 * @see SessionStatus
	 */
	public void setStatus(SessionStatus status);

	/**
	 * Check if the session is ready to send stanzas
	 */
	public boolean isReady();

	/**
	 * Check whenever or not the session is in the given status
	 * 
	 * @param expectedStatus
	 *            the expected status of the session
	 * @return true if the expected status is the actual status
	 */
	public boolean isStatus(SessionStatus expectedStatus);

	/**
	 * <p>
	 * Start a login process with the current credentials. Use onLoggedIn method
	 * to know when you are really logged in. If the uri doesn't provide a
	 * resource, the session will generate one.
	 * <p>
	 * You can use LoginCredentials.ANONYMOUS and to perform an anonumous login.
	 * </p>
	 * 
	 * @param credentials
	 */
	public void login(Credentials credentials);

	/**
	 * <p>
	 * Start a login process with the current xmpp uri and password. It uses the
	 * login(credentials) method with not encoded password.
	 * </p>
	 * 
	 * @param uri
	 *            the user's uri to login
	 * @param password
	 *            the user's password (plain)
	 * 
	 * @see login
	 */
	public void login(final XmppURI uri, final String password);

	/**
	 * Start a logout process in the current session. Use obnLoggedOut to know
	 * when you are really logged out.
	 */
	public void logout();

	/**
	 * Call this method to pause the session. You can use the given object
	 * object (or other with the same data) to resume the session later.
	 * 
	 * @see http://www.xmpp.org/extensions/xep-0124.html#inactive
	 * @see XmppSession.resume
	 * @return The StreamSettings object if the session was ready, null
	 *         otherwise
	 */
	public StreamSettings pause();

	/**
	 * Call this method to resume a session.
	 * 
	 * @see http://www.xmpp.org/extensions/xep-0124.html#inactive
	 * @see XmppSession.pause
	 * @param userURI
	 *            the previous session user's uri
	 * @param settings
	 *            the stream settings given by the pause method
	 */
	public void resume(XmppURI userURI, StreamSettings settings);

	/**
	 * Send a stanza to the server. This method overrides the "from" uri
	 * attribute.
	 * 
	 * <b>All the stanzas sent using this method BEFORE the LoggedIn status are
	 * queued and sent AFTER Ready status.</b>
	 * 
	 * @see sendIQ
	 * @param stanza
	 *            the stanza to be sent
	 */
	public void send(final IPacket stanza);

	/**
	 * A helper method that allows to send a IQ stanza and attach a listener to
	 * the response. This method overrides (if present) the given IQ id using
	 * the category provided and a internal sequential number. This method also
	 * overrides (if present) the given 'from' attribute
	 * 
	 * If the listener is null, the IQ is sent but no callback called
	 * 
	 * <b>All the stanzas sent using this method BEFORE the LoggedIn status are
	 * queued and sent AFTER Ready status.</b>
	 * 
	 * @param category
	 *            a uniqe-per-component string that allows the session to
	 *            generate a sequential and uniqe id for the IQ
	 * @param iq
	 *            the IQ stanza to be sent
	 * @param handler
	 *            the handler called when a IQ of type "result" arrives to the
	 *            server. After the invocation, the handler is discarded. It CAN
	 *            be null
	 * 
	 */
	public void sendIQ(final String category, final IQ iq, final IQCallback iqHandler);

}