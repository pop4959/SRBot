package com.github.pop4959.srbot.trail;

public class Vertex {
    public Vector2 position;
    public Color color;
    public Vector2 textureCoordinate;

    public Vertex(Vector2 position, Color color, Vector2 textureCoordinate) {
        this.position = position;
        this.color = color;
        this.textureCoordinate = textureCoordinate;
    }

    public Vertex(float[] data, int offset) {
        this.position = new Vector2(data[offset], data[offset + 1]);
        this.color = new Color(data[offset + 2], data[offset + 3], data[offset + 4], data[offset + 5]);
        this.textureCoordinate = new Vector2(data[offset + 6], data[offset + 7]);
    }
}
