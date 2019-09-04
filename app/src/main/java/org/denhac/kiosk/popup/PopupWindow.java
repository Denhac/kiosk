package org.denhac.kiosk.popup;

import org.denhac.kiosk.meetup.Event;

public interface PopupWindow {
    void open(Event e);

    interface Callback {
        void onOpen();

        void onClose();
    }
}
