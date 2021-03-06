package mods.eln.autominer;

import java.io.DataInputStream;
import java.io.IOException;

import org.bouncycastle.jcajce.provider.symmetric.DES;
import org.lwjgl.opengl.GL11;

import mods.eln.misc.Direction;
import mods.eln.misc.Obj3D;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElementInventory;
import mods.eln.node.TransparentNodeElementRender;
import mods.eln.node.TransparentNodeEntity;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class AutoMinerRender extends TransparentNodeElementRender {
	AutoMinerDescriptor descriptor;
	public AutoMinerRender(TransparentNodeEntity tileEntity,
			TransparentNodeDescriptor descriptor) {
		super(tileEntity, descriptor);
		this.descriptor = (AutoMinerDescriptor) descriptor;
	}

	@Override
	public void draw() {
		if(pipeLength != 0) {
			GL11.glLineWidth(20f);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			
			GL11.glBegin(GL11.GL_LINES);
				GL11.glColor4f(1f, 0f, 0f, 1f);
				GL11.glVertex3f(0f, -0.5f, 0f);
				GL11.glVertex3f(0f, -0.5f - pipeLength, 0f);
			GL11.glEnd();
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
		front.glRotateXnRef();
		descriptor.draw();
	}

	TransparentNodeElementInventory inventory = new TransparentNodeElementInventory(3, 64, this);
	
	@Override
	public GuiScreen newGuiDraw(Direction side, EntityPlayer player) {
		return new AutoMinerGuiDraw(player, inventory, this);
	}
	
	short pipeLength = 0;
	
	@Override
	public void networkUnserialize(DataInputStream stream) {
		super.networkUnserialize(stream);
		try {
			pipeLength = stream.readShort();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean cameraDrawOptimisation() {
		return false;
	}
}
