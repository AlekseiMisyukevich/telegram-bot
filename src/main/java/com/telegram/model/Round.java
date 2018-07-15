package com.telegram.model;

public class Round {

    private volatile boolean isRegistrationOngoing;
    private volatile boolean isRoundOngoing;
    private volatile byte iteration;

    public Round() {
        this.iteration = 10;
        this.isRegistrationOngoing = false;
        this.isRoundOngoing = false;
    }

    public boolean isRegistrationOngoing() {
        return isRegistrationOngoing;
    }

    public void setRegistrationOngoing(boolean registrationOngoing) {
        isRegistrationOngoing = registrationOngoing;
    }

    public boolean isRoundOngoing() {
        return isRoundOngoing;
    }

    public void setRoundOngoing(boolean roundOngoing) {
        isRoundOngoing = roundOngoing;
    }

    public byte getIteration() {
        return iteration;
    }

    public void setIteration(byte iteration) {
        this.iteration = iteration;
    }
}
