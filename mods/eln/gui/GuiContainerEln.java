package mods.eln.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import mods.eln.gui.GuiTextFieldEln.GuiTextFieldElnObserver;
import mods.eln.gui.IGuiObject.IGuiObjectObserver;
import mods.eln.gui.ISlotSkin.SlotSkin;
import mods.eln.misc.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public abstract class GuiContainerEln extends GuiContainer implements IGuiObjectObserver, GuiTextFieldElnObserver{
	public GuiContainerEln(Container par1Container) {
		super(par1Container);

	}

	public void add(IGuiObject object){
		helper.add(object);
	}
	
	public GuiHelperContainer helper;
	
	
	protected abstract GuiHelperContainer newHelper();

	
	@Override
	public void initGui() {

		helper = newHelper();
		xSize = helper.xSize;
		ySize = helper.ySize;
		super.initGui();
		
		if(helper instanceof GuiHelperContainer) 
		{
			apply((GuiHelperContainer) helper);
		}
	}
	
    void apply(GuiHelperContainer helper)
    {
    	for(int idx =inventorySlots.inventorySlots.size() - 36;idx < inventorySlots.inventorySlots.size();idx++)
    	{
    		Slot s = (Slot)inventorySlots.inventorySlots.get(idx);
    		s.xDisplayPosition += helper.xInv;
    		s.yDisplayPosition += helper.yInv;
    	}
    }
	
	public GuiTextFieldEln newGuiTextField(int x,int y,int width)
	{
		GuiTextFieldEln o =  helper.newGuiTextField(x, y, width);
		o.setObserver(this);
		return o;
	}

	public GuiButtonEln newGuiButton(int x,int y,int width,String name)
	{
		GuiButtonEln o =  helper.newGuiButton(x, y, width,name);
		o.setObserver(this);
		return o;		
	}
	public GuiVerticalTrackBar newGuiVerticalTrackBar(int x,int y,int width,int height)
	{
		GuiVerticalTrackBar o =  helper.newGuiVerticalTrackBar(x, y, width,height);
		o.setObserver(this);
		return o;	
	}
	public GuiVerticalTrackBarHeat newGuiVerticalTrackBarHeat(int x,int y,int width,int height)
	{
		GuiVerticalTrackBarHeat o =  helper.newGuiVerticalTrackBarHeat(x, y, width,height);
		o.setObserver(this);
		return o;			
	}
	public GuiVerticalProgressBar newGuiVerticalProgressBar(int x,int y,int width,int height)
	{
		GuiVerticalProgressBar o =  helper.newGuiVerticalProgressBar(x, y, width,height);

		return o;			
	}

    public void drawTexturedModalRectEln(int x, int y, int u, int v, int width, int height)
    {
		helper.drawTexturedModalRect(x, y, u, v, width, height);
    }
	@Override
	protected void keyTyped(char key, int code)
    {
		helper.keyTyped(key, code);
		if(code != 18){
			super.keyTyped(key, code);
		}
    }
    protected void mouseClicked(int x, int y, int code)
    {
    	helper.mouseClicked(x, y, code);
        super.mouseClicked(x, y, code);
    }
    @Override
    protected void mouseMovedOrUp(int x, int y, int witch) {
    	helper.mouseMovedOrUp(x, y, witch);
    	super.mouseMovedOrUp(x, y, witch);
    }
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int x, int y, float f) {
    	// TODO Auto-generated method stub
    	super.drawScreen(x, y, f);
    	
    }
    


	@Override
	public void textFieldNewValue(GuiTextFieldEln textField, String value) {
		guiObjectEvent(textField);
	}
	
	@Override
	public void guiObjectEvent(IGuiObject object) {
		// TODO Auto-generated method stub
		
	}
	
	static final ResourceLocation slotSkin = new ResourceLocation("textures/gui/container/furnace.png");
	
	@SuppressWarnings("incomplete-switch")
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
		GL11.glColor4f(1f, 1f, 1f, 1f);
		preDraw(f, mx, my);
		helper.mouseMove(mx,my);
		helper.draw(mx, my, f);
		Utils.bindTexture(slotSkin);
		GL11.glColor4f(1f, 1f, 1f, 1f);
		for(Object o : inventorySlots.inventorySlots)
		{
			Slot slot = (Slot) o;
			SlotSkin skin = SlotSkin.none;
			if(slot instanceof ISlotSkin) skin = ((ISlotSkin)slot).getSlotSkin();
			switch (skin) {
			case medium:
				drawTexturedModalRectEln(slot.xDisplayPosition-1, slot.yDisplayPosition -1, 55, 16, 73-55, 34-16	);
				break;
			case big:
				drawTexturedModalRectEln(slot.xDisplayPosition-5, slot.yDisplayPosition -5, 111, 30, 137-111, 56-30);
				break;
			}
		}
		
		postDraw(f, mx, my);

	}
	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		// TODO Auto-generated method stub
		super.drawGuiContainerForegroundLayer(mx, my);
		helper.draw2(mx, my);
		ArrayList<String> list = new ArrayList<String>();
		GL11.glColor4f(1f, 1f, 1f, 1f);

		for(Object o : inventorySlots.inventorySlots)
		{
			Slot slot = (Slot) o;
			if(	slot.getHasStack() == false 
				&&	mx - guiLeft >= slot.xDisplayPosition && my - guiTop >= slot.yDisplayPosition
			   && 	mx - guiLeft < slot.xDisplayPosition + 17 && my - guiTop< slot.yDisplayPosition + 17)
			{
				list.clear();
			    
	
				SlotSkin comment = SlotSkin.none;
				if(slot instanceof ISlotWithComment)
				{
					((ISlotWithComment)slot).getComment(list);
					int x, y;
					int strWidth = 0;
					for(String str : list)
					{
						int size = fontRenderer.getStringWidth(str);
						if(size > strWidth) strWidth = size;
					}
					
					x = slot.xDisplayPosition;
					y = slot.yDisplayPosition;
					
					int xOffset = 0;
					if(guiLeft + x + strWidth +30 > this.width) 
					{
						xOffset -= strWidth + 20;
					}
					if(list.isEmpty() == false) drawHoveringText((java.util.List) list,mx - guiLeft + xOffset,my-guiTop, fontRenderer);
				}
			}
		}
	}

	protected void preDraw(float f, int x, int y)
	{
		
	}
	
	protected void postDraw(float f, int x, int y)
	{
		if(helper.background != null)
			Utils.bindTexture(helper.background);
	}
	
	protected void drawString(int x,int y,String str)
	{
		drawString(x, y, 4210752,str);
	}	
	protected void drawString(int x,int y,int color,String str)
	{
		helper.drawString(x,y,color,str);		
	}
}
