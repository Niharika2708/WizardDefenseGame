package wizard;

import processing.core.PApplet;
import processing.data.JSONObject;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import processing.data.JSONArray;
import java.util.ArrayList;

public class App extends PApplet {

    public static final int WIDTH = 780; 
    public static final int HEIGHT = 680; 

    Terrain terrain;
    int tileSize = 32; 
    int houseTileSize = 64;
    int currentWaveIndex = 0;
    String layoutFile;

    List<Wave> waves;
    int[] spawnPoint;
    List<Button> buttons;
    List<Tower> towers;

    boolean buildTowerMode;
    boolean rangeUpgradeMode;
    boolean speedUpgradeMode;
    boolean damageUpgradeMode;
    boolean manaPoolMode;

    int mana;
    int manaCap;
    float manaGainedPerSecond;
    int towerCost;
    float initialFiringSpeed;

    int rangeUpgradeCost;
    int speedUpgradeCost;
    int damageUpgradeCost;

    int manaPoolSpellInitialCost;
    int manaPoolSpellCostIncreasePerUse;
    float manaPoolSpellCapMultiplier;
    float manaPoolSpellManaGainedMultiplier;
    int manaPoolSpellUses;

    public void settings() {
        size(WIDTH, HEIGHT); 
    }

    public void setup() {
        frameRate(60); 
        JSONObject config = getConfig("config.json");
        layoutFile = config.getString("layout");
        terrain = new Terrain(this, tileSize, houseTileSize, layoutFile);
        spawnPoint = terrain.getStartingPoint(); // Get starting point from terrain
        waves = getWavesFromConfig(config);
        towers = new ArrayList<>();
        mana = config.getInt("initial_mana");
        manaCap = config.getInt("initial_mana_cap");
        manaGainedPerSecond = config.getFloat("initial_mana_gained_per_second");
        initialFiringSpeed = config.getFloat("initial_tower_firing_speed"); // Initialize the firing speed
        towerCost = config.getInt("tower_cost");
        rangeUpgradeCost = 20;
        speedUpgradeCost = 20;
        damageUpgradeCost = 20;

        // Read mana pool spell parameters
        manaPoolSpellInitialCost = config.getInt("mana_pool_spell_initial_cost");
        manaPoolSpellCostIncreasePerUse = config.getInt("mana_pool_spell_cost_increase_per_use");
        manaPoolSpellCapMultiplier = (float) config.getDouble("mana_pool_spell_cap_multiplier");
        manaPoolSpellManaGainedMultiplier = (float) config.getDouble("mana_pool_spell_mana_gained_multiplier");
        manaPoolSpellUses = 0;

        // Initialize buttons
        buttons = new ArrayList<>();
        buttons.add(new Button(this, 650, 50, 52, 52, "FF", 'f', "2x speed", 0));
        buttons.add(new Button(this, 650, 120, 52, 52, "P", 'p', "PAUSE", 0));
        buttons.add(new Button(this, 650, 190, 52, 52, "T", 't', "Build Tower", towerCost));
        buttons.add(new Button(this, 650, 260, 52, 52, "U1", '1', "Upgrade range", rangeUpgradeCost));
        buttons.add(new Button(this, 650, 330, 52, 52, "U2", '2', "Upgrade speed", speedUpgradeCost));
        buttons.add(new Button(this, 650, 400, 52, 52, "U3", '3', "Upgrade damage", damageUpgradeCost));
        buttons.add(new Button(this, 650, 470, 52, 52, "M", 'm', "Mana pool cost: 250", + manaPoolSpellInitialCost));
    }

    public void draw() {
        background(122, 98, 55);
        terrain.draw();

        if (currentWaveIndex < waves.size()) {
            Wave currentWave = waves.get(currentWaveIndex);
            currentWave.update(terrain);
            if (currentWave.waveEnded) {
                currentWaveIndex++;
            }

            // Display countdown timer for the next wave or indicate the current wave
            float timeUntilNextWave = currentWave.preWavePause - (currentWave.frameCount / frameRate);
            fill(0); 
            textSize(20); 
            textAlign(LEFT, TOP);
            if (!currentWave.waveStarted) {
                text("Wave " + (currentWaveIndex + 1) + " starts in: " + String.format("%.0f", timeUntilNextWave), 10, 10);
            } else {
                text("Wave " + (currentWaveIndex + 1) + " in progress", 10, 10);
            }
        }

        Tower hoveredTower = null;
        for (Tower tower : towers) {
        tower.draw();
        tower.update();
        if (tower.isMouseHovering()) {
            hoveredTower = tower;
        }
        }   

        // Draw buttons
        for (Button button : buttons) {
            button.draw();
        }

        // Draw mana bar
        drawManaBar();

        // Increment mana over time
        incrementMana();

        // Display upgrade cost when hovering over a tower
        if (hoveredTower != null) {
        drawInfoTable(hoveredTower);
        }
    }

