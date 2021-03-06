package mods.eln.eggincubator;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mods.eln.cable.CableRenderDescriptor;
import mods.eln.cable.CableRenderType;
import mods.eln.client.ClientProxy;
import mods.eln.client.FrameTime;
import mods.eln.electricalcable.ElectricalCableDescriptor;
import mods.eln.generic.GenericItemUsingDamageDescriptor;
import mods.eln.heatfurnace.HeatFurnaceElement;
import mods.eln.item.FerromagneticCoreDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.LRDUMask;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.misc.Utils;
import mods.eln.node.NodeBase;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElementInventory;
import mods.eln.node.TransparentNodeElementRender;
import mods.eln.node.TransparentNodeEntity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class EggIncubatorRender extends TransparentNodeElementRender {

	TransparentNodeElementInventory inventory = new TransparentNodeElementInventory(3, 64, this);
	EggIncubatorDescriptor descriptor;
	
	public EggIncubatorRender(TransparentNodeEntity tileEntity, TransparentNodeDescriptor descriptor) {
		super(tileEntity, descriptor);
		this.descriptor = (EggIncubatorDescriptor) descriptor;
	}

	@Override
	public void draw() {
		alpha += FrameTime.get() * 60;
		if(alpha >= 360) alpha -= 360;
		
		GL11.glPushMatrix();
		front.glRotateXnRef();
		if(egg != null) {
			Utils.drawEntityItem(egg, 0.0f, -0.3f, 0.13f, alpha, 0.6f);
		}
		descriptor.draw(eggStackSize, (float) (voltage / descriptor.nominalVoltage));
		GL11.glPopMatrix();
		cableRenderType = drawCable(front.down(), descriptor.cable.render, eConn, cableRenderType);
	}

	float alpha = 0;
	
	@Override
	public GuiScreen newGuiDraw(Direction side, EntityPlayer player) {
		return new EggIncubatorGuiDraw(player, inventory, this);
	}
	
	byte eggStackSize;

	EntityItem egg;
	public float voltage;
	
	@Override
	public void networkUnserialize(DataInputStream stream) {
		super.networkUnserialize(stream);
		try {
			eggStackSize = stream.readByte();
			if(eggStackSize != 0) {
				egg = new EntityItem(this.tileEntity.worldObj, 0, 0, 0, new ItemStack(Item.egg));
			}
			else {
				egg = null;
			}	
			eConn.deserialize(stream);
			voltage = stream.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		cableRenderType = null;
	}
	
	LRDUMask priConn = new LRDUMask(), secConn = new LRDUMask(), eConn = new LRDUMask();
	CableRenderType cableRenderType;
	
	@Override
	public CableRenderDescriptor getCableRender(Direction side, LRDU lrdu) {
		return descriptor.cable.render;
	}
	
	@Override
	public void notifyNeighborSpawn() {
		super.notifyNeighborSpawn();
		cableRenderType = null;
	}
}
