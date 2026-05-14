import java.util.ArrayList;
import java.util.List;

public class Companion {
    double x, y; // 像素座標
    double vx, vy; // 速度向量
    final double speed = 4; // 每幀移動像素數
    String name; // 隊友名稱

    // 等級與經驗
    int level = 1;
    int exp = 0;
    int expToNext = 10;

    // 生命與魔力
    int maxHp = 100;
    int hp = 100;
    int maxMana = 60;
    int mana = 60;
    int maxCp = 200;  // 最大戰技值
    int cp = 0;       // 戰技值

    // 戰鬥屬性
    int patk = 28; // 物攻
    int pdef = 10; // 物防
    int matk = 18; // 魔攻
    int mdef = 10; // 魔防
    int battleSpeed = 45; // 戰鬥速度（用於決定行動順序）

    // 隊友跟隨玩家的目標距離
    double targetDistance = 50;

    // 戰技系統
    List<Skill> skills = new ArrayList<>();

    Companion(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        // 初始化戰技
        skills.add(new SkillMoonSlice());
        skills.add(new SkillMoonlight());
    }

    // 跟隨玩家
    void followPlayer(Player player, int[][] map) {
        double dx = player.x - this.x;
        double dy = player.y - this.y;
        double dist = Math.hypot(dx, dy);

        // 如果距離太遠，就跟隨玩家
        if (dist > targetDistance) {
            double moveX = dx / dist * speed;
            double moveY = dy / dist * speed;
            
            // 計算新位置
            double newX = this.x + moveX;
            double newY = this.y + moveY;
            
            // 檢查 X 軸移動是否合法（使用相同的碰撞檢測邏輯）
            int r = 10;
            int cx = (int) (newX + 20), cy = (int) (this.y + 20);
            if (!isWallInMap(cx - r, cy - r, map) && !isWallInMap(cx + r, cy - r, map)
                    && !isWallInMap(cx - r, cy + r, map) && !isWallInMap(cx + r, cy + r, map)) {
                this.x = newX;
            }
            
            // 檢查 Y 軸移動是否合法
            cx = (int) (this.x + 20);
            cy = (int) (newY + 20);
            if (!isWallInMap(cx - r, cy - r, map) && !isWallInMap(cx + r, cy - r, map)
                    && !isWallInMap(cx - r, cy + r, map) && !isWallInMap(cx + r, cy + r, map)) {
                this.y = newY;
            }
        } else {
            // 距離足夠近，停止移動
            this.vx = 0;
            this.vy = 0;
        }
    }

    private boolean isWallInMap(int px, int py, int[][] map) {
        int tx = px / 40, ty = py / 40;  // TILE_SIZE = 40
        if (tx < 0 || ty < 0 || tx >= map[0].length || ty >= map.length)
            return true;
        return map[ty][tx] == 1;
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
        maxHp += 28;
        hp = maxHp;
        maxMana += 18;
        mana = maxMana;
        maxCp = 200;
        cp = Math.min(cp, maxCp);
        patk += 10;
        pdef += 6;
        matk += 7;
        mdef += 6;
    }
}
