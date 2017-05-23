/**
 * Symbols for the symbol table
 * 
 * @author rsmith
 */

public class Symbol {
	
	public Symbol(){
		this.name = "";
		this.type = TokenType.DEFAULT;
		this.intValue = -999999;
		this.boolValue = false;
		this.isScopeMarker = false;
		this.isGlobal = false;
		this.stackNumber = -1;
		this.placeInMemory = 0;
	}
	
	public int getStackNo(){
		return this.stackNumber;
	}
	
	public void setStackNo(int num){
		this.stackNumber = num;
	}
	
	public boolean getGlobal(){
		return this.isGlobal;
	}
	
	public void setGlobal(boolean global){
		this.isGlobal = global;
	}
	
	public boolean getScope(){
		return this.isScopeMarker;
	}
	
	public void setScope(boolean scope){
		this.isScopeMarker = scope;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String newName){
		this.name = newName;
	}
	
	public TokenType getType(){
		return this.type;
	}
	
	public void setType(TokenType newType){
		this.type = newType;
	}
	
	public int getIntValue(){
		return this.intValue;
	}
	
	public void setIntValue(int newIntValue){
		this.intValue = newIntValue;
	}
	
	public boolean getBoolValue(){
		return this.boolValue;
	}
	
	public void setBoolValue(boolean newBoolValue){
		this.boolValue = newBoolValue;
	}
	
	private String name;
	private TokenType type;
	private int intValue;
	private boolean boolValue;
	private boolean isScopeMarker;
	private boolean isGlobal;
	private int stackNumber;
	public int placeInMemory;
	public boolean isProc;
}
