package IOTGateConsole.databridge;

import java.io.Serializable;
import java.util.List;

public class RetData implements Serializable{
	private static final long serialVersionUID = 1L;
	private int retSig;
	private List<Object> data;
	public int getRetSig() {
		return retSig;
	}
	public void setRetSig(int retSig) {
		this.retSig = retSig;
	}
	public List<Object> getData() {
		return data;
	}
	public void setData(List<Object> data) {
		this.data = data;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
}
