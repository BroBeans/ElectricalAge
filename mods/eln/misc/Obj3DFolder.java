package mods.eln.misc;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


import mods.eln.Eln;
import mods.eln.misc.Obj3D.Obj3DPart;


public class Obj3DFolder {
	HashMap<String, Obj3D> nameToObjHash = new HashMap<String, Obj3D>();
	
	
	public void loadFolder(String modName,String folderName)
	{
		
		/*
		System.out.println("public void loadFolder(String folderName) at ");
		System.out.println( " .  ->" + mods.eln.Eln.class.getResource("."));
		System.out.println( " /.  ->" + mods.eln.Eln.class.getResource("/."));
		System.out.print("     " + folderName);*/
			URI url;
			try {
				URL rec = mods.eln.Eln.class.getResource("/assets/" + modName +  folderName);
				
				if(rec == null)
				{
					System.out.println("if(rec == null)");
				}
				else
				{
					url = rec.toURI();
					if (url == null) {
						System.out.println("if(url == null)");

					} else {
					    File dir = new File(url);
						//File dir = new File(mods.eln.Eln.class.getResource(folderName).getFile());
					    File[] lol = dir.listFiles();
					    for (File file : dir.listFiles()) {
						    if (file.isFile()) {
						    	if(file.getName().endsWith(".obj")||file.getName().endsWith(".OBJ"))
						    	{
							    //	String fileName = folder + file.getName();
							    	Obj3D obj =  new Obj3D();
							    	obj.loadFile(modName,folderName + "/" + file.getName());
							    	//System.out.println( '"' + folderName + "/" + file.getName() + '"' + ',');
							    	String tag = file.getName().replaceAll(".obj", "").replaceAll(".OBJ", "");
							    	nameToObjHash.put(tag,obj);
						    	}
						    }
						    if(file.isDirectory())
						    {
						    	//String bi = file.getName();
						    	loadFolder(modName,folderName + "/" + file.getName());
						    }
						}	
					}
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	
	public void loadObj(String modName,String path){
		Obj3D obj =  new Obj3D();
    	if(obj.loadFile(modName,path)){
    		String tag =path.replaceAll(".obj", "").replaceAll(".OBJ", "");
    		tag = tag.substring(tag.lastIndexOf('/')+1, tag.length());
    		nameToObjHash.put(tag,obj);
    		System.out.println(path + " loaded");
    	}
	}
	
	public Obj3D getObj(String obj3DName)
	{
		return nameToObjHash.get(obj3DName);
	}
	
	public Obj3DPart getPart(String objName,String partName)
	{
		Obj3D obj = getObj(objName);
		if(obj == null) return null;
		return obj.getPart(partName);
	}
	public void draw(String objName,String partName)
	{
		Obj3DPart part = getPart(objName,partName);
		if(part != null) part.draw();
	}
}
