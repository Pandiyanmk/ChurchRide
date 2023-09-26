package com.app.chruchridedriver.util;

import android.os.CountDownTimer;

public class DynamicCountdownTimer {

    private CountDownTimer timer = null;
    private double negativeBias = 0.00;
    private double addingBias = 0.00;
    private Long seconds = Long.parseLong("" + 10000);
    private int ticks = 0;
    private boolean supressFinish = false;

    public DynamicCountdownTimer(Long seconds, int ticks) {
        setTimer(seconds, ticks);
    }

    public void updateSeconds(Long seconds) {
        if (timer != null) {
            this.supressFinish = true;
            this.timer.cancel();
            this.timer = null;
            this.seconds = seconds;
            this.addingBias = this.negativeBias + this.addingBias;
            setTimer(this.seconds, this.ticks);
            Start();
        }
    }

    public void setTimer(Long seconds, int ticks) {
        this.seconds = seconds;
        this.ticks = ticks;
        timer = new CountDownTimer(DynamicCountdownTimer.this.seconds, ticks) {
            @Override
            public void onTick(long l) {
                negativeBias = DynamicCountdownTimer.this.seconds - l;
                long calculatedTime = l - (long) addingBias;
                if (calculatedTime <= 0) {
                    onFinish();
                } else {
                    callback.onTick(calculatedTime);
                }
            }

            @Override
            public void onFinish() {
                if (!supressFinish) {
                    callback.onFinish();
                }
                supressFinish = false;
            }
        };
    }

    public void Start() {
        if (timer != null) {
            timer.start();
        }
    }

    public void Cancel() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public DynamicCountdownCallback callback = null;

    public void setDynamicCountdownCallback(DynamicCountdownCallback c) {
        callback = c;
    }


    public interface DynamicCountdownCallback {
        void onTick(long l);

        void onFinish();
    }

}
