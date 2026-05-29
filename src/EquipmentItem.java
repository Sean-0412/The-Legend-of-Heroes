package src;

import java.io.Serializable;

final class EquipmentItem implements Serializable {
    private static final long serialVersionUID = 1L;

    final String slotKey;
    final String name;
    final String description;
    final int patkBonus;
    final int pdefBonus;
    final int matkBonus;
    final int mdefBonus;
    final int battleSpeedBonus;

    EquipmentItem(String slotKey, String name, String description,
            int patkBonus, int pdefBonus, int matkBonus, int mdefBonus, int battleSpeedBonus) {
        this.slotKey = slotKey;
        this.name = name;
        this.description = description;
        this.patkBonus = patkBonus;
        this.pdefBonus = pdefBonus;
        this.matkBonus = matkBonus;
        this.mdefBonus = mdefBonus;
        this.battleSpeedBonus = battleSpeedBonus;
    }

    String bonusText() {
        StringBuilder text = new StringBuilder();
        appendBonus(text, "物攻", patkBonus);
        appendBonus(text, "物防", pdefBonus);
        appendBonus(text, "魔攻", matkBonus);
        appendBonus(text, "魔防", mdefBonus);
        appendBonus(text, "速度", battleSpeedBonus);
        return text.length() == 0 ? "無加成" : text.toString();
    }

    private void appendBonus(StringBuilder text, String label, int value) {
        if (value == 0) {
            return;
        }
        if (text.length() > 0) {
            text.append(" / ");
        }
        text.append(label).append(value > 0 ? "+" : "").append(value);
    }

    @Override
    public String toString() {
        return name + " (" + bonusText() + ")";
    }
}