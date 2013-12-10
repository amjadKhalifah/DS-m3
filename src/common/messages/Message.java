package common.messages;

import java.io.Serializable;

public class Message implements KVMessage, Serializable {

	private String key;
	private String value;
	private StatusType type;
	
	public void setKey(String key){
		this.key = key;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public void setStatus(StatusType type){
		this.type = type;
	}
	
	@Override
	public String getKey() {				
		return this.key;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public StatusType getStatus() {
		return this.type;
	}

}
