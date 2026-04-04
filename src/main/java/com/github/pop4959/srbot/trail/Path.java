package com.github.pop4959.srbot.trail;

public class Path {
    private float deltaTime;
    private Vector2 previousPosition;

    public Path(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    public Vector2 calculateVelocity(Vector2 position) {
        if (previousPosition == null) {
            previousPosition = new Vector2(position.x, position.y);
        }
        Vector2 velocity = new Vector2(position.x - previousPosition.x, position.y - previousPosition.y);
        previousPosition = new Vector2(position.x, position.y);
        return Vector2.scale(velocity, 1.0f / deltaTime);
    }

    public void reset() {
        previousPosition = null;
    }

    public Vector2 infinityPath(float time, float radius, Vector2 center, float speed) {
        time *= speed;
        return Vector2.sum(
            center,
            new Vector2(
                radius * (float)Math.cos(time) / (1 + (float)Math.sin(time) * (float)Math.sin(time)),
                radius * (float)Math.cos(time) * (float)Math.sin(time) / (1 + (float)Math.sin(time) * (float)Math.sin(time))
            )
        );
    }

}
