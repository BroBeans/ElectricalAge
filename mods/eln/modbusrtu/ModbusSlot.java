package mods.eln.modbusrtu;

public abstract class ModbusSlot implements IModbusSlot{
	public ModbusSlot(int offset,int range) {
		this.offset = offset;
		this.range = range;
	}
	
	int range,offset;
	
	
	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return range;
	}
	
	
	@Override
	public int getOffset() {
		// TODO Auto-generated method stub
		return offset;
	}
	
	
	@Override
	public void writeCoil(int id, boolean value) {
		setCoil(id, value);
	}

	@Override
	public void writeHoldingRegister(int id, short value) {
		setHoldingRegister(id, value);
	}
}
