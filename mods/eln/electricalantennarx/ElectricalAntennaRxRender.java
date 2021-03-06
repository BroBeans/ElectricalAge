package mods.eln.electricalantennarx;

import java.io.DataInputStream;

import org.lwjgl.opengl.GL11;

import mods.eln.Eln;
import mods.eln.cable.CableRender;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.cable.CableRenderType;
import mods.eln.electricalantennatx.ElectricalAntennaTxDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.LRDUMask;
import mods.eln.misc.Utils;
import mods.eln.node.TransparentNodeDescriptor;
import mods.eln.node.TransparentNodeElementRender;
import mods.eln.node.TransparentNodeEntity;

public class ElectricalAntennaRxRender extends TransparentNodeElementRender {

	public ElectricalAntennaRxRender(TransparentNodeEntity tileEntity, TransparentNodeDescriptor descriptor) {
		super(tileEntity, descriptor);
		this.descriptor = (ElectricalAntennaRxDescriptor) descriptor;
	}
	
	ElectricalAntennaRxDescriptor descriptor;

	@Override
	public void draw() {
		GL11.glPushMatrix();
			front.glRotateXnRef();
			rot.glRotateOnX();
			descriptor.draw();
		GL11.glPopMatrix();
		
		glCableTransforme(front.getInverse());
		descriptor.cable.bindCableTexture();
		
		if(cableRefresh) {
			cableRefresh = false;
			connectionType = CableRender.connectionType(tileEntity, lrduConnection, front.getInverse());
		}
		
		for(LRDU lrdu : LRDU.values()) {
			Utils.setGlColorFromDye(connectionType.otherdry[lrdu.toInt()]);
			if(lrduConnection.get(lrdu) == false) continue;
			maskTemp.set(1<<lrdu.toInt());
			
			Direction side = front.getInverse().applyLRDU(lrdu);
			CableRender.drawCable(getCableRender(side, side.getLRDUGoingTo(front.getInverse())), maskTemp, connectionType);
		}
	}
	
	LRDUMask maskTemp = new LRDUMask();
	LRDU rot;
	
	LRDUMask lrduConnection = new LRDUMask();
	CableRenderType connectionType;
	boolean cableRefresh = false;
	
	@Override
	public void networkUnserialize(DataInputStream stream) {
		super.networkUnserialize(stream);
		rot = LRDU.deserialize(stream);
		lrduConnection.deserialize(stream);
		cableRefresh = true;
	}
	
	@Override
	public CableRenderDescriptor getCableRender(Direction side, LRDU lrdu) {
		if(front.getInverse() != side.applyLRDU(lrdu)) return null;
		
		if(side == front.applyLRDU(rot.left())) return descriptor.cable.render;
		if(side == front.applyLRDU(rot.right())) return Eln.instance.signalCableDescriptor.render;
		return null;
	}
	
	@Override
	public void notifyNeighborSpawn() {
		super.notifyNeighborSpawn();
		cableRefresh = true;
	}
}
