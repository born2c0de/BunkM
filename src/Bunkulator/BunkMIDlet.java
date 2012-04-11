/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Bunkulator;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import MD5.*;

/**
 * @author Sanchit Karve - born2c0de
 */
public class BunkMIDlet extends MIDlet implements CommandListener
{
    //Universal Stuff
    private Display dMain;
    private boolean actionHandled;
    private Command cmdBack;
    private Command cmdHelp;
    //Main Menu Stuff
    private Command cmdExit;
    private List lstMainMenu;
    private Ticker mainTicker;
    //Settings Stuff
    private Form frmSettings;
    private TextField txtName;
    private TextField txtMinAttPer;
    private DateField dtCollStart;
    private TextField[] txtSubjects;
    private ChoiceGroup[] cgSubHasPracts;
    private TextField txtUserName;
    private TextField txtPassword;
    private Command cmdSaveSettings;    
    private boolean dateEnteredBefore;
    //Upload-Download Stuff
    public String tmp;
    //Add Today's Attendance Stuff
    //private TodayAttCanvas addTodayAtt;
    private EditAttCanvas addEditAtt;
    //View Attendance Stuff
    private Command cmdEdit;
    private ViewAttCanvas viewAttCv;
    private EditAttCanvas editAttCv;
    private Command cmdBackEdit;
    //Stats Range Selection Stuff
    private Form frmStatRange;
    public DateField dtFrom;
    public DateField dtTo;
    private Command cmdShowStats;
    public Gauge gProgress;
    //Stats Canvas Stuff
    public StatsCanvas statsCv;
    public Command cmdBackStatRange;
    private ChoiceGroup cgOverride;
    //Maintenance Stuff
    private List lstMtn;
    //About-Bunkulator Stuff
    private Form frmAbout;    
    //Update Bunkulator Stuff
    //private UpdateCanvas update;
    public Form frmUpdate;
    public Command cmdCheck;
    public Command cmdDownload;
    public String updURL;
    //Help Stuff
    private Form frmHelp;
    
    public BunkMIDlet()
    {        
        //Universal Init
        dMain = Display.getDisplay(this);
        updURL=null;
        cmdBack = new Command("Back",Command.BACK,0);
        cmdHelp = new Command("Help",Command.HELP,0);
        //Main Menu Init
       /* String[] mnuItems = {"Add Today's Attendance","View/Edit Attendance"
                             ,"Statistics","Settings","Import/Export"
                             ,"Assignment Manager","Time-Table Manager"
                             ,"Maintenance","Check For Updates","About"}; */
        String[] mnuItems = {"Add Today's Attendance","View/Edit Attendance"
                            ,"Statistics","Settings","Maintenance","Check for Updates","About"};
        Image[] mnuImgs = null;
        lstMainMenu = new List("BunkM Menu",List.IMPLICIT,mnuItems,mnuImgs);
        cmdExit = new Command("Exit",Command.EXIT,0);
        mainTicker = new Ticker("Choose an Item from the list and click Help to Learn how to use the respective feature.");
        lstMainMenu.addCommand(cmdExit);
        lstMainMenu.addCommand(cmdHelp);
        lstMainMenu.setTicker(mainTicker);
        lstMainMenu.setCommandListener(this);        
    }
    
    public void startApp()
    {
        dMain.setCurrent(lstMainMenu);
    }

    public void pauseApp()
    {
    }

    public void destroyApp(boolean unconditional)
    {        
    }
    
    public void commandAction(Command c, Displayable d)
    {        
        // Universal Stuff
        actionHandled = false;
        if(c == cmdBack)
        {
            // As you create new Forms, add them here and set to null.
            frmAbout = null;            
            frmSettings = null;
            //addTodayAtt = null;  
            //update = null;
            frmUpdate = null;
            addEditAtt = null;
            viewAttCv = null;
            editAttCv = null;
            statsCv = null;
            lstMtn = null;
            frmHelp = null;
            // This is done so that:
            // 1) Same Back Command can be used for all Forms.
            // 2) Memory can be freed immediately.
            actionHandled = true;
            dMain.setCurrent(lstMainMenu);            
        }
        if(!actionHandled)actionHandled = addAttendanceAction(c,d);
        if(!actionHandled)actionHandled = statsAction(c,d);
        if(!actionHandled)actionHandled = viewAttendanceAction(c,d);        
        if(!actionHandled)actionHandled = settingsAction(c,d);
        if(!actionHandled)actionHandled = aboutAndHelpAction(c,d);
        if(!actionHandled)actionHandled = maintenanceAction(c,d);
        if(!actionHandled)actionHandled = updateAction(c,d);
        // Exit Handler
        if(!actionHandled && c == cmdExit)
        {
            actionHandled = true;
            destroyApp(true);
            notifyDestroyed();
        }
        //No Action Handler Found
        if(!actionHandled)
        {
            Alert err = new Alert("Error","No Command Handler Found. Report this to Sanchit Karve (write2sanchit@gmail.com)",null,AlertType.ERROR);
            err.setTimeout(Alert.FOREVER);
            dMain.setCurrent(err);            
        }
    }
    
