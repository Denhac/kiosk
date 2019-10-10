package org.denhac.kiosk;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public abstract class KioskActivity extends AppCompatActivity {
    // To keep track of activity's window focus
    boolean currentFocus;

    // To keep track of activity's foreground/background status
    boolean isPaused;

    Handler collapseNotificationHandler;

    private TextView countdownTextView;
    private Disposable timerDisposable;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        currentFocus = hasFocus;

        if (!hasFocus) {

            // Method that handles loss of window focus
            collapseNow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        isPaused = true;

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);

        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        isPaused = false;

        resetTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
    }

    private void collapseNow() {

        // Initialize 'collapseNotificationHandler'
        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }

        // If window focus has been lost && activity is not in a paused state
        // Its a valid check because showing of notification panel
        // steals the focus from current activity's window, but does not
        // 'pause' the activity
        if (!currentFocus && !isPaused) {

            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    // Use reflection to trigger a method from 'StatusBarManager'

                    @SuppressLint("WrongConstant") Object statusBarService = getSystemService("statusbar");
                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    Method collapseStatusBar = null;

                    try {

                        // Prior to API 17, the method to call is 'collapse()'
                        // API 17 onwards, the method to call is `collapsePanels()`

                        collapseStatusBar = statusBarManager.getMethod("collapsePanels");
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    collapseStatusBar.setAccessible(true);

                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    // Check if the window focus has been returned
                    // If it hasn't been returned, post this Runnable again
                    // Currently, the delay is 100 ms. You can change this
                    // value to suit your needs.
                    if (!currentFocus && !isPaused) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }

                }
            }, 300L);
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        countdownTextView = findViewById(R.id.countdown);
    }

    @Override
    public void onUserInteraction() {
        resetTimer();
        super.onUserInteraction();
    }

    protected abstract long getNoInteractionTimeout();

    protected void resetTimer() {
        if (timerDisposable != null) {
            timerDisposable.dispose();
        }

        timerDisposable = new CountdownTimer(getNoInteractionTimeout())
                .getObservable()
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(final Long timeLeft) {
                        if (timeLeft <= 0) {
                            finish();
                        } else if (timeLeft <= 10) {
                            if (countdownTextView != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        countdownTextView.setText(timeLeft.toString());
                                        countdownTextView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        } else {
                            if (countdownTextView != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        countdownTextView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }
                });
    }
}
