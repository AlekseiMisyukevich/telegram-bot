package com.telegram.model;

public class Round {

    private volatile boolean isRegistrationOngoing;
    private volatile boolean isRoundOngoing;
    private volatile byte iteration;

    public Round() {
        this.iteration = 11;
        this.isRegistrationOngoing = true;
        this.isRoundOngoing = false;
    }

    public boolean isRegistrationOngoing() {
        return isRegistrationOngoing;
    }

    public void setRegistrationOngoing(boolean registrationOngoing) {
        isRegistrationOngoing = registrationOngoing;
    }

    public byte getIteration() {
        return iteration;
    }

    public void setIteration(byte iteration) {
        this.iteration = iteration;
    }

    public boolean isRoundOngoing() {
        return isRoundOngoing;
    }

    public void setRoundOngoing(boolean roundOngoing) {
        isRoundOngoing = roundOngoing;
    }

}
