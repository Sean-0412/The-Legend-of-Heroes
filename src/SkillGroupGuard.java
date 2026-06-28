/**
 * SkillGroupGuard.java
 * 說明：實作「群體防護」戰技，為全隊提供治療與防護效果。
 */

package src;

public class SkillGroupGuard extends Skill {
    SkillGroupGuard() {
        super("群體防護", "回復全隊少量HP，並提升3回合DEF", 28);
    }

    @Override
    void execute(GamePanel gamePanel, Player player, Enemy enemy) {
        gamePanel.healAllAlliesByPercent(0.10 + 0.05 * (level - 1));
        gamePanel.applyGroupGuardToAll(8 + 4 * (level - 1), 3);
    }
}
