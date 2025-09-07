package com.hfr.blocks;

import com.hfr.data.CustomImageStorage;
import com.hfr.tileentity.TileEntityWallImage;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockWallImage extends BlockContainer {
    public static BlockWallImage instance;
    private static final float THICK = 1.0F / 16.0F; // thickness of the sliver

    public BlockWallImage() {
        super(Material.wood);
        setBlockName("wall_image_block");
        setHardness(0.5F);
        instance = this;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityWallImage();
    }

    // When placed, set metadata = face (2..5 for north/south/west/east)
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = 2; // default north
        // try to derive facing from player's rotation so that block faces player
        int dir = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        if (dir == 0) facing = 2; // north
        if (dir == 1) facing = 5; // east
        if (dir == 2) facing = 3; // south
        if (dir == 3) facing = 4; // west
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    }

    // set collision/selection box based on metadata facing
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int m = world.getBlockMetadata(x, y, z);
        setBoundsForFacing(m);
    }

    private void setBoundsForFacing(int meta) {
        switch (meta) {
            case 2: // north (-Z) -> flush to north face (z ~ 0)
                setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, THICK);
                break;
            case 3: // south (+Z)
                setBlockBounds(0.0F, 0.0F, 1.0F - THICK, 1.0F, 1.0F, 1.0F);
                break;
            case 4: // west (-X)
                setBlockBounds(0.0F, 0.0F, 0.0F, THICK, 1.0F, 1.0F);
                break;
            case 5: // east (+X)
                setBlockBounds(1.0F - THICK, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                break;
            default:
                setBlockBounds(0,0,0,1,1,1);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            // client short-circuit (server will handle changes)
            return true;
        }

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityWallImage)) {
            return false;
        }
        TileEntityWallImage tie = (TileEntityWallImage) te;

        // owner check: only owner (placer) can change. Allow ops if you want:
        String owner = tie.ownerUUID == null ? "" : tie.ownerUUID;
        String playerUUID = player.getUniqueID().toString();
        boolean isOp = MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
        if (owner.length() > 0 && !owner.equals(playerUUID) && !isOp) {
            player.addChatMessage(new ChatComponentText("You do not own this image block."));
            return true;
        }

        // fetch player's stored images
        CustomImageStorage storage = CustomImageStorage.get(world);
        List<NBTTagCompound> list = storage.getList(player.getUniqueID());
        if (list == null || list.size() == 0) {
            player.addChatMessage(new ChatComponentText("You have no stored images to cycle."));
            return true;
        }

        // advance index
        int next = tie.currentIndex;
        if (next < 0) next = 0;
        else next = (next + 1) % list.size();

        NBTTagCompound chosen = list.get(next);
        String chosenName = chosen.getString("name");
        String chosenURL = chosen.getString("url");

        // apply chosen entry
        tie.currentIndex = next;
        tie.imageName = chosenName;
        tie.imageURL = chosenURL;

        // set a textureKey so clients can uniquely cache this render (optional but helps)
        tie.textureKey = "dynimg_" + Math.abs((chosenURL + "_" + next).hashCode()) + "_" + System.currentTimeMillis();

        tie.markDirty();
        world.markBlockForUpdate(x, y, z);

        player.addChatMessage(new ChatComponentText("Image set to [" + next + "] " + chosenName));
        return true;
    }

    // ensure collision and selection use our bounds
    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
    }

    // allow render as normal (not full cube)
    @Override
    public boolean isOpaqueCube() { return false; }
    @Override
    public boolean renderAsNormalBlock() { return false; }
}
