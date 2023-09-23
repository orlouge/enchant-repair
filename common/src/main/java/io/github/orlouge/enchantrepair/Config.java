package io.github.orlouge.enchantrepair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public static boolean RANDOM_ENCHANTMENT_PENALTY = true;
    public static float RANDOM_ENCHANTMENT_PENALTY_MEAN_INTERCEPT = 11f;
    public static float RANDOM_ENCHANTMENT_PENALTY_MEAN_SLOPE = 0.1f;
    public static float RANDOM_ENCHANTMENT_PENALTY_STDDEV = 3;
    public static boolean BOOK_ENCHANTMENT_ENABLED = true;
    public static boolean BOOK_ENCHANTMENT_CONSUME_VANISHING = true;
    public static boolean BOOK_ENCHANTMENT_CONSUME_TREASURE = true;
    public static double BOOK_ENCHANTMENT_LEVEL_FACTOR = 1;
    public static double ENCHANT_DAMAGE_CHANCE = 5;

    public static boolean DISABLE_ENCHANTING_XP_COST = true;
    public static boolean DISABLE_ENCHANTING_BOOKS = true;


    public static float REPAIR_CONSUME_CHANCE = 50;
    public static float REPAIR_FAIL_CHANCE = 15;
    public static float REPAIR_DISENCHANT_CHANCE = 15;
    public static boolean ALLOW_CREATIVE_ANVIL_MERGE = true;
    public static boolean ALLOW_SURVIVAL_ANVIL_MERGE = false;
    public static boolean DISABLE_ANVIL_DAMAGE_ON_RENAME = true;
    public static boolean DISABLE_ANVIL_XP_COST = true;
    public static boolean REPAIR_CHEAP = true;
    public static boolean REPAIR_VANISHING = false;

    public static int XP_LEVELS_LOST_ON_DEATH = 1;
    public static float XP_LOST_DROPPED = 50f;
    public static boolean DISABLE_MENDING_XP_REPAIR = true;

    public static boolean GRINDSTONE_DISABLE_XP = true;
    public static boolean GRINDSTONE_EXTRACT_TREASURE = true;
    public static boolean GRINDSTONE_EXTRACT_KEEP_NON_TREASURE = false;
    public static boolean GRINDSTONE_DISENCHANT_KEEP_TREASURE = true;

    public static boolean CURSE_TRADED_BOOKS = true;
    public static boolean CURSE_TRADED_TOOLS = true;

    public static void load() {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("random_enchanting_penalty", Boolean.toString(RANDOM_ENCHANTMENT_PENALTY));
        defaultProps.setProperty("random_enchanting_penalty_mean_intercept", Float.toString(RANDOM_ENCHANTMENT_PENALTY_MEAN_INTERCEPT));
        defaultProps.setProperty("random_enchanting_penalty_mean_slope", Float.toString(RANDOM_ENCHANTMENT_PENALTY_MEAN_SLOPE));
        defaultProps.setProperty("random_enchanting_penalty_stddev", Float.toString(RANDOM_ENCHANTMENT_PENALTY_STDDEV));
        defaultProps.setProperty("bookshelf_enchanting_enabled", Boolean.toString(BOOK_ENCHANTMENT_ENABLED));
        defaultProps.setProperty("bookshelf_enchanting_consume_vanishing", Boolean.toString(BOOK_ENCHANTMENT_CONSUME_VANISHING));
        defaultProps.setProperty("bookshelf_enchanting_consume_treasure", Boolean.toString(BOOK_ENCHANTMENT_CONSUME_TREASURE));
        defaultProps.setProperty("bookshelf_enchanting_level_factor", Double.toString(BOOK_ENCHANTMENT_LEVEL_FACTOR));
        defaultProps.setProperty("enchanting_damage_chance", Double.toString(ENCHANT_DAMAGE_CHANCE));
        defaultProps.setProperty("enchanting_disable_book", Boolean.toString(DISABLE_ENCHANTING_BOOKS));
        defaultProps.setProperty("enchanting_disable_xp_cost", Boolean.toString(DISABLE_ENCHANTING_XP_COST));

        defaultProps.setProperty("anvil_allow_creative_merge", Boolean.toString(ALLOW_CREATIVE_ANVIL_MERGE));
        defaultProps.setProperty("anvil_allow_survival_merge", Boolean.toString(ALLOW_SURVIVAL_ANVIL_MERGE));
        defaultProps.setProperty("anvil_rename_disable_damage", Boolean.toString(DISABLE_ANVIL_DAMAGE_ON_RENAME));
        defaultProps.setProperty("anvil_repair_consume_chance", Float.toString(REPAIR_CONSUME_CHANCE));
        defaultProps.setProperty("anvil_repair_fail_chance", Float.toString(REPAIR_FAIL_CHANCE));
        defaultProps.setProperty("anvil_repair_disenchant_chance", Float.toString(REPAIR_DISENCHANT_CHANCE));
        defaultProps.setProperty("anvil_repair_cheap", Boolean.toString(REPAIR_CHEAP));
        defaultProps.setProperty("anvil_repair_vanishing", Boolean.toString(REPAIR_VANISHING));
        defaultProps.setProperty("anvil_disable_xp_cost", Boolean.toString(DISABLE_ANVIL_XP_COST));

        defaultProps.setProperty("xp_levels_lost_on_death", Integer.toString(XP_LEVELS_LOST_ON_DEATH));
        defaultProps.setProperty("xp_lost_dropped", Float.toString(XP_LOST_DROPPED));
        defaultProps.setProperty("grindstone_disable_xp", Boolean.toString(GRINDSTONE_DISABLE_XP));
        defaultProps.setProperty("grindstone_extract_treasure", Boolean.toString(GRINDSTONE_EXTRACT_TREASURE));
        defaultProps.setProperty("grindstone_extract_keep_non_treasure", Boolean.toString(GRINDSTONE_EXTRACT_KEEP_NON_TREASURE));
        defaultProps.setProperty("grindstone_disenchant_keep_treasure", Boolean.toString(GRINDSTONE_DISENCHANT_KEEP_TREASURE));
        defaultProps.setProperty("disable_mending_xp_repair", Boolean.toString(DISABLE_MENDING_XP_REPAIR));

        defaultProps.setProperty("curse_traded_books", Boolean.toString(CURSE_TRADED_BOOKS));
        defaultProps.setProperty("curse_traded_tools", Boolean.toString(CURSE_TRADED_TOOLS));


        File f = new File(EnchantRepairMod.CONFIG_FNAME);
        if (f.isFile() && f.canRead()) {
            try (FileInputStream in = new FileInputStream(f)) {
                Properties props = new Properties(defaultProps);
                props.load(in);

                RANDOM_ENCHANTMENT_PENALTY = Boolean.parseBoolean(props.getProperty("random_enchanting_penalty"));
                RANDOM_ENCHANTMENT_PENALTY_MEAN_INTERCEPT = Float.parseFloat(props.getProperty("random_enchanting_penalty_mean_intercept"));
                RANDOM_ENCHANTMENT_PENALTY_MEAN_SLOPE = Float.parseFloat(props.getProperty("random_enchanting_penalty_mean_slope"));
                RANDOM_ENCHANTMENT_PENALTY_STDDEV = Float.parseFloat(props.getProperty("random_enchanting_penalty_stddev"));
                BOOK_ENCHANTMENT_ENABLED = Boolean.parseBoolean(props.getProperty("bookshelf_enchanting_enabled"));
                BOOK_ENCHANTMENT_CONSUME_VANISHING = Boolean.parseBoolean(props.getProperty("bookshelf_enchanting_consume_vanishing"));
                BOOK_ENCHANTMENT_CONSUME_TREASURE = Boolean.parseBoolean(props.getProperty("bookshelf_enchanting_consume_treasure"));
                BOOK_ENCHANTMENT_LEVEL_FACTOR = Double.parseDouble(props.getProperty("bookshelf_enchanting_level_factor"));
                ENCHANT_DAMAGE_CHANCE = Double.parseDouble(props.getProperty("enchanting_damage_chance"));
                DISABLE_ENCHANTING_XP_COST = Boolean.parseBoolean(props.getProperty("enchanting_disable_xp_cost"));
                DISABLE_ENCHANTING_BOOKS = Boolean.parseBoolean(props.getProperty("enchanting_disable_book"));

                ALLOW_CREATIVE_ANVIL_MERGE = Boolean.parseBoolean(props.getProperty("anvil_allow_creative_merge"));
                ALLOW_SURVIVAL_ANVIL_MERGE = Boolean.parseBoolean(props.getProperty("anvil_allow_survival_merge"));
                DISABLE_ANVIL_DAMAGE_ON_RENAME = Boolean.parseBoolean(props.getProperty("anvil_rename_disable_damage"));
                REPAIR_CONSUME_CHANCE = Float.parseFloat(props.getProperty("anvil_repair_consume_chance"));
                REPAIR_FAIL_CHANCE = Float.parseFloat(props.getProperty("anvil_repair_fail_chance"));
                REPAIR_DISENCHANT_CHANCE = Float.parseFloat(props.getProperty("anvil_repair_disenchant_chance"));
                REPAIR_CHEAP = Boolean.parseBoolean(props.getProperty("anvil_repair_cheap"));
                REPAIR_VANISHING = Boolean.parseBoolean(props.getProperty("anvil_repair_vanishing"));
                DISABLE_ANVIL_XP_COST = Boolean.parseBoolean(props.getProperty("anvil_disable_xp_cost"));

                XP_LEVELS_LOST_ON_DEATH = Integer.parseInt(props.getProperty("xp_levels_lost_on_death"));
                XP_LOST_DROPPED = Float.parseFloat(props.getProperty("xp_lost_dropped"));
                DISABLE_MENDING_XP_REPAIR = Boolean.parseBoolean(props.getProperty("disable_mending_xp_repair"));

                GRINDSTONE_DISABLE_XP = Boolean.parseBoolean(props.getProperty("grindstone_disable_xp"));
                GRINDSTONE_EXTRACT_TREASURE = Boolean.parseBoolean(props.getProperty("grindstone_extract_treasure"));
                GRINDSTONE_EXTRACT_KEEP_NON_TREASURE = Boolean.parseBoolean(props.getProperty("grindstone_extract_keep_non_treasure"));
                GRINDSTONE_DISENCHANT_KEEP_TREASURE = Boolean.parseBoolean(props.getProperty("grindstone_disenchant_keep_treasure"));

                CURSE_TRADED_BOOKS = Boolean.parseBoolean(props.getProperty("curse_traded_books"));
                CURSE_TRADED_TOOLS = Boolean.parseBoolean(props.getProperty("curse_traded_tools"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileOutputStream out = new FileOutputStream(EnchantRepairMod.CONFIG_FNAME)) {
                defaultProps.store(out, "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
