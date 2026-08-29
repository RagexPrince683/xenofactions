package com.hbm.core;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"com.hbm.core"})
public final class HbmLoadingPlugin implements IFMLLoadingPlugin {

	@Override public String[] getASMTransformerClass() { return new String[] { HbmClassTransformer.class.getName() }; }
	@Override public String getModContainerClass() { return null; }
	@Override public String getSetupClass() { return null; }
	@Override public void injectData(Map<String, Object> data) { }
	@Override public String getAccessTransformerClass() { return null; }
}
