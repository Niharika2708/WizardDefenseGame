package wizard;

import processing.core.PApplet;
import processing.core.PImage;
import java.util.*;

public class Tower {
    PApplet p;
    PImage sprite;
    float x, y;
    int range;
    float firingSpeed; // Fireballs per second
    int damage;
    int rangeLevel;
    int speedLevel;
    int damageLevel;
    float fireballCooldown; // Time until the next fireball can be shot
    float pulsateFactor;
    float rotationAngle;
    float flashAlpha;
    List<Fireball> fireballs;

    public Tower(PApplet p, float x, float y, int initialRange, float initialFiringSpeed, int initialDamage, int initialRangeLevel, int initialSpeedLevel, int initialDamageLevel) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.range = initialRange;
        this.firingSpeed = initialFiringSpeed;
        this.damage = initialDamage;
        this.rangeLevel = initialRangeLevel;
        this.speedLevel = initialSpeedLevel;
        this.damageLevel = initialDamageLevel;
        this.fireballCooldown = 0;
        this.fireballs = new ArrayList<>();
        this.pulsateFactor = 0;
        this.rotationAngle = 0;
        this.flashAlpha = 0;

        updateSprite();
    }

    private void updateSprite() {
        if (rangeLevel >= 2 && speedLevel >= 2 && damageLevel >= 2) {
            sprite = p.loadImage("src/main/resources/wizard/tower-3.png");
        } else if (rangeLevel >= 1 && speedLevel >= 1 && damageLevel >= 1) {
            sprite = p.loadImage("src/main/resources/wizard/tower-2.png");
        } else {
            sprite = p.loadImage("src/main/resources/wizard/tower-1.png");
        }
        sprite.resize(32, 32);
    }

    public void draw() {

        // Draw range circle if mouse is hovering over the tower
        if (isMouseHovering()) {
            p.noFill();
            p.stroke(255, 255, 0);
            p.strokeWeight(2);
            p.pushMatrix();
            p.translate(x, y);
            p.ellipse(0, 0, range * 2, range * 2);
            p.popMatrix();
        }

        p.image(sprite, x - sprite.width / 2, y - sprite.height / 2);

        // Draw upgrade animations
        drawUpgradeAnimations();

        // Draw fireballs
        Iterator<Fireball> iter = fireballs.iterator();
        while (iter.hasNext()) {
            Fireball fireball = iter.next();
            fireball.draw();
            if (fireball.hasHitTarget()) {
                iter.remove();
            }
        }
    }

    private void drawUpgradeAnimations() {

        p.textSize(12);

        if (rangeLevel > 0 && isMouseHovering()) {
            pulsateFactor = (PApplet.sin(p.frameCount * 0.1f) + 1) * 8;
            p.stroke(0, 255, 0, 150);
            p.noFill();
            p.ellipse(x, y, range * 2 + pulsateFactor, range * 2 + pulsateFactor);
        }

        if (speedLevel > 0) {
            rotationAngle += 0.1;
            p.pushMatrix();
            p.translate(x, y);
            p.rotate(rotationAngle);
            p.fill(0, 255, 255);
            p.text("o", -5, -5);
            p.popMatrix();
        }

        if (damageLevel > 0) {
            flashAlpha = PApplet.sin(p.frameCount * 0.2f) * 255;
            p.fill(220, 20, 60, flashAlpha);
            p.noStroke();
            p.rect(x - sprite.width / 2 + 2, y + sprite.height / 2 - 5, sprite.width - 4, 5);
        }
    }

    public boolean isMouseHovering() {
        return p.mouseX > x - sprite.width / 2 && p.mouseX < x + sprite.width / 2 &&
                p.mouseY > y - sprite.height / 2 && p.mouseY < y + sprite.height / 2;
    }

    public void upgradeRange() {
        range += 32; // Increase the range by 32 pixels (1 tile)
        rangeLevel++;
        updateSprite();
    }

    public void upgradeSpeed() {
        firingSpeed += 0.5; // Increase firing speed by 0.5 fireballs per second
        speedLevel++;
        updateSprite();
    }

    public void upgradeDamage() {
        damage += 50; // Increase damage by half of initial_tower_damage
        damageLevel++;
        updateSprite();
    }

    public void update() {

        fireballCooldown -= 1.0 / p.frameRate; // Decrease cooldown over time

        if (fireballCooldown <= 0) {
            // Find a target monster within range and shoot
            Monster target = findTarget();
            if (target != null) {
                fireballCooldown = 1.0f / firingSpeed; // Reset cooldown
                fireballs.add(new Fireball(p, x, y, target.x, target.y, damage));
            }
        }

        Iterator<Fireball> iter = fireballs.iterator();
        while (iter.hasNext()) {
            Fireball fireball = iter.next();
            fireball.update();
            if (fireball.hasHitTarget()) {
                Monster target = findTarget();
                if (target != null && target.isAlive()) {
                    target.takeDamage(fireball.damage); // Apply damage to the monster
                    if (!target.isAlive()) {
                        target.dying = true; // Mark the monster as dying
                    }
                }
                iter.remove();
            }
        }
    }

    private Monster findTarget() {
        for (Wave wave : ((App) p).waves) {
            for (Monster monster : wave.activeMonsters) {
                float distance = PApplet.dist(x, y, monster.x, monster.y);
                if (distance <= range && monster.isAlive()) {
                    return monster;
                }
            }
        }
        return null;
    }
}