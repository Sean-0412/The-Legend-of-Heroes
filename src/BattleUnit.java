/**
 * BattleUnit.java
 * 說明: 戰鬥系統中的單位包裝類（用於排序與管理回合順序的資料結構）。
 */

package src;

public class BattleUnit {
    int speed; // 戰鬥速度
    double at; // 行動值，越低越先行動
    boolean isPlayer; // true = 玩家或隊友，false = 敵人
    Object unit; // 引用玩家、隊友或敵人物件

    BattleUnit(int speed, boolean isPlayer, Object unit) {
        this.speed = speed;
        this.at = 50.0 / Math.max(1, speed);
        this.isPlayer = isPlayer;
        this.unit = unit;
    }

    void addActionCost(int actionCost) {
        at += actionCost / (double) Math.max(1, speed);
    }
}
