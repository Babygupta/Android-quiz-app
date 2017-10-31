package androidquiz.b4a;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	private static final boolean fullScreen = false;
	private static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "androidquiz.b4a", "main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                anywheresoftware.b4a.keywords.Common.Log("Killing previous instance (main).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
		activityBA = new BA(this, layout, processBA, "androidquiz.b4a", "main");
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        initializeProcessGlobals();		
        initializeGlobals();
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (mostCurrent == null || mostCurrent != this)
			return;
        processBA.setActivityPaused(false);
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            anywheresoftware.b4a.keywords.Common.Log("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdq3 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdq2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdq4 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdq5 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _cmdresult = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _a4 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _a3 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _a2 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper _a1 = null;
public static String _ans = "";
public static int _correct = 0;
public anywheresoftware.b4a.objects.LabelWrapper _lblresult = null;
public static String  _a1_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 95;BA.debugLine="Sub a1_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 97;BA.debugLine="If a1.Checked = True Then";
if (mostCurrent._a1.getChecked()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 98;BA.debugLine="cmdQ3.Enabled=True";
mostCurrent._cmdq3.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 99;BA.debugLine="cmdQ2.Enabled=True";
mostCurrent._cmdq2.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 100;BA.debugLine="cmdQ4.Enabled=True";
mostCurrent._cmdq4.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 101;BA.debugLine="cmdQ5.Enabled=True";
mostCurrent._cmdq5.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 102;BA.debugLine="cmdResult.Enabled=True";
mostCurrent._cmdresult.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 103;BA.debugLine="ans=a1.Text";
mostCurrent._ans = mostCurrent._a1.getText();
 };
 //BA.debugLineNum = 105;BA.debugLine="End Sub";
return "";
}
public static String  _a2_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 106;BA.debugLine="Sub a2_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 107;BA.debugLine="If a2.Checked = True Then";
if (mostCurrent._a2.getChecked()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 108;BA.debugLine="cmdQ3.Enabled=True";
mostCurrent._cmdq3.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 109;BA.debugLine="cmdQ2.Enabled=True";
mostCurrent._cmdq2.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 110;BA.debugLine="cmdQ4.Enabled=True";
mostCurrent._cmdq4.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 111;BA.debugLine="cmdQ5.Enabled=True";
mostCurrent._cmdq5.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 112;BA.debugLine="cmdResult.Enabled=True";
mostCurrent._cmdresult.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 113;BA.debugLine="ans=a2.Text";
mostCurrent._ans = mostCurrent._a2.getText();
 };
 //BA.debugLineNum = 116;BA.debugLine="End Sub";
return "";
}
public static String  _a3_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 117;BA.debugLine="Sub a3_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 118;BA.debugLine="If a3.Checked = True Then";
if (mostCurrent._a3.getChecked()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 119;BA.debugLine="cmdQ3.Enabled=True";
mostCurrent._cmdq3.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 120;BA.debugLine="cmdQ2.Enabled=True";
mostCurrent._cmdq2.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 121;BA.debugLine="cmdQ4.Enabled=True";
mostCurrent._cmdq4.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 122;BA.debugLine="cmdQ5.Enabled=True";
mostCurrent._cmdq5.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 123;BA.debugLine="cmdResult.Enabled=True";
mostCurrent._cmdresult.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 124;BA.debugLine="ans=a3.Text";
mostCurrent._ans = mostCurrent._a3.getText();
 };
 //BA.debugLineNum = 126;BA.debugLine="End Sub";
return "";
}
public static String  _a4_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 127;BA.debugLine="Sub a4_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 128;BA.debugLine="If a4.Checked = True Then";
if (mostCurrent._a4.getChecked()==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 129;BA.debugLine="cmdQ3.Enabled=True";
mostCurrent._cmdq3.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 130;BA.debugLine="cmdQ2.Enabled=True";
mostCurrent._cmdq2.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 131;BA.debugLine="cmdQ4.Enabled=True";
mostCurrent._cmdq4.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 132;BA.debugLine="cmdQ5.Enabled=True";
mostCurrent._cmdq5.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 133;BA.debugLine="cmdResult.Enabled=True";
mostCurrent._cmdresult.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 134;BA.debugLine="ans=a4.Text";
mostCurrent._ans = mostCurrent._a4.getText();
 };
 //BA.debugLineNum = 136;BA.debugLine="End Sub";
