package com.github.pop4959.srbot.trail;

import java.util.HashMap;

public class TrailAnimation extends TrailLayer {

    private byte enabled; //0 = never, 1 = always, 2 = only at superspeed, 3 = not at superspeed
    // private String layer;
    private boolean visible;
    private Vector2 spriteCount;
    private int startFrame;
    private int endFrame;
    private float fps;
    private byte loop; //0 = loop, 1 = ping pong, 2 = once then freeze, 3 = once then disappear
    private Color color;
    private float opacity;
    private Vector2 offset;
    private float scale;
    private float fadeIn;
    private float fadeOut;
    private float scaleOut;
    private boolean forceRightSideUp;
    private boolean rotateWithPlayer;
    private boolean moveWhenInactive;

    private int imageWidth;
    private int imageHeight;

    private int currentFrame;
    private float notFastEnoughTimer; //only counts when player is not moving fast enough
    private float frameTimer; //counts time since last frame change, resets when frame changes
    private boolean pingPongForward;

    private Vector2 lastPosition;
    private float lastRotation;
    private float fadeValue;
    private boolean activeSpeedReachedLastFrame;

    public TrailAnimation() {
        super(0, (byte)2);

        imageWidth = 0;
        imageHeight = 0;
        currentFrame = 0;
        frameTimer = 0;
        notFastEnoughTimer = 0;
        pingPongForward = true;
        lastPosition = new Vector2(0, 0);
        lastRotation = 0.0f;
        fadeValue = 0.0f;

        enabled = 0;
        // layer = "TrailBehindLocalPlayersLayer";
        image = "";
        visible = true;
        spriteCount = new Vector2(1, 1);
        startFrame = 0;
        endFrame = 0;
        fps = 30;
        loop = 0;
        color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        opacity = 1.0f;
        offset = new Vector2(0, 0);
        scale = 1.0f;
        fadeIn = 0.0f;
        fadeOut = 0.0f;
        scaleOut = 0.0f;
        forceRightSideUp = false;
        rotateWithPlayer = true;
        moveWhenInactive = true;
    }

    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public String getImage() {
        return image;
    }

    public boolean getIsVisible() {
        return visible;
    }

    public byte getEnabled() {
        return enabled;
    }

    public void loadImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    private void nextFrame() {
        int frameCount = endFrame - startFrame + 1;
        if (frameCount <= 1) {
            return;
        }
        switch (loop) {
            // loop
            case 0 -> currentFrame = (currentFrame + 1) % frameCount + startFrame;
            // ping pong
            case 1 -> {
                if (pingPongForward) {
                    if (currentFrame < endFrame) {
                        currentFrame++;
                    } else {
                        pingPongForward = false;
                        currentFrame--;
                    }
                } else {
                    if (currentFrame > startFrame) {
                        currentFrame--;
                    } else {
                        pingPongForward = true;
                        currentFrame++;
                    }
                }
            }
            // once then freeze
            case 2 -> {
                if (currentFrame < endFrame) {
                    currentFrame++;
                }
            }
            // once then disappear
            case 3 -> {
                if (currentFrame < endFrame) {
                    currentFrame++;
                } else {
                    visible = false;
                }
            }
        }
    }

