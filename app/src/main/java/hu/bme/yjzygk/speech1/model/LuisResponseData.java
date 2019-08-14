package hu.bme.yjzygk.speech1.model;

import java.util.List;

public class LuisResponseData {
    public String query;
    public LuisIntent topScoringIntent;
    public List<LuisIntent> intents;
    public List<LuisEntity> entities;
}