    public boolean settingsAction(Command c, Displayable d)
    {        
        if(c == List.SELECT_COMMAND)
        {
            if(lstMainMenu.getString(lstMainMenu.getSelectedIndex()).equals("Settings"))
            {
                dateEnteredBefore = true;
                frmSettings = new Form("Settings");
                //Read Settings Here and set them to their appropriate controls.
                if(Settings.fileExists() == false)
                {
                    Settings.createFile();
                }
                Settings.open();
                String rsName = Settings.getName();
                String rsMinAttPer = String.valueOf(Settings.getMinAttPer());
                Date rsDt = Settings.getCollStartDate();
                String rsUserName = Settings.getUserName();
                String rsPassword = Settings.getPassword();
                String[] subData = Settings.getSubjectData();
                String[] subNames = new String[8];
                int[] practs = new int[8];
                for(int i=0;i<8;i++)
                {
                    int start=0,end=subData[i].indexOf(';');
                    subNames[i] = subData[i].substring(start,end);
                    practs[i] = Integer.parseInt(subData[i].substring(end+1));                    
                }
                Settings.close();
                txtName = new TextField("Name",rsName,15,TextField.ANY);
                txtMinAttPer = new TextField("Min. Attendance Percent",rsMinAttPer,2,TextField.NUMERIC);
                dtCollStart = new DateField("College Start Date",DateField.DATE);
                txtUserName = new TextField("Username",rsUserName,255,TextField.ANY);
                txtPassword = new TextField("Password",rsPassword,255,TextField.PASSWORD);
                if(rsDt == null)
                {
                    dateEnteredBefore = false;
                    rsDt = new Date(); //sets Date to Today.                
                }
                dtCollStart.setDate(rsDt);                                
                txtSubjects = new TextField[8];
                cgSubHasPracts = new ChoiceGroup[8];
                for(int i=0;i<8;i++)
                {
                    //Read Subject Names Here and set to appropriate TextField.
                    txtSubjects[i] = new TextField("Subject" + String.valueOf(i+1),subNames[i],20,TextField.ANY);                    
                    
                    String[] pracChoices = {"Yes","No"};
                    cgSubHasPracts[i] = new ChoiceGroup("Has Practicals?",Choice.POPUP,pracChoices,null);
                    if(practs[i]==1)
                        cgSubHasPracts[i].setSelectedIndex(1, true);
                    else
                        cgSubHasPracts[i].setSelectedIndex(0, true);
                }
                cmdSaveSettings = new Command("Save",Command.OK,0);
                
                //Form Creation
                frmSettings.append(txtName);
                frmSettings.append(txtMinAttPer);
                frmSettings.append(dtCollStart);
                
                
                frmSettings.append(new Spacer(1,20));
                for(int i=0;i<8;i++)
                {
                    frmSettings.append(txtSubjects[i]);
                    frmSettings.append(cgSubHasPracts[i]);
                    frmSettings.append(new Spacer(1,5));
                }
                //Append BunkM Online Username & Password TextFields
                frmSettings.append(txtUserName);
                frmSettings.append(txtPassword);

                frmSettings.addCommand(cmdSaveSettings);
                frmSettings.addCommand(cmdBack);
                frmSettings.setCommandListener(this);
                dMain.setCurrent(frmSettings);
                return true;
            }            
        }
        if(c == cmdSaveSettings)
        {
            if(dateEnteredBefore)
            {
                Calendar cstat = Calendar.getInstance();
                Calendar chg = Calendar.getInstance();
                cstat.setTime(dtCollStart.getDate());
                Settings.open();                
                chg.setTime(Settings.getCollStartDate());
                Settings.close();
                chg.set(Calendar.HOUR_OF_DAY, cstat.get(Calendar.HOUR_OF_DAY));
                chg.set(Calendar.MINUTE, cstat.get(Calendar.MINUTE));
                chg.set(Calendar.SECOND, cstat.get(Calendar.SECOND));
                chg.set(Calendar.MILLISECOND, cstat.get(Calendar.MILLISECOND));
                if(cstat.getTime().getTime() != chg.getTime().getTime())
                {
                    this.alert("Cannot change Date", "You can only change start Date if you Reset Settings.", AlertType.ERROR,Alert.FOREVER, null);
                    return true;
                }
            }
            Settings.open();            
            Settings.setName(txtName.getString());
            Settings.setMinAttPer(Integer.parseInt(txtMinAttPer.getString()));
            Date now = new Date();
            if(now.getTime() >= dtCollStart.getDate().getTime())
                Settings.setCollStartDate(dtCollStart.getDate());
            
            String[] sData = new String[8];
            for(int i=0;i<8;i++)
            {
                sData[i] = txtSubjects[i].getString() + ";" + String.valueOf(cgSubHasPracts[i].getSelectedIndex());
            }
            Settings.setSubjectData(sData);
            Settings.setUserName(txtUserName.getString());
            Settings.setPassword(txtPassword.getString());
            Settings.close();
            this.alert("Success", "Settings Saved.", AlertType.CONFIRMATION, 1000, lstMainMenu);
            return true;
        }
        return false;
    }
    
