package wizard;

import processing.core.PApplet;
import processing.core.PImage;

public class Fireball {
    PApplet p;
    PImage fireball;
    float x, y;
    float targetX, targetY;
    float speed;
    int damage;

    public Fireball(PApplet p, float x, float y, float targetX, float targetY, int damage) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.targetX = targetX;
        this.targetY = targetY;
        this.speed = 5;
        this.damage = damage;
    }

    public void update() {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = PApplet.dist(x, y, targetX, targetY);

        if (dist > speed) {
            x += speed * (dx / dist);
            y += speed * (dy / dist);
        } else {
            x = targetX;
            y = targetY;
        }
    }

    public void draw() {

        fireball = p.loadImage("src/main/resources/wizard/fireball.png");
        fireball.resize(15, 15);
        p.image(fireball, x - fireball.width / 2, y - fireball.height / 2);
    }

    public boolean hasHitTarget() {
        return PApplet.dist(x, y, targetX, targetY) < speed;
    }

}


