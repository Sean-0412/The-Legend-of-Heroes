/**
 * SkillMoonlight.java
 * 說明: 實作「月光」類型的戰技，處理技能效果、目標與動畫觸發。
 */

package src;

public class SkillMoonlight extends Skill {
    SkillMoonlight() {
        super("月光治療", "幫全隊回復20%最大HP", 30);
    }

    @Override
    void execute(GamePanel gamePanel, Player player, Enemy enemy) {
        gamePanel.healAllAlliesByPercent(0.20);
    }
}
