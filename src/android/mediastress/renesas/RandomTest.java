package android.mediastress.renesas;

import android.app.Instrumentation;
import android.os.ParcelFileDescriptor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME;
import static android.mediastress.renesas.ConfigResolver.EMPTY;
import static android.mediastress.renesas.ConfigResolver.TEST_APP;
import static android.mediastress.renesas.ConfigResolver.TEST_AUDIO;
import static android.mediastress.renesas.ConfigResolver.TEST_FS;
import static android.mediastress.renesas.ConfigResolver.TEST_IMAGE;
import static android.mediastress.renesas.ConfigResolver.TEST_RANDOM;
import static android.mediastress.renesas.ConfigResolver.TEST_URL;
import static android.mediastress.renesas.ConfigResolver.TEST_VIDEO;


@RunWith(AndroidJUnit4.class)
public class RandomTest extends TestCase {
	private static final String TAG = "RandomTest";
	private static final String ASSERT_MESSAGE_PREFIX = "";


	ConfigResolver configResolver;



	public final int NUM_TEST_VIDEO = 0;
	public final int NUM_TEST_AUDIO = 1;
	public final int NUM_TEST_IMAGE = 2;
	public final int NUM_TEST_URL = 3;
	public final int NUM_TEST_APP = 4;
	public final int NUM_TEST_FS = 5;
	public final int NUM_TEST_RANDOM = -1;


	Instrumentation inst;
	public final int TESTS_COUNT = 6;
	BaseTest randomTest=null;
	public int random=-1;

	@Before
	public void initTest(){
		inst = InstrumentationRegistry.getInstrumentation();
		checkPermPM();
		configResolver =new ConfigResolver(inst);
		configResolver.configure();
		boolean isDefined = detectTestType();
		if(isDefined && configResolver.testFile!=null && !configResolver.testFile.equals(EMPTY)){
			if(!configResolver.testType.contains(TEST_URL)) {
				File file = new File(configResolver.testFile);
				assertTrue(ASSERT_MESSAGE_PREFIX + configResolver.testFile + " not found on device", file.exists());
			}
			if (configResolver.testType.equals(TEST_VIDEO)) randomTest=new MediaTestCts(inst, true, configResolver.testFile);
			else if (configResolver.testType.equals(TEST_AUDIO)) randomTest=new MediaTestCts(inst, false, configResolver.testFile);
			else if (configResolver.testType.equals(TEST_IMAGE)) randomTest=new ImageTestCts(inst, configResolver.testFile);
			else if (configResolver.testType.equals(TEST_URL)) randomTest=new UrlTest(inst, configResolver.testFile);
			else if (configResolver.testType.equals(TEST_APP)) randomTest=new AppTest(inst, configResolver.testFile);
			else if (configResolver.testType.equals(TEST_FS) && configResolver.testMd5!=null && configResolver.testMd5.length()>0)
				randomTest=new FileTest(inst, configResolver.testFile, configResolver.testMd5);
			else fail("test type error: "+ configResolver.testType);
		}else {
			for(int i = 0 ; randomTest==null; i++){
				Log.w("RANDOM","randomize "+i);
				if(i>100) fail("randomize failed");
				configResolver.testType=randomize();
				if (configResolver.testType.equals(TEST_VIDEO) && configResolver.includeVideo) randomTest=new MediaTestCts(inst, true);
				else if (configResolver.testType.equals(TEST_AUDIO)&& configResolver.includeAudio) randomTest=new MediaTestCts(inst, false);
				else if (configResolver.testType.equals(TEST_IMAGE) && configResolver.includeImage) randomTest=new ImageTestCts(inst);
				else if (configResolver.testType.equals(TEST_URL) && configResolver.includeUrl) randomTest=new UrlTest(inst);
				else if (configResolver.testType.equals(TEST_APP) && configResolver.includeApp) randomTest=new AppTest(inst);
				else if (configResolver.testType.equals(TEST_FS) && configResolver.includeFs) randomTest=new FileTest(inst);
				else continue;
			}

		}
	}

	@Test
	public void testRandom(){
		assertNotNull(ASSERT_MESSAGE_PREFIX, randomTest);
		Log.w(ASSERT_MESSAGE_PREFIX, "run test");
		randomTest.run();
		goHome();
	}

	public boolean detectTestType(){
		if(configResolver.testType==null || TEST_RANDOM.equals(configResolver.testType)) {
			random = new Random().nextInt(TESTS_COUNT);
			return false;
		}else return true;
	}
	private String randomize() {
		random = new Random().nextInt(TESTS_COUNT);
		switch (random){
			case NUM_TEST_VIDEO:
				return TEST_VIDEO;
			case NUM_TEST_AUDIO:
				return TEST_AUDIO;
			case NUM_TEST_IMAGE:
				return TEST_IMAGE;
			case NUM_TEST_URL:
				return TEST_URL;
			case NUM_TEST_APP:
				return TEST_APP;
			case NUM_TEST_FS:
				return TEST_FS;
			default:
				return TEST_VIDEO;
		}

	}

	public void goHome(){
		assertTrue(ASSERT_MESSAGE_PREFIX+"test finished: "
				,inst.getUiAutomation().performGlobalAction(GLOBAL_ACTION_HOME));
	}

	public void checkPermPM() {
		String currentPackage = inst.getTargetContext().getPackageName();
		ParcelFileDescriptor fileDescriptor = inst.getUiAutomation()
				.executeShellCommand("pm grant "+currentPackage+" android.permission.READ_EXTERNAL_STORAGE");
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDescriptor.getFileDescriptor())));
		String line, res = "";
		try {
			while ((line = reader.readLine()) != null) {
				res += line;
			}
		} catch (IOException e) {
			fail(ASSERT_MESSAGE_PREFIX + e.getMessage());
		}
		Log.d(TAG, "checkPermPM: " + res);
	}

}
