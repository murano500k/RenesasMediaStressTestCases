package android.mediastress.renesas;

import android.app.Instrumentation;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Created by artem on 12/14/16.
 */

public class FileTest extends BaseTest {
	public static String MD5_TEST_FILE= "/sdcard/test/rvideo/AV_001097_1080p_crowdrun_HP_cabac_2B_wBpred_adct_30fps_20mbps_1000fr_AAC_HE_48kHz_64kbps_stereo.mp4";
	public static String MD5_TEST_SUM= "a31327325bede22b8b55025e6b086dae";
	private String md5TestFile;
	private String md5Expected;

	public FileTest(Instrumentation inst, String testFile, String testMd5) {
		prepare(testFile, testMd5);
		initBase(inst, "TEST_FS");
	}
	public FileTest(Instrumentation inst) {
		prepare();
		initBase(inst, "TEST_FS");
	}

	public void prepare() {
		prepare(MD5_TEST_FILE, MD5_TEST_SUM);
	}

	private void prepare(String md5File, String Md5Sum) {
		md5TestFile=md5File;
		md5Expected = Md5Sum;
	}


	@Override
	public void run() {
		testFsSingleFile();
	}
	private void testFsSingleFile() {
		String md5Actual= null;
		File testFile=new File(md5TestFile);
		if(!testFile.isDirectory() && testFile.exists()) md5Actual=getMd5OfFile(md5TestFile).toLowerCase();
		Log.w(TAG, "md5Expected="+md5Expected);
		Log.w(TAG, "md5Actual="+md5Actual);
		Assert.assertNotNull("md5Expected",md5Expected);
		Assert.assertNotNull("md5Actual",md5Actual);
		Assert.assertEquals("md5 doesn't match: ",md5Expected,md5Actual);

	}

/*
	private void testFs() {
		String before=null;
		String after = null;

		before = getMD5HashOfString(dirMD5Sum("PATH_FSTEST_FROM"));

		int countBefore=countFiles("PATH_FSTEST_FROM");

		Assert.assertTrue(runSingleCmd("rm -rf "+BASE_PATH_FSTEST_TO));
		Assert.assertTrue(runSingleCmd("mkdir -p "+PATH_FSTEST_TO));
		Assert.assertTrue(runSingleCmd("cp -R "+ "PATH_FSTEST_FROM"
				+" "+BASE_PATH_FSTEST_TO, true));

		int countAfter=countFiles(BASE_PATH_FSTEST_TO+BASE_TO_DIR);
		after = getMD5HashOfString(dirMD5Sum(BASE_PATH_FSTEST_TO+BASE_TO_DIR));

		Assert.assertTrue(runSingleCmd("rm -rf "+BASE_PATH_FSTEST_TO));

		Log.w(TAG, "Before="+before);
		Log.w(TAG, "After="+after);

		Assert.assertEquals("number of files doesn't match: ",countBefore,countAfter);
		Assert.assertEquals("md5 doesn't match: ",before,after);



	}*/

	public boolean runSingleCmd(String cmd) {
		return runSingleCmd(cmd,false);
	}

	public boolean runSingleCmd(final String cmd, final boolean verbose) {
		inst.runOnMainSync(new Runnable() {
			                   @Override
			                   public void run() {
				                   if(verbose)Log.w(TAG,"STARTED "+cmd);
				                   ParcelFileDescriptor fileDescriptor=inst.getUiAutomation().executeShellCommand(
						                   cmd
				                   );
				                   BufferedReader reader=new BufferedReader(
						                   new InputStreamReader(new FileInputStream(fileDescriptor.getFileDescriptor())));
				                   String line;
				                   String res="";
				                   try{
					                   while((line=reader.readLine())!=null){
						                   Log.w(TAG,"line="+line);
						                   assertNotNull(line);
						                   res+=line+"\n";
					                   }
				                   }catch(IOException e){
					                   fail(ASSERT_MESSAGE_PREFIX+e.getMessage());
				                   }
				                   SystemClock.sleep(2000);
				                   if(verbose)Log.w(TAG,"FINISHED "+res);
				                   //Log.w(TAG, res);
			                   }
		});
		inst.waitForIdleSync();
		return true;
	}

	private int countFiles(String pathFstestFrom) {
		File dir = new File(pathFstestFrom);
		if(!dir.exists()) return 0;
		else if(!dir.isDirectory()) return 1;
		else return dir.listFiles().length;
	}
	public String dirMD5Sum(String dir)
	{
		//Log.w(TAG, "dirMD5: "+dir);
		String md5    = "";
		File folder = new File(dir);
		if(folder.exists()){
			if(folder.isDirectory()) {
				File[] files = folder.listFiles();
				if (files != null && files.length > 0) {
					File subFile;
					for (int i = 0; i < files.length; i++) {
						subFile = files[i];
						Assert.assertNotNull(subFile);
						if(folder.exists()) {
							if (subFile.isDirectory()) md5 += dirMD5Sum(subFile.getAbsolutePath());
							else md5 += getMd5OfFile(subFile.getAbsolutePath());
						}
					}
				}
			}
		}
		return md5;
	}


	public String getMd5OfFile(String filePath)
	{
		//Log.w(TAG, "getMd5OfFile: "+filePath);
		String returnVal = "";
		try
		{
			InputStream input   = new FileInputStream(filePath);
			byte[]        buffer  = new byte[1024];
			MessageDigest md5Hash = MessageDigest.getInstance("MD5");
			int           numRead = 0;
			while (numRead != -1)
			{
				numRead = input.read(buffer);
				if (numRead > 0)
				{
					md5Hash.update(buffer, 0, numRead);
				}
			}
			input.close();

			byte [] md5Bytes = md5Hash.digest();
			for (int i=0; i < md5Bytes.length; i++)
			{
				returnVal += Integer.toString( ( md5Bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
			}
		}
		catch(Throwable t) {
			Assert.fail(TAG+filePath+" "+t.getMessage());
			t.printStackTrace();}
		return returnVal.toUpperCase();
	}

	public String getMD5HashOfString(String str) {
		//Log.w(TAG, "getMD5HashOfString: "+str);
		MessageDigest md5 ;
		StringBuffer  hexString = new StringBuffer();
		try
		{
			md5 = MessageDigest.getInstance("md5");
			md5.reset();
			md5.update(str.getBytes());
			byte messageDigest[] = md5.digest();
			for (int i = 0; i < messageDigest.length; i++)
			{
				hexString.append(Integer.toHexString((0xF0 & messageDigest[i])>>4));
				hexString.append(Integer.toHexString (0x0F & messageDigest[i]));
			}
		}
		catch (Throwable t) {
			Assert.fail(t.getMessage());
		}
		return hexString.toString();
	}
}
