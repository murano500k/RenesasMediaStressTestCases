package android.mediastress.renesas;

import android.app.Instrumentation;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static android.mediastress.renesas.BaseTest.BASE_PATH_CONFIG;
import static junit.framework.Assert.fail;


public class ConfigResolver {
	private static final String TAG = "ConfigResolver";

	public static final String TEST_VIDEO = "VIDEO";
	public static final String TEST_AUDIO = "AUDIO";
	public static final String TEST_IMAGE= "IMAGE";
	public static final String TEST_URL = "URL";
	public static final String TEST_APP= "APP";
	public static final String TEST_RANDOM= "RANDOM";
	public static final String TEST_FS= "FS";
	public static final String EMPTY= "EMPTY";

	private static final String PARAM_TEST_TYPE = "testType";
	private static final String PARAM_TEST_FILEPATH = "testFile";
	private static final String PARAM_TEST_MD5 = "testMd5";
	private static final String PARAM_RANDOM_INCLUDE = "randomInclude";
	public String testType = TEST_RANDOM;
	public String testFile = null;
	public String testMd5 = null;

	public boolean includeVideo = false;

	public boolean includeAudio = false;

	public boolean includeImage = false;

	public boolean includeUrl = false;

	public boolean includeApp = false;

	public boolean includeFs = true;


	Instrumentation inst;

	public ConfigResolver(Instrumentation inst) {
		this.inst=inst;
	}

	public void configure(){
		try{
			readFile(BASE_PATH_CONFIG);
		}catch (IOException e){
			fail(TAG+" read config error "+ e.getMessage());
		}
		Log.w(TAG, "TEST_TYPE "+testType);
		Log.w(TAG, "testFile "+testFile);
		Log.w(TAG, "testMd5 "+testMd5);
		Log.w(TAG, "includeApp "+includeApp);
		Log.w(TAG, "includeUrl "+includeUrl);
		Log.w(TAG, "includeVideo "+includeVideo);
		Log.w(TAG, "includeAudio "+includeAudio);
		Log.w(TAG, "includeImage "+includeImage);
		Log.w(TAG, "includeFs "+includeFs);

	}



	private String readFile(String pathname) throws IOException {
		File file = new File(pathname);
		Scanner scanner = new Scanner(file);
		String lineSeparator = System.getProperty("line.separator");
		String line;
		try {
			while(scanner.hasNextLine()) {
				line=scanner.nextLine();
				//Log.w(TAG, line);
				checkLine(line);
			}
			return null;
		} finally {
			scanner.close();
		}
	}
	public void checkLine(String line){
		if(line.contains(PARAM_TEST_TYPE)){
			testType=line.substring(line.indexOf(" ")+1);
		} else if(line.contains(PARAM_TEST_FILEPATH)){
			testFile=line.substring(line.indexOf(" ")+1);
		}else if(line.contains(PARAM_TEST_MD5)){
			testMd5=line.substring(line.indexOf(" ")+1);
		} else if(line.contains(PARAM_RANDOM_INCLUDE)){
			includeApp=line.contains(TEST_APP);
			includeUrl=line.contains(TEST_URL);
			includeImage=line.contains(TEST_IMAGE);
			includeAudio=line.contains(TEST_AUDIO);
			includeVideo=line.contains(TEST_VIDEO);
			includeFs=line.contains(TEST_FS);
		}
	}
}
