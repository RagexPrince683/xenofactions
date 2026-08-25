"""Structural regression checks for the Forge 1.7.10 TDM kit-protection lifecycle."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MANAGER = (ROOT / "src/main/java/com/hfr/tdm/TDMManager.java").read_text()
BOMB = (ROOT / "src/main/java/com/hfr/tdm/TDMBombManager.java").read_text()


def method(source, signature, next_signature):
    return source[source.index(signature):source.index(next_signature, source.index(signature))]


def test_all_owned_effects_are_cleared():
    body = method(MANAGER, "public static void clearKitSelectionProtection", "public static void closeKitGui")
    for potion in ("invisibility", "moveSlowdown", "resistance", "regeneration"):
        assert "removePotionEffect(Potion.%s.id)" % potion in body


def test_selection_contexts_separate_buy_phase_from_respawn_lock():
    body = method(MANAGER, "public static void tickKitSelection", "public static KitSelectionResult selectKit")
    assert "context==KitSelectionContext.BUY_PHASE" in body
    assert "BombRoundState.PRE_ROUND" in body
    assert "context==KitSelectionContext.RESPAWN_LOCK" in body
    assert "isHardcoreRespawns" in body
    assert "applyKitSelectionProtection(player)" in body


def test_non_hardcore_round_skips_buy_phase_and_defers_bomb():
    body = method(BOMB, "public static void beginNextBombRound", "public static void startRoundWithoutBuyTime")
    assert "isHardcoreRespawns(world)" in body
    assert "beginBuyTime(world)" in body
    assert "startRoundWithoutBuyTime(world)" in body
    direct = method(BOMB, "public static void startRoundWithoutBuyTime", "public static void startRound(World")
    assert "state=BombRoundState.LIVE" in direct
    assert "KitSelectionContext.RESPAWN_LOCK" in direct
    assert direct.index("KitSelectionContext.RESPAWN_LOCK") < direct.index("ensureLiveRoundBombAssigned")


def test_freeze_anchor_is_server_authoritative():
    body = method(MANAGER, "public static void tickKitSelection", "public static KitSelectionResult selectKit")
    for token in ("motionX=player.motionY=player.motionZ=0", "fallDistance=0", "setPlayerLocation"):
        assert token in body


def test_live_transition_cleans_players_before_state_change():
    body = method(BOMB, "public static void startRound(World", "private static boolean assignBombToRandomTerrorist")
    cleanup = body.index("TDMManager.cancelKitSelection(p)")
    live = body.index("state=BombRoundState.LIVE")
    assert cleanup < live


def test_dead_entities_are_not_processed_during_buy_setup():
    body = method(BOMB, "public static void beginBuyTime", "public static void beginNextBombRound")
    assert "TDMManager.isAliveForTDM(p)" in body
    assert body.index("TDMManager.isAliveForTDM(p)") < body.index("TDMManager.respawnPlayer(p,r)")


if __name__ == "__main__":
    tests = [value for name, value in sorted(globals().items()) if name.startswith("test_")]
    for test in tests:
        test()
    print("%d TDM kit-protection structural checks passed" % len(tests))
