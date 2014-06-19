package object;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class FormInnerListProxy implements Parcelable
{
	private String idDataBase;
	private String formId;
	private String pathForm;
	private String formName;
	private String formNameInstance;
	private String strPathInstance;
	private String formNameAutoGen;
	private String formEnumeratorId;
	private String dataInvio;
	private String lastSavedDateOn;
	private String dataDownload;
	private String dataDiCompletamento;
	
	public FormInnerListProxy()
	{
		idDataBase = null;
		formId = null;
		pathForm = null;
		formName = null;
		formNameInstance = null;
		strPathInstance = null;
		formNameAutoGen = null;
		
		formEnumeratorId = null;
		dataInvio = null;	
		dataDiCompletamento = null;
		lastSavedDateOn = null;
		dataDownload = null;
	}
	
	private FormInnerListProxy(Parcel in)
	{
		
		idDataBase = in.readString();//LL aggiunto
		
		formId = in.readString();
		pathForm = in.readString();
		formName = in.readString();
		formNameInstance = in.readString();
		strPathInstance = in.readString();
		formNameAutoGen = in.readString();
		
		formEnumeratorId = in.readString();
		dataInvio = in.readString();
		dataDiCompletamento = in.readString();
		lastSavedDateOn = in.readString();
		dataDownload = in.readString();
	}
	public String getIdDataBase()
	{
		return idDataBase;
	}
	public void setIdDataBase(String idDataBase)
	{
		this.idDataBase = idDataBase;
	}
	public String getFormEnumeratorId()
	{
		return formEnumeratorId;
	}
	public void setFormEnumeratorId(String formEnumeratorId)
	{
		this.formEnumeratorId = formEnumeratorId;
	}
	public String getFormId() {
		return formId;
	}
	public void setFormId(String formId) {
		this.formId = formId;
	}
	public String getPathForm() {
		return pathForm;
	}
	public void setPathForm(String pathForm) {
		this.pathForm = pathForm;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getStrPathInstance() {
		return strPathInstance;
	}
	public void setStrPathInstance(String strPathInstance) {
		this.strPathInstance = strPathInstance;
	}
	public String getFormNameAutoGen() {
		return formNameAutoGen;
	}
	public void setFormNameAutoGen(String formNameAutoGen) {
		this.formNameAutoGen = formNameAutoGen;
	}

	public String getFormNameInstance() {
		return formNameInstance;
	}
	public void setFormNameInstance(String formNameInstance) {
		this.formNameInstance = formNameInstance;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		//nell'oggetto Parcel ci andiamo a mettere tutti i valori degli attributi dell'oggetto FormInnerListProxy
		dest.writeString(idDataBase);
		dest.writeString(formId);
	    dest.writeString(pathForm);
	    dest.writeString(formName);	
	    dest.writeString(formNameInstance);	
	    dest.writeString(strPathInstance);	
	    dest.writeString(formNameAutoGen);	
	    dest.writeString(formEnumeratorId);
	    dest.writeString(dataInvio);
	    dest.writeString(lastSavedDateOn);
	    dest.writeString(dataDiCompletamento);
	}
	
	public String getDataDiCompletamento()
	{
		return dataDiCompletamento;
	}
	public void setDataDiCompletamento(String dataDiCompletamento)
	{
		this.dataDiCompletamento = dataDiCompletamento;
	}
	
	public String getDataInvio() {
		return dataInvio;
	}

	public void setDataInvio(String dataInvio) {
		this.dataInvio = dataInvio;
	}
	
	public String getDataDownload() {
		return dataDownload;
	}

	public void setDataDownload(String dataDownload) {
		this.dataDownload = dataDownload;
	}

	public String getLastSavedDateOn() {
		return lastSavedDateOn;
	}

	public void setLastSavedDateOn(String lastSavedDateOn) {
		this.lastSavedDateOn = lastSavedDateOn;
	}
	
}