    @Override
    public void update(float deltaTime, Vector2 position, Vector2 velocity) {
        vertexArray.setSize(4);
        float spriteWidth = imageWidth / spriteCount.x;
        float spriteHeight = imageHeight / spriteCount.y;
        if (spriteWidth <= 0 || spriteHeight <= 0) {
            return;
        }

        if (frameTimer >= 1.0f / fps) {
            nextFrame();
            frameTimer = 0;
        }

        float speed = Vector2.length(velocity);
        boolean activeSpeedReached = (enabled == 1 && speed >= Trail.AFTERIMAGE_THRESHOLD) || (enabled == 2 && speed >= Trail.SUPERSPEED_THRESHOLD) || (enabled == 3 && speed < Trail.SUPERSPEED_THRESHOLD && speed >= Trail.AFTERIMAGE_THRESHOLD);

        float fadeValueIncrementUp = fadeIn > 0 ? deltaTime / fadeIn : Float.MAX_VALUE;
        float fadeValueIncrementDown = fadeOut > 0 ? deltaTime / fadeOut : Float.MAX_VALUE;
        float scaleValue = 1.0f;
        if (activeSpeedReached) {
            fadeValue = Math.min(1.0f, fadeValue + fadeValueIncrementUp);
        } else {
            fadeValue = Math.max(0.0f, fadeValue - fadeValueIncrementDown);
        }
        if (!activeSpeedReached && scaleOut > 0) {
            scaleValue = Math.max(0.0f, 1.0f - notFastEnoughTimer / scaleOut);
        }

        Color vertexColor = new Color(color.r, color.g, color.b, color.a * opacity);
        vertexColor.a *= fadeValue;

        float u0 = (currentFrame % (int)spriteCount.x) * spriteWidth / imageWidth;
        float v0 = (currentFrame / (int)spriteCount.x) * spriteHeight / imageHeight;
        float u1 = u0 + spriteWidth / imageWidth;
        float v1 = v0 + spriteHeight / imageHeight;

        float scaledHalfWidth = spriteWidth * scale * scaleValue / 2.0f;
        float scaledHalfHeight = spriteHeight * scale * scaleValue / 2.0f;

        //idk what the devs were thinking with this nonsense, but it's true to the original
        float rotation = rotateWithPlayer ? (float)Math.atan2(velocity.y, velocity.x) : 0;
        if (forceRightSideUp && velocity.x < 0 && !rotateWithPlayer) {
            rotation += (float)Math.PI/2;
        } else if (forceRightSideUp && velocity.x < 0) {
            rotation -= (float)Math.PI/2;
        } else if (!rotateWithPlayer && !forceRightSideUp) {
            rotation = (float)Math.PI/2; //????
        }

        if (!activeSpeedReached && !moveWhenInactive) {
            rotation = lastRotation;
        }

        Vector2 topLeft = new Vector2(-scaledHalfWidth, -scaledHalfHeight);
        topLeft = Vector2.rotate(topLeft, rotation);
        Vector2 bottomLeft = new Vector2(-topLeft.y, topLeft.x);
        Vector2 topRight = new Vector2(topLeft.y, -topLeft.x);
        Vector2 bottomRight = new Vector2(-topLeft.x, -topLeft.y);

        Vector2 positionA = !activeSpeedReached && !moveWhenInactive ? lastPosition : position;

        vertexArray.addVertex(new Vertex(Vector2.sum(positionA, topLeft, offset), vertexColor, new Vector2(u0, v0)));
        vertexArray.addVertex(new Vertex(Vector2.sum(positionA, bottomLeft, offset), vertexColor, new Vector2(u0, v1)));
        vertexArray.addVertex(new Vertex(Vector2.sum(positionA, topRight, offset), vertexColor, new Vector2(u1, v0)));
        vertexArray.addVertex(new Vertex(Vector2.sum(positionA, bottomRight, offset), vertexColor, new Vector2(u1, v1)));

        if (activeSpeedReached) {
            notFastEnoughTimer = 0;
        } else {
            notFastEnoughTimer += deltaTime;
        }
        frameTimer += deltaTime;

        if (activeSpeedReached && !activeSpeedReachedLastFrame) {
            currentFrame = startFrame;
            frameTimer = 0;
        }

        lastPosition = positionA;
        lastRotation = rotation;
        activeSpeedReachedLastFrame = activeSpeedReached;
    }

    @Override
    public void reset() {
        currentFrame = startFrame;
        frameTimer = 0;
        notFastEnoughTimer = 0;
        pingPongForward = true;
        lastPosition = new Vector2(0, 0);
        lastRotation = 0.0f;
        fadeValue = 0.0f;
    }

    @Override
    public void loadFromHashMap(HashMap<String, String> properties) {
        String enabledString = properties.getOrDefault("Enabled", "NEVER");
        String loopString = properties.getOrDefault("Loop", "LOOP");

        switch (enabledString.toUpperCase()) {
            case "NEVER" -> enabled = 0;
            case "ALWAYS" -> enabled = 1;
            case "ONLY AT SUPERSPEED" -> enabled = 2;
            case "NOT AT SUPERSPEED" -> enabled = 3;
            default -> throw new IllegalArgumentException("Invalid Enabled value: " + enabledString);
        }

        // layer = properties.getOrDefault("Layer", "TrailBehindLocalPlayersLayer");
        image = properties.getOrDefault("Image", "");
        visible = properties.getOrDefault("Visible", "TRUE").equalsIgnoreCase("TRUE");
        spriteCount = Vector2.parse(properties.getOrDefault("SpriteCount", "1,1"));
        startFrame = Integer.parseInt(properties.getOrDefault("Start frame", "0"));
        endFrame = Integer.parseInt(properties.getOrDefault("End frame", "0"));
        fps = Float.parseFloat(properties.getOrDefault("FPS", "30"));
        switch (loopString.toUpperCase()) {
            case "LOOP" -> loop = 0;
            case "PING PONG" -> loop = 1;
            case "ONCE THEN FREEZE" -> loop = 2;
            case "ONCE THEN DISSAPEAR" -> loop = 3; //yes it's misspelled in the original
            default -> throw new IllegalArgumentException("Invalid Loop value: " + loopString);
        }
        color = Color.parse(properties.getOrDefault("Color", "1,1,1"));
        opacity = Float.parseFloat(properties.getOrDefault("Opacity", "1"));
        offset = Vector2.parse(properties.getOrDefault("Offset", "0,0"));
        scale = Float.parseFloat(properties.getOrDefault("Scale", "1"));
        fadeIn = Float.parseFloat(properties.getOrDefault("FadeIn", "0"));
        fadeOut = Float.parseFloat(properties.getOrDefault("FadeOut", "0"));
        scaleOut = Float.parseFloat(properties.getOrDefault("ScaleOut", "0"));
        forceRightSideUp = properties.getOrDefault("Force right side Up", "FALSE").equalsIgnoreCase("TRUE");
        rotateWithPlayer = properties.getOrDefault("Rotate with Player", "TRUE").equalsIgnoreCase("TRUE");
        moveWhenInactive = properties.getOrDefault("Move when inactive", "TRUE").equalsIgnoreCase("TRUE");

        currentFrame = startFrame;
    }
}
