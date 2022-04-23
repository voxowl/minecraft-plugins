package com.particubes.witch.Variables;

import java.util.Objects;

public class Pos {

    private int x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0;
    private String world = "";
    private boolean pos1 = false, pos2 = false;

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getZ1() {
        return z1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getZ2() {
        return z2;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public boolean isPos1() {
        return pos1;
    }

    public boolean isPos2() {
        return pos2;
    }

    public void setPos1(int x1, int y1, int z1, String world) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.pos1 = true;
        if (!this.world.equalsIgnoreCase(world)) {
            pos2 = false;
            this.world = world;
        }
    }

    public void setPos2(int x2, int y2, int z2, String world) {
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.pos2 = true;
        if (!this.world.equalsIgnoreCase(world)) {
            pos1 = false;
            this.world = world;
        }
    }

    public int getDeltaX () {
        return Math.abs(x1 - x2) + 1; // +1 because start point count
    }

    public int getDeltaY () {
        return Math.abs(y1 - y2) + 1;
    }

    public int getDeltaZ () {
        return Math.abs(z1 - z2) + 1;
    }

    public int getMinX() {
        return Math.min(x1, x2);
    }

    public int getMinY() {
        return Math.min(y1, y2);
    }

    public int getMinZ() {
        return Math.min(z1, z2);
    }

    public int getMaxX() {
        return Math.max(x1, x2);
    }

    public int getMaxY() {
        return Math.max(y1, y2);
    }

    public int getMaxZ() {
        return Math.max(z1, z2);
    }
}
