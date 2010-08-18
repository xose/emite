package com.calclab.emite.core.client.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;

public class GwtEmiteEventBus extends HandlerManager implements EmiteEventBus {

    public GwtEmiteEventBus() {
	super(null);
    }

    @Override
    public void fireEvent(final GwtEvent<?> event) {
	GWT.log(event.toDebugString());
	GwtEmiteEventBus.super.fireEvent(event);
	// DeferredCommand.addCommand(new Command() {
	// @Override
	// public void execute() {
	// }
	// });
    }

}