public class SkillMoonSlice extends Skill {
    SkillMoonSlice() {
        super("月牙斬", "對敵方全體造成自身攻擊力60%的傷害", 25);
    }

    @Override
    void execute(GamePanel gamePanel, Player player, Enemy enemy) {
        int casterPatk = gamePanel.getCurrentSkillCasterPatk(player);
        int damage = Math.max(1, (int) Math.round(casterPatk * 0.6));
        gamePanel.dealFlatDamageToAllEnemies(damage);
    }
}
