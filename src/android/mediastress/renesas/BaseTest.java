package android.mediastress.renesas;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.fail;

/**
 * Created by artem on 11/17/16.
 */
public abstract class BaseTest {
	Instrumentation inst;
	public String packageName;

	public static String BASE_PATH_VIDEO = "/sdcard/test/rvideo/";
	public static String BASE_PATH_AUDIO = "/sdcard/test/raudio/";
	public static String BASE_PATH_IMAGE = "/sdcard/test/rimage/";
	public static String BASE_PATH_URL = "/sdcard/test/urls.txt";
	public static String BASE_PATH_APP = "/sdcard/test/rapk/";
	public static String BASE_PATH_CONFIG = "/sdcard/test/config.txt";
	public static String MD5_CHECK_FILE = "/sdcard/test/md5sum.txt";

	public String TAG = "BaseTest";
	String ASSERT_MESSAGE_PREFIX;
	MainActivity activity;
	String fileName;

	public abstract void run();


	public void initRandom(Instrumentation inst, String msgPrefix, String pathToFiles){
		ASSERT_MESSAGE_PREFIX=msgPrefix;
		this.inst = inst;
		List<File> listFiles=getListFiles(new File(pathToFiles));
		assertNotNull(listFiles);
		int size = listFiles.size();
		int rNumber = new Random().nextInt(size-1);
		File file = listFiles.get(rNumber);
		fileName=file.getAbsolutePath();
		String samplePrefix= fileName.substring(fileName.lastIndexOf("/")+1);
		setAssertSamplePrefix(samplePrefix);
		Log.w(TAG,ASSERT_MESSAGE_PREFIX);
	}

	public void initTestWithFile(Instrumentation inst, String pathToFiles, String sampleFileName){
		if(pathToFiles.contains("video"))this.ASSERT_MESSAGE_PREFIX="TEST_VIDEO";
		else if(pathToFiles.contains("audio"))this.ASSERT_MESSAGE_PREFIX="TEST_AUDIO";
		else if(pathToFiles.contains("image"))this.ASSERT_MESSAGE_PREFIX="TEST_IMAGE";
		else this.ASSERT_MESSAGE_PREFIX="TEST";
		this.inst = inst;
		File file = new File(pathToFiles+sampleFileName);
		fileName=file.getAbsolutePath();
		setAssertSamplePrefix(sampleFileName);
		Log.w(TAG,ASSERT_MESSAGE_PREFIX);
	}

	public void initBase(Instrumentation inst,  String msgPrefix){
		this.ASSERT_MESSAGE_PREFIX=msgPrefix;
		this.inst = inst;
	}

	public void launchExternalIntent(final Intent intent){
		try {
			inst.runOnMainSync(new Runnable() {
				@Override
				public void run() {
					assertNotNull(ASSERT_MESSAGE_PREFIX+"getLaunchIntentForPackage error. app not installed",intent);
					String homeLauncherPackage=inst.getUiAutomation().getRootInActiveWindow().getPackageName().toString();
					String testPackage=intent.getPackage();
										inst.getTargetContext().startActivity(intent);

					SystemClock.sleep(5000);

					String currentPackage=inst.getUiAutomation().getRootInActiveWindow().getPackageName().toString();

					Log.w("homeLauncherPackage: ", ""+ homeLauncherPackage);
					Log.w("testPackage: ",""+ testPackage);
					Log.w("currentPackage: ", ""+ currentPackage);


					if(testPackage==null){
						assertNotSame("current package not changed. test app not opened", homeLauncherPackage, currentPackage);
					}else {
						assertEquals("current package differ from expected. test app not opened", testPackage, currentPackage);
					}
				}
			});
		}catch (ActivityNotFoundException e){
			fail(ASSERT_MESSAGE_PREFIX+"ERROR ActivityNotFoundException " +packageName);
		}
	}

	public int getRandomSeconds(){
		Random random= new Random();
		return random.nextInt(6)-2;
	}

	public MainActivity launchMainActivity(){
		Intent intent = new Intent();
		intent.setClass(inst.getTargetContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
		//return activity;
		return  (MainActivity) inst.startActivitySync(intent);
	}


	static List<File> getListFiles(File parentDir) {
		ArrayList<File> inFiles = new ArrayList<File>();
		File[] files = parentDir.listFiles();
		for (File file : files) {
			inFiles.add(file);
		}
		return inFiles;
	}

	public void setTitle(final Activity act, final String fileName) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int lastSlash=fileName.lastIndexOf("/")+1;
				String title=ASSERT_MESSAGE_PREFIX;
				//if(lastSlash>0)	title+=fileName.substring(lastSlash);
				//else title+=fileName;
				act.setTitle(title);
			}
		});
	}
	private void showToast(final Activity act,final String text) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(act, text, Toast.LENGTH_SHORT).show();
			}
		});
	}
	public ArrayList<String> readFile(final String path) throws IOException {
		ArrayList<String>strLineList=new ArrayList<>();
		String strLine;
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
			while ((strLine = reader.readLine()) != null) {
				strLineList.add(strLine);
			}
		} catch (final IOException e) {
			fail(""+ e.getMessage());
		}
		return strLineList;
	}


	public void setAssertSamplePrefix(String samplePrefix){
		ASSERT_MESSAGE_PREFIX += "[ "+samplePrefix + " ] ";
		Log.w("testRandom", ASSERT_MESSAGE_PREFIX + "started");
	}

	public MainActivity killLaunchMainActivity(Activity activity){
		if(activity!=null) {
			inst.callActivityOnPause(activity);
			inst.callActivityOnStop(activity);
			inst.callActivityOnDestroy(activity);
		}
		Intent intent = new Intent();
		intent.setClass(inst.getTargetContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP );

		return  (MainActivity) inst.startActivitySync(intent);
	}

}
