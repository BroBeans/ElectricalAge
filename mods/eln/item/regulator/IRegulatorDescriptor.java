package mods.eln.item.regulator;

import mods.eln.generic.GenericItemUsingDamageDescriptor;
import mods.eln.item.GenericItemUsingDamageDescriptorUpgrade;
import mods.eln.sim.RegulatorProcess;
import mods.eln.sim.RegulatorType;
import mods.eln.sim.ThermalRegulator;

public abstract class IRegulatorDescriptor extends GenericItemUsingDamageDescriptorUpgrade{
	
	
	public IRegulatorDescriptor(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public abstract RegulatorType getType();
	public abstract void applyTo(RegulatorProcess regulator,double workingPoint,double P,double I,double D);


}
