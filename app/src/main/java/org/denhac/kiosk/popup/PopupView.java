package org.denhac.kiosk.popup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.denhac.kiosk.R;

public class PopupView extends ConstraintLayout {
    public PopupView(Context context) {
        super(context);
        init();
    }

    public PopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PopupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.popup_event, this);
    }
}
