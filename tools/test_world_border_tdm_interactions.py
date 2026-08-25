#!/usr/bin/env python3
"""Source-level regression checks for world-border wand and BOMB event gating."""

from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[1]
COMMON = ROOT / "src/main/java/com/hfr/main/CommonEventHandler.java"
TDM_HANDLER = ROOT / "src/main/java/com/hfr/tdm/TDMHandler.java"
BOMB_MANAGER = ROOT / "src/main/java/com/hfr/tdm/TDMBombManager.java"
WAND = ROOT / "src/main/java/com/hfr/items/ItemWorldBorderWand.java"
COMMAND = ROOT / "src/main/java/com/hfr/command/WorldBorderCommandHandler.java"


class WorldBorderTdmInteractionTest(unittest.TestCase):
    def test_wand_listener_receives_canceled_events_and_cancels_both_clicks(self):
        source = COMMON.read_text(encoding="utf-8")
        annotation = re.search(
            r"@SubscribeEvent\s*\(\s*priority\s*=\s*EventPriority\.HIGHEST\s*,"
            r"\s*receiveCanceled\s*=\s*true\s*\)\s*"
            r"public void onWorldBorderWandInteract",
            source,
        )
        self.assertIsNotNone(annotation)
        self.assertIn("ItemWorldBorderWand.select(player, true, event.x, event.z);", source)
        self.assertIn("ItemWorldBorderWand.select(player, false, event.x, event.z);", source)
        self.assertGreaterEqual(source.count("event.setCanceled(true);"), 3)

    def test_tdm_uses_one_phase_specific_world_interaction_lock(self):
        manager = BOMB_MANAGER.read_text(encoding="utf-8")
        handler = TDM_HANDLER.read_text(encoding="utf-8")
        helper = re.search(
            r"public static boolean shouldRestrictWorldInteraction\(World world\)\{(?P<body>.*?)\n    \}",
            manager,
            re.DOTALL,
        )
        self.assertIsNotNone(helper)
        body = helper.group("body")
        self.assertIn("state==BombRoundState.PRE_ROUND", body)
        self.assertIn("state==BombRoundState.ROUND_END", body)
        self.assertIn("!TDMManager.isMapVoteActive(world)", body)
        self.assertNotIn("!isRoundActive()", body)
        self.assertEqual(handler.count("TDMBombManager.shouldRestrictWorldInteraction("), 3)
        self.assertNotIn("TDMManager.isBombMode(event.world)&&!TDMBombManager.isRoundActive()", handler)

    def test_tdm_bypass_is_exact_wand_and_permission(self):
        source = TDM_HANDLER.read_text(encoding="utf-8")
        self.assertIn("player.getHeldItem().getItem() == ModItems.world_border_wand", source)
        self.assertIn('player.canCommandSenderUseCommand(3, "xclowder")', source)
        self.assertGreaterEqual(source.count("isWorldBorderAdminWand(player)"), 2)

    def test_waiting_restores_observers_without_awarding_a_round(self):
        source = BOMB_MANAGER.read_text(encoding="utf-8")
        cleanup = re.search(
            r"private static void cleanupTransientState\(World world\)\{(?P<body>.*?)\}\n",
            source,
            re.DOTALL,
        )
        self.assertIsNotNone(cleanup)
        self.assertIn("TDMSpectatorManager.restoreAll()", cleanup.group("body"))
        self.assertIn(
            "private static void waitForTeams(World world){cleanupTransientState(world);"
            "state=BombRoundState.WAITING_FOR_TEAMS;",
            source,
        )
        self.assertNotIn("completeRound", source[source.index("private static void waitForTeams"):source.index("public static void onTestModeChanged")])

    def test_selection_remains_uuid_owned_and_command_consumed(self):
        wand = WAND.read_text(encoding="utf-8")
        command = COMMAND.read_text(encoding="utf-8")
        self.assertIn("Map<UUID, Selection> SELECTIONS", wand)
        self.assertIn("Selection selection = ItemWorldBorderWand.get(player);", command)
        self.assertIn("data.addRegion(selection.dimension", command)


if __name__ == "__main__":
    unittest.main()
