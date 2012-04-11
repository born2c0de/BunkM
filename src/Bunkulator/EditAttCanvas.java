/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Bunkulator;
import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author Owner
 */
public class EditAttCanvas extends Canvas
{
    private int curSubject;
    private boolean showOriTog;
    private boolean calledForAdd;
    private Image attKey;
    String[] subNames;
    int[] practBits;
    String[] options;
    int lectTog, practTog, extraTog;
    int[] lectBytes;
    int[] practBytes;
    int[] extraBytes;
    private BunkMIDlet bMIDlet;
    private Date editDate;
    
    public EditAttCanvas(BunkMIDlet bmd,Date d,boolean AddCv)
    {
        try
        {
            calledForAdd=AddCv;
            showOriTog=true;
            this.bMIDlet = bmd;                  
            String[] s;
            editDate = d;
            //byte[] b;
            int end=0;            
            if(getHeight() > 220)
                attKey = Image.createImage("/images/attKey.PNG");            
            else
                attKey = Image.createImage("/images/attKeySmall.PNG");            
            options = new String[3];
            options[0] = "Not Taken";
            options[1] = "Attended";
            options[2] = "Bunked";
            Settings.open();
            int noSubs = Settings.getNumberOfSubjects();
            subNames = new String[noSubs];
            practBits = new int[noSubs];
            s = Settings.getSubjectData();
            for(int i=0;i<noSubs;i++)
            {
                end = s[i].indexOf(';');
                subNames[i] = s[i].substring(0,end);                
                practBits[i] = Integer.parseInt(s[i].substring(end+1));
            }
            Settings.close();
            //lectBytes = new int[noSubs];
            //practBytes = new int[noSubs];
            //extraBytes = new int[noSubs];
            curSubject = 0;
            lectTog = 0;
            practTog = 0;
            extraTog = 0;
            if(!Attendance.fileExists()) Attendance.createFile();
            Attendance.open();
            String sx = Attendance.getAttData(editDate);
            lectBytes = Attendance.getLectFromAttByte(sx);
            practBytes = Attendance.getPractFromAttByte(sx);
            extraBytes = Attendance.getExtraFromAttByte(sx);
            Attendance.close();
        }
        catch(IOException e)
        {
            System.out.println("Couldn't load image in TodayAttCanvas()" + e.getMessage());
        }
    }
    
    public void paint(Graphics g)
    {
        g.setColor(255,255,255);
        g.fillRect(0, 0, getWidth(), getHeight());
        if(showOriTog)
        {
            lectTog = lectBytes[curSubject];
            practTog = practBytes[curSubject];
            extraTog = extraBytes[curSubject];
            showOriTog = false;
        }
        //draw Key
        g.drawImage(attKey, getWidth()/2, getHeight()-(attKey.getHeight()/2), Graphics.HCENTER | Graphics.VCENTER);
        //Set Font for Text
        Font f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        g.setFont(f);
        g.setColor(255,0,0);
        
        g.drawString(subNames[curSubject], getWidth()/2, 0, Graphics.HCENTER | Graphics.TOP);
        g.setColor(0,0,0);
        g.drawString("Lecture " + options[lectTog], getWidth()/2, 30, Graphics.HCENTER | Graphics.TOP);
        g.drawString("Practicals " + options[practTog], getWidth()/2, 50, Graphics.HCENTER | Graphics.TOP);
        g.drawString("Xtra Lect " + options[extraTog], getWidth()/2, 70, Graphics.HCENTER | Graphics.TOP);        
    }
    
    public void keyPressed(int keyCode)
    {
        int action = getGameAction(keyCode);
        
        switch(action)
        {       
            case Canvas.DOWN:
                extraTog = (extraTog + 1) % options.length;
                repaint();
                break;
                
            case Canvas.LEFT:
                practTog = (practTog + 1) % options.length;
                repaint();
                break;
                
            case Canvas.UP:
                lectTog = (lectTog + 1) % options.length;
                repaint();
                break;
                
            case Canvas.FIRE:
                // Set lectByte, practByte etc.
                lectBytes[curSubject] = lectTog;
                practBytes[curSubject] = practTog;
                extraBytes[curSubject] = extraTog;
                // if curSubject is in limit, change subject
                // else save lectByte;practByte;extraByte to file.
                if(curSubject < (subNames.length-1))
                {
                    // set bytes
                    
                    // Go to next subject
                    lectTog = 0;
                    practTog = 0;
                    extraTog = 0;
                    curSubject++;
                    showOriTog = true;
                    repaint();
                }
                else
                {
                    //for now...till rest is implemented
                    curSubject = 0;
                    repaint();
                    //save record and go back
                    if(!Attendance.fileExists())Attendance.createFile();
                    Attendance.open();
                    Attendance.setAttData(editDate, lectBytes, practBytes, extraBytes);
                    Attendance.close();
                    if(calledForAdd)
                        bMIDlet.addEditAttToMenu();
                    else
                        bMIDlet.editAttToMenu();
                     
                }
                break;           
        }
    }    
}