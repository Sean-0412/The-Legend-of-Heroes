package src;

import java.util.ArrayList;
import java.util.List;

public class Player {
    double x, y; // 像素座標
    double vx, vy; // 速度向量
    final double speed = 4; // 每幀移動像素數
    String name = "勇者"; // 玩家名字

    // 等級與經驗
    int level = 1;
    int exp = 0;
    int expToNext = 10;

    // 生命與魔力
    int maxHp = 120;
    int hp = 120;
    int maxMana = 80;
    int mana = 80;
    int maxCp = 200;  // 最大戰技值
    int cp = 0;       // 戰技值

    // 戰鬥屬性
    int patk = 32; // 物攻
    int pdef = 12; // 物防
    int matk = 20; // 魔攻
    int mdef = 12; // 魔防
    int battleSpeed = 50; // 戰鬥速度（用於決定行動順序）

    int smallPotions = 3;
    int largePotions = 1;
    int gold = 0;  // 金幣
    List<Skill> skills = new ArrayList<>();

    Player(double x, double y) {
        this.x = x;
        this.y = y;
        // 初始化戰技
        skills.add(new SkillSaintBlood());
    }

    void heal(int amount) {
        hp = Math.min(hp + amount, maxHp);
    }

    /** 獲得經驗值，回傳升級次數 */
    int gainExp(int amount) {
        exp += amount;
        int leveled = 0;
        while (exp >= expToNext) {
            exp -= expToNext;
            levelUp();
            leveled++;
        }
        return leveled;
    }

    private void levelUp() {
        level++;
        expToNext = level * 12;
        maxHp += 32;
        hp = maxHp;
        maxMana += 20;
        mana = maxMana;
        maxCp = 200;
        cp = Math.min(cp, maxCp);
        patk += 12;
        pdef += 8;
        matk += 8;
        mdef += 8;
    }
}
