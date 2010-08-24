package com.calclab.emite.core.client.events;

import com.calclab.emite.core.client.packet.IPacket;
import com.calclab.emite.core.client.packet.MatcherFactory;
import com.calclab.emite.core.client.packet.PacketMatcher;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class ErrorEvent extends GwtEvent<ErrorHandler> {

    private static final PacketMatcher ERROR_STANZA_MATCHER = MatcherFactory.byName("error");
    private static final Type<ErrorHandler> TYPE = new Type<ErrorHandler>();

    public static HandlerRegistration bind(EmiteEventBus eventBus, ErrorHandler handler) {
	return eventBus.addHandler(TYPE, handler);
    }
    private final String errorType;
    private final String description;
    private final IPacket stanza;
    private final IPacket errorStanza;

    public ErrorEvent(String errorType, String description, IPacket stanza) {
	this.errorType = errorType;
	this.description = description;
	this.stanza = stanza;
	this.errorStanza = stanza.getFirstChild(ERROR_STANZA_MATCHER);
    }

    @Override
    public Type<ErrorHandler> getAssociatedType() {
	return TYPE;
    }

    public String getDescription() {
	return description;
    }

    public IPacket getErrorStanza() {
	return errorStanza;
    }

    public String getErrorType() {
	return errorType;
    }

    public IPacket getStanza() {
	return stanza;
    }

    @Override
    protected void dispatch(ErrorHandler handler) {
	handler.onError(this);
    }

}
