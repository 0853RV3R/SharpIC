package nmargie.scaleapptest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Entry implements Serializable{
	
	private int venue;
	private int room;
	private Date dateTime;
	private String strTime;
	private String strBarcode;
	private String strWeightG;
	private String strWeightOZ;
	private double weightG;
	private double weightOZ;
	private int auditorID; // to identify auditor who scanned the entry
	//private String strEntryID; // UNIQUE to each scanned entry
	boolean uploaded;
	long unixTime;
    String UnixTime;
	
	//private static AtomicInteger nextID= new AtomicInteger(0);
	
	/*
	private static String getNextId() {
		return Integer.toString(nextID.incrementAndGet());
	}
	
	private static String getNextId(int audID) {
		String strAuditor = Integer.toString(audID);
		String strNext = Integer.toString(nextID.incrementAndGet());
		String strFull = strAuditor + " - " + strNext;
		return strFull;
	}
	*/
	public Entry(){
		super();
	}
	
	// constructor --> takes weight reading (grams), auditor ID, venue, room, Barcode, Date object for scan time
	// pass this to the constructor for the current time:
			//Date now = new Date();
			
	public Entry(int auditorID, int venue, int room, String strWeightG, String strBarcode, Date dateTime, long unixTime){
		super(); 
		//not uploaded by default
		this.uploaded =false;
		// arguments:
		this.auditorID=auditorID;
		this.venue=venue;
		this.room=room;
		this.strBarcode=strBarcode;
		this.dateTime=dateTime;
		this.strWeightG=strWeightG;
		// converted attributes:
		this.weightG= string2double(strWeightG);
		this.weightOZ = grams2oz(weightG);
		this.strWeightOZ= double2string(weightOZ);
		
		this.strTime = date2string(dateTime);
		this.unixTime = unixTime;
		//this.UnixTime = Long.toString(unixTime);
		
		// STEVE'S CODE WILL HANDLE THIS
		//assign unique entryID based on auditorID
		//this.strEntryID = getNextId(this.auditorID);
	}
	
	@Override
	public String toString(){
		String someThings = "" + this.strBarcode + "; " + "Weight (g): " + this.strWeightG + "; " + 
				"Weight (oz): " + this.strWeightOZ + "; " +"Date: " + this.strTime;
		return someThings;
	}
	
	public String toStringComplete(){
		String everything = "Auditor ID: " + Integer.toString(this.auditorID) + "\n" + "Venue: " + this.venue + "\n"
				+ "Room: " + this.room + "\n" + "Barcode: " + this.strBarcode + "\n" + "Weight (g): " + this.strWeightG + "\n" + 
				"Weight (oz): " + this.strWeightOZ + "\n" +"Date: " + this.strTime + "\n";
		return everything;
	}
	
	
	public boolean isUploaded(){
		return this.uploaded;
	}
	
	// SET METHODS
	
	public void setUploaded(){
		this.uploaded = true;
	}
	
	
	// GET METHODS
	
	public String getBarcode(){
		return this.strBarcode;
	}
	
	public String getStringWeightG(){
		return this.strWeightG;
	}
	public String getStringWeightOZ(){
		return this.strWeightOZ;
	}
	public String getScanTime(){
		return this.strTime;
	}
	public String getAuditorID(){
		return String.valueOf(this.auditorID);
	}
	public String getVenue(){
		return String.valueOf(this.venue);
	}
	public String getRoom(){
		return String.valueOf(this.room);
	}
	public String getUnixTime(){
		return Long.toString(unixTime);
	}
	
	//  CONVERSION METHODS
	
	public static double oz2grams(double oz){
		double grams = oz * 28.3495;
		grams = round(grams, 4);
		return grams;
	}
	private double grams2oz(double grams){
		double oz = grams * 0.035274;
		oz = round(oz, 4);
		return oz;
	}
	public static double string2double(String strNum){
		double d = Double.parseDouble(strNum);
		return d;
	}
	
	public static String double2string(double doubleNum){
		String num = String.valueOf(doubleNum);
		return num;
	}
	
	// this method accepts a date object and converts into a String
	private static String date2string(Date now) {
		
		//sets the string format as: year - month - day - hour - minute - second - millisecond
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss.SSS");
		
		
		//Allocates a Date object and initializes it so that it represents the time at which it was allocated
		//, measured to the nearest millisecond.
	    String strDate = sdfDate.format(now);
	    return strDate;
	}
	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
	    return bd.doubleValue();
	}
	
	
}
