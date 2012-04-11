/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Bunkulator;
import javax.microedition.lcdui.*;
import java.util.*;
/**
 *
 * @author Owner
 */
public class StatsCanvas extends Canvas
{
    private Date dtFrom;
    private Date dtTo;
    private int numSubs;
    private int[][] stats; //[][0]=lects att [][1]=taken, [][2]=p att [][3]ptaken
    private boolean lectMode; //true = lecture, false = practs
    private boolean percentMode;  //true = percent, false = numbers
    private String[] subNames;
    
    public StatsCanvas(Date dtFrom, Date dtTo)
    {
        this.dtFrom = dtFrom;
        this.dtTo = dtTo;
        if(!Attendance.fileExists())Attendance.createFile();
        Attendance.open();
        stats = Attendance.getStats(dtFrom, dtTo);
        Attendance.close();
        Settings.open();
        numSubs = Settings.getNumberOfSubjects();        
        subNames = new String[numSubs];
        String[] rawSubs = Settings.getSubjectData();
        Settings.close();
        for(int i=0;i<numSubs;i++)
        {
            int end = rawSubs[i].indexOf(';');
            subNames[i] = rawSubs[i].substring(0,end);            
        }
        lectMode = true;
        percentMode = true;
    }
    
    public StatsCanvas(Date dtFrom, Date dtTo,int[][] argStats)
    {
        this.dtFrom = dtFrom;
        this.dtTo = dtTo;
        stats = argStats;
        Settings.open();
        numSubs = Settings.getNumberOfSubjects();        
        subNames = new String[numSubs];
        String[] rawSubs = Settings.getSubjectData();
        Settings.close();
        for(int i=0;i<numSubs;i++)
        {
            int end = rawSubs[i].indexOf(';');
            subNames[i] = rawSubs[i].substring(0,end);            
        }
        lectMode = true;
        percentMode = true;
    }
    