    private void drawManaBar() {
        float manaBarX = 290; 
        float manaBarY = 10; 
        float manaBarWidth = WIDTH - 300; 
        float manaBarHeight = 24; 
        float manaRatio = (float) mana / manaCap; // Ratio of current mana to mana cap

        fill(255);
        rect(manaBarX, manaBarY, manaBarWidth, manaBarHeight); // Background of the mana bar

        fill(0, 255, 255);
        rect(manaBarX, manaBarY, manaBarWidth * manaRatio, manaBarHeight); // Filled portion of the mana bar

        fill(0);
        textSize(16);
        textAlign(CENTER, CENTER);
        text(mana + " / " + manaCap, manaBarX + manaBarWidth / 2, manaBarY + manaBarHeight / 2); 

        textAlign(RIGHT, CENTER);
        text("MANA:", manaBarX - 10, manaBarY + manaBarHeight / 2);
    }

    private void incrementMana() {
        if (frameCount % frameRate == 0) { // Increment mana once every second
            mana = min(mana + (int) manaGainedPerSecond, manaCap); 
        }
    }

    private void drawInfoTable(Tower tower) {

        float tableX = WIDTH - 130; 
        float tableY = HEIGHT - 90; 
        float tableWidth = 100;
        float tableHeight = 83;
    
        fill(255); 
        stroke(0); 
        strokeWeight(1); 
        rect(tableX, tableY, tableWidth, tableHeight);
    
        fill(0);
        textSize(12);
        textAlign(CENTER, TOP);
    
        float textX = tableX + tableWidth / 2;
        float textY = tableY + 5;
        float lineHeight = 18;
    
        text("Upgrade cost", textX, textY);
        textAlign(LEFT, TOP);
        line(tableX, textY + lineHeight, tableX + tableWidth, textY + lineHeight); // Line under heading
        text("Speed:        " + (speedUpgradeMode ? speedUpgradeCost : 0), tableX + 5, textY + lineHeight + 5);
        text("Damage:     " + (damageUpgradeMode ? damageUpgradeCost : 0), tableX + 5, textY + 2 * lineHeight + 5);
        line(tableX, textY + 3 * lineHeight + 4, tableX + tableWidth, textY + 3 * lineHeight + 4); // Line under damage
        text("Total:          " + getTotalUpgradeCost(), tableX + 5, textY + 3 * lineHeight + 7);
    }
    
    private int getTotalUpgradeCost() {
        int totalCost = 0;
        if (rangeUpgradeMode) totalCost += rangeUpgradeCost;
        if (speedUpgradeMode) totalCost += speedUpgradeCost;
        if (damageUpgradeMode) totalCost += damageUpgradeCost;
        return totalCost;
    }

