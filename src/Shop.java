/**
 * Shop.java
 * 說明: 商店功能的輔助類別，提供顯示商店列表與處理購買邏輯的靜態方法。
 */

package src;

import javax.swing.*;
import java.awt.*;

class Shop {
    static void showShop(GamePanel game) {
        Player player = game.player;

        String[] shopItems = {
                "劣質小刀 - 50G (+5 物攻)",
                "劣質法杖 - 50G (+5 魔攻)",
                "劣質護甲 - 40G (+5 物防)",
                "劣質斗篷 - 40G (+5 魔防)",
                "小藥水 - 15G (+40 HP)",
                "大藥水 - 30G (+80 HP)",
                "CP藥水 - 25G (+50 CP)",
                "取消"
        };

        JList<String> shopListUI = new JList<>(shopItems);
        shopListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shopListUI.setSelectedIndex(0);
        shopListUI.setFont(new java.awt.Font("Microsoft JhengHei", java.awt.Font.PLAIN, 14));
        shopListUI.setFixedCellHeight(30);
        shopListUI.setVisibleRowCount(Math.min(8, shopItems.length));

        JScrollPane scrollPane = new JScrollPane(shopListUI);
        scrollPane.setPreferredSize(new java.awt.Dimension(420, 220));

        int result = JOptionPane.showConfirmDialog(game, scrollPane, "商店",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION || shopListUI.getSelectedIndex() < 0) {
            return;
        }

        int choice = shopListUI.getSelectedIndex();
        boolean bought = false;

        switch (choice) {
            case 0:
                bought = player.buyEquipment("劣質小刀", 50, "patk", 5);
                break;
            case 1:
                bought = player.buyEquipment("劣質法杖", 50, "matk", 5);
                break;
            case 2:
                bought = player.buyEquipment("劣質護甲", 40, "pdef", 5);
                break;
            case 3:
                bought = player.buyEquipment("劣質斗篷", 40, "mdef", 5);
                break;
            case 4:
                bought = player.buyPotion("small", 15);
                break;
            case 5:
                bought = player.buyPotion("large", 30);
                break;
            case 6:
                bought = player.buyPotion("cp", 25);
                break;
            default:
                return;
        }

        if (bought) {
            JOptionPane.showMessageDialog(game, "購買成功！");
            game.repaint();
        } else {
            JOptionPane.showMessageDialog(game, "金幣不足！");
        }
    }
}
