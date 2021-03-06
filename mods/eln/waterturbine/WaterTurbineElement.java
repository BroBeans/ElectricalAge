package mods.eln.waterturbine;

import java.io.DataOutputStream;
import java.io.IOException;

import mods.eln.Eln;
import mods.eln.PlayerManager;
import mods.eln.ghost.GhostObserver;
import mods.eln.item.DynamoDescriptor;
import mods.eln.misc.Coordonate;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.NodeBase;
import mods.eln.node.NodeElectricalLoad;
import mods.eln.node.NodePeriodicPublishProcess;
import mods.eln.node.TransparentNode;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElement;
import mods.eln.node.TransparentNodeElementInventory;
import mods.eln.sim.ElectricalLoad;
import mods.eln.sim.ElectricalPowerSource;
import mods.eln.sim.ThermalLoad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class WaterTurbineElement extends TransparentNodeElement{

	NodeElectricalLoad positiveLoad = new NodeElectricalLoad("positiveLoad");

	ElectricalPowerSource powerSource = new ElectricalPowerSource(positiveLoad, ElectricalLoad.groundLoad);
	
	WaterTurbineSlowProcess slowProcess = new WaterTurbineSlowProcess(this);
	
	WaterTurbineDescriptor descriptor;
	

	
	public WaterTurbineElement(TransparentNode transparentNode,
			TransparentNodeDescriptor descriptor) {
		super(transparentNode, descriptor);
		

		
		this.descriptor = (WaterTurbineDescriptor) descriptor;
		
		electricalLoadList.add(positiveLoad);
		
		electricalProcessList.add(powerSource);
		slowProcessList.add(new NodePeriodicPublishProcess(transparentNode, 2, 2));
		slowProcessList.add(slowProcess);
	}

	@Override
	public ElectricalLoad getElectricalLoad(Direction side, LRDU lrdu) {
		if(lrdu != LRDU.Down) return null;
		if(side == front) return positiveLoad;
		return null;
	}

	@Override
	public ThermalLoad getThermalLoad(Direction side, LRDU lrdu) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConnectionMask(Direction side, LRDU lrdu) {
		// TODO Auto-generated method stub
		if(lrdu != LRDU.Down) return 0;
		if(side == front) return NodeBase.maskElectricalPower;
		return 0;
	}

	@Override
	public String multiMeterString(Direction side) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String thermoMeterString(Direction side) {
		// TODO Auto-generated method stub
		return null;
	}

	Coordonate waterCoord;
		
	@Override
	public void initialize() {

		setPhysicalValue();
		waterCoord = descriptor.getWaterCoordonate(node.coordonate.world());
		waterCoord.applyTransformation(front, node.coordonate);
		powerSource.setUmax(descriptor.maxVoltage);
		powerSource.setImax(descriptor.nominalPower*5/descriptor.maxVoltage);
		connect();
	}


	private void setPhysicalValue() {
		descriptor.cable.applyTo(positiveLoad,false);
	}


	TransparentNodeElementInventory inventory = new TransparentNodeElementInventory(0 , 64, this);
	
	@Override
	public IInventory getInventory() {
		// TODO Auto-generated method stub
		return inventory;
	}
	
	@Override
	public boolean hasGui() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Container newContainer(Direction side, EntityPlayer player) {
		// TODO Auto-generated method stub
		return new WaterTurbineContainer(this.node, player, inventory);
	}


	
	@Override
	public void networkSerialize(DataOutputStream stream) {
		// TODO Auto-generated method stub
		super.networkSerialize(stream);
		try {
			stream.writeFloat((float) (powerSource.getP()/descriptor.nominalPower));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityPlayer, Direction side, float vx, float vy, float vz) {
		// TODO Auto-generated method stub
		return false;
	}


	
	
	 

}
