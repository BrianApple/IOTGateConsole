package IOTGateConsole.databridge;

public class Strategy {
	
	public Strategy() {
		super();
	}
	public Strategy(String name, String val) {
		super();
		this.name = name;
		this.val = val;
	}
	
	
	private String name;
	private String val;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}

	
}
