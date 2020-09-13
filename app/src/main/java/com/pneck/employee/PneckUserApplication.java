package com.pneck.employee;

import android.app.Application;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


@ReportsCrashes(mailTo = "ankitrajdwivedi@gmail.com", // my email here
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_toast_text, formKey = "")
     public class PneckUserApplication extends Application {
	public static final String TAG = Application.class.getName();
	private static Application _instance;
	private final Handler _handler;
	/**
	 * Thread to execute tasks in background..
	 */
	private final ExecutorService _backgroundExecutor;
	private boolean closed;
	private String applicationLocale;

	public PneckUserApplication() {
		_instance = this;
		_handler = new Handler();
		_backgroundExecutor = Executors
				.newSingleThreadExecutor(new ThreadFactory() {
					@Override
					public Thread newThread(Runnable runnable) {
						Thread thread = new Thread(runnable,
								"Background executor service");
						thread.setPriority(Thread.MIN_PRIORITY);
						thread.setDaemon(true);
						return thread;
					}
				});
		closed = false;
	//	uiListeners = new HashMap<Class<? extends BaseUIListener>, Collection<? extends BaseUIListener>>();
	}

	public static Application getInstance() {
		if (_instance == null) {
			_instance = new Application();
		}
		return _instance;

	}





	@SuppressWarnings("unchecked")


	/**
	 * Submits request to be executed in UI thread.
	 *
	 * @param runnable
	 */
	public void runOnUiThread(final Runnable runnable) {
		_handler.post(runnable);
	}

	/**
	 * Submits request to be executed in background.
	 *
	 * @param runnable
	 */
	public void runInBackground(final Runnable runnable) {
		_backgroundExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Exception e) {
					Log.e(TAG, "run In Background", e);
				}
			}
		});
	}

	public void setLocale(String locale) {
		applicationLocale = locale;
	}

	public String getLocale(String defLocale) {
		if (applicationLocale == null) {
			return defLocale;
		} else {
			return applicationLocale;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MultiDex.install(this);
		ACRA.init(this);

		//logUser();
	}
	/*private void logUser() {
		// TODO: Use the current user's information
		// You can call any combination of these three methods
		Crashlytics.setUserIdentifier("9453991639");
		Crashlytics.setUserEmail("prabalpratap.kanpur@gmail.com");
		Crashlytics.setUserName("prabal");
	}*/

}
