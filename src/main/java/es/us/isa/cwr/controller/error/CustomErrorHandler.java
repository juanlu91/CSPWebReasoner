package es.us.isa.cwr.controller.error;

import ilog.opl.IloCustomOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplLocation;
import ilog.opl.IloOplMessage;

import java.util.ArrayList;
import java.util.List;

import es.us.isa.cwr.controller.error.AppAnnotations.Type;

public class CustomErrorHandler extends IloCustomOplErrorHandler {
    
	List<AppAnnotations> annotations = new ArrayList<AppAnnotations>();
	
	public CustomErrorHandler(IloOplFactory oplF) {
    	super(oplF);
    	annotations = new ArrayList<AppAnnotations>();
    }
	
	public List<AppAnnotations> getAnnotations(){
		return annotations;
	}
	
    @Override
	public boolean customHandleError(IloOplMessage message, IloOplLocation location) {
    	AppAnnotations anno = new AppAnnotations();
    	anno.setText(message.getLocalized());
    	anno.setColumn(String.valueOf(location.getColumn()));
    	anno.setRow(String.valueOf(location.getLine() - 1));
    	anno.setType(Type.ERROR);
    	annotations.add(anno);
		return true;
	}
	@Override
	public boolean customHandleFatal(IloOplMessage message, IloOplLocation location) {
		AppAnnotations anno = new AppAnnotations();
    	anno.setText(message.toString());
    	anno.setColumn(String.valueOf(location.getColumn()));
    	anno.setRow(String.valueOf(location.getLine()));
    	anno.setType(Type.FATAL);
    	annotations.add(anno);
		return true;
	}
	@Override
	public boolean customHandleWarning(IloOplMessage message, IloOplLocation location) {
		AppAnnotations anno = new AppAnnotations();
    	anno.setText(message.toString());
    	anno.setColumn(String.valueOf(location.getColumn()));
    	anno.setRow(String.valueOf(location.getLine()));
    	anno.setType(Type.WARNING);
    	annotations.add(anno);
		return true;
	}
}