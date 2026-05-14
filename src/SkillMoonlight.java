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
