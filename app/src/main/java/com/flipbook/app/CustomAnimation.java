package com.flipbook.app;

import android.graphics.drawable.AnimationDrawable;
import android.os.SystemClock;

/**
 * Created by Hayden on 2017-03-16.
 */

public class CustomAnimation extends AnimationDrawable {

    private volatile int duration;
    private int currentFrame;

    public CustomAnimation() {
        currentFrame = 0;
        duration = 33;
    }

    @Override
    public void run() {
        int n = getNumberOfFrames();
        currentFrame++;
        if (currentFrame >= n) {
            currentFrame = 0;
        }

        selectDrawable(currentFrame);
        scheduleSelf(this, SystemClock.uptimeMillis() + duration);
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
        unscheduleSelf(this);
        selectDrawable(currentFrame);
        scheduleSelf(this, SystemClock.uptimeMillis() + duration);
    }

}
