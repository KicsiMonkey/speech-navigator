package hu.bme.yjzygk.speech1.model;

import android.support.annotation.NonNull;

public class LuisEntity implements Comparable<LuisEntity> {
    public String entity;
    public String type;
    public int startIndex;
    public int endIndex;
    public float score;

    @Override
    public int compareTo(@NonNull LuisEntity o) {
        if (this.score > o.score) return -1;
        if (this.score < o.score) return 1;
        return 0;
    }
}
