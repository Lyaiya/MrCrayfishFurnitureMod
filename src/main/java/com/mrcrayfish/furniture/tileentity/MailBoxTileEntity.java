package com.mrcrayfish.furniture.tileentity;

import com.mrcrayfish.furniture.common.mail.Mail;
import com.mrcrayfish.furniture.common.mail.PostOffice;
import com.mrcrayfish.furniture.core.ModTileEntities;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MailBoxTileEntity extends BasicLootTileEntity implements ITickableTileEntity
{
    private UUID id;
    private String ownerName;
    private UUID ownerId;

    public MailBoxTileEntity()
    {
        super(ModTileEntities.MAIL_BOX);
    }

    public void setId(UUID id)
    {
        if(this.id == null)
        {
            this.id = id;
        }
    }

    public UUID getId()
    {
        return this.id;
    }

    public void setOwner(ServerPlayerEntity entity)
    {
        this.ownerId = entity.getUniqueID();
        this.ownerName = entity.getName().getString();
    }

    public UUID getOwnerId()
    {
        return this.ownerId;
    }

    public String getOwnerName()
    {
        return this.ownerName;
    }

    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }

    @Override
    public void tick()
    {
        if(world != null && !world.isRemote)
        {
            if(!this.isFull() && this.ownerId != null && this.id != null)
            {
                Supplier<Mail> supplier = PostOffice.getMailForPlayerMailBox(this.ownerId, this.id);
                while(!this.isFull())
                {
                    Mail mail = supplier.get();
                    if(mail == null) break;
                    this.addItem(mail.getStack());
                }
            }
        }
    }

    @Override
    public int getSizeInventory()
    {
        return 18;
    }

    @Override
    protected ITextComponent getDefaultName()
    {
        return new TranslationTextComponent("container.cfm.mail_box", this.getOwnerName());
    }

    @Override
    protected Container createMenu(int windowId, PlayerInventory playerInventory)
    {
        return new ChestContainer(ContainerType.GENERIC_9X2, windowId, playerInventory, this, 2);
    }

    @Override
    public void read(CompoundNBT compound)
    {
        super.read(compound);
        this.readData(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        this.writeData(compound);
        return super.write(compound);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.writeData(new CompoundNBT());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        CompoundNBT compound = pkt.getNbtCompound();
        this.readData(compound);
    }

    private void readData(CompoundNBT compound)
    {
        if(compound.hasUniqueId("MailBoxUUID"))
        {
            this.id = compound.getUniqueId("MailBoxUUID");
        }
        if(compound.contains("OwnerName", Constants.NBT.TAG_STRING))
        {
            this.ownerName = compound.getString("OwnerName");
        }
        if(compound.hasUniqueId("OwnerUUID"))
        {
            this.ownerId = compound.getUniqueId("OwnerUUID");
        }
    }

    private CompoundNBT writeData(CompoundNBT compound)
    {
        if(this.id != null)
        {
            compound.putUniqueId("MailBoxUUID", this.id);
        }
        if(this.ownerName != null && this.ownerId != null)
        {
            compound.putString("OwnerName", this.ownerName);
            compound.putUniqueId("OwnerUUID", this.ownerId);
        }
        return compound;
    }
}
