package mods.eln.electricalwindsensor;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import mods.eln.Eln;
import mods.eln.item.ThermalIsolatorElement;
import mods.eln.misc.IFunction;
import mods.eln.misc.Obj3D;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.misc.Utils;
import mods.eln.node.SixNodeDescriptor;
import mods.eln.sim.DiodeProcess;
import mods.eln.sim.ThermalLoad;
import mods.eln.sim.ThermalLoadInitializer;
import mods.eln.wiki.Data;

import com.google.common.base.Function;


public class ElectricalWindSensorDescriptor extends SixNodeDescriptor{


	private Obj3DPart baseWall,baseGround,anemometer;

	public ElectricalWindSensorDescriptor(
			String name,
			Obj3D obj,
			double windMax
			) {
		super(name, ElectricalWindSensorElement.class,ElectricalWindSensorRender.class);
		this.windMax = windMax;
		this.obj = obj;
		
		if(obj != null)
		{
			baseWall = obj.getPart("base_wall");
			baseGround = obj.getPart("base_ground");
			anemometer = obj.getPart("anemometer");
		}
	}

	Obj3D obj;

	public double windMax;

	void draw(float alpha)
	{
		if(baseWall != null) baseWall.draw();
		if(anemometer != null) {
			Utils.disableCulling();
			anemometer.draw(alpha, 0, 1, 0);
			Utils.enableCulling();
		}
	}
	
	@Override
	public void setParent(Item item, int damage) {
		// TODO Auto-generated method stub
		super.setParent(item, damage);
		Data.addSignal(newItemStack());
	}
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer,
			List list, boolean par4) {
		// TODO Auto-generated method stub
		super.addInformation(itemStack, entityPlayer, list, par4);
		list.add("Provides an electrical signal");
		list.add("dependant on weather type.");
		list.add("0V -> clear ");
		list.add(Eln.SVU/2 +"V -> rain ");
		list.add(Eln.SVU +"V -> thunder ");
	}
	
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		// TODO Auto-generated method stub
		GL11.glScalef(2f, 2f, 2f);
		draw(0);
	}
}
