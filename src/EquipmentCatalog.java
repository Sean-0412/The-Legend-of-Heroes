package src;

final class EquipmentCatalog {
    private static final String[] SLOT_LABELS = { "武器", "衣服", "鞋子", "飾品1", "飾品2" };
    private static final String[] DEFAULT_NAMES = { "訓練用武器", "訓練用衣服", "訓練用鞋子", "", "" };

    private static final EquipmentItem[] WEAPON_OPTIONS = {
            null,
            new EquipmentItem("weapon", "鐵劍", "穩定的近戰武器，適合勇者使用。", 8, 0, 0, 0, 0),
            new EquipmentItem("weapon", "魔導杖", "偏向魔法輸出的武器。", 0, 0, 8, 0, 0),
            new EquipmentItem("weapon", "疾風短刃", "輕巧的短刃，兼顧攻擊與行動速度。", 5, 0, 0, 0, 2),
            new EquipmentItem("weapon", "聖光長槍", "兼具攻擊與少量魔力的長槍。", 6, 0, 3, 0, 0)
    };

    private static final EquipmentItem[] CLOTHES_OPTIONS = {
            null,
            new EquipmentItem("clothes", "布甲", "輕便的基礎防具。", 0, 4, 0, 0, 0),
            new EquipmentItem("clothes", "皮甲", "兼顧防禦與活動性的防具。", 0, 6, 0, 2, 0),
            new EquipmentItem("clothes", "魔導長袍", "增強魔法防禦的長袍。", 0, 2, 0, 7, 0)
    };

    private static final EquipmentItem[] SHOES_OPTIONS = {
            null,
            new EquipmentItem("shoes", "皮靴", "基礎的行動鞋。", 0, 0, 0, 0, 2),
            new EquipmentItem("shoes", "疾行靴", "提升反應速度的靴子。", 0, 0, 0, 0, 4),
            new EquipmentItem("shoes", "守護長靴", "增加穩定性的重型靴子。", 0, 2, 0, 0, 1)
    };

    private static final EquipmentItem[] ACCESSORY_OPTIONS = {
            null,
            new EquipmentItem("accessory", "小護符", "提供少量攻擊加成的護符。", 2, 0, 0, 0, 0),
            new EquipmentItem("accessory", "守護戒指", "提升防禦能力的戒指。", 0, 3, 0, 3, 0),
            new EquipmentItem("accessory", "敏捷墜飾", "提升行動速度的墜飾。", 0, 0, 0, 0, 3)
    };

    private EquipmentCatalog() {
    }

    static String slotLabel(int slotIndex) {
        return SLOT_LABELS[slotIndex];
    }

    static String defaultName(int slotIndex) {
        return DEFAULT_NAMES[slotIndex];
    }

    static EquipmentItem[] optionsForSlot(int slotIndex) {
        switch (slotIndex) {
            case 0:
                return WEAPON_OPTIONS;
            case 1:
                return CLOTHES_OPTIONS;
            case 2:
                return SHOES_OPTIONS;
            case 3:
            case 4:
                return ACCESSORY_OPTIONS;
            default:
                return new EquipmentItem[] { null };
        }
    }

    static String[] optionLabelsForSlot(int slotIndex) {
        EquipmentItem[] options = optionsForSlot(slotIndex);
        String[] labels = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            if (options[i] == null) {
                labels[i] = slotIndex < 3 ? defaultName(slotIndex) : "空白";
            } else {
                labels[i] = options[i].toString();
            }
        }
        return labels;
    }
}