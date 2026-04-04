package com.github.pop4959.srbot.trail;

public class VertexArray {
    private float[] vertices;
    private int vertexCount;
    private int index;

    public VertexArray() {
        vertices = new float[0];
        this.index = 0;
        this.vertexCount = 0;
    }

    public VertexArray(int size) {
        vertices = new float[size];
        this.index = 0;
        this.vertexCount = 0;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setSize(int size) {
        vertices = new float[size];
        this.index = 0;
        this.vertexCount = 0;
    }

    public float[] getVertices() {
        return vertices;
    }

    public Vertex getVertex(int i) {
        return new Vertex(vertices, i * 8);
    }
    
    public void addVertex(Vertex vertex) {
        if (index + 8 >= vertices.length) {
            float[] newVertices = new float[vertices.length * 2];
            System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
            vertices = newVertices;
        }

        vertices[index++] = vertex.position.x;
        vertices[index++] = vertex.position.y;
        vertices[index++] = vertex.color.r;
        vertices[index++] = vertex.color.g;
        vertices[index++] = vertex.color.b;
        vertices[index++] = vertex.color.a;
        vertices[index++] = vertex.textureCoordinate.x;
        vertices[index++] = vertex.textureCoordinate.y;
        vertexCount++;
    }

    public void addVertices(java.util.List<Vertex> vertices) {
        for (Vertex vertex : vertices) {
            addVertex(vertex);
        }
    }
}
