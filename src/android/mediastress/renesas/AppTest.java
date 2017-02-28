package android.mediastress.renesas;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by artem on 11/17/16.
 */
public class AppTest extends BaseTest{
	public AppTest(Instrumentation inst) {
		TAG = "AppTest";
		initBase(inst, "TEST_APP");
	}
	public AppTest(Instrumentation inst, String testFileName) {
		TAG = "AppTest";
		initBase(inst, "TEST_APP");
		fileName=testFileName;
	}


	@Override
	public void run(){
		if(fileName==null)fileName = getRandom3dAppFileName(inst);
		setAssertSamplePrefix(fileName);
		Log.w(TAG,fileName);
		String filePath;
		if(fileName.contains("/")){
			filePath=fileName;
		}else {
			filePath=BASE_PATH_APP+fileName;
		}
		String appName=getAppNameFromFilePath(filePath);
		boolean isInstalled = checkInstall(inst, appName);
		if(!isInstalled){
			Log.w(TAG, "app not installed. Will install");
			installApp(inst,filePath);
			isInstalled = checkInstall(inst, appName);
			assertTrue(ASSERT_MESSAGE_PREFIX+filePath+" install error", isInstalled);
		}
		packageName=getPackageNameFromAppName(appName);
		assertNotNull(ASSERT_MESSAGE_PREFIX, packageName);
		Intent intent=inst.getTargetContext()
				.getPackageManager().getLaunchIntentForPackage(packageName);



		// workaround for issue:
		// OGLES apps crash if opened when already running in background
		if(packageName.contains("OGLES")) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);




		launchExternalIntent(intent);
	}
	public String getAppNameFromFilePath( String filePath){
		String appName=filePath.substring(0, filePath.indexOf(".apk"));
		int indexSlash=appName.lastIndexOf("/")+1;
		if(indexSlash>0 && indexSlash<appName.length()-2){
			appName=filePath.substring(indexSlash, appName.length());
		}
		assertNotNull(ASSERT_MESSAGE_PREFIX+"getAppNameFromFilePath failed",appName);
		return appName;
	}
	public String getPackageNameFromAppName( String appName){
		ParcelFileDescriptor fileDescriptor =  inst.getUiAutomation().executeShellCommand("pm list packages ");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDescriptor.getFileDescriptor())));
		String line;
		String packageName=null;
		try {
			while ((line = reader.readLine()) != null) {
				if(line.contains(appName)){
					packageName=line.substring(line.lastIndexOf(":")+1);
					//Log.w(TAG, "line="+line);
					//Log.w(TAG, "packageName="+packageName);
					break;
				}
			}
		} catch (IOException e) {
			fail(ASSERT_MESSAGE_PREFIX+e.getMessage());
		}
		assertNotNull(ASSERT_MESSAGE_PREFIX+"getPackageNameFromAppName failed",packageName);
		return packageName;
	}

	public boolean checkInstall(Instrumentation inst, String appName){
		Log.w(TAG, "appName="+appName);
		ParcelFileDescriptor fileDescriptor =  inst.getUiAutomation().executeShellCommand("pm list packages ");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDescriptor.getFileDescriptor())));
		String line;
		String packageName=null;
		try {
			while ((line = reader.readLine()) != null) {
				if(line.contains(appName)){
					packageName=line.substring(line.lastIndexOf(":")+1);
					//Log.w(TAG, "line="+line);
					Log.w(TAG, "packageName="+packageName);
					break;
				}
			}
		} catch (IOException e) {
			fail(ASSERT_MESSAGE_PREFIX+e.getMessage());
		}
			return !(packageName==null);
	}
	public String installApp(Instrumentation inst, String absoluteFilePath){
		String appName=getAppNameFromFilePath(absoluteFilePath);
		Log.w("installApp", "install "+appName+", filePath "+absoluteFilePath);
		ParcelFileDescriptor fileDescriptor =  inst.getUiAutomation().executeShellCommand("pm install -r "+absoluteFilePath);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDescriptor.getFileDescriptor())));
		String line;
		String res="";
		try {
			while ((line = reader.readLine()) != null) {
				res+=line;
			}
		} catch (IOException e) {
			fail(ASSERT_MESSAGE_PREFIX+e.getMessage());
		}
		assertTrue(ASSERT_MESSAGE_PREFIX+res,res.length()>0);
		assertTrue(ASSERT_MESSAGE_PREFIX+res,res.contains("Success"));

		fileDescriptor =  inst.getUiAutomation().executeShellCommand("pm list packages ");
		reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDescriptor.getFileDescriptor())));
		try {
			while ((line = reader.readLine()) != null) {
				if(line.contains(appName))
					return line;
			}
		} catch (IOException e) {
			fail(ASSERT_MESSAGE_PREFIX+e.getMessage());
		}
		return null;
	}


	public String getRandom3dAppFileName(Instrumentation inst){
		ParcelFileDescriptor fileDescriptor =  inst.getUiAutomation().executeShellCommand("ls "+BASE_PATH_APP);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDescriptor.getFileDescriptor())));
		ArrayList<String>imeList=new ArrayList<>();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				//Log.w(TAG, "line:"+line);
				if(line.contains(".apk"))
					imeList.add(line);
			}
		} catch (IOException e) {
			fail(ASSERT_MESSAGE_PREFIX+e.getMessage());
		}
		assertTrue(ASSERT_MESSAGE_PREFIX+imeList, imeList.size()>0);
		//for(String s: imeList) Log.w(TAG, "parsed FileName: "+ s);
		int random=new Random().nextInt(imeList.size());
		return imeList.get(random);
	}
}
