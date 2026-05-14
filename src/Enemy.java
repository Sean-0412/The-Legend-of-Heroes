package src;

public class Enemy {
    double x, y; // pixel position
    int maxHp = 120;
    int hp = 120;
    int maxCp = 200;
    int cp = 0;
    int patk = 36; // 物攻
    int pdef = 12; // 物防
    int mdef = 12; // 魔防
    int expReward = 60;
    int speed = 35; // 戰鬥速度（用於決定行動順序）
    boolean defeated = false;

    // 玩家逃跑成功後的戰鬥鎖（閃爍期）
    long battleLockUntilMs = 0;

    // AI
    boolean chasing = false;
    double moveSpeed = 1.5; // pixels per frame
    double originX, originY; // spawn point in pixels
    double roamRadius = 5 * 40; // 5 tiles in pixels
    double detectRange = 4 * 40;
    double chaseLoseRange = 6 * 40;

    // target in pixels
    double tgtX, tgtY;
    boolean hasTarget = false;
    double roamTimer = 0;
    double roamInterval = 1.2; // pick new roam target every N seconds

    Enemy(int tileX, int tileY) {
        this.x = tileX * 40.0;
        this.y = tileY * 40.0;
        this.originX = this.x;
        this.originY = this.y;
        this.tgtX = this.x;
        this.tgtY = this.y;
    }

    boolean isDefeated() {
        return defeated;
    }

    void setBattleLock(long durationMs) {
        battleLockUntilMs = System.currentTimeMillis() + durationMs;
    }

    boolean isBattleLocked() {
        return System.currentTimeMillis() < battleLockUntilMs;
    }

    boolean shouldRenderOnMap() {
        if (defeated)
            return false;
        if (isBattleLocked()) {
            // 閃爍效果：每 150ms 切換可見
            return ((System.currentTimeMillis() / 150) % 2) == 0;
        }
        return true;
    }

    void fleeAndHideUntilRefresh() {
        defeated = true;
        chasing = false;
        hasTarget = false;
    }

    void update(double delta, double playerX, double playerY, int[][] map) {
        if (defeated)
            return;

        double distToPlayer = Math.hypot(playerX - x, playerY - y);

        // chase logic
        if (chasing) {
            if (distToPlayer > chaseLoseRange) {
                chasing = false;
                pickRoamTarget();
            } else {
                tgtX = playerX;
                tgtY = playerY;
            }
        } else {
            if (distToPlayer <= detectRange && Math.random() < 0.01) {
                chasing = true;
            } else {
                roamTimer -= delta;
                if (roamTimer <= 0 || !hasTarget) {
                    pickRoamTarget();
                }
            }
        }

        // move towards tgtX/tgtY
        double dx = tgtX - x;
        double dy = tgtY - y;
        double dist = Math.hypot(dx, dy);
        if (dist > moveSpeed) {
            double nx = x + dx / dist * moveSpeed;
            double ny = y + dy / dist * moveSpeed;
            // wall collision check on center
            if (!isWall(nx + 12, ny + 12, map)) {
                x = nx;
                y = ny;
            }
            // limit to roam radius from origin
            double fromOrigin = Math.hypot(x - originX, y - originY);
            if (fromOrigin > roamRadius && !chasing) {
                // nudge back toward origin
                double bx = originX - x;
                double by = originY - y;
                double bd = Math.hypot(bx, by);
                x += bx / bd * moveSpeed;
                y += by / bd * moveSpeed;
            }
        }
    }

    private void pickRoamTarget() {
        double angle = Math.random() * 2 * Math.PI;
        double r = Math.random() * roamRadius;
        tgtX = originX + Math.cos(angle) * r;
        tgtY = originY + Math.sin(angle) * r;
        hasTarget = true;
        roamTimer = roamInterval;
    }

    private boolean isWall(double px, double py, int[][] map) {
        int tx = (int) (px / 40);
        int ty = (int) (py / 40);
        if (tx < 0 || ty < 0 || ty >= map.length || tx >= map[0].length)
            return true;
        return map[ty][tx] == 1;
    }
}
