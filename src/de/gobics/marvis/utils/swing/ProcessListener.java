package de.gobics.marvis.utils.swing;


public interface ProcessListener {
	public void processStatus(ProcessStatus s);
	public void processDone(Object result);
	public void processError(Exception e);
}
