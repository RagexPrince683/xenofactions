package com.hfr.main;


import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class HardnessDegradationData extends WorldSavedData {
    private Map<BlockPos, Float> degradedHardnessMap = new HashMap<BlockPos, Float>();

    public HardnessDegradationData(String name) {
        super(name);
    }

    public static HardnessDegradationData get(World world) {
        HardnessDegradationData data = (HardnessDegradationData) world.mapStorage.loadData(HardnessDegradationData.class, "HardnessDegradation");
        if (data == null) {
            data = new HardnessDegradationData("HardnessDegradation");
            world.mapStorage.setData("HardnessDegradation", data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("DegradedHardness", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int x = tag.getInteger("x");
            int y = tag.getInteger("y");
            int z = tag.getInteger("z");
            float hardness = tag.getFloat("hardness");

            degradedHardnessMap.put(new BlockPos(x, y, z), hardness);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<BlockPos, Float> entry : degradedHardnessMap.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("x", entry.getKey().getX());
            tag.setInteger("y", entry.getKey().getY());
            tag.setInteger("z", entry.getKey().getZ());
            tag.setFloat("hardness", entry.getValue());
            list.appendTag(tag);
        }
        nbt.setTag("DegradedHardness", list);
    }
}