    public boolean addAttendanceAction(Command c, Displayable d)
    {        
        if(c == List.SELECT_COMMAND)
        {
            if(lstMainMenu.getString(lstMainMenu.getSelectedIndex()).equals("Add Today's Attendance"))
            {
                if(!hasAtLeastOneSubject())
                {
                    this.alert("Error", "Create at least one subject in Settings Menu.", AlertType.ERROR, Alert.FOREVER, lstMainMenu);
                }
                else
                {
                    /*addTodayAtt = new TodayAttCanvas(this);
                    addTodayAtt.addCommand(cmdBack);
                    addTodayAtt.setCommandListener(this);
                    dMain.setCurrent(addTodayAtt);*/
                    
                    /*Calendar cx = Calendar.getInstance();
                    cx.set(Calendar.DAY_OF_MONTH, viewAttCv.curDay);
                    cx.set(Calendar.MONTH, viewAttCv.curMonth);
                    cx.set(Calendar.YEAR, viewAttCv.curYear);*/
                    addEditAtt = new EditAttCanvas(this,new Date(),true);
                    cmdBackEdit = new Command("Back",Command.BACK,0);
                    addEditAtt.addCommand(cmdBack);
                    addEditAtt.setCommandListener(this);
                    dMain.setCurrent(addEditAtt);
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean viewAttendanceAction(Command c, Displayable d)
    {      
        if(c == List.SELECT_COMMAND)
        {
            if(lstMainMenu.getString(lstMainMenu.getSelectedIndex()).equals("View/Edit Attendance"))
            {
                if(!hasAtLeastOneSubject())
                {
                    this.alert("Error", "Create at least one subject in Settings Menu.", AlertType.ERROR, Alert.FOREVER, lstMainMenu);
                }
                else
                {
                    viewAttCv = new ViewAttCanvas(this);
                    viewAttCv.addCommand(cmdBack);
                    cmdEdit = new Command("Edit",Command.ITEM,0);
                    viewAttCv.addCommand(cmdEdit);
                    viewAttCv.setCommandListener(this);
                    dMain.setCurrent(viewAttCv);                    
                }                
                return true;
            }
        }
        if(c == cmdEdit)
        {
            //Load Edit Canvas here
            if(viewAttCv.dateValid)
            {            
                Calendar cx = Calendar.getInstance();
                cx.set(Calendar.DAY_OF_MONTH, viewAttCv.curDay);
                cx.set(Calendar.MONTH, viewAttCv.curMonth);
                cx.set(Calendar.YEAR, viewAttCv.curYear);
                editAttCv = new EditAttCanvas(this,cx.getTime(),false);
                cmdBackEdit = new Command("Back",Command.BACK,0);
                editAttCv.addCommand(cmdBackEdit);
                editAttCv.setCommandListener(this);
                dMain.setCurrent(editAttCv);                
            }
            return true;
        }
        if(c == cmdBackEdit)
        {
            dMain.setCurrent(viewAttCv);
            editAttCv = null;
            return true;
        }
        return false;
    }
    
    public boolean aboutAndHelpAction(Command c, Displayable d)
    {    
        if(c == List.SELECT_COMMAND)            
        {
            if(lstMainMenu.getString(lstMainMenu.getSelectedIndex()).equals("About"))
            {                
                frmAbout = new Form("About BunkM");
                frmAbout.append("Version : " + this.getAppProperty("MIDlet-Version") + " Beta");
                frmAbout.append("\nWritten by Sanchit Karve");
                frmAbout.append("\nVisit www.sanchitkarve.com");
                frmAbout.addCommand(cmdBack);
                frmAbout.setCommandListener(this);
                AlertType.INFO.playSound(dMain);
                dMain.setCurrent(frmAbout);
                return true;
            }            
        }
        if(c == cmdHelp)
        {
            String helpForMenu = lstMainMenu.getString(lstMainMenu.getSelectedIndex());
            frmHelp = new Form("Help");
            this.setHelpText(frmHelp, helpForMenu);
            frmHelp.addCommand(cmdBack);
            frmHelp.setCommandListener(this);
            dMain.setCurrent(frmHelp);
            return true;
        }
        return false;
    }

    public boolean statsAction(Command c, Displayable d)
    {          
        if(c == List.SELECT_COMMAND)
        {
            if(lstMainMenu.getString(lstMainMenu.getSelectedIndex()).equals("Statistics"))
            {
                if(!hasAtLeastOneSubject())
                {
                    this.alert("Error", "Create at least one subject in Settings Menu.", AlertType.ERROR, Alert.FOREVER, lstMainMenu);
                }
                else
                {
                    frmStatRange = new Form("Select Date Range");
                    cmdShowStats = new Command("Show",Command.OK,0);
                    frmStatRange.append("Choose a Date-Range.");
                    dtFrom = new DateField("From",DateField.DATE);
                    Settings.open();
                    dtFrom.setDate(Settings.getCollStartDate());
                    Settings.close();
                    dtTo = new DateField("To",DateField.DATE);
                    dtTo.setDate(new Date());
                    String[] ovrideOpt = {"No","Yes"};
                    cgOverride = new ChoiceGroup("Bypass Date Check",Choice.POPUP,ovrideOpt,null);
                    gProgress = new Gauge("Progress",false,100,0);
                    frmStatRange.append(new Spacer(1,1));
                    frmStatRange.append(dtFrom);
                    frmStatRange.append(dtTo);     
                    frmStatRange.append(cgOverride);
                    frmStatRange.append(gProgress);
                    frmStatRange.addCommand(cmdShowStats);
                    frmStatRange.addCommand(cmdBack);
                    frmStatRange.setCommandListener(this);
                    dMain.setCurrent(frmStatRange);    
                }                
                return true;
            }
        }
        if(c == cmdShowStats)
        {
            if(dateWithinRange(dtFrom.getDate(),dtTo.getDate()) || cgOverride.getString(cgOverride.getSelectedIndex()).equals("Yes"))
            {
                frmStatRange.removeCommand(cmdShowStats);
                StatProcess stp = new StatProcess(this); 
                stp.start();
                //statsCv = new StatsCanvas(dtFrom.getDate(),dtTo.getDate());
                //cmdBackStatRange = new Command("Back",Command.BACK,0);
                //statsCv.addCommand(cmdBackStatRange);
                //statsCv.setCommandListener(this);
                //dMain.setCurrent(statsCv);
            }
            else
            {
                this.alert("Error", "Choose Dates in the correct Range.", AlertType.ERROR, 1000, null);
            }
            return true;
        }
        if(c == cmdBackStatRange)
        {
            gProgress.setValue(0);
            frmStatRange.addCommand(cmdShowStats);
            dMain.setCurrent(frmStatRange);            
            statsCv = null;
            return true;
        }
        return false;
    }
    
    public boolean maintenanceAction(Command c, Displayable d)
    {    
        if(c == List.SELECT_COMMAND)            
        {
            if(d == lstMainMenu && lstMainMenu.getString(lstMainMenu.getSelectedIndex()).equals("Maintenance"))
            {      
                //String[] mnuItems = {"Reset Settings","Reset Attendance","Import","Export","Speed Test","Upload Data","Download Data"};
                String[] mnuItems = {"Reset Settings","Reset Attendance","Speed Test","Upload Data","Download Data"};
                Image[] mnuImgs = null;
                lstMtn = new List("Maintenance",List.IMPLICIT,mnuItems,mnuImgs);
                Ticker mtnTicker = new Ticker("WARNING : There are NO confirmations here. Be VERY Careful!");
                lstMtn.addCommand(cmdBack);
                lstMtn.setTicker(mtnTicker);
                lstMtn.setCommandListener(this);                      
                AlertType.WARNING.playSound(dMain);
                dMain.setCurrent(lstMtn);
                return true;
            }
            if(d == lstMtn && lstMtn.getString(lstMtn.getSelectedIndex()).equals("Reset Settings"))
            {
                Settings.deleteFile();
                Attendance.deleteFile();
                this.alert("Success", "Settings and Attendance Info Cleared.", AlertType.CONFIRMATION, 1500, null);
                return true;
            }
            if(d == lstMtn && lstMtn.getString(lstMtn.getSelectedIndex()).equals("Reset Attendance"))
            {
                Attendance.deleteFile();
                this.alert("Success", "Attendance Info Cleared.", AlertType.CONFIRMATION, 1500, null);
                return true;
            }
            /*if(d == lstMtn && lstMtn.getString(lstMtn.getSelectedIndex()).equals("Export"))
            {
                if(!BackupRestoreFile.isFileHandlingSupported())
                {
                    alert("Error", "File Handling is not supported on your phone.", AlertType.ERROR, Alert.FOREVER, null);                    
                }
                else
                {
                    alert("Incomplete", "Feature still in development.", AlertType.INFO, Alert.FOREVER, null);                    
                    //ExportProcess ep = new ExportProcess(this);
                    //ep.start();
                }                
                return true;
            }
            if(d == lstMtn && lstMtn.getString(lstMtn.getSelectedIndex()).equals("Import"))
            {
                if(!BackupRestoreFile.isFileHandlingSupported())
                {
                    alert("Error", "File Handling is not supported on your phone.", AlertType.ERROR, Alert.FOREVER, null);                                        
                }
                else
                {
                    alert("Incomplete", "Feature still in development.", AlertType.INFO, Alert.FOREVER, null);                    
                }
                return true;
            }*/
            if(d == lstMtn && lstMtn.getString(lstMtn.getSelectedIndex()).equals("Speed Test"))
            {
                long b4  = System.currentTimeMillis();
                
                for(int i=0;i<2000;i++)
                {
                    b4++;
                    for(int j=0;j<4000;j+=2)
                    {
                        if(j == 2570)
                            b4--;
                    }
                }
                long diff = System.currentTimeMillis() - b4;
                //this.alert("Result", "Your Phone Score : " + String.valueOf(diff) + ". The Lower the score, the better.", AlertType.CONFIRMATION, Alert.FOREVER, null);
                if(diff > 250)
                    this.alert("Slow Phone", "Your Score : " + String.valueOf(diff) + ". Optimum Score should be below 250.", AlertType.INFO, Alert.FOREVER, null);
                else
                    this.alert("Fast Phone", "Your Score : " + String.valueOf(diff) + ". This phone will work well with Bunklator.", AlertType.INFO, Alert.FOREVER, null);
                return true;
            }
            if(d == lstMtn && lstMtn.getString(lstMtn.getSelectedIndex()).equals("Upload Data"))
            {
                //Attendance.deleteFile();
                if(!hasAtLeastOneSubject())
                {
                    this.alert("Error", "Create at least one subject in Settings Menu.", AlertType.ERROR, Alert.FOREVER, lstMtn);
                }
                else
                {
                    if(!Attendance.fileExists())
                    {
                        this.alert("Error", "Add attendance for at least one day.", AlertType.ERROR, Alert.FOREVER, lstMtn);
                        return true;
                    }
                    Attendance.open();
                    String data = Attendance.consolidateData();
                    Attendance.close();
                    Settings.open();
                    String sData=Settings.consolidateSettingsData();
                    Settings.close();
                    tmp="X";
                    UploadDataProcess p = new UploadDataProcess(this,data,sData);
                    p.start();                    
                    while(tmp.equals("X")){}
                    //this.alert("Upload Data Stub", data, AlertType.CONFIRMATION, 1500, null);
                    this.alert("Upload Result", tmp, AlertType.CONFIRMATION, Alert.FOREVER, null);
                    tmp="";
                }
                return true;
            }
            if(d == lstMtn && lstMtn.getString(lstMtn.getSelectedIndex()).equals("Download Data"))
            {
                //Attendance.deleteFile();
                String dloadSettings="", dloadAttendance="";
                if(!Settings.fileExists())
                {
                    this.alert("Error", "Please Enter your BunkM-Online Username and Password in the Settings Menu.", AlertType.ERROR, Alert.FOREVER, null);
                    return true;
                }
                tmp="X";
                DownloadDataProcess ddpAttendance = new DownloadDataProcess(this,true);
                ddpAttendance.start();
                while(tmp.equals("X")){}
                dloadAttendance=tmp;

                tmp="Y";
                DownloadDataProcess ddpSettings = new DownloadDataProcess(this,false);
                ddpSettings.start();
                while(tmp.equals("Y")){}
                dloadSettings=tmp;
                tmp="";
                if(dloadAttendance.startsWith("[Error]") || dloadAttendance.equals("") || dloadSettings.startsWith("[Error]") || dloadSettings.equals(""))
                    this.alert("Download Failed", "Wrong Username/Password or unstable Internet Connection", AlertType.ERROR, Alert.FOREVER, null);
                else
                {
                    Settings.open();
                    Settings.importConsolidatedSettingsData(dloadSettings);
                    Settings.close();

                    if(Attendance.fileExists())Attendance.deleteFile();
                    Attendance.createFile();
                    Attendance.open();
                    Attendance.importConsolidatedData(dloadAttendance);
                    Attendance.close();
                    this.alert("Download Success", "Settings and Attendance Data Retrieved", AlertType.CONFIRMATION, Alert.FOREVER, null);
                }

                //del lines below
                //Settings.open();
                //String data = Settings.consolidateSettingsData();
                //Settings.close();
                //this.alert("Download Data Stub", data, AlertType.CONFIRMATION, 1500, null);

                return true;
            }
        }
        return false;
    }
    
    public boolean updateAction(Command c, Displayable d)
    {    
        if(c == List.SELECT_COMMAND)            
        {
            if(lstMainMenu.getString(lstMainMenu.getSelectedIndex()).equals("Check for Updates"))
            {                
                /*update = new UpdateCanvas();                
                //viewAttCv = new ViewAttCanvas(this);
                update.addCommand(cmdBack);
                update.setCommandListener(this);
                dMain.setCurrent(update);*/
                
                frmUpdate = new Form("Update Checker");
                frmUpdate.addCommand(cmdBack);
                cmdCheck = new Command("Check",Command.OK,0);
                frmUpdate.addCommand(cmdCheck);
                //frmStatRange.append(new Spacer(1,1));
                //frmStatRange.append(dtFrom);
                //frmStatRange.append(dtTo);
                //    frmStatRange.addCommand(cmdShowStats);
                //    frmStatRange.addCommand(cmdBack);
                StringItem ver = new StringItem("Current Version",getAppProperty("MIDlet-Version"));
                frmUpdate.append(ver);
                frmUpdate.setCommandListener(this);
                dMain.setCurrent(frmUpdate);                
                return true;
            }
        }
        if(c == cmdCheck)
        {
            UpProcess p = new UpProcess(this); 
            p.start();
            return true;
        }
        if(c == cmdDownload)
        {
            try
            {
                System.out.println("Download Initiated.");
                platformRequest(updURL);
                destroyApp(true);
            }
            catch(Exception e)
            {
                System.out.println("Update Download error : " + e.getMessage());
            }
            return true;
        }
        return false;
    }
    
    private boolean hasAtLeastOneSubject()
    {
        boolean res = true;
        int numSubs = 0;
        boolean test = Settings.fileExists();
        if(test)
        {
            Settings.open();
            numSubs = Settings.getNumberOfSubjects();
            Settings.close();
        }
        if(numSubs < 1 || numSubs > 8)        
            res = false;
        
        return res;
    }
    
    public void alert(String title,String msg,AlertType at,int duration,Displayable next)
    {
        Alert err = new Alert(title,msg,null,at);
        err.setTimeout(duration);
        if(next != null)
            dMain.setCurrent(err,next);   
        else
            dMain.setCurrent(err);
    }
    
    public void todayAttToMenu()
    {
        this.alert("Success", "Attendance Saved.", AlertType.CONFIRMATION, 1000, lstMainMenu);        
        //addTodayAtt = null;
    }
    
    public void editAttToMenu()
    {
        this.alert("Success", "Attendance Saved.", AlertType.CONFIRMATION, 1000, viewAttCv);        
        editAttCv=null;
    }
    
    public void addEditAttToMenu()
    {
        this.alert("Success", "Attendance Saved.", AlertType.CONFIRMATION, 1000, lstMainMenu);
        addEditAtt=null;
    }
    
    private boolean dateWithinRange(Date from,Date to)
    {
        boolean res = true;
        Settings.open();
        Date low = Settings.getCollStartDate();        
        Settings.close();
        Date high = new Date();
        Calendar c = Calendar.getInstance();
        Calendar chg = Calendar.getInstance();
        c.setTime(from);
        chg.setTime(low);
        chg.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        chg.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        chg.set(Calendar.SECOND, c.get(Calendar.SECOND));
        chg.set(Calendar.MILLISECOND, c.get(Calendar.MILLISECOND));
        low = chg.getTime();
        
        c.setTime(to);
        chg.setTime(high);
        chg.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        chg.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        chg.set(Calendar.SECOND, c.get(Calendar.SECOND));
        chg.set(Calendar.MILLISECOND, c.get(Calendar.MILLISECOND));
        high = chg.getTime();
        
        if(from.getTime() < low.getTime() || to.getTime() > high.getTime() || from.getTime() > to.getTime())
            res = false;
        
        return res;
        
    }
    
    private void setHelpText(Form help,String menu)
    {
        if(menu.equals("Add Today's Attendance"))
        {
            StringItem info1 = new StringItem("What does this do","This Menu allows you to quickly add the attendance for today's lectures.");
            StringItem info2 = new StringItem("How to use","The edges of the picture in the Add screen represent the buttons on your keypad. Press the corresponding key to toggle Attendance, Practicals or Extra Lectures.");
            StringItem info3 = new StringItem("Tip","This menu remembers the attendance that you've added for today. Hence you can use this menu multiple times during the day setting information of only one lecture at a time if you wish. To skip adding information for other subjects, press the Centre key or 5 on your keypad.");
            help.append(info1);
            help.append(info2);
            help.append(info3);
            help.setTitle("Help - Add Attendance");
        }
        if(menu.equals("View/Edit Attendance"))
        {
            StringItem info1 = new StringItem("What does this do","This Menu allows you to view Attendance information for any date and allows you to edit it if you wish.");
            StringItem info2 = new StringItem("How to use","Use the Left and Right direction Keys to select Day, Month and Year and Press the Up and Down Keys to Increase or Decrease the values respectively. The Attendance information will automatically appear as you select the date.");
            StringItem info3 = new StringItem("Editing Attendance","Select the desired Date and press Edit. The Edit Screen is similar to the Add Today's Attendance screen. Read its help for more information.");
            help.append(info1);
            help.append(info2);
            help.append(info3);
            help.setTitle("Help - View/Edit Attendance");
        }
        if(menu.equals("Statistics"))
        {
            StringItem info1 = new StringItem("What does this do","This Menu shows you your attendance record in a tabulated format between any valid date range.");
            StringItem infoOver = new StringItem("Bypass Date Check","On some phones, the date verifying algorithm does not work. If your phone gives you an error message even for dates in the correct range, use this option to disable the algorithm.");
            StringItem info2 = new StringItem("Tip","The ? column displays the number of lectures that you need to attend to reach the minimum percentage for that subject. A negative number means that you can afford to skip the specified number of lectures and yet maintain your attendance >= the minimum required percentage.");
            StringItem info3 = new StringItem("Changing Fields","Press the Centre button to toggle between percentage and Number Mode. Press Left or Right to toggle between Lecture and Practical statistics.");
            StringItem info4 = new StringItem("Percentage Field","This field displays attended lectures or practicals as a percentage and is shown under the P column.");
            StringItem info5 = new StringItem("Numbers Field","This field displays lecture and practical information in the Attended vs Taken format. Number of Attended Lectures appear under the A column and Number of Taken Lectures appear under the T column.");
            help.append(info1);
            help.append(infoOver);
            help.append(info2);
            help.append(info3);
            help.append(info4);
            help.append(info5);
            help.setTitle("Help - Statistics");
        }
        if(menu.equals("Settings"))
        {
            StringItem info1 = new StringItem("What does this do","This menu allows you to set your Subject names and college start date and is required before you can use this application.");
            StringItem info2 = new StringItem("Tip","Use abbreviations of Subject Names if you have a phone with a small screen so that the names can be displayed properly on the Add, View and Statistics page.");
            StringItem info3 = new StringItem("Caution","You cannot change the college start date once you add the Settings for the first time. To change it again, you must Reset Settings from the Maintenance Menu but you will lose your entire attendance record.");
            help.append(info1);
            help.append(info2);
            help.append(info3);
            help.setTitle("Help - Settings");
        }
        if(menu.equals("Maintenance"))
        {
            StringItem info1 = new StringItem("What does this do","This Menu allows you to Reset Settings or Attendance Data.");
            StringItem info2 = new StringItem("Reset Settings","This option will erase the settings and your entire attendance information.");
            StringItem info3 = new StringItem("Reset Attendance","This option resets Attendance information but leaves the settings information intact.");
            StringItem info4 = new StringItem("Speed Test","This option judges your cell-phone processing power. It was written purely for fun and your BunkM user experience has nothing to do with its result.");
            help.append(info1);
            help.append(info2);
            help.append(info3);
            help.append(info4);
            help.setTitle("Help - Maintenance");
        }
        if(menu.equals("Check for Updates"))
        {
            StringItem info1 = new StringItem("What does this do","This Menu searches for a newer version of BunkM. An internet connection is required to check for an update.");
            StringItem info2 = new StringItem("Download Latest Version","If a newer version of BunkM is available the Check option will turn into Download. You can then click the Download option to Download the latest version of BunkM straight from your phone.");
            StringItem info3 = new StringItem("Tip","Newer Versions have New Features and bug-fixes so check for updates at least once a week to ensure that you have the latest version for the best user experience.");
            StringItem info4 = new StringItem("Caution","During installation of the latest version, you might be asked if you wish to retain the previous application data. Choose YES or else you will lose all settings and attendance information.");
            help.append(info1);
            help.append(info2);
            help.append(info3);
            help.append(info4);
            help.setTitle("Help - Update Checker");
        }
        if(menu.equals("About"))
        {
            StringItem info1 = new StringItem("About","This Menu displays all information about the BunkM application.");
            StringItem info2 = new StringItem("Application Home Page","Visit www.sanchitkarve.com/projects/bunkm for more information.");
            help.append(info1);
            help.append(info2);
            help.setTitle("Help - About BunkM");
        }
    }
}

class UpProcess implements Runnable
{
    BunkMIDlet bm;
    
    public UpProcess(BunkMIDlet bm)
    {
        this.bm = bm;
    }
    
    public void run()
    {
        try
        {
            receiveText();
        }
        catch(Exception e)
        {
            System.out.println("Download Error" + e.getMessage());
        }
    }
    
    public void start()
    {
        Thread t = new Thread(this);
        try
        {
            t.start();        
        }
        catch(Exception e)
        {            
        }
    }
    
    private void receiveText() throws IOException
    {
        StringBuffer b = new StringBuffer();
        InputStream is = null;
	HttpConnection c = null;
	
	try
        {
	    long len = 0 ;
	    int ch = 0;

            c = (HttpConnection)Connector.open("http://www.sanchitkarve.com/projects/bunkm/update.txt");
            is = c.openInputStream();

	    len = c.getLength() ;
	    if ( len != -1)
            {
		// Read exactly Content-Length bytes
   		for (int i =0 ; i < len ; i++ )
		    if ((ch = is.read()) != -1)
			b.append((char) ch);
	    }
            else
            {
                // Read till the connection is closed.
		while ((ch = is.read()) != -1) {
                    len = is.available() ;
		    b.append((char)ch);
		}
	    }
            //stuff
            String updText = b.toString();
            int start=0,end=updText.indexOf(';');
            String ver = updText.substring(start, end);
            start = end+1;
            end = updText.indexOf(';',start);
            String desc =updText.substring(start, end);
            String url = updText.substring(end+1);
            
            StringItem newVer = new StringItem("Latest Version",ver);
            StringItem newDesc = new StringItem("Description",desc);            
            
            if(!bm.getAppProperty("MIDlet-Version").equals(ver))
            {
                bm.frmUpdate.append(newVer);
                bm.frmUpdate.append(newDesc);
                bm.updURL = url;
                bm.frmUpdate.removeCommand(bm.cmdCheck);
                bm.cmdDownload = new Command("Download",Command.OK,0);
                bm.frmUpdate.addCommand(bm.cmdDownload);
            }
            else
            {
                bm.frmUpdate.deleteAll();
                bm.frmUpdate.append("You have the latest version.");
            }
        }
        finally
        {
           is.close();
           c.close();
        }
	
    }
}

class StatProcess implements Runnable
{
    BunkMIDlet bm;
    
    public StatProcess(BunkMIDlet bm)
    {
        this.bm = bm;
    }
    
    public void run()
    {
        try
        {
            populateStats();
        }
        catch(Exception e)
        {
            System.out.println("Stats override Error : " + e.getMessage());
        }
    }
    
    public void start()
    {
        Thread t = new Thread(this);
        try
        {
            t.start();        
        }
        catch(Exception e)
        {            
        }
    }
    
    private void populateStats()
    {
        int progTotalDates=0;
        int progCounter=0;
        Date from = bm.dtFrom.getDate();
        Date to = bm.dtTo.getDate();
        Settings.open();
        int[][] stats = new int[Settings.getNumberOfSubjects()][4]; //0-Lects Attended 1-Lec Taken 2-Prac Att 3-Prac Taken
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
        int recFrom = Attendance.getDateDifferenceInDays(dloc, cStart.getTime())+1;
        cd.setTime(to);
        cd.set(Calendar.HOUR_OF_DAY, cStart.get(Calendar.HOUR_OF_DAY));
        cd.set(Calendar.MINUTE, cStart.get(Calendar.MINUTE));
        cd.set(Calendar.SECOND, cStart.get(Calendar.SECOND));
        cd.set(Calendar.MILLISECOND, cStart.get(Calendar.MILLISECOND));
        dloc = cd.getTime();
        int recTo = Attendance.getDateDifferenceInDays(dloc, cStart.getTime())+1;
        progTotalDates = recTo - recFrom + 1;
        String atLine;
        int[] lect;
        int[] pract;
        int[] extra;
        byte[] b = null;
        for(int x=0;x<numSubs;x++)
        {
            for(int y=0;y<4;y++)
            {
                stats[x][y]=0;
            }
        }
        Attendance.open();
        for(int i=recFrom;i<=recTo;i++)
        {
            b = Attendance.getRecord(i);

            atLine = new String(b);
            lect = Attendance.getLectFromAttByte(atLine);
            pract = Attendance.getPractFromAttByte(atLine);
            extra = Attendance.getExtraFromAttByte(atLine);
            
            
            for(int j=0;j<lect.length;j++)
            {
                //attended = 1, bunked = 2, not taken = 0
                if(lect[j] == 1)
                {
                    stats[j][0]++; //lectures attended
                    stats[j][1]++; //lectures taken
                }
                else if(lect[j] == 2)
                {
                    stats[j][1]++;
                }
                
                if(extra[j] == 1)
                {
                    stats[j][0]++; //lectures attended
                    stats[j][1]++; //lectures taken
                }
                else if(extra[j] == 2)
                {
                    stats[j][1]++;
                }
                
                if(pract[j] == 1)
                {
                    stats[j][2]++; //practs attended
                    stats[j][3]++; //practs taken
                }
                else if(pract[j] == 2)
                {
                    stats[j][3]++;
                }
            }
            progCounter++;
            int val = progCounter * 100 / progTotalDates;
            bm.gProgress.setValue(val);
        }
        Attendance.close();
        bm.statsCv = new StatsCanvas(from,to,stats);
        bm.cmdBackStatRange = new Command("Back",Command.BACK,0);
        bm.statsCv.addCommand(bm.cmdBackStatRange);
        bm.statsCv.setCommandListener(bm);
        Display d = Display.getDisplay(bm);
        d.setCurrent(bm.statsCv);        
    }    
}

class UploadDataProcess implements Runnable
{
    BunkMIDlet bm;
    String sData;
    String settingsData;

    public UploadDataProcess(BunkMIDlet bm,String d,String s)
    {
        this.bm = bm;
        this.sData=d;
        this.settingsData=s;
    }

    public void run()
    {
        //try
        //{
            //sendData();
            bm.tmp = sendPostRequest("http://www.sanchitkarve.com/projects/bunkm/updata.php");
        //}
        //catch(IOException e)
        //{
            //System.out.println("Download Error" + e.getMessage());
        //}
    }

    public void start()
    {
        Thread t = new Thread(this);
        try
        {
            t.start();
        }
        catch(Exception e)
        {
        }
    }

    private void sendData() throws IOException
    {
        HttpConnection c = (HttpConnection) Connector.open("http://www.sanchitkarve.com/projects/bunkm/updata.php");
        c.setRequestMethod(HttpConnection.POST);
        // This is a sample
        Settings.open();
        String userName = Settings.getUserName();
        String password = Settings.getPassword();
        Settings.close();

        byte plain[] = password.getBytes();
        MD5 md5 = new MD5(plain); // create MD5 object
        byte[] result = md5.doFinal(); //get the resulting hashed byte
        //convert the hashed byte into hexadecimal character for display
        String MD5HashResult = MD5.toHex(result);
        String strData = "userName=" + userName + "&password=" + MD5HashResult + "&attendanceData="+sData + "&settingsData="+settingsData;
        
        byte[] data = strData.getBytes();
        // data should be filled with binary data to send
        c.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        c.setRequestProperty("Content-Length", Integer.toString(data.length));

        OutputStream sending = c.openOutputStream();
        sending.write(data);
        sending.close();
    }

    public String sendPostRequest(String urlstring)
    {
	HttpConnection hc = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;

	String message = "";

        Settings.open();
        String userName = Settings.getUserName();
        String password = Settings.getPassword();
        Settings.close();

        byte plain[] = password.getBytes();
        MD5 md5 = new MD5(plain); // create MD5 object
        byte[] result = md5.doFinal(); //get the resulting hashed byte
        //convert the hashed byte into hexadecimal character for display
        String MD5HashResult = MD5.toHex(result);
        String strData = "userName=" + userName + "&password=" + MD5HashResult + "&attendanceData="+sData+ "&settingsData="+settingsData;

	// specifying the query string
	String requeststring = strData; //"request=gettimestamp";
	try
	{
		// openning up http connection with the web server
		// for both read and write access
		hc = (HttpConnection) Connector.open(urlstring, Connector.READ_WRITE);

		// setting the request method to POST
		hc.setRequestMethod(HttpConnection.POST);
                hc.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hc.setRequestProperty("Content-Length", Integer.toString(requeststring.getBytes().length));

		// obtaining output stream for sending query string
		dos = hc.openDataOutputStream();
		byte[] request_body = requeststring.getBytes();

		// sending query string to web server
		for (int i = 0; i < request_body.length; i++)
		{
			dos.writeByte(request_body[i]);
		}
		// flush outdos.flush();

		// obtaining input stream for receiving HTTP response
		dis = new DataInputStream(hc.openInputStream());

		// reading the response from web server character by character
		int ch;
		while ((ch = dis.read()) != -1)
		{
			message = message + (char) ch;
		}

	}
	catch (IOException ioe)
	{
		message = "ERROR";
	}
	finally
	{
		// freeing up i/o streams and http connection
		try
		{
			if (hc != null)
				hc.close();
		}
		catch (IOException ignored)
		{
		}
		try
		{
			if (dis != null)
				dis.close();
		}
		catch (IOException ignored)
		{
		}
		try
		{
			if (dos != null)
				dos.close();
		}
		catch (IOException ignored)
		{
		}
	}
	return message;
    }
}
class DownloadDataProcess implements Runnable
{
    BunkMIDlet bm;
    String sData;
    String settingsData;
    boolean att;

    public DownloadDataProcess(BunkMIDlet bm, boolean attendance)
    {
        this.bm = bm;
        //this.sData=d;
        //this.settingsData=s;
        att=attendance;
    }

    public void run()
    {
        //try
        //{
            //sendData();
            bm.tmp = sendPostRequest("http://www.sanchitkarve.com/projects/bunkm/downdata.php");
        //}
        //catch(IOException e)
        //{
            //System.out.println("Download Error" + e.getMessage());
        //}
    }

    public void start()
    {
        Thread t = new Thread(this);
        try
        {
            t.start();
        }
        catch(Exception e)
        {
        }
    }

    private void sendData() throws IOException
    {
        HttpConnection c = (HttpConnection) Connector.open("http://www.sanchitkarve.com/projects/bunkm/updata.php");
        c.setRequestMethod(HttpConnection.POST);
        // This is a sample
        Settings.open();
        String userName = Settings.getUserName();
        String password = Settings.getPassword();
        Settings.close();

        byte plain[] = password.getBytes();
        MD5 md5 = new MD5(plain); // create MD5 object
        byte[] result = md5.doFinal(); //get the resulting hashed byte
        //convert the hashed byte into hexadecimal character for display
        String MD5HashResult = MD5.toHex(result);
        String strData = "userName=" + userName + "&password=" + MD5HashResult + "&attendanceData="+sData + "&settingsData="+settingsData;

        byte[] data = strData.getBytes();
        // data should be filled with binary data to send
        c.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        c.setRequestProperty("Content-Length", Integer.toString(data.length));

        OutputStream sending = c.openOutputStream();
        sending.write(data);
        sending.close();
    }

    public String sendPostRequest(String urlstring)
    {
	HttpConnection hc = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;

	String message = "";

        Settings.open();
        String userName = Settings.getUserName();
        String password = Settings.getPassword();
        Settings.close();

        byte plain[] = password.getBytes();
        MD5 md5 = new MD5(plain); // create MD5 object
        byte[] result = md5.doFinal(); //get the resulting hashed byte
        //convert the hashed byte into hexadecimal character for display
        String MD5HashResult = MD5.toHex(result);
        String attOrSettings = "";
        attOrSettings = att == true ? "attendance" : "settings";

        String strData = "userName=" + userName + "&password=" + MD5HashResult + "&request="+attOrSettings;

	// specifying the query string
	String requeststring = strData; //"request=gettimestamp";
	try
	{
		// openning up http connection with the web server
		// for both read and write access
		hc = (HttpConnection) Connector.open(urlstring, Connector.READ_WRITE);

		// setting the request method to POST
		hc.setRequestMethod(HttpConnection.POST);
                hc.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hc.setRequestProperty("Content-Length", Integer.toString(requeststring.getBytes().length));

		// obtaining output stream for sending query string
		dos = hc.openDataOutputStream();
		byte[] request_body = requeststring.getBytes();

		// sending query string to web server
		for (int i = 0; i < request_body.length; i++)
		{
			dos.writeByte(request_body[i]);
		}
		// flush outdos.flush();

		// obtaining input stream for receiving HTTP response
		dis = new DataInputStream(hc.openInputStream());

		// reading the response from web server character by character
		int ch;
		while ((ch = dis.read()) != -1)
		{
			message = message + (char) ch;
		}

	}
	catch (IOException ioe)
	{
		message = "ERROR";
	}
	finally
	{
		// freeing up i/o streams and http connection
		try
		{
			if (hc != null)
				hc.close();
		}
		catch (IOException ignored)
		{
		}
		try
		{
			if (dis != null)
				dis.close();
		}
		catch (IOException ignored)
		{
		}
		try
		{
			if (dos != null)
				dos.close();
		}
		catch (IOException ignored)
		{
		}
	}
	return message;
    }
}
/*
class ExportProcess implements Runnable
{
    BunkMIDlet bm;
    
    public ExportProcess(BunkMIDlet bm)
    {
        this.bm = bm;
    }
    
    public void run()
    {
        try
        {
            export();
        }
        catch(Exception e)
        {
            System.out.println("Download Error" + e.getMessage());
        }
    }
    
    public void start()
    {
        Thread t = new Thread(this);
        try
        {
            t.start();        
        }
        catch(Exception e)
        {            
        }
    }
    
    public void export()
    {
        boolean set=false,att=true;
        set = BackupRestoreFile.exportData(true);
        //att = BackupRestoreFile.exportData(false);
        if(set == true && att==true)
            bm.alert("Success","Settings and Attendance Successfully exported.",AlertType.CONFIRMATION,Alert.FOREVER,null);
        else
            bm.alert("Failure","Settings and/or Attendance could not be exported.",AlertType.ERROR,Alert.FOREVER,null);
        
    }
}*/