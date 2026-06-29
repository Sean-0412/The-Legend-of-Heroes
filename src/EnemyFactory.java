/**
 * EnemyFactory.java
 * 說明：集中建立不同種類的敵人，管理各地圖敵人的能力、外觀與生成配置。
 */

package src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

final class EnemyFactory {
    private EnemyFactory() {
    }

    static List<Enemy> spawnForMap(int mapIndex, boolean bossDefeated) {
        List<Enemy> enemies = new ArrayList<>();
        if (mapIndex == 0) {
            enemies.add(createSlime(5, 3));
            enemies.add(createWolf(7, 7));
        } else if (mapIndex == 1) {
            enemies.add(createGoblin(10, 4));
            enemies.add(createSkeleton(15, 8));
            enemies.add(createShadowMage(12, 11));
        } else if (mapIndex == 3 && !bossDefeated) {
            enemies.add(createDemonKing(10, 7));
        }
        return enemies;
    }

    static Enemy createRandomForMap(int mapIndex, int tileX, int tileY) {
        double roll = Math.random();
        if (mapIndex == 1) {
            if (roll < 0.40) {
                return createGoblin(tileX, tileY);
            }
            if (roll < 0.75) {
                return createSkeleton(tileX, tileY);
            }
            return createShadowMage(tileX, tileY);
        }
        return roll < 0.55 ? createSlime(tileX, tileY) : createWolf(tileX, tileY);
    }

    private static Enemy createSlime(int x, int y) {
        Enemy enemy = new Enemy(x, y, "綠色史萊姆", 3, 85, 26, 8, 14, 35, 28, false);
        enemy.kind = EnemyKind.SLIME;
        enemy.bodyColor = new Color(70, 190, 95);
        enemy.displaySize = 22;
        enemy.moveSpeed = 1.0;
        enemy.detectRange = 3 * 40;
        return enemy;
    }

    private static Enemy createWolf(int x, int y) {
        Enemy enemy = new Enemy(x, y, "疾風狼", 4, 100, 34, 9, 8, 50, 52, false);
        enemy.kind = EnemyKind.WOLF;
        enemy.bodyColor = new Color(125, 140, 155);
        enemy.displaySize = 24;
        enemy.moveSpeed = 2.2;
        enemy.detectRange = 5 * 40;
        enemy.chaseLoseRange = 8 * 40;
        return enemy;
    }

    private static Enemy createGoblin(int x, int y) {
        Enemy enemy = new Enemy(x, y, "哥布林戰士", 5, 135, 40, 16, 9, 65, 38, false);
        enemy.kind = EnemyKind.GOBLIN;
        enemy.bodyColor = new Color(105, 155, 65);
        enemy.displaySize = 25;
        enemy.moveSpeed = 1.6;
        return enemy;
    }

    private static Enemy createSkeleton(int x, int y) {
        Enemy enemy = new Enemy(x, y, "骷髏兵", 6, 155, 43, 20, 15, 80, 30, false);
        enemy.kind = EnemyKind.SKELETON;
        enemy.bodyColor = new Color(220, 215, 185);
        enemy.displaySize = 26;
        enemy.moveSpeed = 1.3;
        return enemy;
    }

    private static Enemy createShadowMage(int x, int y) {
        Enemy enemy = new Enemy(x, y, "暗影法師", 7, 110, 30, 8, 24, 90, 42, false);
        enemy.kind = EnemyKind.SHADOW_MAGE;
        enemy.matk = 48;
        enemy.usesMagicAttack = true;
        enemy.bodyColor = new Color(145, 75, 190);
        enemy.displaySize = 24;
        enemy.moveSpeed = 1.4;
        enemy.detectRange = 5 * 40;
        return enemy;
    }

    private static Enemy createDemonKing(int x, int y) {
        Enemy boss = new Enemy(x, y, "魔王", 12, 520, 70, 24, 20, 320, 28, true);
        boss.kind = EnemyKind.DEMON_KING;
        boss.bodyColor = new Color(150, 40, 30);
        boss.displaySize = 32;
        boss.moveSpeed = 1.2;
        boss.roamRadius = 3 * 40;
        boss.detectRange = 6 * 40;
        boss.chaseLoseRange = 8 * 40;
        return boss;
    }
}
