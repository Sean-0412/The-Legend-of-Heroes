/**
 * MapManager.java
 * 說明: 管理地圖資料、敵人生成、碰撞檢查與場景切換（傳送門處理）。
 */

package src;

import java.util.ArrayList;
import java.util.List;

class MapManager {
    private final GamePanel game;

    MapManager(GamePanel game) {
        this.game = game;
    }

    void initMaps() {
        game.allMaps = new int[2][][];

        // ------- 地圖 0：草原 -------
        game.allMaps[0] = new int[15][20];
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 20; x++) {
                boolean border = (x == 0 || y == 0 || x == 19 || y == 14);
                boolean portal = (x == 19 && y >= 7 && y <= 8);
                game.allMaps[0][y][x] = (border && !portal) ? 1 : 0;
            }
        }
        game.allMaps[0][4][4] = 1;
        game.allMaps[0][4][5] = 1;
        game.allMaps[0][8][10] = 1;
        game.allMaps[0][3][10] = 1;
        game.allMaps[0][3][11] = 1;
        game.allMaps[0][3][12] = 1;
        game.allMaps[0][10][3] = 1;
        game.allMaps[0][10][4] = 1;

        // ------- 地圖 1：地下城 -------
        game.allMaps[1] = new int[15][20];
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 20; x++) {
                boolean border = (x == 0 || y == 0 || x == 19 || y == 14);
                boolean portal = (x == 0 && y >= 7 && y <= 8);
                game.allMaps[1][y][x] = (border && !portal) ? 1 : 0;
            }
        }
        game.allMaps[1][3][5] = 1;
        game.allMaps[1][3][6] = 1;
        game.allMaps[1][3][7] = 1;
        game.allMaps[1][6][12] = 1;
        game.allMaps[1][6][13] = 1;
        game.allMaps[1][6][14] = 1;
        game.allMaps[1][9][8] = 1;
        game.allMaps[1][10][8] = 1;
        game.allMaps[1][11][8] = 1;
        game.allMaps[1][5][3] = 1;
        game.allMaps[1][5][4] = 1;
        game.allMaps[1][11][15] = 1;
        game.allMaps[1][12][15] = 1;

        game.allEnemies = new ArrayList<>();
        game.allEnemies.add(spawnEnemiesForMap(0));
        game.allEnemies.add(spawnEnemiesForMap(1));

        game.map = game.allMaps[0];
        game.enemies = game.allEnemies.get(0);
    }

    List<Enemy> spawnEnemiesForMap(int idx) {
        List<Enemy> list = new ArrayList<>();
        if (idx == 0) {
            list.add(new Enemy(5, 3));
            list.add(new Enemy(7, 7));
        } else {
            list.add(new Enemy(10, 4));
            list.add(new Enemy(15, 8));
            list.add(new Enemy(12, 11));
        }
        return list;
    }

    boolean hitWall(double px, double py) {
        int r = 10;
        int cx = (int) (px + 20), cy = (int) (py + 20);
        return isWallPixel(cx - r, cy - r) || isWallPixel(cx + r, cy - r)
                || isWallPixel(cx - r, cy + r) || isWallPixel(cx + r, cy + r);
    }

    boolean isWallPixel(int px, int py) {
        int tx = px / game.TILE_SIZE, ty = py / game.TILE_SIZE;
        if (tx < 0 || ty < 0 || tx >= game.map[0].length || ty >= game.map.length) {
            return true;
        }
        return game.map[ty][tx] == 1;
    }

    void checkPortal() {
        double cx = game.player.x + 20, cy = game.player.y + 20;
        boolean inPortalRows = cy >= 7 * game.TILE_SIZE && cy <= 9 * game.TILE_SIZE;
        if (game.mapIndex == 0 && inPortalRows && cx >= 19 * game.TILE_SIZE) {
            switchMap(1, 1 * game.TILE_SIZE, 7 * game.TILE_SIZE + 10);
        } else if (game.mapIndex == 1 && inPortalRows && cx <= game.TILE_SIZE) {
            switchMap(0, 18 * game.TILE_SIZE, 7 * game.TILE_SIZE + 10);
        }
    }

    void switchMap(int newIdx, double spawnX, double spawnY) {
        game.originalBattleEnemies.clear();

        game.mapIndex = newIdx;
        game.map = game.allMaps[game.mapIndex];
        game.allEnemies.set(game.mapIndex, spawnEnemiesForMap(game.mapIndex));
        game.enemies = game.allEnemies.get(game.mapIndex);
        game.player.x = spawnX;
        game.player.y = spawnY;
        game.player.vx = 0;
        game.player.vy = 0;
        for (Companion c : game.companions) {
            c.x = spawnX;
            c.y = spawnY;
            c.vx = 0;
            c.vy = 0;
        }
        game.playerPathHistory.clear();
        game.updateMapMusic();
    }

    void updateExplorationFrame() {
        if (game.mouseDown || game.keyDown) {
            double newX = game.player.x + game.player.vx;
            double newY = game.player.y + game.player.vy;
            boolean canX = !hitWall(newX, game.player.y);
            boolean canY = !hitWall(game.player.x, newY);
            if (canX) {
                game.player.x = newX;
            }
            if (canY) {
                game.player.y = newY;
            }
            if (!canX) {
                game.player.vx = 0;
            }
            if (!canY) {
                game.player.vy = 0;
            }
        }
        game.updatePlayerWalkAnimation();

        if (game.mouseDown) {
            int tx = game.mouseGridX;
            int ty = game.mouseGridY;
            int dirX = Integer.compare(tx, (int) (game.player.x / game.TILE_SIZE));
            int dirY = Integer.compare(ty, (int) (game.player.y / game.TILE_SIZE));
            game.player.vx = dirX * game.player.speed;
            game.player.vy = dirY * game.player.speed;
        }

        double dt = 0.03;
        for (Enemy en : game.enemies) {
            en.update(dt, game.player.x, game.player.y, game.map);
        }

        for (Companion c : game.companions) {
            c.followPlayer(game.player, game.map);
        }

        for (Enemy en : game.enemies) {
            if (!en.isDefeated() && !en.isBattleLocked() && Math.hypot(game.player.x - en.x, game.player.y - en.y) < 22) {
                game.state = 1;
                game.triggeredEnemy = en;
                game.battleManager.spawnRandomEnemies();
                game.playBattleMusic();
                game.previewDrops = game.battleManager.generateDropsPreview();
                break;
            }
        }

        checkPortal();
        game.repaint();
    }
}
