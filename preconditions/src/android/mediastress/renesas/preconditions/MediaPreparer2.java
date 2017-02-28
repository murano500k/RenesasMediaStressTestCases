package android.mediastress.renesas.preconditions;

import com.android.compatibility.common.tradefed.targetprep.PreconditionPreparer;
import com.android.ddmlib.IDevice;
import com.android.tradefed.build.IBuildInfo;
import com.android.tradefed.config.Option;
import com.android.tradefed.config.OptionClass;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.targetprep.BuildError;
import com.android.tradefed.targetprep.TargetSetupError;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@OptionClass(alias="media-preparer")
public class MediaPreparer2 extends PreconditionPreparer {

	public static String BASE_PATH_VIDEO = "/sdcard/test/rvideo/";
	public static String BASE_PATH_AUDIO = "/sdcard/test/raudio/";
	public static String BASE_PATH_IMAGE = "/sdcard/test/rimage/";
	public static String BASE_PATH_URL = "/sdcard/test/urls.txt";
	public static String BASE_PATH_APP = "/sdcard/test/rapk/";
	public static String BASE_NAME_MD5 = "md5sum.txt";

    @Option(name = "local-media-path",
            description = "Absolute path of the media files directory, containing rvideo dir")
    protected String localFullDir = null;
	@Option(name = "local-md5file-path",
			description = "md5file")
	protected String pathToMd5File = null;

    @Option(name = "skip-media-download",
            description = "Whether to skip the media files precondition")
    protected boolean mSkipMediaDownload = false;

    protected String baseDeviceFullDir;
	protected static final String MEDIA_FOLDER_NAME = "android-cts-media";
	private static final String MEDIA_FILES_URL_KEY = "media_files_url";
	private static final String DYNAMIC_CONFIG_MODULE = "cts";

    protected boolean mediaDirsExistOnDevice(ITestDevice device)
            throws DeviceNotAvailableException{
        String rvideo = BASE_PATH_VIDEO;
        String raudio = BASE_PATH_AUDIO;
        String rimage = BASE_PATH_IMAGE;
        String rapk = BASE_PATH_APP;
        String rurl = BASE_PATH_URL;
	    logError(rvideo+ " - "+device.doesFileExist(rvideo));
	    logError(raudio+ " - "+device.doesFileExist(raudio));
	    logError(rimage+ " - "+device.doesFileExist(rimage));
	    logError(rapk+ " - "+device.doesFileExist(rapk));
	    logError(rurl+ " - "+device.doesFileExist(rurl));
	    boolean res = device.doesFileExist(rvideo) &&
			    device.doesFileExist(raudio) &&
			    device.doesFileExist(rapk) &&
			    device.doesFileExist(rurl) &&
			    device.doesFileExist(rimage);
	    return res;
    }
protected boolean mediaFilesExistOnDevice(ITestDevice device)
            throws DeviceNotAvailableException{
	boolean res=false;
	List<String> sampleDirs=new ArrayList<>();
	sampleDirs.add(BASE_PATH_APP);
	sampleDirs.add(BASE_PATH_AUDIO);
	sampleDirs.add(BASE_PATH_VIDEO);
	sampleDirs.add(BASE_PATH_IMAGE);

	for(String dirPath : sampleDirs){
		File dir = new File(dirPath);
	}
	    return res;
    }

    protected void setMountPoint(ITestDevice device) {
        String mountPoint = device.getMountPoint(IDevice.MNT_EXTERNAL_STORAGE);
        baseDeviceFullDir = String.format("%s/", mountPoint);
    }

    @Override
    public void run(ITestDevice device, IBuildInfo buildInfo) throws TargetSetupError,
            BuildError, DeviceNotAvailableException {
	    mSkipMediaDownload=true;
	    if(pathToMd5File!=null) {
		    logWarning("pushing "+pathToMd5File);
		    Assert.assertTrue(device.pushFile(new File(pathToMd5File), "/sdcard"));
	    }else logError("ERROR NULL");

        if (mSkipMediaDownload) return;
        setMountPoint(device);
	    File hostDir =new File(localFullDir);
	    File md5File =new File(localFullDir+BASE_NAME_MD5);

	    mediaFilesExistOnDevice(device);
	    logWarning("pushing "+localFullDir+" to "+baseDeviceFullDir);
	    device.syncFiles(new File(localFullDir), baseDeviceFullDir);
	    String md5="";
	    /*try {
		    //md5=FileUtil.calculateMd5(new File(localFullDir));
		    logWarning("TAG", "md5: "+md5);
		    FileUtil.writeToFile("TEST123", new File(baseDeviceFullDir+"/TEST_TEST_TEST.txt"));
		    //FileUtil.writeToFile(md5, md5File);
	    } catch (IOException e) {
		    e.printStackTrace();
	    }*/

	    /*

        if (mediaFilesExistOnDevice(device)) {
            logError("Media files found on the device");
            return;
        }else{
	        logError("Media files NOT found on the device");
	        assertNotNull("localFullDir: "+localFullDir, localFullDir);
	        logError("pushing "+localFullDir+" to "+baseDeviceFullDir);
        }
*/
    }

}
