package wizard;

import processing.core.PApplet;

public class Button {

    PApplet p;
    float x, y, w, h;
    String label;
    String description;
    boolean isSelected;
    char key;
    float descriptionWidth; 
    int cost;

    Button(PApplet p, float x, float y, float w, float h, String label, char key, String description, int cost) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.label = label;
        this.isSelected = false;
        this.key = key;
        this.description = description;
        this.descriptionWidth = p.width - (x + w + 20);
        this.cost = cost;
    }

    void draw() {
        p.strokeWeight(3);
        p.stroke(0);
        if (isSelected) {
            p.fill(255, 255, 0); // Gray when hovered
        } else if (isMouseOver()) {
            p.fill(150); // Yellow when selected
        } else {
            p.fill(122, 98, 55);
        }
        p.rect(x, y, w, h);
        
        p.textSize(18);
        p.fill(0);
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.text(label, x + w / 2, y + h / 2);
        
        // Draw the description text next to the button
        p.textSize(12);
        p.textAlign(PApplet.LEFT, PApplet.CENTER);
        drawDescription(x + w + 10, y + h / 2);

        // Draw the cost tooltip if hovered
        if (isMouseOver() && (label.equals("T") || label.equals("M"))) {
            drawCostTooltip();
        }
    }

    void drawDescription(float startX, float startY) {
        String[] words = description.split(" ");
        StringBuilder currentLine = new StringBuilder();
        float lineHeight = 14;
        float yOffset = -lineHeight / 2;

        for (String word : words) {
            if (p.textWidth(currentLine + word) < descriptionWidth) {
                currentLine.append(word).append(" ");
            } else {
                p.text(currentLine.toString(), startX, startY + yOffset);
                yOffset += lineHeight;
                currentLine = new StringBuilder(word + " ");
            }
        }

        if (currentLine.length() > 0) {
            p.text(currentLine.toString(), startX, startY + yOffset);
        }
    }

    void drawCostTooltip() {
        float tooltipX = x - 90; // Position to the left
        float tooltipY = y + h / 2;
        float tooltipWidth = 80;
        float tooltipHeight = 24;

        p.fill(255);
        p.stroke(0);
        p.strokeWeight(1);
        p.rect(tooltipX, tooltipY - tooltipHeight / 2, tooltipWidth, tooltipHeight);

        p.fill(0);
        p.textSize(12);
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.text("Cost: " + cost, tooltipX + tooltipWidth / 2, tooltipY);
    }

    boolean isMouseOver() {
        return p.mouseX > x && p.mouseX < x + w && p.mouseY > y && p.mouseY < y + h;
    }

    void toggle() {
        isSelected = !isSelected;
    }

    void handleMousePressed() {
        if (isMouseOver()) {
            toggle();
            p.redraw();
        }
    }

    void handleKeyPressed(char k) {
        if (k == key) {
            toggle();
        }
    }
}