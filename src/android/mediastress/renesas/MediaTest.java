package android.mediastress.renesas;

import android.app.Instrumentation;
import android.media.MediaPlayer;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by artem on 11/17/16.
 */
public class MediaTest extends  BaseTest{

	private MediaPlayer mMediaPlayer;
	private int WAIT_FOR_COMMAND_TO_COMPLETE = 60000;  //1 min max.
	private boolean mPrepareReset = false;
	private Looper mLooper = null;
	private final Object mLock = new Object();
	private final Object mPrepareDone = new Object();
	private final Object mOnCompletion = new Object();
	private final long PAUSE_WAIT_TIME = 2000;
	private final long WAIT_TIME = 2000;
	private final int SEEK_TIME = 10000;
	private boolean mInitialized = false;

	private boolean mOnPrepareSuccess = false;
	public boolean mOnCompleteSuccess = false;
	public boolean mPlaybackError = false;
	public int mMediaInfoUnknownCount = 0;
	public int mMediaInfoVideoTrackLaggingCount = 0;
	public int mMediaInfoBadInterleavingCount = 0;
	public int mMediaInfoNotSeekableCount = 0;
	public int mMediaInfoMetdataUpdateCount = 0;

	public MediaTest(Instrumentation inst, String msgPrefix, boolean isVideo) {
		TAG = "MediaTest";
		initRandom(inst, msgPrefix, isVideo ? BASE_PATH_VIDEO : BASE_PATH_AUDIO);
	}

	@Override
	public void run(){
		playRandomMediaTest();
	}

	public void playRandomMediaTest(){
		assertTrue(ASSERT_MESSAGE_PREFIX,playMediaSample(fileName));
	}

	public boolean playMediaSample(final String fileName ) {
		File file = new File(fileName);
		activity = launchMainActivity();
		int duration = 0;
		int waittime = 0;
		mOnCompleteSuccess = false;
		mMediaInfoUnknownCount = 0;
		mMediaInfoVideoTrackLaggingCount = 0;
		mMediaInfoBadInterleavingCount = 0;
		mMediaInfoNotSeekableCount = 0;
		mMediaInfoMetdataUpdateCount = 0;
		mPlaybackError = false;
		initializeMessageLooper();
		synchronized (mLock) {
			try {
				mLock.wait(WAIT_FOR_COMMAND_TO_COMPLETE);
			} catch(Exception e) {
				fail(ASSERT_MESSAGE_PREFIX+" looper was interrupted.");
				return false;
			}
		}
		try {
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mOnErrorListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			Log.v(TAG, "playMediaSample: sample file name " + fileName);
			mMediaPlayer.setDataSource(file.getAbsolutePath());
			setTitle(activity, ASSERT_MESSAGE_PREFIX);
			mMediaPlayer.setDisplay(activity.getSurfaceView().getHolder());
			mMediaPlayer.prepare();
			duration = mMediaPlayer.getDuration();
			Log.v(TAG, "duration of media " + duration);
			mMediaPlayer.start();



			int lastSeek=duration*8/10;
			mMediaPlayer.seekTo(lastSeek);

			int currentPosition = mMediaPlayer.getCurrentPosition();
			Log.v(TAG, "video currentPosition " + currentPosition);
			assertTrue(ASSERT_MESSAGE_PREFIX+
							" currentPosition="+currentPosition+
							" should be at least "+lastSeek*0.9,
					currentPosition > lastSeek*0.8);
			waittime = duration - mMediaPlayer.getCurrentPosition();
			SystemClock.sleep(1000);

			synchronized(mOnCompletion) {
				try {
					mOnCompletion.wait(waittime + 3000);
				} catch (Exception e) {
					fail(ASSERT_MESSAGE_PREFIX+" playMediaSamples are interrupted.");
					return false;
				}
			}
			//mMediaPlayer.stop();


			assertTrue(ASSERT_MESSAGE_PREFIX+"wait timed-out without onCompletion notification",mOnCompleteSuccess);
			terminateMessageLooper();
			if(activity!=null)activity.finish();

		} catch (Exception e) {
			e.printStackTrace();
			fail(ASSERT_MESSAGE_PREFIX + e.getMessage());
		}


		return mOnCompleteSuccess;
	}
	private void initializeMessageLooper() {
		Log.v(TAG, "start looper");
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Log.v(TAG, "start loopRun");
				mLooper = Looper.myLooper();
				mMediaPlayer = new MediaPlayer();
				synchronized (mLock) {
					mInitialized = true;
					mLock.notify();
				}
				Looper.loop();
				Log.v(TAG, "initializeMessageLooper: quit.");
			}
		}.start();
	}

	private void terminateMessageLooper() {
		mLooper.quit();
		mMediaPlayer.release();
	}

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			synchronized (mPrepareDone) {
				if(mPrepareReset) {
					Log.v(TAG, "call Reset");
					mMediaPlayer.reset();
				}
				Log.v(TAG, "notify the prepare callback");
				mPrepareDone.notify();
				mOnPrepareSuccess = true;
			}
		}
	};
	MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			synchronized (mOnCompletion) {
				Log.v(TAG, "notify the completion callback");
				mOnCompletion.notify();
				mOnCompleteSuccess = true;
			}
		}
	};

	MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.v(TAG, "playback error");
			mPlaybackError = true;
			mp.reset();
			synchronized (mOnCompletion) {
				Log.v(TAG, "notify the completion callback");
				mOnCompletion.notify();
				mOnCompleteSuccess = false;
			}
			fail(ASSERT_MESSAGE_PREFIX+"playback error");
			return true;
		}
	};

	MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			switch (what) {
				case MediaPlayer.MEDIA_INFO_UNKNOWN:
					mMediaInfoUnknownCount++;
					break;
				case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
					mMediaInfoVideoTrackLaggingCount++;
					fail(ASSERT_MESSAGE_PREFIX+"MEDIA_INFO_VIDEO_TRACK_LAGGING");

					break;
				case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
					mMediaInfoBadInterleavingCount++;
					fail(ASSERT_MESSAGE_PREFIX+"MEDIA_INFO_BAD_INTERLEAVING");

					break;
				case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
					mMediaInfoNotSeekableCount++;
					fail(ASSERT_MESSAGE_PREFIX+"MEDIA_INFO_NOT_SEEKABLE");
					break;
				case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
					mMediaInfoMetdataUpdateCount++;
					break;
			}
			return true;
		}
	};

}
