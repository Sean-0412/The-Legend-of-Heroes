/**
 * Player.java
 * 說明: 玩家角色資料與行為（屬性、等級、金幣、購買方法、治療等）。
 */

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

    public int smallPotions = 3;
    public int largePotions = 1;
    public int cpPotions = 0;
    public int gold = 0;  // 金幣，初始值為 0
    public int activeQuestLevel = 0; // 0代表無任務，1代表低難度，2代表中難度，3代表高難度
    public int questKillProgress = 0; // 記錄目前任務已擊殺的目標怪數
    List<Skill> skills = new ArrayList<>();

    EquipmentItem weapon;
    EquipmentItem clothes;
    EquipmentItem shoes;
    EquipmentItem accessory1;
    EquipmentItem accessory2;
    List<EquipmentItem> equipmentInventory = new ArrayList<>();

    Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.gold = 0;
        this.weapon = null;
        this.clothes = null;
        this.shoes = null;
        this.accessory1 = null;
        this.accessory2 = null;
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

    public boolean buyEquipment(String equipName, int cost, String statType, int bonus) {
        if (gold < cost) {
            return false;
        }

        String slotKey;
        switch (statType) {
            case "patk":
            case "matk":
                slotKey = "weapon";
                break;
            case "pdef":
            case "mdef":
                slotKey = "clothes";
                break;
            default:
                return false;
        }

        gold -= cost;
        equipmentInventory.add(new EquipmentItem(slotKey, equipName, "",
                "patk".equals(statType) ? bonus : 0,
                "pdef".equals(statType) ? bonus : 0,
                "matk".equals(statType) ? bonus : 0,
                "mdef".equals(statType) ? bonus : 0,
                0));
        return true;
    }

    void addEquipmentToInventory(EquipmentItem item) {
        if (item != null) {
            equipmentInventory.add(item);
        }
    }

    boolean removeEquipmentFromInventory(EquipmentItem item) {
        return equipmentInventory.remove(item);
    }

    public boolean buyPotion(String potionType, int cost) {
        if (gold < cost) {
            return false;
        }

        gold -= cost;
        switch (potionType) {
            case "small":
                smallPotions++;
                break;
            case "large":
                largePotions++;
                break;
            case "cp":
                cpPotions++;
                break;
            default:
                gold += cost;
                return false;
        }

        return true;
    }
}
