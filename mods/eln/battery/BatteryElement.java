package mods.eln.battery;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import mods.eln.Eln;
import mods.eln.misc.Direction;
import mods.eln.misc.FunctionTable;
import mods.eln.misc.LRDU;
import mods.eln.misc.LRDUMask;
import mods.eln.misc.Utils;
import mods.eln.node.IThermalDestructorDescriptor;
import mods.eln.node.NodeBase;
import mods.eln.node.NodeBatteryProcess;
import mods.eln.node.NodeBatterySlowProcess;
import mods.eln.node.NodeElectricalLoad;
import mods.eln.node.NodeThermalLoad;
import mods.eln.node.NodeThermalWatchdogProcess;
import mods.eln.node.SixNode;
import mods.eln.node.TransparentNode;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElement;
import mods.eln.node.TransparentNodeElementInventory;
import mods.eln.sim.ElectricalConnection;
import mods.eln.sim.ElectricalLoad;
import mods.eln.sim.ElectricalLoadHeatThermalLoadProcess;
import mods.eln.sim.ElectricalResistor;
import mods.eln.sim.ElectricalSourceRefGroundProcess;
import mods.eln.sim.ITemperatureWatchdogDescriptor;
import mods.eln.sim.ThermalLoad;
import mods.eln.sim.TransformerProcess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BatteryElement extends TransparentNodeElement implements IThermalDestructorDescriptor, ITemperatureWatchdogDescriptor {

	public NodeElectricalLoad positiveLoad = new NodeElectricalLoad("positiveLoad");
	public NodeElectricalLoad negativeLoad = new NodeElectricalLoad("negativeLoad");
	public NodeThermalLoad thermalLoad = new NodeThermalLoad("thermalLoad");
	public NodeBatteryProcess batteryProcess = new NodeBatteryProcess(positiveLoad, negativeLoad, null, 0);
	public ElectricalLoadHeatThermalLoadProcess positiveETProcess = new ElectricalLoadHeatThermalLoadProcess(positiveLoad, thermalLoad);
//	public ElectricalLoadHeatThermalLoadProcess negativeETProcess = new ElectricalLoadHeatThermalLoadProcess(negativeLoad, thermalLoad);
	public ElectricalResistor dischargeResistor = new ElectricalResistor(positiveLoad, negativeLoad);
	
	public BatteryInventoryProcess inventoryProcess = new BatteryInventoryProcess(this);
	
	double syncronizedPositiveUc, syncronizedNegativeUc, syncronizedCurrent, syncronizedTc;
	
	NodeBatterySlowProcess batterySlowProcess = new NodeBatterySlowProcess(node, batteryProcess, thermalLoad);
	NodeThermalWatchdogProcess thermalWatchdogProcess = new NodeThermalWatchdogProcess(node, this, this, thermalLoad);
		 
	TransparentNodeElementInventory inventory = new TransparentNodeElementInventory(2, 64, this);
	
	@Override
	public IInventory getInventory() {
		return inventory;
	}
	
	boolean fromNBT = false;
	
	public BatteryDescriptor descriptor;
	
	public BatteryElement(TransparentNode transparentNode, TransparentNodeDescriptor descriptor) {
		super(transparentNode, descriptor);
		this.descriptor = (BatteryDescriptor) descriptor;
		
	   	electricalLoadList.add(positiveLoad);
	   	electricalLoadList.add(negativeLoad);
	   	electricalProcessList.add(dischargeResistor);
	   	thermalLoadList.add(thermalLoad);
	   	electricalProcessList.add(batteryProcess);
	   	thermalProcessList.add(positiveETProcess);
	    //	thermalProcessList.add(negativeETProcess);

	   	slowProcessList.add(batterySlowProcess);
    	slowProcessList.add(thermalWatchdogProcess);
    	slowProcessList.add(inventoryProcess);
    	
    	grounded = false;
    	batteryProcess.setIMax(this.descriptor.IMax);
	}

	public boolean hasOverVoltageProtection() {
		return inventory.getStackInSlot(0) != null;
	}
	
	public boolean hasOverHeatingProtection() {
		return inventory.getStackInSlot(1) != null;
	}
	
	@Override
	public ElectricalLoad getElectricalLoad(Direction side, LRDU lrdu) {
		if(lrdu != LRDU.Down) return null;
		if(side == front.left()) return positiveLoad;
		if(side == front.right() && ! grounded) return negativeLoad;
		return null;	
	}

	@Override
	public ThermalLoad getThermalLoad(Direction side, LRDU lrdu) {
		if(lrdu != LRDU.Down) return null;
	/*	if(side == front.left()) return thermalLoad;
		if(side == front.right() && ! grounded) return thermalLoad;*/
		return null;			
	}

	@Override
	public int getConnectionMask(Direction side, LRDU lrdu) {
		if(lrdu != LRDU.Down) return 0;
		if(side == front.left()) return node.maskElectricalPower;
		if(side == front.right() && ! grounded) return node.maskElectricalPower;
		return 0;		
	}

	@Override
	public String multiMeterString(Direction side) {
	//	if(side == front)return  Utils.plotVolt("U+", positiveLoad.Uc );
	//	if(side == front.back() && ! grounded)return  Utils.plotVolt("U-", negativeLoad.Uc );
		return  Utils.plotVolt("Ubat:", batteryProcess.getU()) + Utils.plotAmpere("Current Output:", batteryProcess.dischargeCurrentMesure);
	}

	@Override
	public String thermoMeterString(Direction side) {
		return  Utils.plotCelsius("Tbat:", thermalLoad.Tc);
	}
	
	@Override
	public void networkSerialize(DataOutputStream stream) {
		super.networkSerialize(stream);
    	try {
	    	stream.writeFloat((float)((positiveLoad.Uc - negativeLoad.Uc) * batteryProcess.dischargeCurrentMesure));
	    	stream.writeFloat((float) batteryProcess.getEnergy());
	    	stream.writeShort((short)(batteryProcess.life * 1000));

	    	node.lrduCubeMask.getTranslate(Direction.YN).serialize(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void initialize() {
		initPhysicalValue();
		connect();		
	}
	
	public void initPhysicalValue() {
		descriptor.applyTo(batteryProcess);
		descriptor.applyTo(thermalLoad);
		descriptor.applyTo(negativeLoad, Eln.simulator);
		descriptor.applyTo(positiveLoad, Eln.simulator);
		descriptor.applyTo(dischargeResistor);
		descriptor.applyTo(batterySlowProcess);
		
		if(fromItemStack) {
			batteryProcess.life = fromItemStack_life;
			batteryProcess.setCharge(fromItemStack_charge);
			fromItemStack = false;
		}
		negativeLoad.grounded(grounded);
	}

	@Override
    public void inventoryChange(IInventory inventory) {
	//	initPhysicalValue();
    }
	
	@Override
	public boolean onBlockActivated(EntityPlayer entityPlayer, Direction side,
			float vx, float vy, float vz) {
		return false;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt, String str) {
		super.writeToNBT(nbt, str);
		//inventory.writeToNBT(nbt, str + "inv");
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt, String str) {
		super.readFromNBT(nbt, str);
		fromNBT = true;
		//inventory.readFromNBT(nbt, str + "inv");
	}
	
	@Override
	public boolean hasGui() {
		return true;
	}
	
	@Override
	public Container newContainer(Direction side, EntityPlayer player) {
		return new BatteryContainer(this.node, player, inventory);
	}

	@Override
	public void onGroundedChangedByClient() {
		super.onGroundedChangedByClient();
		
		disconnect();
		initPhysicalValue();
		reconnect();
	}
	
	//static int UUIDCounter = 0;
	//int UUID = 0;
	boolean fromItemStack = false;
	double fromItemStack_charge;
	double fromItemStack_life;
	
	@Override
	public void readItemStackNBT(NBTTagCompound nbt) {
		super.readItemStackNBT(nbt);
		
		if(nbt == null) nbt = descriptor.getDefaultNBT();
		fromItemStack_charge = nbt.getDouble("charge");
		fromItemStack_life = nbt.getDouble("life");
		
		fromItemStack = true;
	}
	
	@Override
	public NBTTagCompound getItemStackNBT() {
		NBTTagCompound nbt = new NBTTagCompound("itemStackNBT");
		nbt.setDouble("charge", batteryProcess.getCharge());
		nbt.setDouble("life", batteryProcess.life);
		return nbt;
	}

	@Override
	public double getThermalDestructionMax() {
		return 3;
	}

	@Override
	public double getThermalDestructionStart() {
		return 2;
	}

	@Override
	public double getThermalDestructionPerOverflow() {
		return 0.2;
	}

	@Override
	public double getThermalDestructionProbabilityPerOverflow() {
		return 1 / descriptor.thermalWarmLimit / 0.1;
	}

	@Override
	public double getTmax() {
		return descriptor.thermalWarmLimit;
	}

	@Override
	public double getTmin() {
		return descriptor.thermalCoolLimit;
	}

	/*
	public static NBTTagCompound newItemStackNBT() {
		NBTTagCompound nbt = new NBTTagCompound("itemStackNBT");
		nbt.setDouble("charge", 0.5);
		nbt.setDouble("life", 1.0);
		return nbt;
	}*/
}
