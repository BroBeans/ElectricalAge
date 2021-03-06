package mods.eln.ghost;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import mods.eln.Eln;
import mods.eln.misc.Coordonate;
import mods.eln.node.NodeBase;
import mods.eln.node.NodeManager;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;



public class GhostManager extends WorldSavedData
{
	public GhostManager(String par1Str) {
		super(par1Str);
		// TODO Auto-generated constructor stub
	}
	Hashtable<Coordonate, GhostElement> ghostTable = new Hashtable<Coordonate, GhostElement>();
	Hashtable<Coordonate, GhostObserver> observerTable = new Hashtable<Coordonate, GhostObserver>();

	public void init()
	{
		
	} 
	@Override
	public boolean isDirty() {
		return true;
	}
	/*
	public void addGhost(GhostElement element)
	{
		ghostTable.put(element.elementCoordonate, element);
	}*/
	public GhostElement getGhost(Coordonate coordonate)
	{
		return ghostTable.get(coordonate);
	}
	public void removeGhost(Coordonate coordonate)
	{
		removeGhostNode(coordonate);
		ghostTable.remove(coordonate);
	}
	
	public void addObserver(GhostObserver observer)
	{
		observerTable.put(observer.getGhostObserverCoordonate(), observer);
	}
	public GhostObserver getObserver(Coordonate coordonate)
	{
		return observerTable.get(coordonate);
	}
	public void removeObserver(Coordonate coordonate)
	{
		observerTable.remove(coordonate);
	}
	
	public void removeGhostAndBlockWithObserver(Coordonate observerCoordonate)
	{
		Iterator<Entry<Coordonate, GhostElement>> iterator = ghostTable.entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<Coordonate, GhostElement> entry = iterator.next();
			GhostElement element = entry.getValue();
			if(element.observatorCoordonate.equals(observerCoordonate))
			{  
				iterator.remove();
				removeGhostNode(element.elementCoordonate);
				element.elementCoordonate.world().setBlock(element.elementCoordonate.x,element.elementCoordonate.y,element.elementCoordonate.z,0);//caca1.5.1				
			}			
		}	
	}
	public void removeGhostAndBlockWithObserver(Coordonate observerCoordonate,int uuid)
	{
		Iterator<Entry<Coordonate, GhostElement>> iterator = ghostTable.entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<Coordonate, GhostElement> entry = iterator.next();
			GhostElement element = entry.getValue();
			if(element.observatorCoordonate.equals(observerCoordonate) && element.getUUID() == uuid)
			{  
				iterator.remove();
				removeGhostNode(element.elementCoordonate);
				element.elementCoordonate.world().setBlock(element.elementCoordonate.x,element.elementCoordonate.y,element.elementCoordonate.z,0);//caca1.5.1				
			}			
		}	
	}
	public void removeGhostNode(Coordonate c)
	{
		NodeBase node = NodeManager.instance.getNodeFromCoordonate(c);
		if(node == null) return;
		node.onBreakBlock();
	}
	
	public void removeGhostAndBlock(Coordonate coordonate)
	{
		removeGhost(coordonate);
		coordonate.world().setBlock(coordonate.x,coordonate.y,coordonate.z,0);//caca1.5.1
	}
	
	
	
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		for(Object o : nbt.getTags())
		{
			NBTTagCompound tag = (NBTTagCompound) o;

			GhostElement ghost = new GhostElement();
			ghost.readFromNBT(tag, "");
			ghostTable.put(ghost.elementCoordonate, ghost);
		}		
	}
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		int nodeCounter = 0;
		
		for(GhostElement ghost : ghostTable.values())
		{
			NBTTagCompound nbtGhost = new NBTTagCompound();
			ghost.writeToNBT(nbtGhost,"");
			nbt.setCompoundTag("n" + nodeCounter++, nbtGhost);
		}		
	}
	
		
	public boolean canCreateGhostAt(World world,int x,int y, int z)
	{
		if(world.getChunkProvider().chunkExists(x>>4 , z>>4) == false) return false;
		if(world.getBlockId(x ,y ,z) != 0 && Block.blocksList[world.getBlockId(x ,y ,z)].isBlockReplaceable(world, x, y, z) == false) return false;
		return true;
	}
	public void createGhost(Coordonate coordonate,Coordonate observerCoordonate,int UUID)
	{
		coordonate.world().setBlock(coordonate.x ,coordonate.y ,coordonate.z,0);
		if(coordonate.world().setBlock(coordonate.x ,coordonate.y ,coordonate.z,Eln.ghostBlock.blockID))
		{
			coordonate = new Coordonate(coordonate);
			GhostElement element = new GhostElement(coordonate,observerCoordonate,UUID);
			ghostTable.put(element.elementCoordonate, element);
		}
	}
	
		
	
}
