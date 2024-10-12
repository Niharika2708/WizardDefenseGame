package wizard;

import processing.core.PApplet;
import processing.core.PImage;

class Monster {
    PApplet p;
    PImage sprite;
    float x, y;
    float speed;
    boolean alive;
    float scale;
    int targetRow, targetCol;
    int currentRow, currentCol;
    int hp, maxHp;
    boolean dying;
    int deathAnimationFrame;
    int deathAnimationLength;

    public Monster(PApplet p, String type, float speed, int hp, float scale) {
        this.p = p;
        this.sprite = p.loadImage("src/main/resources/wizard/" + type + ".png");
        this.speed = speed;
        this.hp = hp;
        this.maxHp = hp;
        this.alive = true;
        this.scale = scale;
        this.dying = false;
        this.deathAnimationFrame = 0;
        this.deathAnimationLength = 4; // Adjust this value for faster animation
    }

    public void setPosition(float x, float y, int row, int col) {
        this.currentRow = row;
        this.currentCol = col;
        this.x = x;
        this.y = y;
    }

    public void setTarget(int row, int col) {
        this.targetRow = row;
        this.targetCol = col;
    }

    public void move(Terrain terrain) {
        if (!alive) return;

        if (!dying) {
            int tileSize = terrain.tileSize;
            float targetX = targetCol * tileSize + tileSize / 2;
            float targetY = targetRow * tileSize + 40 + tileSize / 2;

            float dx = targetX - x;
            float dy = targetY - y;
            float dist = PApplet.dist(x, y, targetX, targetY);

            if (dist > speed) {
                float step = speed / dist;
                x += dx * step;
                y += dy * step;
            } else {
                x = targetX;
                y = targetY;
                currentRow = targetRow;
                currentCol = targetCol;
                if (terrain.terrainMap[targetRow][targetCol] == 'W') {
                    alive = false;
                    return;
                }
                int[] nextTile = terrain.getNextPathTile(currentRow, currentCol);
                if (nextTile != null) {
                    setTarget(nextTile[0], nextTile[1]);
                } else {
                    alive = false;
                }
            }
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void draw() {
        if (alive) {
            if (dying) {
                drawDeathAnimation();
            } else {
                p.image(sprite, x - sprite.width * scale / 2, y - sprite.height * scale / 2, sprite.width * scale, sprite.height * scale); // Center the image on the tile
                drawHealthBar();
            }
        }
    }

    private void drawDeathAnimation() {
        if (deathAnimationFrame < deathAnimationLength) {
            float alpha = PApplet.map(deathAnimationFrame, 0, deathAnimationLength, 255, 0);
            float blur = PApplet.map(deathAnimationFrame, 0, deathAnimationLength, 0, 10);

            PImage blurredSprite = sprite.copy();
            blurredSprite.filter(PApplet.BLUR, blur);

            p.tint(255, alpha);
            p.image(blurredSprite, x - blurredSprite.width * scale / 2, y - blurredSprite.height * scale / 2, blurredSprite.width * scale, blurredSprite.height * scale);
            p.noTint();

            deathAnimationFrame++;
        } else {
            alive = false;
        }
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0 && !dying) {
            dying = true;
            deathAnimationFrame = 0;
        }
    }

    private void drawHealthBar() {
        float barWidth = sprite.width * scale;
        float barHeight = 3; // Adjusted to make the bar thinner
        float healthRatio = (float) hp / maxHp;
        p.fill(255, 0, 0);
        p.noStroke();
        p.rect(x - barWidth / 2, y - sprite.height * scale / 2 - barHeight - 2, barWidth, barHeight);
        p.fill(0, 255, 0);
        p.rect(x - barWidth / 2, y - sprite.height * scale / 2 - barHeight - 2, barWidth * healthRatio, barHeight);
    }
}