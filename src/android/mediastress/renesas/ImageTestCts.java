package android.mediastress.renesas;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by artem on 11/17/16.
 */
public class ImageTestCts extends BaseTest{

	@Override
	public void run(){
		openRandomImage();
	}
	public ImageTestCts(Instrumentation inst) {
		TAG = "ImageTest";
		initRandom(inst, "TEST_IMAGE", BASE_PATH_IMAGE);
	}
	public ImageTestCts(Instrumentation inst, String fileName) {
		TAG = "ImageTest";
		initTestWithFile(inst, BASE_PATH_IMAGE, fileName);
	}

	public void openRandomImage() {
		Log.w("TAG", "openRandomImage"+fileName);
		activity = launchMainActivity();
		try {
			File file = new File(fileName);
			setTitle(activity, fileName);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			final Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
			int beforeH=activity.getMeasuredHeight();
			int beforeW = activity.getMeasuredWidth();
			Log.w("BEFORE", "H: "+beforeH+ " W: "+ beforeW);
			activity.setImage(bitmap);
			SystemClock.sleep(5000);
			int afterH=activity.getMeasuredHeight();
			int afterW = activity.getMeasuredWidth();
			Log.w("AFTER", "H: "+afterH+ " W: "+ afterW);
			assertNotSame("imageView Height not changed after set src",beforeH, afterH);
			assertNotSame("imageView Width not changed after set src",beforeW, afterW);
			assertTrue("imageView size incorrect",(afterH>100 && afterW>100));

		} catch (FileNotFoundException e) {
			fail(" FileNotFoundException "+ e.getMessage());
		}
		return;
	}
}
