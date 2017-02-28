package android.mediastress.renesas;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static android.content.Intent.CATEGORY_DEFAULT;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by artem on 11/17/16.
 */
public class UrlTest extends BaseTest{
	String url;



	@Override
	public void run(){
		openTestUrl();
	}

	public UrlTest(Instrumentation inst) {
		TAG = "UrlTest";
		initBase(inst,  "TEST_URL");
		assertTrue(prepareUrl());
	}

	public UrlTest(Instrumentation inst, String testUrl ) {
		TAG = "UrlTest";
		url=testUrl;
		initBase(inst, "TEST_URL");
		setAssertSamplePrefix(url);
	}


	public boolean prepareUrl() {
		ArrayList<String> urls=null;
		try {
			urls = readFile(BASE_PATH_URL);
		} catch (IOException e) {
			fail(ASSERT_MESSAGE_PREFIX+e.getMessage());
		}
		assertNotNull(ASSERT_MESSAGE_PREFIX+urls,urls);
		int size = urls.size();
		int random = new Random().nextInt(size);
		url = urls.get(random);
		setAssertSamplePrefix(url);
		return true;
	}


	public void openTestUrl() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		intent.addCategory(CATEGORY_DEFAULT);
		inst.getTargetContext().startActivity(intent);
		launchExternalIntent(intent);
	}
}
