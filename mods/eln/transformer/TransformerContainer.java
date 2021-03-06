package mods.eln.transformer;

import mods.eln.BasicContainer;
import mods.eln.Eln;
import mods.eln.electricalcable.ElectricalCableDescriptor;
import mods.eln.generic.GenericItemUsingDamageSlot;
import mods.eln.gui.ISlotSkin.SlotSkin;
import mods.eln.item.FerromagneticCoreDescriptor;
import mods.eln.node.SixNodeItemSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class TransformerContainer extends BasicContainer {
	public static final int primaryCableSlotId = 0;
	public static final int secondaryCableSlotId = 1;
	public static final int ferromagneticSlotId = 2;
	
	public TransformerContainer(EntityPlayer player, IInventory inventory) {
		
		super(player, inventory,new Slot[]{
				new SixNodeItemSlot(inventory,primaryCableSlotId,58,63,4,new Class[]{ElectricalCableDescriptor.class},SlotSkin.medium,new String[]{"Electrical Cable Slot"}),
				new SixNodeItemSlot(inventory,secondaryCableSlotId,100,63,4,new Class[]{ElectricalCableDescriptor.class},SlotSkin.medium,new String[]{"Electrical Cable Slot"}),
				new GenericItemUsingDamageSlot(inventory,ferromagneticSlotId,58 + (100-58)/2 + 0,63,1,new Class[]{FerromagneticCoreDescriptor.class},SlotSkin.medium,new String[]{"Ferromagnetic Core Slot"})
			
				//	new SlotFilter(inventory,1,62 + 18,17,1,new ItemStackFilter[]{new ItemStackFilter(Eln.sixNodeBlock,0xFF,Eln.electricalCableId)})
			});
		
		// TODO Auto-generated constructor stub
	}


}
