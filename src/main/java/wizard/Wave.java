package wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import processing.core.PApplet;

class Wave {
    PApplet p;
    float duration;
    float preWavePause;
    List<Monster> monsters;
    List<Monster> activeMonsters;
    int frameCount;
    boolean waveStarted;
    boolean waveEnded;
    Random random;
    float monsterScale;
    int[] spawnPoint; // Single spawn point for the wave

    public Wave(PApplet p, float duration, float preWavePause, List<Monster> monsters, int[] spawnPoint, float monsterScale) {
        this.p = p;
        this.duration = duration;
        this.preWavePause = preWavePause;
        this.monsters = new ArrayList<>(monsters);
        this.activeMonsters = new ArrayList<>();
        this.frameCount = 0;
        this.waveStarted = false;
        this.waveEnded = false;
        this.random = new Random();
        this.monsterScale = monsterScale;
        this.spawnPoint = spawnPoint;
    }

    public void update(Terrain terrain) {
        if (!waveStarted) {
            frameCount++;
            if (frameCount >= preWavePause * p.frameRate) {
                waveStarted = true;
                frameCount = 0;
            } else {
                return;
            }
        }

        int totalMonsters = monsters.size();
        int framesPerSpawn = (int) (p.frameRate * duration / totalMonsters);
        int minDistance = 40; // Increase this value for more spacing between monsters

        if (frameCount < duration * p.frameRate) {
            if (frameCount % framesPerSpawn == 0 && !monsters.isEmpty()) {
                if (activeMonsters.isEmpty() || (activeMonsters.get(activeMonsters.size() - 1).y - activeMonsters.get(activeMonsters.size() - 1).sprite.height * activeMonsters.get(activeMonsters.size() - 1).scale) > minDistance) {
                    Monster monster = monsters.remove(0);
                    float x = spawnPoint[1] * terrain.tileSize + terrain.tileSize / 2;
                    float y = spawnPoint[0] * terrain.tileSize + 40 + terrain.tileSize / 2; // Center on the tile
                    monster.setPosition(x, y, spawnPoint[0], spawnPoint[1]);
                    int[] nextPathTile = terrain.getNextPathTile(spawnPoint[0], spawnPoint[1]);
                    if (nextPathTile != null) {
                        monster.setTarget(nextPathTile[0], nextPathTile[1]);
                    }
                    activeMonsters.add(monster);
                }
            }
            frameCount++;
        } else {
            if (allMonstersReachedHouse()) {
                waveEnded = true;
            }
        }

        for (Monster monster : activeMonsters) {
            monster.move(terrain);
            monster.draw();
        }
    }

    private boolean allMonstersReachedHouse() {
        for (Monster monster : activeMonsters) {
            if (monster.isAlive() && !monster.dying) {
                return false;
            }
        }
        return true;
    }
}