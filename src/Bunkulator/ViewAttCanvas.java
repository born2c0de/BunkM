/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Bunkulator;
import javax.microedition.lcdui.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Owner
 */
public class ViewAttCanvas extends Canvas
{
    private BunkMIDlet bmd;
    public int curDay;
    public int curMonth;
    public int curYear;
    public boolean dateValid;
    private int numSubs;
    private String[] subNames;
    private int[] practBits;
    private Image cross;
    private Image tick;
    private int curItem; //which date portion is selected
    private int collDay;
    private int collMonth;
    private int collYear;
    
    public ViewAttCanvas(BunkMIDlet bm)
    {
        bmd = bm;
        dateValid=true;
        Date d = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(d);           
        curDay=c.get(Calendar.DAY_OF_MONTH);
        curMonth=c.get(Calendar.MONDAY);
        curYear = c.get(Calendar.YEAR);
        Settings.open();        
        numSubs = Settings.getNumberOfSubjects();
        curItem = 0;
        subNames = new String[numSubs];
        practBits = new int[numSubs];
        String[] rawSubs = Settings.getSubjectData();
        Calendar cc = Calendar.getInstance();
        cc.setTime(Settings.getCollStartDate());
        collDay = cc.get(Calendar.DAY_OF_MONTH);
        collMonth = cc.get(Calendar.MONTH);
        collYear = cc.get(Calendar.YEAR);
        Settings.close();
        for(int i=0;i<numSubs;i++)
        {
            int end = rawSubs[i].indexOf(';');
            subNames[i] = rawSubs[i].substring(0,end);
            practBits[i] = Integer.parseInt(rawSubs[i].substring(end+1));   
        }
        

        try
        {
            cross = Image.createImage("/images/cross.PNG");
            tick = Image.createImage("/images/tick.PNG");            
        }
        catch(IOException e)
        {
            System.out.println("Cannot load images ViewAttCanvas() : " + e.getMessage());
        }
    }
    
    public void paint(Graphics g)
    {
        //CLS
        dateValid=true;
        g.setColor(255,255,255);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        //Set Font for Text
        Font f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        g.setFont(f);
        
        g.setColor(0,0,0);
        int H = getHeight() / (numSubs+4);
        int dateW = getWidth() / 4;
        int maxSubLength=0;
        for(int i=0;i<numSubs;i++)
        {
            if(subNames[i].length() > maxSubLength)
                maxSubLength = subNames[i].length();
        }
        //Offset at which Att Icons should be displayed on screen
        int imgOff = ((getWidth()/2) + 15*(getWidth()/50));
        
        //Day
        if(curItem == 0) g.setColor(255,0,0); else g.setColor(0,0,0);
        g.drawString(String.valueOf(curDay), dateW, 0, Graphics.HCENTER | Graphics.TOP);
        
        //Month
        if(curItem == 1) g.setColor(255,0,0); else g.setColor(0,0,0);
        g.drawString(getMonth(curMonth), 2 * dateW, 0, Graphics.HCENTER | Graphics.TOP);
        
        //Year
        if(curItem == 2) g.setColor(255,0,0); else g.setColor(0,0,0);
        g.drawString(String.valueOf(curYear), 3 * dateW, 0, Graphics.HCENTER | Graphics.TOP);
        
        g.setColor(0,0,255); //Set Colour to Blue for Titles
        g.drawString("SUBJECT", (getWidth()/3), 2 * H, Graphics.HCENTER | Graphics.TOP);
        g.drawString("L", imgOff + 0, 2 * H, Graphics.HCENTER | Graphics.TOP);
        g.drawString("P", imgOff + 20, 2 *H, Graphics.HCENTER | Graphics.TOP);
        g.drawString("X", imgOff + 40, 2 *H, Graphics.HCENTER | Graphics.TOP);
        // Colour back to black
        g.setColor(0,0,0);
        // Show Att Info based on date
        Calendar selDate = Calendar.getInstance();
        try
        {            
            selDate.setTime(new Date());
            selDate.set(Calendar.DAY_OF_MONTH, curDay);
            selDate.set(Calendar.MONTH, curMonth);
            selDate.set(Calendar.YEAR, curYear);
        }
        catch(Exception e)
        {
            g.setColor(255, 0, 0);
            g.drawString("[ CHOOSE A LEGAL DATE ]", (getWidth()/2), (getHeight()/2), Graphics.HCENTER | Graphics.TOP);
            dateValid=false;
            return;
        }        
        if(!Attendance.fileExists()) Attendance.createFile();
            Attendance.open();
        
        String attData="";
        try
        {
            attData = Attendance.getAttData(selDate.getTime());
        }
        catch(Exception e)
        {
            g.setColor(255, 100, 0);
            g.drawString("[ CHOOSE A PROPER DATE ]", (getWidth()/2), (getHeight()/2), Graphics.HCENTER | Graphics.TOP);
            dateValid=false;
            return;
        }
        int[] lectB = Attendance.getLectFromAttByte(attData);
        int[] practB = Attendance.getPractFromAttByte(attData);
        int[] extraB = Attendance.getExtraFromAttByte(attData);
        Attendance.close();
        for(int i=0;i<numSubs;i++)
        {
            //g.drawImage(cross, getWidth()/2, getHeight()-(cross.getHeight()/2), Graphics.HCENTER | Graphics.VCENTER);
            // Read Att Info and show Icons Accordingly
            g.drawString(subNames[i], (getWidth()/3), (i+4) * H, Graphics.HCENTER | Graphics.TOP);
            switch(lectB[i])
            {
                case 1: //attended
                    g.drawImage(tick, imgOff + 0 , (i+4)*H, Graphics.HCENTER | Graphics.TOP);                
                    break;
                    
                case 2: //bunked
                    g.drawImage(cross, imgOff + 0 , (i+4)*H, Graphics.HCENTER | Graphics.TOP);                
                    break;
                    
                default:                    
            }
            
            switch(practB[i])
            {
                case 1: //attended
                    g.drawImage(tick,  imgOff + 20, (i+4)*H, Graphics.HCENTER | Graphics.TOP);
                    break;
                    
                case 2: //bunked
                    g.drawImage(cross,  imgOff + 20, (i+4)*H, Graphics.HCENTER | Graphics.TOP);
                    break;
                    
                default:                    
            }
            
            switch(extraB[i])
            {
                case 1: //attended
                    g.drawImage(tick, imgOff + 40, (i+4)*H, Graphics.HCENTER | Graphics.TOP);
                    break;
                    
                case 2: //bunked
                    g.drawImage(cross, imgOff + 40, (i+4)*H, Graphics.HCENTER | Graphics.TOP);
                    break;
                    
                default:                    
            }  
        }
    }
    
