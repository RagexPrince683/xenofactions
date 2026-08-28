package com.hfr.world.earth.pregen;

import java.util.ArrayList;
import java.util.List;

public final class XFEarthPregenState {
 public int formatVersion=1,totalRegions,totalChunks,completedChunks;
 public String profile,sourceManifestHash,generatorVersion,currentRegion,completionState;
 public long startTime,lastUpdateTime;
 public List<String> completedRegions=new ArrayList<String>();
}
