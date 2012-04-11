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
public class Attendance
{
    private static RecordStore rs = null;
    
    public synchronized static boolean fileExists()
    {
        try
        {
            rs = RecordStore.openRecordStore("attendance",false);
            rs.closeRecordStore();
        }
        catch(RecordStoreNotFoundException e)
        {
            //System.out.println("Error in fileExists() : " + e.getMessage());
            return false;
        }
        catch(RecordStoreException e)
        {            
            System.out.println("Error in fileExists() : " + e.getMessage());
        }
        
        return true;
    }
    
    public synchronized static boolean open()
    {
        try
        {
            rs = RecordStore.openRecordStore("attendance",false);
            normalizeFile();
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in Att.open() : " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public static int getDateDifferenceInDays(Date d1, Date d2)
    {
        return  Integer.parseInt(String.valueOf((((d1.getTime() - d2.getTime() ) / 60000 ) / (60*24))-0));
    }
    
    
    public synchronized static void normalizeFile()
    {
        try
        {
            int len = rs.getNumRecords();
            Date today = new Date();
            Settings.open();
            Date collStart = Settings.getCollStartDate();            
            Settings.close();
            Calendar cToday = Calendar.getInstance();
            Calendar cColl = Calendar.getInstance();
            cToday.setTime(today);
            cColl.setTime(collStart);
            cToday.set(Calendar.HOUR_OF_DAY, cColl.get(Calendar.HOUR_OF_DAY));
            cToday.set(Calendar.MINUTE, cColl.get(Calendar.MINUTE));
            cToday.set(Calendar.SECOND, cColl.get(Calendar.SECOND));
            cToday.set(Calendar.MILLISECOND, cColl.get(Calendar.MILLISECOND));
            today = cToday.getTime();            
            int offset = getDateDifferenceInDays(today, collStart)+1;
            if(offset > len)
            {
                for(int i=0;i<(offset-len);i++)
                {
                    String s = "0;0;0";
                    byte[] b = s.getBytes();
                    rs.addRecord(b, 0, b.length);
                }
            }
            if(offset < len)
            {
                System.err.println("Extreme logic-bug at normalizeFile()");
            }
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in normalizeFile() : " + e.getMessage());
        }
        
        
    }
    
    public synchronized static boolean close()
    {
        try
        {
            rs.closeRecordStore();
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in Att.close() : " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public synchronized static void deleteFile()
    {
        try
        {
            RecordStore.deleteRecordStore("attendance");
        }
        catch(RecordStoreException e)
        {       
            System.out.println("Error in Att.deleteFile() : " + e.getMessage());
        }
    }
    
    public synchronized static boolean createFile()
    {
        try
        {
            rs = RecordStore.openRecordStore("attendance",true);  
            normalizeFile();
            rs.closeRecordStore();
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in Att.createFile() : " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public synchronized static void setAttData(Date d,int[] lect, int[] pract, int[] extra)
    {
        Calendar cd = Calendar.getInstance();
        Calendar cStart = Calendar.getInstance();
        cd.setTime(d);
        Settings.open();
        cStart.setTime(Settings.getCollStartDate());
        Settings.close();
        cd.set(Calendar.HOUR_OF_DAY, cStart.get(Calendar.HOUR_OF_DAY));
        cd.set(Calendar.MINUTE, cStart.get(Calendar.MINUTE));
        cd.set(Calendar.SECOND, cStart.get(Calendar.SECOND));
        cd.set(Calendar.MILLISECOND, cStart.get(Calendar.MILLISECOND));
        Date dloc = cd.getTime();
        int recId = getDateDifferenceInDays(dloc, cStart.getTime())+1;
        int lByte=0;
        int pByte=0;
        int eByte=0;
        int bitOpTaken = 1;
        int bitOpAtt = 256;
        for(int i=0;i<lect.length;i++)
        {
            switch(lect[i])
            {
                case 1:
                    lByte |= bitOpTaken;
                    lByte |= bitOpAtt;
                    break;
                
                case 2:
                    lByte |= bitOpTaken;
            }
            
            switch(pract[i])
            {
                case 1:
                    pByte |= bitOpTaken;
                    pByte |= bitOpAtt;
                    break;
                
                case 2:
                    pByte |= bitOpTaken;
            }
            
            switch(extra[i])
            {
                case 1:
                    eByte |= bitOpTaken;
                    eByte |= bitOpAtt;
                    break;
                
                case 2:
                    eByte |= bitOpTaken;
            }
            bitOpTaken *= 2;
            bitOpAtt *= 2;            
        }
        String recFormat = String.valueOf(lByte) + ";" + String.valueOf(pByte) + ";" + String.valueOf(eByte);
        byte[] bRec = recFormat.getBytes();
        try
        {
            rs.setRecord(recId, bRec, 0, bRec.length);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Couldn't store record in setAttData() : "+e.getMessage());
        }
    }
    
    public synchronized static String getAttData(Date d)
    {
        Calendar cd = Calendar.getInstance();
        Calendar cStart = Calendar.getInstance();
        cd.setTime(d);
        Settings.open();
        cStart.setTime(Settings.getCollStartDate());
        Settings.close();
        cd.set(Calendar.HOUR_OF_DAY, cStart.get(Calendar.HOUR_OF_DAY));
        cd.set(Calendar.MINUTE, cStart.get(Calendar.MINUTE));
        cd.set(Calendar.SECOND, cStart.get(Calendar.SECOND));
        cd.set(Calendar.MILLISECOND, cStart.get(Calendar.MILLISECOND));
        Date dloc = cd.getTime();
        int recId = getDateDifferenceInDays(dloc, cStart.getTime())+1;
        byte[] b=null;
        try
        {
            b = rs.getRecord(recId);                                    
        }
        catch(RecordStoreException e)
        {
            System.out.println("Couldnt read string from getAttData() : "+e.getMessage());
        }
        String res = new String(b);
        return res;
    }
    
    public synchronized static int[] getLectFromAttByte(String s)
    {
        int end = s.indexOf(';');
        int lByte = Integer.parseInt(s.substring(0,end));
        Settings.open();
        int[] lect = new int[Settings.getNumberOfSubjects()];
        Settings.close();
        int bitOpTaken = 1;
        int bitOpAtt = 256;
        for(int i=0;i<lect.length;i++)
        {
            lect[i]=0;
            if((lByte & bitOpTaken)!=0)
            {
                if((lByte & bitOpAtt)!=0)
                    lect[i]=1;
                else
                    lect[i]=2;
            }
            bitOpTaken *= 2;
            bitOpAtt *= 2;
        }
        return lect;
    }
    
    public synchronized static int[] getPractFromAttByte(String s)
    {
        int start = s.indexOf(';')+1;
        int end = s.lastIndexOf(';');
        //int end = s.indexOf(';',start);
        int pByte = Integer.parseInt(s.substring(start,end));
        Settings.open();
        int[] pract = new int[Settings.getNumberOfSubjects()];
        Settings.close();
        int bitOpTaken = 1;
        int bitOpAtt = 256;
        for(int i=0;i<pract.length;i++)
        {
            pract[i]=0;
            if((pByte & bitOpTaken)!=0)
            {
                if((pByte & bitOpAtt)!=0)
                    pract[i]=1;
                else
                    pract[i]=2;
            }
            bitOpTaken *= 2;
            bitOpAtt *= 2;
        }
        return pract;
    }
    
    public synchronized static int[] getExtraFromAttByte(String s)
    {
        int start = s.indexOf(';')+1;
        int end = s.indexOf(';',start);
        start = end+1;
        int eByte = Integer.parseInt(s.substring(start));
        Settings.open();
        int[] extra = new int[Settings.getNumberOfSubjects()];
        Settings.close();
        int bitOpTaken = 1;
        int bitOpAtt = 256;
        for(int i=0;i<extra.length;i++)
        {
            extra[i]=0;
            if((eByte & bitOpTaken)!=0)
            {
                if((eByte & bitOpAtt)!=0)
                    extra[i]=1;
                else
                    extra[i]=2;
            }
            bitOpTaken *= 2;
            bitOpAtt *= 2;
        }
        return extra;
    }
    
    public static int[][] getStats(Date from, Date to)
    {
        Settings.open();
        int[][] res = new int[Settings.getNumberOfSubjects()][4]; //0-Lects Attended 1-Lec Taken 2-Prac Att 3-Prac Taken
        Calendar cd = Calendar.getInstance();
        Calendar cStart = Calendar.getInstance();
        cd.setTime(from);
        int numSubs = Settings.getNumberOfSubjects();
        cStart.setTime(Settings.getCollStartDate());
        Settings.close();
        cd.set(Calendar.HOUR_OF_DAY, cStart.get(Calendar.HOUR_OF_DAY));
        cd.set(Calendar.MINUTE, cStart.get(Calendar.MINUTE));
        cd.set(Calendar.SECOND, cStart.get(Calendar.SECOND));
        cd.set(Calendar.MILLISECOND, cStart.get(Calendar.MILLISECOND));
        Date dloc = cd.getTime();
        int recFrom = getDateDifferenceInDays(dloc, cStart.getTime())+1;
        cd.setTime(to);
        cd.set(Calendar.HOUR_OF_DAY, cStart.get(Calendar.HOUR_OF_DAY));
        cd.set(Calendar.MINUTE, cStart.get(Calendar.MINUTE));
        cd.set(Calendar.SECOND, cStart.get(Calendar.SECOND));
        cd.set(Calendar.MILLISECOND, cStart.get(Calendar.MILLISECOND));
        dloc = cd.getTime();
        int recTo = getDateDifferenceInDays(dloc, cStart.getTime())+1;
        String atLine;
        int[] lect;
        int[] pract;
        int[] extra;
        byte[] b = null;
        for(int x=0;x<numSubs;x++)
        {
            for(int y=0;y<4;y++)
            {
                res[x][y]=0;
            }
        }
        for(int i=recFrom;i<=recTo;i++)
        {
            try
            {
                b = rs.getRecord(i);                                    
            }
            catch(RecordStoreException e)
            {
                System.out.println("Couldnt read string from getStats() : "+e.getMessage());
            }
            atLine = new String(b);
            lect = getLectFromAttByte(atLine);
            pract = getPractFromAttByte(atLine);
            extra = getExtraFromAttByte(atLine);
            
            
            for(int j=0;j<lect.length;j++)
            {
                //attended = 1, bunked = 2, not taken = 0
                if(lect[j] == 1)
                {
                    res[j][0]++; //lectures attended
                    res[j][1]++; //lectures taken
                }
                else if(lect[j] == 2)
                {
                    res[j][1]++;
                }
                
                if(extra[j] == 1)
                {
                    res[j][0]++; //lectures attended
                    res[j][1]++; //lectures taken
                }
                else if(extra[j] == 2)
                {
                    res[j][1]++;
                }
                
                if(pract[j] == 1)
                {
                    res[j][2]++; //practs attended
                    res[j][3]++; //practs taken
                }
                else if(pract[j] == 2)
                {
                    res[j][3]++;
                }
            }
        }
        return res;
    }
    
    public static byte[] getRecord(int rec)
    {
        try
        {
            return rs.getRecord(rec);            
        }
        catch(RecordStoreException e)
        {
                System.out.println("Couldnt read string from getRecord() : "+e.getMessage());
                String x="0;0;0";
                return x.getBytes();
        }
    }
    
    public static int getNumberOfRecords()
    {
        try
        {
            return rs.getNumRecords();
        }
        catch(RecordStoreException e){return 0;}
    }

    public synchronized static String consolidateData()
    {
        String ret="";
        String tmp="";
        int num=0;
        try
        {
            num = rs.getNumRecords();
            for(int i=1;i<=num;i++)
            {
                tmp = new String(rs.getRecord(i));
                ret = ret + tmp + "#";
                tmp="";
            }
        }
        catch(RecordStoreNotOpenException e){}
        catch(RecordStoreException e)
        {
            System.out.println("Couldnt read string from consolidateData() : "+e.getMessage());
        }

        return ret;
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

    public synchronized static int importConsolidatedData(String cData)
    {
        int ret=0;
        Attendance.close();
        Attendance.deleteFile();
        try
        {
            rs = RecordStore.openRecordStore("attendance",true);
            rs.closeRecordStore();
            rs = RecordStore.openRecordStore("attendance",false);
        }
        catch(RecordStoreException e)
        {
            System.out.println("Error in importConsolidatedData.1() : " + e.getMessage());
            //return false;
        }

        String[] arrData = Attendance.splitConsolidatedData(cData);
        try
        {
            for(int i=0;i<arrData.length-1;i++)
            {
                //rs.setRecord(i+1, arrData[i].getBytes(), 0, arrData[i].getBytes().length);
                rs.addRecord(arrData[i].getBytes(), 0, arrData[i].getBytes().length);
            }
            ret=1;
            Attendance.normalizeFile();
        }
        catch(RecordStoreException e)
        {
            System.out.println("error in importConsolidatedData.2() : " + e.getMessage());
        }
        //int end = cData.indexOf('#');
        //int lByte = Integer.parseInt(cData.substring(0,end));
        return ret;
    }
    
    
}