    private JSONObject getConfig(String configFilename) {
        try {
            Scanner scanner = new Scanner(new File(configFilename));
            StringBuilder jsonContent = new StringBuilder();

            while (scanner.hasNextLine()) {
                jsonContent.append(scanner.nextLine());
            }
            scanner.close();
            return JSONObject.parse(jsonContent.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Wave> getWavesFromConfig(JSONObject config) {
        List<Wave> waves = new ArrayList<>();
        JSONArray waveArray = config.getJSONArray("waves");
    
        for (int i = 0; i < waveArray.size(); i++) {
            JSONObject waveObj = waveArray.getJSONObject(i);
    
            float duration = waveObj.getFloat("duration");
            float preWavePause = waveObj.getFloat("pre_wave_pause");
            JSONArray monstersArray = waveObj.getJSONArray("monsters");
            List<Monster> monsters = new ArrayList<>();
    
            for (int j = 0; j < monstersArray.size(); j++) {
                JSONObject monsterObj = monstersArray.getJSONObject(j);
                String type = monsterObj.getString("type");
                float speed = monsterObj.getFloat("speed");
                int hp = monsterObj.getInt("hp");
                int quantity = monsterObj.getInt("quantity");
    
                for (int k = 0; k < quantity; k++) {
                    monsters.add(new Monster(this, type, speed, hp, 0.07f)); 
                }
            }
            waves.add(new Wave(this, duration, preWavePause, monsters, spawnPoint, 0.07f)); 
        }
        return waves;
    }

    public void mousePressed() {
        // Check if any button was clicked
        for (Button button : buttons) {
            if (button.isMouseOver()) {
                button.handleMousePressed();
                handleButtonAction(button);
                return;
            }
        }
    
        if (buildTowerMode) {
            // Place tower on clicked position
            int row = (mouseY - 40) / tileSize;
            int col = mouseX / tileSize;
            int totalCost = towerCost;
            int initialRangeLevel = rangeUpgradeMode ? 1 : 0;
            int initialSpeedLevel = speedUpgradeMode ? 1 : 0;
            int initialDamageLevel = damageUpgradeMode ? 1 : 0;
            totalCost += (initialRangeLevel + initialSpeedLevel + initialDamageLevel) * 20;
    
            if (mana >= totalCost && row >= 0 && row < terrain.terrainMap.length && col >= 0 && col < terrain.terrainMap[0].length && terrain.terrainMap[row][col] == ' ') {
                towers.add(new Tower(this, col * tileSize + tileSize / 2, row * tileSize + 40 + tileSize / 2, 96, initialFiringSpeed, 40, initialRangeLevel, initialSpeedLevel, initialDamageLevel));
                mana -= totalCost;
                buildTowerMode = false; // Exit build mode after placing the tower
                rangeUpgradeMode = false;
                speedUpgradeMode = false;
                damageUpgradeMode = false;
            }
        } else if (rangeUpgradeMode || speedUpgradeMode || damageUpgradeMode) {
            // Upgrade tower on clicked position
            for (Tower tower : towers) {
                if (tower.isMouseHovering()) {
                    int totalUpgradeCost = getTotalUpgradeCost();
                    if (mana >= totalUpgradeCost) {
                        if (rangeUpgradeMode) {
                            tower.upgradeRange();
                            mana -= rangeUpgradeCost;
                        }
                        if (speedUpgradeMode) {
                            tower.upgradeSpeed();
                            mana -= speedUpgradeCost;
                        }
                        if (damageUpgradeMode) {
                            tower.upgradeDamage();
                            mana -= damageUpgradeCost;
                        }
                        rangeUpgradeMode = false;
                        speedUpgradeMode = false;
                        damageUpgradeMode = false;
                        break;
                    }
                }
            }
        }
    }

    private void handleButtonAction(Button button) {
        if (button.label.equals("T")) {
            toggleBuildTowerMode();
        } else if (button.label.equals("U1")) {
            toggleRangeUpgradeMode();
        } else if (button.label.equals("U2")) {
            toggleSpeedUpgradeMode();
        } else if (button.label.equals("U3")) {
            toggleDamageUpgradeMode();
        } else if (button.label.equals("M")) {
            castManaPoolSpell();
        }
    }

    public void keyPressed() {
        for (Button button : buttons) {
            button.handleKeyPressed(key);
            handleButtonAction(button); 
        }
    
        if (key == 't') {
            toggleBuildTowerMode();
        } else if (key == '1') {
            toggleRangeUpgradeMode();
        } else if (key == '2') {
            toggleSpeedUpgradeMode();
        } else if (key == '3') {
            toggleDamageUpgradeMode();
        }
    }

    public float getInitialFiringSpeed() {
        JSONObject config = getConfig("config.json");
        return config.getFloat("initial_tower_firing_speed");
    }

    private void castManaPoolSpell() {
        int cost = manaPoolSpellInitialCost + (manaPoolSpellCostIncreasePerUse * manaPoolSpellUses);
        if (mana >= cost) {
            mana -= cost;
            manaCap *= manaPoolSpellCapMultiplier;
            manaGainedPerSecond *= manaPoolSpellManaGainedMultiplier;
            manaPoolSpellUses++;
        }
    }

    private void toggleBuildTowerMode() {
        buildTowerMode = !buildTowerMode;
        if (buildTowerMode) {
            rangeUpgradeMode = false;
            speedUpgradeMode = false;
            damageUpgradeMode = false;
        }
    }
    
    private void toggleRangeUpgradeMode() {
        rangeUpgradeMode = !rangeUpgradeMode;
        if (rangeUpgradeMode) {
            buildTowerMode = false;
            speedUpgradeMode = false;
            damageUpgradeMode = false;
        }
    }
    
    private void toggleSpeedUpgradeMode() {
        speedUpgradeMode = !speedUpgradeMode;
        if (speedUpgradeMode) {
            buildTowerMode = false;
            rangeUpgradeMode = false;
            damageUpgradeMode = false;
        }
    }
    
    private void toggleDamageUpgradeMode() {
        damageUpgradeMode = !damageUpgradeMode;
        if (damageUpgradeMode) {
            buildTowerMode = false;
            rangeUpgradeMode = false;
            speedUpgradeMode = false;
        }
    }

    public static void main(String[] args) {
        PApplet.main("wizard.App");
    }
}