package de.gobics.marvis.utils.swing;

public class ProcessStatus {
	public String description = "Calculating";
	public int current = 0;
	public int max = 0;
	public String warning_message = null;
	private boolean percent_changed = false;
	private long start = System.nanoTime();
	
	public ProcessStatus(){
		
	}
	public ProcessStatus(String description){
		this.description = description;
	}

	public ProcessStatus(int c, int m){
		this.current = c;
		this.max = m;
	}

	public ProcessStatus(String description, int c, int m){
		this.description = description;
		this.current = c;
		this.max = m;
	}
	public ProcessStatus(String description, int c, int m, String warning){
		this.description = description;
		this.current = c;
		this.max = m;
		this.warning_message = warning;
	}

	public void setWarningMessage(String msg){
		this.warning_message = msg;
	}

	public boolean hasWarningMessage(){
		return warning_message != null;
	}

	public String getWarningMessage(){
		return warning_message;
	}

	public boolean hasDescription(){
		return description != null;
	}
	
	public boolean percentChanged(){
		return percent_changed;
	}
	
	public int percent(){
		if( max == 0 )
			return 0;
		int percent = (this.current*100)/this.max;
		if( percent > 100 )
			percent = 100;
		return percent;
	}
	
	public void inc(){
		int percent_old = percent();
		current++;
		if( percent_old != percent() )
			percent_changed = true;
		else 
			percent_changed = false;
	}
	public void set(int v){
		int percent_old = percent();
		current = v;
		if( percent_old != percent() )
			percent_changed = true;
		else 
			percent_changed = false;
	}
	
	public void resetStart(){
		this.start = System.nanoTime();
	}
	
	public String eta(){
		long diff = System.nanoTime() - this.start;
		long timePerEntry = diff / this.current;
		long timeNeeded = timePerEntry * (this.max - this.current);
		Double seconds = new Long(timeNeeded / 1000000000).doubleValue();
		
		if( seconds < 60 )
			return seconds.intValue()+" seconds";
		
		if( seconds < 3600 )
			return new Double(Math.ceil(seconds/60)).intValue() + " minutes";
		
		return new Double(Math.ceil(seconds/3600)).intValue() + " hours";
	}
	
}
