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


def test_bomb_pending_state_cannot_apply_outside_pre_round():
    body = method(MANAGER, "public static void tickKitSelection", "public static KitSelectionResult selectKit")
    pre_round = body.index("BombRoundState.PRE_ROUND")
    application = body.index("applyKitSelectionProtection", pre_round)
    stale_cleanup = body.index("pendingKitSelection.remove", application)
    assert pre_round < application < stale_cleanup
    assert "clearKitSelectionProtection(player)" in body[stale_cleanup:]


def test_live_transition_cleans_players_before_state_change():
    body = method(BOMB, "public static void startRound", "private static boolean assignBombToRandomTerrorist")
    cleanup = body.index("TDMManager.cancelKitSelection(p)")
    live = body.index("state=BombRoundState.LIVE")
    assert cleanup < live


def test_dead_entities_are_not_processed_during_buy_setup():
    body = method(BOMB, "public static void beginBuyTime", "public static void startRound")
    assert "TDMManager.isAliveForTDM(p)" in body
    assert body.index("TDMManager.isAliveForTDM(p)") < body.index("TDMManager.respawnPlayer(p,r)")


if __name__ == "__main__":
    tests = [value for name, value in sorted(globals().items()) if name.startswith("test_")]
    for test in tests:
        test()
    print("%d TDM kit-protection structural checks passed" % len(tests))
