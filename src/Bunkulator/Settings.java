/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Bunkulator;
import javax.microedition.rms.*;
//import javax.microedition.lcdui.*;

//import java.util.Enumeration;
import java.util.Vector;
//import java.io.*;
import java.util.Date;
import java.util.Calendar;
/**
 *
 * @author Owner
 */
public class Settings
{
    private static RecordStore rs = null;
    
    public synchronized static boolean fileExists()
    {
        try
        {
            rs = RecordStore.openRecordStore("settings",false);
            rs.closeRecordStore();
        }
        catch(RecordStoreNotFoundException e)
        {
            //System.out.println("Error in fileExists() : " + e.getMessage());
            return false;
        }
        catch(RecordStoreException e)
        {            
            System.out.println("Error in Sett.fileExists() : " + e.getMessage());
        }
        
        return true;
    }
    
    public synchronized static boolean open()
    {
        try
        {
            rs = RecordStore.openRecordStore("settings",false);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in Sett.open() : " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public synchronized static boolean close()
    {
        try
        {
            rs.closeRecordStore();
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in Sett.close() : " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public synchronized static void deleteFile()
    {
        try
        {
            RecordStore.deleteRecordStore("settings");
        }
        catch(RecordStoreException e)
        {       
            System.out.println("Error in Sett.deleteFile() : " + e.getMessage());
        }
    }
    
    public synchronized static boolean createFile()
    {
        try
        {
            rs = RecordStore.openRecordStore("settings",true);  
            addBlankSettings();
            rs.closeRecordStore();
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in Sett.createFile() : " + e.getMessage());
            return false;
        }
        return true;
    }
    
    private synchronized static void addBlankSettings()
    {
        //Assumption: File Exists because fn is called from createFile()
        String tmp = "";
        byte[] bytes = tmp.getBytes();
        try
        {
            rs.addRecord(bytes, 0, bytes.length); //name
            tmp = "0"; //chosen default
            bytes = tmp.getBytes();
            rs.addRecord(bytes, 0, bytes.length); //minAttPer
            tmp="";
            bytes = tmp.getBytes();
            rs.addRecord(bytes, 0, bytes.length); //collStartDate
            tmp=";0"; //subject format: subjectName;HasPractsBoolean
            bytes = tmp.getBytes();
            for(int i=0;i<8;i++)
            {
                rs.addRecord(bytes, 0, bytes.length);//8 subjects    
            }
            tmp=""; //username=position 12, password=position 13
            bytes = tmp.getBytes();
            rs.addRecord(bytes, 0, bytes.length); //blank username stored
            rs.addRecord(bytes, 0, bytes.length); //blank password stored
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in addBlankSettings() : " + e.getMessage());
        }        
    }
    
    public synchronized static String getName()
    {
        try
        {
            byte[] bName = rs.getRecord(1);
            String name = new String(bName);
            return name;
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in getName() : " + e.getMessage());
            return null;
        }        
        catch(NullPointerException e)
        {
            return "";
        }
    }
    
    public synchronized static void setName(String name)
    {
        try
        {
            byte[] bName = name.getBytes();
            rs.setRecord(1, bName, 0, bName.length);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in setName() : " + e.getMessage());            
        }        
    }
    
    public synchronized static int getMinAttPer()
    {
        try
        {
            byte[] bName = rs.getRecord(2);
            String name = new String(bName);
            return Integer.valueOf(name).intValue();
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in getMinAttPer() : " + e.getMessage());
            return -1;
        }        
    }
    
    public synchronized static void setMinAttPer(int val)
    {
        try
        {
            byte[] bMinAttPer = String.valueOf(val).getBytes();
            rs.setRecord(2, bMinAttPer, 0, bMinAttPer.length);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in setMinAttPer() : " + e.getMessage());            
        }     
    }   
    
    public synchronized static void setCollStartDate(Date dt)
    {
        String sDt = String.valueOf(getDayFromDate(dt)) + "/"
                + String.valueOf(getMonthFromDate(dt)) + "/"
                + String.valueOf(getYearFromDate(dt));
        
        byte[] bsDt = sDt.getBytes();
        try
        {
            rs.setRecord(3, bsDt, 0, bsDt.length);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in setCollStartDate() : " + e.getMessage());
        }      
    }
    
    public synchronized static Date getCollStartDate()
    {
        try
        {
            byte[] bsDt = rs.getRecord(3);
            String sDt = new String(bsDt);
            int day=0,month=0,year=0;
            int start=0, end=0;
            //split code
            end = sDt.indexOf('/');
            day = Integer.parseInt(sDt.substring(start, end));
            start = end + 1;
            end = sDt.indexOf('/',start);
            month = Integer.parseInt(sDt.substring(start, end));
            start = end + 1;            
            year = Integer.parseInt(sDt.substring(start));
            //set Calendar to Date
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH,day);
            c.set(Calendar.MONTH,month);
            c.set(Calendar.YEAR,year);
            return c.getTime();            
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in getCollStartDate() : " + e.getMessage());
        }
        catch(NullPointerException e)
        {
            return null;
        }
        return null;
    }
    
    public synchronized static void setSubjectData(String[] subjectFormat)
    {
        //Takes string in Formatted Form i.e. Subjectname;PractBit
        for(int i=0;i<8;i++)
        {
            byte[] b = subjectFormat[i].getBytes();
            try
            {
                rs.setRecord(4+i, b, 0, b.length);
            }
            catch(RecordStoreException e)
            {
                System.out.println("Error in setSubjectData() : " + e.getMessage());
            }
        }
    }
    
    public synchronized static String[] getSubjectData()
    {
        //IMPORTANT: This returns the entire record string without separating
        //           the subject name and practical bit.
        String[] s = new String[8];
        for(int i=0;i<8;i++)
        {
            try
            {
                byte[] b = rs.getRecord(i+4);
                s[i] = new String(b);                
            }
            catch(RecordStoreException e)
            {
                System.out.println("Error in getSubjectData() : " + e.getMessage());
            }
        }
        return s;
    }
    
    public synchronized static int getNumberOfSubjects()
    {
        byte[] b;
        String s;
        int count=0;
        try
        {            
            for(int i=0;i<8;i++)
            {
                b = rs.getRecord(i+4);
                s = new String(b);
                if(s.trim().length() == 2) //i.e if subject = ";0" i.e. no-sub
                    break;
                else
                    count++;
            }            
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in getNumberOfSubjects() : " + e.getMessage());
        }
        return count;
    }

    public synchronized static String getUserName()
    {
        try
        {
            byte[] bName = rs.getRecord(12);
            String name = new String(bName);
            return name;
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in getUserName() : " + e.getMessage());
            return null;
        }
        catch(NullPointerException e)
        {
            return "";
        }
    }

    public synchronized static void setUserName(String name)
    {
        try
        {
            byte[] bName = name.getBytes();
            rs.setRecord(12, bName, 0, bName.length);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in setUserName() : " + e.getMessage());
        }
    }

    public synchronized static String getPassword()
    {
        try
        {
            byte[] bName = rs.getRecord(13);
            String name = new String(bName);
            return name;
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in getPassword() : " + e.getMessage());
            return null;
        }
        catch(NullPointerException e)
        {
            return "";
        }
    }

    public synchronized static void setPassword(String name)
    {
        try
        {
            byte[] bName = name.getBytes();
            rs.setRecord(13, bName, 0, bName.length);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in setPassword() : " + e.getMessage());
        }
    }

    public synchronized static String[] splitConsolidatedData(String original)
    {
        Vector nodes = new Vector();
        String separator = "#";
        //System.out.println("split start...................");
        // Parse nodes into vector
        int index = original.indexOf(separator);
        while(index>=0)
        {
            nodes.addElement( original.substring(0, index) );
            original = original.substring(index+separator.length());
            index = original.indexOf(separator);
        }
        // Get the last node
        nodes.addElement( original );

        // Create splitted string array
        String[] result = new String[ nodes.size() ];
        if( nodes.size()>0 )
        {
            for(int loop=0; loop<nodes.size(); loop++)
            {
                result[loop] = (String)nodes.elementAt(loop);
                //System.out.println(result[loop]);
            }

        }
        return result;
    }

    public synchronized static String consolidateSettingsData()
    {
        byte[] tmp=null;
        String stmp = "";
        String ret="";
        try
        {
            for(int i=1;i<=13;i++)
            {
                tmp = rs.getRecord(i);
                stmp= new String(tmp);
                ret = ret + stmp + "#";
                stmp="";
            }
            return ret;
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in getName() : " + e.getMessage());
            return null;
        }
        catch(NullPointerException e)
        {
            return "";
        }
    }

    public synchronized static int importConsolidatedSettingsData(String cData)
    {
        int ret=0;
        String[] arrData = Settings.splitConsolidatedData(cData);
        try
        {
            for(int i=0;i<arrData.length-1;i++)
            {
                rs.setRecord(i+1, arrData[i].getBytes(), 0, arrData[i].getBytes().length);
            }
            ret=1;
        }
        catch(RecordStoreException e)
        {
            System.out.println("error in importConsolidatedSettingsData() : " + e.getMessage());
        }
        //int end = cData.indexOf('#');
        //int lByte = Integer.parseInt(cData.substring(0,end));
        return ret;
    }
    
    /*
    * Create a string from the date portion of a time/date as yyyy-mm-dd
    * @param date the dat/time as milliseconds since the epoch
    */
    private static String dateToString (long date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(date));
        
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DATE);
        String t = (y<10? "0": "")+y+"-"+(m<10? "0": "")+m+"-"+(d<10 ? "0": "")+d;
        return t;
    }
    
    public static int getMonthFromDate(Date dt)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        return c.get(Calendar.MONTH);
    }
    
    public static int getYearFromDate(Date dt)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        return c.get(Calendar.YEAR);
    }
    
    public static int getDayFromDate(Date dt)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        return c.get(Calendar.DAY_OF_MONTH);
    }    
    
}