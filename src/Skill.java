/**
 * Skill.java
 * 說明: 戰技基底類別，定義共用屬性與介面方法，各技能類別繼承並實作細節。
 */

package src;

public abstract class Skill {
    String name;
    String description;
    int cpCost;  // 技能 CP 消耗
    int level = 1;

    Skill(String name, String description, int cpCost) {
        this.name = name;
        this.description = description;
        this.cpCost = cpCost;
    }

    void upgrade() {
        level++;
        cpCost = Math.max(1, cpCost - 2);
        if (!description.contains("Lv.")) {
            description += " (Lv." + level + ")";
        } else {
            description = description.replaceAll("\\(Lv\\.\\d+\\)", "(Lv." + level + ")");
        }
    }

    abstract void execute(GamePanel gamePanel, Player player, Enemy enemy);
}
