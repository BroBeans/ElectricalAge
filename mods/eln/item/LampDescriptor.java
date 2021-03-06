package mods.eln.item;

import java.util.List;

import mods.eln.generic.GenericItemUsingDamageDescriptor;
import mods.eln.lampsocket.LampSocketType;
import mods.eln.misc.Utils;
import mods.eln.sim.ElectricalResistor;
import mods.eln.wiki.Data;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;



public class LampDescriptor  extends GenericItemUsingDamageDescriptorUpgrade
{


	public enum Type {Incandescent,eco}
	public double nominalP,nominalLight,nominalLife;
	public String name,description;
	public Type type;
	public LampSocketType socket;
	
	public int textureId;
	
	
	public double nominalU,minimalU;
	public double stableU,stableUNormalised,stableTime,vegetableGrowRate;
	
	public LampDescriptor(	
			String name,String iconName,
			Type type,LampSocketType socket,
			double nominalU,double nominalP,double nominalLight,double nominalLife,
			double vegetableGrowRate
			)
	{
		super( name);
		changeDefaultIcon(iconName);
		this.type = type;
		this.socket = socket;
		this.nominalU = nominalU;
		this.nominalP = nominalP;
		this.nominalLight = nominalLight;
		this.nominalLife = nominalLife;
		//this.description = description;
		this.vegetableGrowRate = vegetableGrowRate;

		switch (type) {
		case Incandescent:
			minimalU = nominalU*0.5; 
			break;
		case eco:
			stableUNormalised = 0.75;
			minimalU = nominalU * 0.5;
			stableU = nominalU * stableUNormalised;
			stableTime = 4;
			break;

		default:
			break;
		}
		
		
	}
	
	
	@Override
	public void setParent(Item item, int damage) {
		// TODO Auto-generated method stub
		super.setParent(item, damage);
		Data.addLight(newItemStack());
	}
	
	public double getR()
	{
		return nominalU*nominalU/nominalP;
	}
	
	
	double getLifeInTag(ItemStack stack)
	{
		if(stack.hasTagCompound() == false)
			stack.setTagCompound(getDefaultNBT());
		return stack.getTagCompound().getDouble("life");
	}
	
	@Override
	public NBTTagCompound getDefaultNBT() {
		// TODO Auto-generated method stub
		NBTTagCompound nbt = new NBTTagCompound("lampnbt");
		nbt.setDouble("life", 1.0);
		return nbt;
	}
	
	@Override
	public ItemStack newItemStack(int size) {
		// TODO Auto-generated method stub

		return super.newItemStack(size);
	}
	
	public void applyTo(ElectricalResistor resistor)
	{
		resistor.setR(getR());
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer,
			List list, boolean par4) {
		// TODO Auto-generated method stub
		super.addInformation(itemStack, entityPlayer, list, par4);
		
		list.add("Socket : " + socket);
		list.add("Techno : " + type);
		list.add(Utils.plotTime("Life : ",getLifeInTag(itemStack)*nominalLife));
		
	}
}
