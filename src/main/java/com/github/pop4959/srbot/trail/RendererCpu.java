package com.github.pop4959.srbot.trail;

import java.awt.image.BufferedImage;

public class RendererCpu {
    private int width;
    private int height;
    private BufferedImage texture;
    private BufferedImage framebuffer;
    public Camera camera;

    public RendererCpu(int width, int height) {
        this.width = width;
        this.height = height;
        this.framebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private float edgeFunction(Vector2 a, Vector2 b, Vector2 c) {
        return (float)((c.x - a.x) * (b.y - a.y) - (c.y - a.y) * (b.x - a.x));
    }

    private float[] calculateInterpolationFactors(Vertex v1, Vertex v2, Vertex v3, Vector2 p) {
        float area = edgeFunction(v1.position, v2.position, v3.position);
        float w0 = edgeFunction(v2.position, v3.position, p) / area;
        float w1 = edgeFunction(v3.position, v1.position, p) / area;
        float w2 = edgeFunction(v1.position, v2.position, p) / area;
        return new float[]{w0, w1, w2};
    }

    private int min(float a, float b, float c) {
        return (int)Math.floor(Math.min(a, Math.min(b, c)));
    }

    private int max(float a, float b, float c) {
        return (int)Math.ceil(Math.max(a, Math.max(b, c)));
    }

    private boolean isTopLeftEdge(Vector2 a, Vector2 b) {
        return (a.y < b.y) || (a.y == b.y && a.x > b.x);
    }

    public void drawTriangle(Vertex v0, Vertex v1, Vertex v2) {
        Vertex sv0 = vertexShader(v0);
        Vertex sv1 = vertexShader(v1);
        Vertex sv2 = vertexShader(v2);

        int minX = min(sv0.position.x, sv1.position.x, sv2.position.x);
        int maxX = max(sv0.position.x, sv1.position.x, sv2.position.x);
        int minY = min(sv0.position.y, sv1.position.y, sv2.position.y);
        int maxY = max(sv0.position.y, sv1.position.y, sv2.position.y);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (x < 0 || x >= width || y < 0 || y >= height) {
                    continue;
                }

                Vector2 p = new Vector2(x + 0.5f, y + 0.5f);
                float[] factors = calculateInterpolationFactors(sv0, sv1, sv2, p);

                boolean e0 = isTopLeftEdge(sv1.position, sv2.position);
                boolean e1 = isTopLeftEdge(sv2.position, sv0.position);
                boolean e2 = isTopLeftEdge(sv0.position, sv1.position);

                boolean inside = (factors[0] > 0 || (factors[0] == 0 && e0)) &&
                                 (factors[1] > 0 || (factors[1] == 0 && e1)) &&
                                 (factors[2] > 0 || (factors[2] == 0 && e2));

                if (!inside) {
                    continue;
                }

                Vector2 normalizedFragCoord = new Vector2(p.x / width, p.y / height);
                Color color = fragmentShader(normalizedFragCoord,
                    new Color(
                        factors[0] * sv0.color.r + factors[1] * sv1.color.r + factors[2] * sv2.color.r,
                        factors[0] * sv0.color.g + factors[1] * sv1.color.g + factors[2] * sv2.color.g,
                        factors[0] * sv0.color.b + factors[1] * sv1.color.b + factors[2] * sv2.color.b,
                        factors[0] * sv0.color.a + factors[1] * sv1.color.a + factors[2] * sv2.color.a
                    ),
                    new Vector2(factors[0] * sv0.textureCoordinate.x + factors[1] * sv1.textureCoordinate.x + factors[2] * sv2.textureCoordinate.x,
                                factors[0] * sv0.textureCoordinate.y + factors[1] * sv1.textureCoordinate.y + factors[2] * sv2.textureCoordinate.y)
                );
                Color existingColor = new Color(
                    ((framebuffer.getRGB(x, y) >> 16) & 0xFF) / 255f,
                    ((framebuffer.getRGB(x, y) >> 8) & 0xFF) / 255f,
                    (framebuffer.getRGB(x, y) & 0xFF) / 255f,
                    ((framebuffer.getRGB(x, y) >> 24) & 0xFF) / 255f
                );
                Color blendedColor = Color.blend(color, existingColor);
                framebuffer.setRGB(x, y, blendedColor.toRGB());
            }
        }
    }

    public void drawTriangleStrip(VertexArray vertexArray, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex - 2; i++) {
            if (i % 2 == 0) {
                drawTriangle(vertexArray.getVertex(i), vertexArray.getVertex(i + 1), vertexArray.getVertex(i + 2));
            } else {
                drawTriangle(vertexArray.getVertex(i), vertexArray.getVertex(i + 2), vertexArray.getVertex(i + 1));
            }
        }
    }

    public void drawTriangleStrip(VertexArray vertexArray) {
        drawTriangleStrip(vertexArray, 0, vertexArray.getVertexCount());
    }

    public void drawQuads(VertexArray vertexArray) {
        for (int i = 0; i < vertexArray.getVertexCount() - 3; i += 4) {
            drawTriangle(vertexArray.getVertex(i), vertexArray.getVertex(i + 1), vertexArray.getVertex(i + 2));
            drawTriangle(vertexArray.getVertex(i + 2), vertexArray.getVertex(i + 3), vertexArray.getVertex(i + 1));
        }
    }

    private Color sampleTextureNearest(Vector2 textureCoordinate) {
        BufferedImage texture = getTexture();
        int x = (int)(textureCoordinate.x * (texture.getWidth() - 1));
        int y = (int)(textureCoordinate.y * (texture.getHeight() - 1));
        int rgb = texture.getRGB(x, y);
        return new Color(
            ((rgb >> 16) & 0xFF) / 255f,
            ((rgb >> 8) & 0xFF) / 255f,
            (rgb & 0xFF) / 255f,
            ((rgb >> 24) & 0xFF) / 255f
        );
    }
    
    private Color sampleTextureBilinear(Vector2 textureCoordinate) {
        BufferedImage texture = getTexture();
        float x = textureCoordinate.x * (texture.getWidth() - 1);
        float y = textureCoordinate.y * (texture.getHeight() - 1);
        int x0 = (int)Math.floor(x);
        int x1 = (int)Math.ceil(x);
        int y0 = (int)Math.floor(y);
        int y1 = (int)Math.ceil(y);

        Color c00 = sampleTextureNearest(new Vector2(x0 / (float)texture.getWidth(), y0 / (float)texture.getHeight()));
        Color c10 = sampleTextureNearest(new Vector2(x1 / (float)texture.getWidth(), y0 / (float)texture.getHeight()));
        Color c01 = sampleTextureNearest(new Vector2(x0 / (float)texture.getWidth(), y1 / (float)texture.getHeight()));
        Color c11 = sampleTextureNearest(new Vector2(x1 / (float)texture.getWidth(), y1 / (float)texture.getHeight()));

        float tx = x - x0;
        float ty = y - y0;

        return new Color(
            c00.r * (1 - tx) * (1 - ty) + c10.r * tx * (1 - ty) + c01.r * (1 - tx) * ty + c11.r * tx * ty,
            c00.g * (1 - tx) * (1 - ty) + c10.g * tx * (1 - ty) + c01.g * (1 - tx) * ty + c11.g * tx * ty,
            c00.b * (1 - tx) * (1 - ty) + c10.b * tx * (1 - ty) + c01.b * (1 - tx) * ty + c11.b * tx * ty,
            c00.a * (1 - tx) * (1 - ty) + c10.a * tx * (1 - ty) + c01.a * (1 - tx) * ty + c11.a * tx * ty
        );
    }

    private Color sampleTexture(Vector2 textureCoordinate) {
        return sampleTextureBilinear(textureCoordinate);
    }

    private Vertex vertexShader(Vertex vertex) {
        if (camera == null) {
            return vertex;
        }
        Vector2 center = new Vector2(width / 2, height / 2);
        Vertex transformedVertex = new Vertex(
            new Vector2(
                (int)((vertex.position.x - camera.position.x - center.x) * camera.zoom + center.x),
                (int)((vertex.position.y - camera.position.y - center.y) * camera.zoom + center.y)
            ),
            vertex.color,
            vertex.textureCoordinate
        );
        return transformedVertex;
    }

    private Color fragmentShader(Vector2 fragCoord, Color color, Vector2 textureCoordinate) {
        return Color.multiply(color, sampleTexture(textureCoordinate));
    }

    private BufferedImage getTexture() {
        if (texture == null) {
            // Placeholder for texture loading logic
            texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            texture.setRGB(0, 0, 0xFFFFFFFF); // White pixel
        }
        return texture;
    }

    public void setTexture(BufferedImage texture) {
        this.texture = texture;
    }

    public void clear(Color clearColor) {
        int rgb = clearColor.toRGB();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                framebuffer.setRGB(x, y, rgb);
            }
        }
    }

    public void clear(int rgb) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                framebuffer.setRGB(x, y, rgb);
            }
        }
    }

    public void saveToFile(String filename) {
        try {
            javax.imageio.ImageIO.write(framebuffer, "png", new java.io.File(filename));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getFramebuffer() {
        return framebuffer;
    }
}
