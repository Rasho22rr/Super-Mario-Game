package com.TETOSOFT.graphics;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class Animation implements AnimationComponent {
    private List<AnimationComponent> components;
    private int currFrameIndex;
    private long animTime;
    private long totalDuration;

    public Animation() {
        this(new ArrayList<>(), 0);
    }

    private Animation(List<AnimationComponent> components, long totalDuration) {
        this.components = components;
        this.totalDuration = totalDuration;
        start();
    }

    @Override
    public Object clone() {
        return new Animation(new ArrayList<>(components), totalDuration);
    }

    @Override
    public synchronized void addFrame(Image image, long duration) {
        AnimFrame frame = new AnimFrame(image, totalDuration + duration);
        components.add(frame);
        totalDuration += duration;
    }

    @Override
    public synchronized void start() {
        animTime = 0;
        currFrameIndex = 0;
        components.forEach(AnimationComponent::start);
    }

    @Override
    public synchronized void update(long elapsedTime) {
        if (!components.isEmpty()) {
            animTime += elapsedTime;
            if (animTime >= totalDuration) {
                animTime = animTime % totalDuration;
                currFrameIndex = 0;
            }

            for (int i = currFrameIndex; i < components.size(); i++) {
                AnimationComponent component = components.get(i);
                component.update(elapsedTime);
                if (component.getEndTime() > animTime) {
                    currFrameIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    public synchronized Image getImage() {
        if (components.isEmpty()) {
            return null;
        } else {
            return components.get(currFrameIndex).getImage();
        }
    }

    @Override
    public long getEndTime() {
        return totalDuration;
    }
}

interface AnimationComponent {
    Object clone();
    void addFrame(Image image, long duration);
    void start();
    void update(long elapsedTime);
    Image getImage();
    long getEndTime();
}

class AnimFrame implements AnimationComponent {
    Image image;
    long endTime;

    public AnimFrame(Image image, long endTime) {
        this.image = image;
        this.endTime = endTime;
    }

    @Override
    public Object clone() {
        return new AnimFrame(image, endTime);
    }

    @Override
    public void addFrame(Image image, long duration) {
        // Not applicable for a single frame
    }

    @Override
    public void start() {
        // No-op
    }

    @Override
    public void update(long elapsedTime) {
        // No-op
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }
}