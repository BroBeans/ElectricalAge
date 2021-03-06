package mods.eln.modbusrtu;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.cable.CableRenderType;
import mods.eln.client.ClientProxy;
import mods.eln.client.FrameTime;
import mods.eln.electricalcable.ElectricalCableDescriptor;
import mods.eln.generic.GenericItemUsingDamageDescriptor;
import mods.eln.heatfurnace.HeatFurnaceElement;
import mods.eln.item.FerromagneticCoreDescriptor;
import mods.eln.misc.Coordonate;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.LRDUMask;
import mods.eln.misc.PhysicalInterpolator;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.misc.Utils;
import mods.eln.node.NodeBase;
import mods.eln.node.SixNodeDescriptor;
import mods.eln.node.SixNodeElementRender;
import mods.eln.node.SixNodeEntity;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElementInventory;
import mods.eln.node.TransparentNodeElementRender;
import mods.eln.node.TransparentNodeEntity;
import mods.eln.wirelesssignal.WirelessSignalTxElement;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.google.common.base.CaseFormat;


public class ModbusRtuRender extends SixNodeElementRender{

	Coordonate coord;
	PhysicalInterpolator interpolator;
	float modbusActivityTimeout = 0;
	float modbusErrorTimeout = 0;
	
	ModbusRtuDescriptor descriptor;
	public ModbusRtuRender(SixNodeEntity tileEntity, Direction side,SixNodeDescriptor descriptor) {
		super(tileEntity,side,descriptor);
		this.descriptor = (ModbusRtuDescriptor) descriptor;
		
		interpolator = new PhysicalInterpolator(0.4f,8.0f,0.9f,0.2f);
		coord = new Coordonate(tileEntity);
	}

	
	HashMap<Integer,WirelessTxStatus> wirelessTxStatusList = new HashMap<Integer,WirelessTxStatus>();
	HashMap<Integer,WirelessRxStatus> wirelessRxStatusList = new HashMap<Integer,WirelessRxStatus>();
	
	@Override
	public void draw() {
		super.draw();
		// TODO Auto-generated method stub

		if(Utils.isPlayerAround(tileEntity.worldObj,coord.getAxisAlignedBB(0)) == false)
			interpolator.setTarget(0f);
		else
			interpolator.setTarget(1f);
		
		
		interpolator.stepGraphic();
		
		LRDU.Down.glRotateOnX();
		
		descriptor.draw(interpolator.get(), modbusActivityTimeout > 0, modbusErrorTimeout > 0);
		
		if (modbusActivityTimeout > 0) 
			modbusActivityTimeout -= FrameTime.get();
		
		if (modbusErrorTimeout > 0) 
			modbusErrorTimeout -= FrameTime.get();
	}

	int station = -1;
	String name;
	boolean boot = true;
	@Override
	public void publishUnserialize(DataInputStream stream) {
		// TODO Auto-generated method stub
		super.publishUnserialize(stream);
		
		try {
			station = stream.readInt();
			name = stream.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(boot)
			clientSend(ModbusRtuElement.serverAllSyncronise);
		boot = false;
		
	}
	

	@Override
	public void serverPacketUnserialize(DataInputStream stream)
			throws IOException {
		// TODO Auto-generated method stub
		super.serverPacketUnserialize(stream);
		
		switch (stream.readByte()) {
		case ModbusRtuElement.clientAllSyncronise:
		{
			wirelessTxStatusList.clear();
			for(int idx = stream.readInt();idx > 0;idx--){
				WirelessTxStatus tx = new WirelessTxStatus();
				tx.readFrom(stream);
				wirelessTxStatusList.put(tx.uuid, tx);
			}
			wirelessRxStatusList.clear();
			for(int idx = stream.readInt();idx > 0;idx--){
				WirelessRxStatus rx = new WirelessRxStatus();
				rx.readFrom(stream);
				wirelessRxStatusList.put(rx.uuid, rx);
			}
			
			
			rxTxChange = true;
		}
		break;
		case ModbusRtuElement.clientTx1Syncronise:
		{
			WirelessTxStatus newTx = new WirelessTxStatus();
			newTx.readFrom(stream);
			wirelessTxStatusList.put(newTx.uuid, newTx);
			rxTxChange = true;
		}
		break;
		case ModbusRtuElement.clientRx1Syncronise:
		{
			WirelessRxStatus newRx = new WirelessRxStatus();
			newRx.readFrom(stream);
			wirelessRxStatusList.put(newRx.uuid, newRx);
			rxTxChange = true;
		}
		break;
		case ModbusRtuElement.clientTxDelete:
		{
			wirelessTxStatusList.remove(stream.readInt());
			rxTxChange = true;
		}
		break;
		case ModbusRtuElement.clientRxDelete:
		{
			wirelessRxStatusList.remove(stream.readInt());
			rxTxChange = true;
		}
		break;
		case ModbusRtuElement.clientRx1Connected:
			WirelessRxStatus rx = wirelessRxStatusList.get(stream.readInt());
			if(rx != null){
				rx.connected = stream.readBoolean();
			}
			break;
			
		case ModbusRtuElement.ClientModbusActivityEvent:
			modbusActivityTimeout = 0.05f;
			break;
			
		case ModbusRtuElement.ClientModbusErrorEvent:
			modbusErrorTimeout = 1f;
			break;
		}
	}
	
	boolean rxTxChange = false;
	
	@Override
	public GuiScreen newGuiDraw(Direction side, EntityPlayer player) {
		// TODO Auto-generated method stub
		return new ModbusRtuGui(player, this);
	}

    public CableRenderDescriptor getCableRender(LRDU lrdu)
    {
    	return Eln.instance.signalCableDescriptor.render;
    }

}
