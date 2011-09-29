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

package com.calclab.emite.core.client;

import com.calclab.emite.core.client.browser.AutoConfigBoot;
import com.calclab.emite.core.client.conn.XmppConnection;
import com.calclab.emite.core.client.conn.bosh.XmppBoshConnection;
import com.calclab.emite.core.client.services.Services;
import com.calclab.emite.core.client.services.ServicesImplGWT;
import com.calclab.emite.core.client.session.SessionReady;
import com.calclab.emite.core.client.session.XmppSession;
import com.calclab.emite.core.client.session.XmppSessionImpl;
import com.calclab.emite.core.client.session.sasl.SASLManager;
import com.calclab.emite.core.client.session.sasl.SASLManagerImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.web.bindery.event.shared.EventBus;

/**
 * The Emite core module implements the Extensible Messaging and Presence
 * Protocol (XMPP): Core
 * 
 * The Extensible Messaging and Presence Protocol (XMPP) is an open Extensible
 * Markup Language XML [XML] protocol for near-real-time messaging, presence,
 * and request-response services. The basic syntax and semantics were developed
 * originally within the Jabber open-source community, mainly in 1999.
 * 
 * The core features -- mainly XML streams, use of TLS and SASL, and the
 * <message/>, <presence/>, and <iq/> children of the stream root -- provide the
 * building blocks for many types of near-real-time applications, which may be
 * layered on top of the core by sending application-specific data qualified by
 * particular XML namespaces [XML‑NAMES]
 * 
 * @see http://xmpp.org/rfcs/rfc3920.html
 */
public class EmiteCoreModule extends AbstractGinModule {

	@Override
	protected void configure() {
		bind(EventBus.class).annotatedWith(Names.named("emite")).to(LoggingEventBus.class).in(Singleton.class);
		bind(AutoConfigBoot.class).asEagerSingleton();
		
		bind(XmppConnection.class).to(XmppBoshConnection.class);
		bind(XmppSession.class).to(XmppSessionImpl.class);
		bind(SASLManager.class).to(SASLManagerImpl.class);
		bind(SessionReady.class).asEagerSingleton();
		
		bind(Services.class).to(ServicesImplGWT.class);
	}

}