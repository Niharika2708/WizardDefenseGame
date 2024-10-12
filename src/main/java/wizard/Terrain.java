package wizard;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PGraphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Terrain {
    PApplet p;
    PImage grass, shrub, pathStraight, pathT, pathL, pathAll, house;
    PImage[][] rotatedPaths;
    char[][] terrainMap;
    int tileSize;
    int houseTileSize;
    List<int[]> pathWaypoints;

    public Terrain(PApplet p, int tileSize, int houseTileSize, String layoutFile) {
        this.p = p;
        this.tileSize = tileSize;
        this.houseTileSize = houseTileSize;
        this.grass = loadImageWithPath("src/main/resources/wizard/grass.png");
        this.shrub = loadImageWithPath("src/main/resources/wizard/shrub.png");
        this.pathStraight = loadImageWithPath("src/main/resources/wizard/path_straight.png");
        this.pathT = loadImageWithPath("src/main/resources/wizard/path_t.png");
        this.pathL = loadImageWithPath("src/main/resources/wizard/path_l.png");
        this.pathAll = loadImageWithPath("src/main/resources/wizard/path_all.png");
        this.house = loadImageWithPath("src/main/resources/wizard/house.png");

        loadTerrain(layoutFile);
        generateRotatedPaths();
        pathWaypoints = getPathWaypoints();
    }

    private PImage loadImageWithPath(String path) {
        File file = new File(path);
        if (file.exists()) {
            return p.loadImage(file.getAbsolutePath());
        } else {
            return null;
        }
    }

    private void loadTerrain(String filename) {
        try {
            Scanner scanner = new Scanner(new File(filename));
            terrainMap = new char[20][20]; 

            for (int row = 0; row < 20; row++) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    for (int col = 0; col < 20 && col < line.length(); col++) {
                        terrainMap[row][col] = line.charAt(col);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generateRotatedPaths() {
        rotatedPaths = new PImage[4][4];
        PImage[] originalPaths = {pathStraight, pathT, pathL, pathAll};
    
        for (int i = 0; i < originalPaths.length; i++) {
            rotatedPaths[i][0] = originalPaths[i]; 
            for (int j = 1; j < 4; j++) {
                rotatedPaths[i][j] = rotateImage(originalPaths[i], 90 * j);
            }
        }
    }

    private PImage rotateImage(PImage img, float angle) {
        PGraphics pg = p.createGraphics(img.width, img.height);
        pg.beginDraw();
        pg.translate(img.width / 2, img.height / 2);
        pg.rotate(PApplet.radians(angle));
        pg.image(img, -img.width / 2, -img.height / 2);
        pg.endDraw();
        return pg.get();
    }
    
    public void draw() {
        for (int row = 0; row < terrainMap.length; row++) {
            for (int col = 0; col < terrainMap[row].length; col++) {
                char tile = terrainMap[row][col];
                PImage img = getImageForTile(tile, row, col);
                if (img != null) {
                    if (tile != 'W') {  
                        p.image(img, col * tileSize, row * tileSize + 40, tileSize, tileSize);
                    }
                }
            }
        }

        for (int row = 0; row < terrainMap.length; row++) {
            for (int col = 0; col < terrainMap[row].length; col++) {
                if (terrainMap[row][col] == 'W') {
                    float x = col * tileSize + (tileSize - houseTileSize) / 2;
                    float y = row * tileSize + 40 + (tileSize - houseTileSize) / 2;
                    p.image(house, x, y, houseTileSize, houseTileSize);
                }
            }
        }
    }

    private PImage getImageForTile(char tile, int row, int col) {
        switch (tile) {
            case ' ':
                return grass;
            case 'S':
                return shrub;
            case 'X':
                return getPathImage(row, col);
            case 'W':
                return house;
            default:
                return null;
        }
    }

    private PImage getPathImage(int row, int col) {
        boolean up = row > 0 && terrainMap[row - 1][col] == 'X';
        boolean down = row < terrainMap.length - 1 && terrainMap[row + 1][col] == 'X';
        boolean left = col > 0 && terrainMap[row][col - 1] == 'X';
        boolean right = col < terrainMap[row].length - 1 && terrainMap[row][col + 1] == 'X';

        if (up && down && left && right) {
            return rotatedPaths[3][0]; // 4 sides path (cross)
        } 
        else if (up && down && left) {
            return rotatedPaths[1][1]; // T-shaped path rotated 90 degrees 
        } else if (up && down && right) {
            return rotatedPaths[1][3]; // T-shaped path rotated 270 degrees 
        } else if (left && right && up) {
            return rotatedPaths[1][2]; // T-shaped path rotated 180 degrees
        } else if (left && right && down) {
            return rotatedPaths[1][0]; // T-shaped path without rotation
        } 
        else if (up && down || up || down) {
            return rotatedPaths[0][1]; // Straight path rotated 90 degrees 
        } else if (left && right || left || right) {
            return rotatedPaths[0][0]; // Straight path without rotation 
        } 
        else if (up && right) {
            return rotatedPaths[2][2]; // L-shaped path rotated 180 degrees 
        } else if (up && left) {
            return rotatedPaths[2][1]; // L-shaped path rotated 90 degrees
        } else if (down && right) {
            return rotatedPaths[2][3]; // L-shaped path rotated 270 degrees
        } else if (down && left) {
            return rotatedPaths[2][0]; // L-shaped path without rotation
        }

        return null;
    }

    private List<int[]> getPathWaypoints() {
        List<int[]> waypoints = new ArrayList<>();
        for (int row = 0; row < terrainMap.length; row++) {
            for (int col = 0; col < terrainMap[row].length; col++) {
                if (terrainMap[row][col] == 'X' || terrainMap[row][col] == 'W') {
                    waypoints.add(new int[]{row, col});
                }
            }
        }
        return waypoints;
    }    

    public int[] getNextPathTile(int currentRow, int currentCol) {
        for (int i = 0; i < pathWaypoints.size(); i++) {
            int[] waypoint = pathWaypoints.get(i);
            if (waypoint[0] == currentRow && waypoint[1] == currentCol) {
                if (i + 1 < pathWaypoints.size()) {
                    return pathWaypoints.get(i + 1);
                } else {
                    return null; // Reached the end of the path
                }
            }
        }
        return null;
    }

    public int[] getStartingPoint() {
        for (int i = 0; i < terrainMap.length; i++) {
            if (terrainMap[i][0] == 'X') {
                return new int[]{i, 0};
            }
            if (terrainMap[i][terrainMap[i].length - 1] == 'X') {
                return new int[]{i, terrainMap[i].length - 1};
            }
        }
        for (int j = 0; j < terrainMap[0].length; j++) {
            if (terrainMap[0][j] == 'X') {
                return new int[]{0, j};
            }
            if (terrainMap[terrainMap.length - 1][j] == 'X') {
                return new int[]{terrainMap.length - 1, j};
            }
        }
        return null; // Return null if no starting point found 
    }
}