return "";
}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 28;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 29;BA.debugLine="Activity.LoadLayout(\"main\")";
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 //BA.debugLineNum = 30;BA.debugLine="cmdQ3.Initialize(\"\")";
mostCurrent._cmdq3.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 31;BA.debugLine="cmdQ2.Initialize(\"\")";
mostCurrent._cmdq2.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 32;BA.debugLine="cmdQ4.Initialize(\"\")";
mostCurrent._cmdq4.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 33;BA.debugLine="cmdQ5.Initialize(\"\")";
mostCurrent._cmdq5.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 34;BA.debugLine="cmdResult.Initialize(\"\")";
mostCurrent._cmdresult.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 35;BA.debugLine="correct=0";
_correct = (int)(0);
 //BA.debugLineNum = 37;BA.debugLine="End Sub";
return "";
}
public static String  _cmdhome_click() throws Exception{
 //BA.debugLineNum = 90;BA.debugLine="Sub cmdHome_Click";
 //BA.debugLineNum = 91;BA.debugLine="correct=0";
_correct = (int)(0);
 //BA.debugLineNum = 92;BA.debugLine="RemoveView";
_removeview();
 //BA.debugLineNum = 93;BA.debugLine="Activity.LoadLayout(\"main\")";
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 //BA.debugLineNum = 94;BA.debugLine="End Sub";
return "";
}
public static String  _cmdplay_click() throws Exception{
 //BA.debugLineNum = 38;BA.debugLine="Sub cmdPlay_Click";
 //BA.debugLineNum = 39;BA.debugLine="RemoveView";
_removeview();
 //BA.debugLineNum = 40;BA.debugLine="Activity.LoadLayout(\"q1\")";
mostCurrent._activity.LoadLayout("q1",mostCurrent.activityBA);
 //BA.debugLineNum = 41;BA.debugLine="End Sub";
return "";
}
public static String  _cmdq2_click() throws Exception{
 //BA.debugLineNum = 50;BA.debugLine="Sub cmdQ2_Click";
 //BA.debugLineNum = 53;BA.debugLine="If ans=\" Java\" Then";
if ((mostCurrent._ans).equals(" Java")) { 
 //BA.debugLineNum = 54;BA.debugLine="correct = correct+1";
_correct = (int)(_correct+1);
 };
 //BA.debugLineNum = 56;BA.debugLine="RemoveView";
_removeview();
 //BA.debugLineNum = 57;BA.debugLine="Activity.LoadLayout(\"q2\")";
mostCurrent._activity.LoadLayout("q2",mostCurrent.activityBA);
 //BA.debugLineNum = 58;BA.debugLine="End Sub";
return "";
}
public static String  _cmdq3_click() throws Exception{
 //BA.debugLineNum = 59;BA.debugLine="Sub cmdQ3_Click";
 //BA.debugLineNum = 60;BA.debugLine="If ans=\" Mobile OS\" Then";
if ((mostCurrent._ans).equals(" Mobile OS")) { 
 //BA.debugLineNum = 61;BA.debugLine="correct = correct+1";
_correct = (int)(_correct+1);
 };
 //BA.debugLineNum = 63;BA.debugLine="RemoveView";
_removeview();
 //BA.debugLineNum = 64;BA.debugLine="Activity.LoadLayout(\"q3\")";
mostCurrent._activity.LoadLayout("q3",mostCurrent.activityBA);
 //BA.debugLineNum = 65;BA.debugLine="End Sub";
return "";
}
public static String  _cmdq4_click() throws Exception{
 //BA.debugLineNum = 66;BA.debugLine="Sub cmdQ4_Click";
 //BA.debugLineNum = 67;BA.debugLine="If ans=\" Linux\" Then";
if ((mostCurrent._ans).equals(" Linux")) { 
 //BA.debugLineNum = 68;BA.debugLine="correct = correct+1";
_correct = (int)(_correct+1);
 };
 //BA.debugLineNum = 70;BA.debugLine="RemoveView";
_removeview();
 //BA.debugLineNum = 71;BA.debugLine="Activity.LoadLayout(\"q4\")";
mostCurrent._activity.LoadLayout("q4",mostCurrent.activityBA);
 //BA.debugLineNum = 72;BA.debugLine="End Sub";
return "";
}
public static String  _cmdq5_click() throws Exception{
 //BA.debugLineNum = 73;BA.debugLine="Sub cmdQ5_Click";
 //BA.debugLineNum = 74;BA.debugLine="If ans=\" Android Inc.\" Then";
if ((mostCurrent._ans).equals(" Android Inc.")) { 
 //BA.debugLineNum = 75;BA.debugLine="correct = correct+1";
_correct = (int)(_correct+1);
 };
 //BA.debugLineNum = 77;BA.debugLine="RemoveView";
_removeview();
 //BA.debugLineNum = 78;BA.debugLine="Activity.LoadLayout(\"q5\")";
mostCurrent._activity.LoadLayout("q5",mostCurrent.activityBA);
 //BA.debugLineNum = 79;BA.debugLine="End Sub";
return "";
}
public static String  _cmdresult_click() throws Exception{
 //BA.debugLineNum = 80;BA.debugLine="Sub cmdResult_Click";
 //BA.debugLineNum = 81;BA.debugLine="RemoveView";
_removeview();
 //BA.debugLineNum = 82;BA.debugLine="Activity.LoadLayout(\"result\")";
mostCurrent._activity.LoadLayout("result",mostCurrent.activityBA);
 //BA.debugLineNum = 83;BA.debugLine="If ans=\" Google\" Then";
if ((mostCurrent._ans).equals(" Google")) { 
 //BA.debugLineNum = 84;BA.debugLine="correct = correct+1";
_correct = (int)(_correct+1);
 };
 //BA.debugLineNum = 87;BA.debugLine="lblresult.Text=\"You got \" &correct& \" correct answer.\"";
mostCurrent._lblresult.setText((Object)("You got "+BA.NumberToString(_correct)+" correct answer."));
 //BA.debugLineNum = 89;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (processGlobalsRun == false) {
	    processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 8;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 12;BA.debugLine="Dim cmdQ3 As Button";
mostCurrent._cmdq3 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 13;BA.debugLine="Dim cmdQ2 As Button";
mostCurrent._cmdq2 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 14;BA.debugLine="Dim cmdQ4 As Button";
mostCurrent._cmdq4 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 15;BA.debugLine="Dim cmdQ5 As Button";
mostCurrent._cmdq5 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 16;BA.debugLine="Dim cmdResult As Button";
mostCurrent._cmdresult = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 17;BA.debugLine="Dim a4 As RadioButton";
mostCurrent._a4 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 18;BA.debugLine="Dim a3 As RadioButton";
mostCurrent._a3 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 19;BA.debugLine="Dim a2 As RadioButton";
mostCurrent._a2 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 20;BA.debugLine="Dim a1 As RadioButton";
mostCurrent._a1 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.RadioButtonWrapper();
 //BA.debugLineNum = 22;BA.debugLine="Dim ans As String";
mostCurrent._ans = "";
 //BA.debugLineNum = 23;BA.debugLine="Dim correct As Int";
_correct = 0;
 //BA.debugLineNum = 25;BA.debugLine="Dim lblresult As Label";
mostCurrent._lblresult = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 26;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 2;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 6;BA.debugLine="End Sub";
return "";
}
public static String  _removeview() throws Exception{
int _i = 0;
 //BA.debugLineNum = 42;BA.debugLine="Sub RemoveView";
 //BA.debugLineNum = 43;BA.debugLine="Dim i As Int";
_i = 0;
 //BA.debugLineNum = 45;BA.debugLine="For i = Activity.NumberOfViews-1 To 0 Step - 1";
{
final double step31 = -1;
final double limit31 = (int)(0);
for (_i = (int)(mostCurrent._activity.getNumberOfViews()-1); (step31 > 0 && _i <= limit31) || (step31 < 0 && _i >= limit31); _i += step31) {
 //BA.debugLineNum = 46;BA.debugLine="Activity.RemoveViewAt(i)";
mostCurrent._activity.RemoveViewAt(_i);
 }
};
 //BA.debugLineNum = 49;BA.debugLine="End Sub";
return "";
}
}
