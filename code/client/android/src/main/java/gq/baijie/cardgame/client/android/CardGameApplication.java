package gq.baijie.cardgame.client.android;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
    mailTo = "baijie1991@qq.com",
    mode = ReportingInteractionMode.DIALOG,
    resDialogTitle = R.string.app_name,
    resDialogText = R.string.crash_dialog_text,
    resDialogTheme = R.style.AppTheme_Dialog
)
public class CardGameApplication extends Application {

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    ACRA.init(this);
  }

}
