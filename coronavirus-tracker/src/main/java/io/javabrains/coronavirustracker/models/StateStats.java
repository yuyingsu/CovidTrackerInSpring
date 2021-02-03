package io.javabrains.coronavirustracker.models;

public class StateStats {
    private String state;
    private int diffFromPrevDay;
    private int deathDiffFromPrevDay;
    private int cumulativeCases;
    private int cumulativeDeath;
    private double fatalityRate;

    public int getCumulativeDeath() {
        return cumulativeDeath;
    }

    public void setCumulativeDeath(int cumulativeDeath) {
        this.cumulativeDeath = cumulativeDeath;
    }

    public int getCumulativeCases() {
        return cumulativeCases;
    }

    public void setCumulativeCases(int cumulativeCases) {
        this.cumulativeCases = cumulativeCases;
    }

    public double getFatalityRate() {
        return fatalityRate;
    }

    public void setFatalityRate(double fatalityRate) {
        this.fatalityRate = fatalityRate;
    }

    public int getDeathDiffFromPrevDay() {
        return deathDiffFromPrevDay;
    }

    public void setDeathDiffFromPrevDay(int deathDiffFromPrevDay) {
        this.deathDiffFromPrevDay = deathDiffFromPrevDay;
    }

    public int getDiffFromPrevDay() {
        return diffFromPrevDay;
    }

    public void setDiffFromPrevDay(int diffFromPrevDay) {
        this.diffFromPrevDay = diffFromPrevDay;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