    public void paint(Graphics g)
    {
        //CLS
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
        String ls = (getWidth()<172) ? "Lecture" : "Lecture Stats";
        String ps = (getWidth()<172) ? "Practical" : "Practical Stats";
        if(lectMode)
            g.drawString(ls, dateW, 0, Graphics.HCENTER | Graphics.TOP);
        else
            g.drawString(ps, dateW, 0, Graphics.HCENTER | Graphics.TOP);
        
        
        if(percentMode)
            g.drawString("Percentages", 3 * dateW, 0, Graphics.HCENTER | Graphics.TOP);
        else
            g.drawString("Numbers", 3 * dateW, 0, Graphics.HCENTER | Graphics.TOP);
        /*
        //Day
        if(curItem == 0) g.setColor(255,0,0); else g.setColor(0,0,0);
        g.drawString(String.valueOf(curDay), dateW, 0, Graphics.HCENTER | Graphics.TOP);
        
        //Month
        if(curItem == 1) g.setColor(255,0,0); else g.setColor(0,0,0);
        g.drawString(getMonth(curMonth), 2 * dateW, 0, Graphics.HCENTER | Graphics.TOP);
        
        //Year
        if(curItem == 2) g.setColor(255,0,0); else g.setColor(0,0,0);
        g.drawString(String.valueOf(curYear), 3 * dateW, 0, Graphics.HCENTER | Graphics.TOP);
        */
        g.setColor(0,0,255); //Set Colour to Blue for Titles
        g.drawString("SUBJECT", (getWidth()/3), 2 * H, Graphics.HCENTER | Graphics.TOP);
        if(!lectMode && !percentMode)
            g.drawString("A", imgOff + 0, 2 * H, Graphics.HCENTER | Graphics.TOP);
        else if(percentMode)
            g.drawString("P", imgOff + 0, 2 * H, Graphics.HCENTER | Graphics.TOP);
        else if(lectMode && !percentMode)
            g.drawString("A", imgOff + 0, 2 * H, Graphics.HCENTER | Graphics.TOP);        
        
        if(percentMode)
        {
            //see which position works best for lects needed
            //g.drawString("?", imgOff + 20, 2 *H, Graphics.HCENTER | Graphics.TOP);
            if(lectMode)
                g.drawString("?", imgOff + 40, 2 *H, Graphics.HCENTER | Graphics.TOP);
        }
        else
        {
            g.drawString("T", imgOff + 20, 2 *H, Graphics.HCENTER | Graphics.TOP); //number taken
            if(lectMode)
                g.drawString("?", imgOff + 40, 2 *H, Graphics.HCENTER | Graphics.TOP); //lects needed
        }        
        // Colour back to black
        g.setColor(0,0,0);
        // Show Att Info based on date
        
        for(int i=0;i<numSubs;i++)
        {
            //g.drawImage(cross, getWidth()/2, getHeight()-(cross.getHeight()/2), Graphics.HCENTER | Graphics.VCENTER);
            // Read Att Info and show Icons Accordingly
            g.drawString(subNames[i], (getWidth()/3), (i+4) * H, Graphics.HCENTER | Graphics.TOP);
            int tmp;
            if(lectMode)
            {
                if(percentMode)
                {
                    tmp = (stats[i][1]!=0) ? stats[i][0] * 100 / stats[i][1] : 0;
                    g.drawString(String.valueOf(tmp),imgOff + 0, (i+4) * H, Graphics.HCENTER | Graphics.TOP);
                }
                else
                {
                    g.drawString(String.valueOf(stats[i][0]),imgOff + 0,  (i+4) * H, Graphics.HCENTER | Graphics.TOP);
                    g.drawString(String.valueOf(stats[i][1]),imgOff + 20, (i+4) * H, Graphics.HCENTER | Graphics.TOP);
                }
            }
            else
            {
                if(percentMode)
                {
                    tmp = (stats[i][3]!=0) ? stats[i][2] * 100 / stats[i][3] : 0;
                    g.drawString(String.valueOf(tmp),imgOff + 0, (i+4) * H, Graphics.HCENTER | Graphics.TOP);
                }
                else
                {
                    g.drawString(String.valueOf(stats[i][2]),imgOff + 0,  (i+4) * H, Graphics.HCENTER | Graphics.TOP);
                    g.drawString(String.valueOf(stats[i][3]),imgOff + 20, (i+4) * H, Graphics.HCENTER | Graphics.TOP);
                }
            }
            if(lectMode)
            {
                int lNeeded = getLectsNeeded(stats[i][0],stats[i][1]);
                if(lNeeded > 0)
                    g.setColor(255, 0, 0);
                else
                    g.setColor(0, 255, 0);
                
                g.drawString(String.valueOf(lNeeded),imgOff + 40, (i+4) * H, Graphics.HCENTER | Graphics.TOP);              
                g.setColor(0, 0, 0);
            }
        }
    }
    
    private int getLectsNeeded(int attended, int taken)
    {
        int lectsNeeded=0;
        Settings.open();
        int minAttpercent = Settings.getMinAttPer();
        Settings.close();
        if(taken <=0) return 0; //To Prevent Division By Zero Exception.
        int percent = (100 * attended) / taken;
        if(percent < minAttpercent)
        {
            while(percent < minAttpercent)
            {
                lectsNeeded++;
                attended++;
                taken++;
                percent = (100 * attended) / taken;
            }
        }
        else
        {
            while(percent > minAttpercent)
            {
                lectsNeeded--;
                taken++;
                percent = (100 * attended) / taken;
                if(percent < minAttpercent) lectsNeeded++;
            }
        }        
        return lectsNeeded;
    }
    
    public void keyPressed(int keyCode)
    {
        int action = getGameAction(keyCode);
        switch(action)
        {
            case Canvas.FIRE:
                percentMode = !percentMode;
                repaint();
                break;
                
            case Canvas.LEFT:
            case Canvas.RIGHT:
                lectMode = !lectMode;
                repaint();
                break;
        }
    }

}