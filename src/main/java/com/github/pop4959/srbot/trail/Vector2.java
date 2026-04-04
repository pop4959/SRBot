package com.github.pop4959.srbot.trail;

public class Vector2 {
    public float x, y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 normalize(Vector2 x) {
        float length = length(x);
        if (length == 0) {
            return new Vector2(0,0);
        } else {
            return new Vector2(x.x / length, x.y / length);
        }
    }

    public static float length(Vector2 x) {
        return (float)Math.sqrt(x.x * x.x + x.y * x.y);
    }

    public static Vector2 sum(Vector2... a) {
        float sumX = 0;
        float sumY = 0;
        for (Vector2 v : a) {
            sumX += v.x;
            sumY += v.y;
        }
        return new Vector2(sumX, sumY);
    }

    public static Vector2 subtract(Vector2 a, Vector2 b) {
        return new Vector2(a.x - b.x, a.y - b.y);
    }

    public static Vector2 scale(Vector2 a, float scalar) {
        return new Vector2(a.x * scalar, a.y * scalar);
    }

    public static Vector2 rotate(Vector2 a, float angle) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        return new Vector2(a.x * cos - a.y * sin, a.x * sin + a.y * cos);
    }

    public static Vector2 transform(Vector2 vec, Vector2 normal) {
        float angle = (float)Math.atan2(normal.y, normal.x);
        return rotate(vec, angle);
    }

    public static float distance(Vector2 a, Vector2 b) {
        return length(subtract(a, b));
    }

    public static Vector2 parse(String s) {
        String[] parts = s.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Vector2 format: " + s);
        }
        return new Vector2(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
    }
}