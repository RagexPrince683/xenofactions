#!/usr/bin/env python3
"""Regression check that the claim DrawStep leaves JourneyMap's depth mask intact."""

from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
HANDLER = ROOT / "src/main/java/com/hfr/client/journeymap/JourneyMapClaimDrawHandler.java"


class JourneyMapClaimDepthStateTest(unittest.TestCase):
    def test_claim_renderer_does_not_override_depth_state(self):
        source = HANDLER.read_text(encoding="utf-8")

        self.assertNotIn("GL_DEPTH_TEST", source)
        self.assertNotIn("glDepthFunc", source)
        self.assertNotIn("glDepthMask", source)
        self.assertIn("JourneyMap owns the active depth state", source)


if __name__ == "__main__":
    unittest.main()
