package com.github.pop4959.srbot.trail;

import java.util.ArrayList;
import java.util.List;

public class TrailParticle {
    // int textureId;
    private byte spriteMode; //default, animated, random, sequential
    private int spriteCountX;
    private int spriteCountY;
    private float fps;
    private float lifetime; private float totalLifetime;
    private boolean fade;
    private Vector2 scale;
    private float scaleSpeed;
    private float rotation;
    private float rotationSpeed;
    private Color color;
    private float opacity;

    private Vector2 acceleration; //gravity
    private Vector2 velocity;
    private Vector2 position;

    private int imageWidth;
    private int imageHeight;

    // precalculated from spriteCountX/Y and imageWidth/Height
    private float spriteWidth;
    private float spriteHeight;
    private float timeSinceLastFrame;

    private int currentFrameIndex;

    private List<Vertex> vertices;

    public TrailParticle() {
        vertices = new ArrayList<>(4);

        spriteMode = 0;
        spriteCountX = 1;
        spriteCountY = 1;
        fps = 30;
        lifetime = 1.0f;
        totalLifetime = 1.0f;
        fade = false;
        scale = new Vector2(1, 1);
        scaleSpeed = 0;
        rotation = 0;
        rotationSpeed = 0;
        color = new Color(1, 1, 1, 1);
        opacity = 1;

        acceleration = new Vector2(0, 0);
        velocity = new Vector2(0, 0);
        position = new Vector2(0, 0);

        imageWidth = 1;
        imageHeight = 1;

        spriteWidth = imageWidth / spriteCountX;
        spriteHeight = imageHeight / spriteCountY;

        currentFrameIndex = 0;
    }

    public TrailParticle(byte spriteMode, int spriteCountX, int spriteCountY, float fps, float lifetime, boolean fade,
                         Vector2 scale, float scaleSpeed, float rotation, float rotationSpeed, Color color, float opacity,
                         Vector2 position, Vector2 velocity, Vector2 acceleration, int initialFrameIndex,
                         int imageWidth, int imageHeight) {
        this.spriteMode = spriteMode;
        this.spriteCountX = spriteCountX;
        this.spriteCountY = spriteCountY;
        this.fps = fps;
        this.lifetime = lifetime;
        this.totalLifetime = lifetime;
        this.fade = fade;
        this.scale = scale;
        this.scaleSpeed = scaleSpeed;
        this.rotation = rotation;
        this.rotationSpeed = rotationSpeed;
        this.color = color;
        this.opacity = opacity;
        this.acceleration = acceleration;
        this.position = position;
        this.velocity = velocity;

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        spriteWidth = imageWidth / spriteCountX;
        spriteHeight = imageHeight / spriteCountY;

        currentFrameIndex = initialFrameIndex;
        if (spriteMode == 2) { //random
            currentFrameIndex = getRandomFrameIndex();
        }

        vertices = new ArrayList<>(4);
    }

    public float getRemainingLifetime() {
        return lifetime;
    }

    public Vector2 getScale() {
        return scale;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void updateVertexBuffer(float deltaTime) {
        vertices = new ArrayList<>(4);
        float alpha = fade ? (lifetime / totalLifetime) : 1.0f;
        alpha *= opacity;
        Color vertexColor = new Color(color.r, color.g, color.b, alpha); //color.a is always 1
        int frameX = currentFrameIndex % spriteCountX;
        int frameY = currentFrameIndex / spriteCountX;
        float u0, v0, u1, v1;
        switch (spriteMode) {
            case 0: //default (display full image, no animation)
                u0 = 0.0f;
                v0 = 0.0f;
                u1 = 1.0f;
                v1 = 1.0f;
                break;
            case 1: //animated
                u0 = frameX * spriteWidth / imageWidth;
                v0 = frameY * spriteHeight / imageHeight;
                u1 = u0 + spriteWidth / imageWidth;
                v1 = v0 + spriteHeight / imageHeight;
                updateSpriteFrame(deltaTime);
                break;
            case 2: //random (chosen at initialization, does not change)
            case 3: //sequential (chosen at initialization, changes every frame)
                u0 = frameX * spriteWidth / imageWidth;
                v0 = frameY * spriteHeight / imageHeight;
                u1 = u0 + spriteWidth / imageWidth;
                v1 = v0 + spriteHeight / imageHeight;
                break;
            default:
                throw new IllegalArgumentException("Invalid sprite mode: " + spriteMode);
        }

        float scaledHalfWidth = spriteWidth * scale.x / 2.0f;
        float scaledHalfHeight = spriteHeight * scale.y / 2.0f;

        Vector2 topLeft = new Vector2(-scaledHalfWidth, -scaledHalfHeight);
        topLeft = Vector2.rotate(topLeft, rotation);
        Vector2 bottomLeft = new Vector2(-topLeft.y, topLeft.x);
        Vector2 topRight = new Vector2(topLeft.y, -topLeft.x);
        Vector2 bottomRight = new Vector2(-topLeft.x, -topLeft.y);

        vertices.add(new Vertex(Vector2.sum(position, topLeft), vertexColor, new Vector2(u0, v0)));
        vertices.add(new Vertex(Vector2.sum(position, bottomLeft), vertexColor, new Vector2(u0, v1)));
        vertices.add(new Vertex(Vector2.sum(position, topRight), vertexColor, new Vector2(u1, v0)));
        vertices.add(new Vertex(Vector2.sum(position, bottomRight), vertexColor, new Vector2(u1, v1)));
    }

    public void update(float deltaTime) {
        velocity = Vector2.sum(velocity, Vector2.scale(acceleration, deltaTime));
        position = Vector2.sum(position, Vector2.scale(velocity, deltaTime));
        rotation += rotationSpeed * deltaTime;
        scale = Vector2.sum(scale, Vector2.scale(new Vector2(scaleSpeed, scaleSpeed), deltaTime));
        lifetime -= deltaTime;
    }

    private int getRandomFrameIndex() {
        return (int)(Math.random() * spriteCountX * spriteCountY);
    }

    private void updateSpriteFrame(float deltaTime) {
        timeSinceLastFrame += deltaTime;
        float frameDuration = 1.0f / fps;
        while (timeSinceLastFrame >= frameDuration) {
            timeSinceLastFrame -= frameDuration;
            currentFrameIndex = (currentFrameIndex + 1) % (spriteCountX * spriteCountY);
        }
    }

}
