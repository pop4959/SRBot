package com.github.pop4959.srbot.trail;

import java.util.HashMap;

public abstract class TrailLayer {
    public int order;
    private byte layerType; //0 = stripe, 1 = particle, 2 = animation
    public VertexArray vertexArray;
    public String image;

    public TrailLayer(int order, byte layerType) {
        this.order = order;
        this.vertexArray = new VertexArray();
        this.layerType = layerType;
        this.image = null;
    }

    public byte getLayerType() {
        return layerType;
    }

    public abstract void update(float deltaTime, Vector2 position, Vector2 velocity);
    public abstract void loadFromHashMap(HashMap<String, String> properties);
    public abstract void reset();
}