    public void keyPressed(int keyCode)
    {
        int action = getGameAction(keyCode);
        switch(action)
        {
            case Canvas.LEFT:
                if(curItem==0) curItem=2; else curItem--;
                repaint();
                break;
                
            case Canvas.RIGHT:
                if(curItem == 2) curItem = 0; else curItem++;                
                repaint();
                break;
                
            case Canvas.UP:
                switch(curItem)
                {
                    case 0: //day
                        if(curDay < getDaysInMonth(curMonth,curYear))
                        {
                            curDay++;
                            Date dd = new Date();
                            Calendar cc = Calendar.getInstance();
                            cc.setTime(dd);
                            //reject if newday is > Today
                            if(curYear == cc.get(Calendar.YEAR) && curMonth == cc.get(Calendar.MONTH) && curDay > cc.get(Calendar.DAY_OF_MONTH) )
                            {
                                curDay--;
                            }
                        }                        
                        break;
                        
                    case 1: //month
                        Calendar today = Calendar.getInstance();
                        today.setTime(new Date());
                        if(curMonth==11) curMonth = 0; else curMonth++;                        
                        if(curDay > getDaysInMonth(curMonth,curYear)) curDay=1;
                        if(curYear == collYear && curMonth <= collMonth)
                        {
                            curMonth = collMonth;
                            curDay = collDay;
                        }
                        if(curYear == today.get(Calendar.YEAR) && curMonth == today.get(Calendar.MONTH) && curDay > today.get(Calendar.DAY_OF_MONTH))
                        {
                            curDay = today.get(Calendar.DAY_OF_MONTH);
                        }
                        break;
                        
                    case 2:
                        Date dt = new Date();
                        Calendar ct = Calendar.getInstance();
                        ct.setTime(dt);
                        
                        if(curYear < ct.get(Calendar.YEAR)) //enter coll start year here
                            curYear++;                        
                        break;
                }
                repaint();
                break;
                
            case Canvas.DOWN:
                switch(curItem)
                {
                    case 0: //day
                        if(curDay > 1)
                        {
                            curDay--;
                            if(curMonth == collMonth && curYear == collYear && curDay < collDay)
                            {
                                curDay++;                                
                            }
                            
                        }
                        break;
                        
                    case 1: //month
                        if(curMonth == 0)
                            curMonth = 11;
                        else
                            curMonth--;
                        
                        if(curDay > getDaysInMonth(curMonth,curYear))
                            curDay=1;
                        
                        if(curYear == collYear && curMonth <= collMonth)
                        {
                            curMonth = collMonth;
                            curDay = collDay;
                        }
                        break;
                        
                    case 2:
                        if(curYear > collYear) //enter coll start year here
                            curYear--;                        
                        
                        if(curYear == collYear && curMonth <= collMonth)
                        {
                            curMonth = collMonth;
                            curDay = collDay;
                        }
                        break;
                }
                repaint();
                break;
                
            case Canvas.FIRE:
                repaint();
                break;
        }
    }
    
    private String getMonth(int m)
    {
        switch(m)
        {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
        }
        return "error";
    }
    
    private int getDaysInMonth(int m,int y)
    {
        int[] d={31,28,31,30,31,30,31,31,30,31,30,31};
        if(y%4 == 0)
            d[1]++;
        
        return d[m];
    }
    
    private boolean isDateOkay(int testDay, int testMonth, int testYear,int csDay,int csMonth, int csYear)
    {
        boolean res = true;
        
        return res;
    }

}