package mods.eln.computercraftio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.management.Descriptor;

import com.google.common.base.CaseFormat;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

import mods.eln.Eln;
import mods.eln.INBTTReady;
import mods.eln.electricalcable.ElectricalCableDescriptor;
import mods.eln.heatfurnace.HeatFurnaceContainer;
import mods.eln.item.FerromagneticCoreDescriptor;
import mods.eln.misc.Coordonate;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.NodeBase;
import mods.eln.node.NodeElectricalGateInput;
import mods.eln.node.NodeElectricalGateInputOutput;
import mods.eln.node.NodeElectricalGateOutput;
import mods.eln.node.NodeElectricalGateOutputProcess;
import mods.eln.node.NodeElectricalLoad;
import mods.eln.node.NodeManager;
import mods.eln.node.NodeThermalLoad;
import mods.eln.node.SixNode;
import mods.eln.node.TransparentNode;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElement;
import mods.eln.node.TransparentNodeElementInventory;
import mods.eln.sim.ElectricalConnection;
import mods.eln.sim.ElectricalLoad;
import mods.eln.sim.ElectricalLoadHeatThermalLoadProcess;
import mods.eln.sim.ElectricalSourceRefGroundProcess;
import mods.eln.sim.IProcess;
import mods.eln.sim.ThermalLoad;
import mods.eln.sim.TransformerProcess;
import mods.eln.sim.VoltageWatchdogProcessForInventoryItemBlockDamageDualLoad;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

public class ComputerCraftIoElement extends TransparentNodeElement{
	
	public NodeElectricalGateInputOutput[] ioGate = new NodeElectricalGateInputOutput[4];
	public NodeElectricalGateOutputProcess[] ioGateProcess = new NodeElectricalGateOutputProcess[4];

	ComputerCraftIoDescriptor descriptor;
	
	public ComputerCraftIoElement(TransparentNode transparentNode, TransparentNodeDescriptor descriptor) {
		super(transparentNode, descriptor);
		for(int idx = 0; idx < 4; idx++){
			ioGate[idx] = new NodeElectricalGateInputOutput("ioGate" + idx);
			ioGateProcess[idx] = new NodeElectricalGateOutputProcess("ioGateProcess" + idx, ioGate[idx]);
			
			electricalLoadList.add(ioGate[idx]);
			electricalProcessList.add(ioGateProcess[idx]);
			
			ioGateProcess[idx].setHighImpedance(true);
		}
	   	this.descriptor = (ComputerCraftIoDescriptor) descriptor;
	}

	@Override
	public ElectricalLoad getElectricalLoad(Direction side, LRDU lrdu) {
		if(lrdu != LRDU.Down || side.isY()) return null;
		return ioGate[side.getHorizontalIndex()];
	}

	@Override
	public ThermalLoad getThermalLoad(Direction side, LRDU lrdu) {
		return null;
	}

	@Override
	public int getConnectionMask(Direction side, LRDU lrdu) {
		if(lrdu == lrdu.Down && side.isNotY()) {
			return NodeBase.maskElectricalGate;	
		}
		return 0;
	}

	@Override
	public String multiMeterString(Direction side) {
		return null;
		//Utils.plotUIP(powerLoad.Uc, powerLoad.getCurrent());
	}
	
	@Override
	public String thermoMeterString(Direction side) {
		return  null;
	}

	@Override
	public void initialize() {
		connect();		
	}

	@Override
	public boolean onBlockActivated(EntityPlayer entityPlayer, Direction side,
			float vx, float vy, float vz) {
		return false;
	}
	
	public String getType() {
		return "Probe";
	}

	@Override
	public String[] getMethodNames() {
		return new String[]{"writeDirection", "readDirection", "writeOutput", "readOutput", "readInput"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		int id = -1;
		if(arguments.length < 1) return null;
		if(arguments[0] instanceof String == false) return null;
		String arg0 = (String) arguments[0];
		if(arg0.length() < 2) return null;
		
		String sideStr = arg0.substring(0, 2);
		String remaineStr = arg0.substring(2, arg0.length());
		
		//System.out.println(sideStr + " " + remaineStr);
		
		if(sideStr.equals("XN")) id = 0;
		if(sideStr.equals("XP")) id = 1;
		if(sideStr.equals("ZN")) id = 2;
		if(sideStr.equals("ZP")) id = 3;
		if(id == -1) return null;
		
		if(remaineStr.length() != 0) {
			Coordonate c = new Coordonate(this.node.coordonate);
			Direction side = Direction.fromHorizontalIndex(id);	
			c.move(side);
			//System.out.println("SUB probe ! " + side + " " + c);
			NodeBase n = NodeManager.instance.getNodeFromCoordonate(c);
			if(n == null) return null;
			//System.out.println("  NodeBase");
			if(n instanceof TransparentNode == false) return null;
			//System.out.println("  TransparentNode");
			TransparentNode tn = (TransparentNode)n;
			if(tn.element instanceof ComputerCraftIoElement == false) return null;
			//System.out.println("  ComputerCraftIoElement");
			ComputerCraftIoElement e = (ComputerCraftIoElement) tn.element;
			Object[] argumentsCopy = arguments.clone();
			argumentsCopy[0] = remaineStr;
			return e.callMethod(computer, context, method, argumentsCopy);
		}
		
		switch (method) {
		case 0:
			if(arguments.length < 2) return null;
			ioGateProcess[id].setHighImpedance((Boolean) arguments[1]);
			break;
		case 1:
			return new Object[]{ioGateProcess[id].isHighImpedance()};
		case 2:
			if(arguments.length < 2) return null;
			ioGateProcess[id].setOutputNormalized((Double) arguments[1]);
			break;
		case 3:
			return new Object[]{ioGateProcess[id].getOutputNormalized()};
		case 4:
			return new Object[]{ioGate[id].getInputNormalized()};
		default:
			break;
		}
		return null;
	}

	@Override
	public boolean canAttachToSide(int side) {
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public void detach(IComputerAccess computer) {
	}    
}
