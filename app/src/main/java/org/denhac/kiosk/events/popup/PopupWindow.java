package org.denhac.kiosk.events.popup;

import org.denhac.kiosk.events.meetup.Event;

public interface PopupWindow {
    void open(Event e);

    interface Callback {
        void onOpen();

        void onClose();
    }
}
