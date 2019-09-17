package com.gluonhq.picluster.display.model;

public class Chunk {
    private int x;
    private int y;
    private double opacity;

    public Chunk(int x, int y, double opacity) {
        this.x = x;
        this.y = y;
        this.opacity = opacity;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public static Chunk parseChunk(String message) {
        String[] split = message.split(",");
        if (split.length == 3) {
            try {
                return new Chunk(Integer.valueOf(split[0]), Integer.valueOf(split[1]),
                        Double.valueOf(split[2]));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Chunk getNextChunk(Chunk chunk, int maxX, int maxY) {
        if (chunk.getX() + 1 < maxX) {
            return new Chunk(chunk.getX() + 1, chunk.getY(), chunk.getOpacity());
        }
        if (chunk.getY() + 1 < maxY) {
            return new Chunk(chunk.getX(), chunk.getY() + 1, chunk.getOpacity());
        }
        return new Chunk(0, 0, chunk.getOpacity());
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "x=" + x +
                ", y=" + y +
                ", opacity=" + opacity +
                '}';
    }
}
