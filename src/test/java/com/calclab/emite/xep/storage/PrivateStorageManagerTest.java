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

package com.calclab.emite.xep.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.calclab.emite.base.xml.XMLBuilder;
import com.calclab.emite.core.stanzas.IQ;
import com.calclab.emite.xep.storage.PrivateStorageManager;
import com.calclab.emite.xep.storage.PrivateStorageManagerImpl;
import com.calclab.emite.xtesting.XmppSessionTester;
import com.calclab.emite.xtesting.handlers.PrivateStorageResponseTestHandler;

public class PrivateStorageManagerTest {
	private XmppSessionTester session;
	private PrivateStorageManager manager;

	String storeData = "<iq type=\"set\" ><query xmlns=\"jabber:iq:private\">"
			+ "<exodus xmlns=\"exodus:prefs\"><defaultnick>Hamlet</defaultnick></exodus></query></iq>";
	String storeResponse = "<iq type=\"result\" ></iq>";
	String data = "<exodus xmlns=\"exodus:prefs\"><defaultnick>Hamlet</defaultnick></exodus>";

	String dataToRetrieve = "<exodus xmlns=\"exodus:prefs\"/>";
	String retriveData = "<iq type=\"get\"><query xmlns=\"jabber:iq:private\"><exodus xmlns=\"exodus:prefs\"/></query></iq>";
	String retrieveResponse = "<iq type=\"result\" from=\"hamlet@shakespeare.lit/denmark\" to=\"hamlet@shakespeare.lit/denmark\"> <query xmlns=\"jabber:iq:private\"><exodus xmlns=\"exodus:prefs\"><defaultnick>Hamlet</defaultnick></exodus></query></iq>";

	@Before
	public void setup() {
		session = new XmppSessionTester("test@domain");
		manager = new PrivateStorageManagerImpl(session);
	}

	@Test
	public void shouldStore() {
		final PrivateStorageResponseTestHandler handler = new PrivateStorageResponseTestHandler();
		manager.store(XMLBuilder.fromXML(data), handler);
		session.verifyIQSent(storeData);
		session.answerSuccess(new IQ(XMLBuilder.fromXML(storeResponse)));
		assertTrue("handler called " + handler.getCalledTimes() + " times", handler.isCalledOnce());
	}

	@Test
	public void shoulGet() {
		final PrivateStorageResponseTestHandler handler = new PrivateStorageResponseTestHandler();
		manager.retrieve(XMLBuilder.create("exodus", "exodus:prefs"), handler);
		session.verifyIQSent(retriveData);
		session.answerSuccess(new IQ(XMLBuilder.fromXML(retrieveResponse)));
		assertTrue(handler.isCalledOnce());
		assertEquals("Hamlet", handler.getLastEvent().getResponseIQ().getChild("query", "jabber:iq:private").getFirstChild("exodus").getChildText("defaultnick"));
	}
}
