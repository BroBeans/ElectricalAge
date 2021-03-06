package mods.eln.autominer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import mods.eln.Eln;
import mods.eln.ghost.GhostObserver;
import mods.eln.heatfurnace.HeatFurnaceContainer;
import mods.eln.misc.Coordonate;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.NodeBase;
import mods.eln.node.NodeElectricalLoad;
import mods.eln.node.TransparentNode;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElectricalLoadWatchdog;
import mods.eln.node.TransparentNodeElement;
import mods.eln.node.TransparentNodeElementInventory;
import mods.eln.sim.ElectricalLoad;
import mods.eln.sim.ThermalLoad;
import mods.eln.sim.VoltageWatchdogProcessForInventoryItemDamageSingleLoad;
import mods.eln.teleporter.TeleporterPowerNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public class AutoMinerElement extends TransparentNodeElement implements GhostObserver {
	TransparentNodeElementInventory inventory = new TransparentNodeElementInventory(3, 64, this);
	
	NodeElectricalLoad inPowerLoad = new NodeElectricalLoad("inPowerLoad");
	AutoMinerSlowProcess slowProcess = new AutoMinerSlowProcess(this);
	TransparentNodeElectricalLoadWatchdog electricalLoadWatchdog = new TransparentNodeElectricalLoadWatchdog(this, inPowerLoad, 4);
	
	//VoltageWatchdogProcessForInventoryItemDamageSingleLoad electricalDrillWatchDog = new VoltageWatchdogProcessForInventoryItemDamageSingleLoad(inventory, AutoMinerContainer.electricalDrillSlotId, inPowerLoad);
	//VoltageWatchdogProcessForInventoryItemDamageSingleLoad electricalScannerWatchDog = new VoltageWatchdogProcessForInventoryItemDamageSingleLoad(inventory, AutoMinerContainer.OreScannerSlotId, inPowerLoad);
	
	AutoMinerDescriptor descriptor;
	
	Coordonate lightCoordonate;
	
	public AutoMinerElement(TransparentNode transparentNode,
			TransparentNodeDescriptor descriptor) {
		super(transparentNode, descriptor);
		this.descriptor = (AutoMinerDescriptor) descriptor;
		this.slowProcessList.add(electricalLoadWatchdog);
		electricalLoadList.add(inPowerLoad);
		slowProcessList.add(slowProcess);
		//slowProcessList.add(electricalDrillWatchDog);
		//slowProcessList.add(electricalScannerWatchDog);
		//Eln.ghostManager.addObserver(this);
	}

	@Override
	public ElectricalLoad getElectricalLoad(Direction side, LRDU lrdu) {
		return inPowerLoad;
	}

	@Override
	public ThermalLoad getThermalLoad(Direction side, LRDU lrdu) {
		return null;
	}

	@Override
	public int getConnectionMask(Direction side, LRDU lrdu) {
		return NodeBase.maskElectricalPower;
	}

	@Override
	public String multiMeterString(Direction side) {
		return Utils.plotUIP(inPowerLoad.Uc, inPowerLoad.getCurrent());
	}

	@Override
	public String thermoMeterString(Direction side) {
		return "";
	}

	@Override
	public void initialize() {
		lightCoordonate = new Coordonate(this.descriptor.lightCoord);
		lightCoordonate.applyTransformation(front, node.coordonate);
		
		int idx = 0;
		for(Coordonate c : descriptor.getPowerCoordonate(node.coordonate.world())){
			AutoMinerPowerNode n = new AutoMinerPowerNode();
			n.setElement(this);
			c.applyTransformation(front,node.coordonate);
			
			Direction dir;
			if(idx != 0)
				dir = front.left();
			else
				dir = front.right();
			
			//dir = front;
			n.onBlockPlacedBy(c, dir, null, null);
			
			powerNodeList.add(n);
			idx++;
		}
		
		descriptor.applyTo(inPowerLoad);
		descriptor.applyTo(electricalLoadWatchdog);
		connect();
	}
	
	@Override
	public void onBreakElement() {
		// TODO Auto-generated method stub
		super.onBreakElement();
		slowProcess.onBreakElement();
	
		for(AutoMinerPowerNode n : powerNodeList){
			n.onBreakBlock();
		}
		powerNodeList.clear();
		
	}
	ArrayList<AutoMinerPowerNode> powerNodeList = new ArrayList<AutoMinerPowerNode>();


	@Override
	public boolean onBlockActivated(EntityPlayer entityPlayer, Direction side,
			float vx, float vy, float vz) {

		return false;
	}

	@Override
	public boolean hasGui() {
		return true;
	}
	
	@Override
	public Container newContainer(Direction side, EntityPlayer player) {
		return new AutoMinerContainer(node,player, inventory);
	}
	
	@Override
	public IInventory getInventory() {
		return inventory;
	}	
	


	@Override
	public Coordonate getGhostObserverCoordonate() {
		return node.coordonate;
	}

	@Override
	public void ghostDestroyed(int UUID) {
		super.ghostDestroyed(UUID);
		slowProcess.ghostDestroyed(UUID);
	}

	@Override
	public boolean ghostBlockActivated(int UUID, EntityPlayer entityPlayer,
			Direction side, float vx, float vy, float vz) {
		
		return super.ghostBlockActivated(UUID, entityPlayer, side, vx, vy, vz);
	}
	
	@Override
	public void networkSerialize(DataOutputStream stream) {
		super.networkSerialize(stream);
		try {
			stream.writeShort(slowProcess.pipeLength);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
