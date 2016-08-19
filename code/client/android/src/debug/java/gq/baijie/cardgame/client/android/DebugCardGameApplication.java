package gq.baijie.cardgame.client.android;

public class DebugCardGameApplication extends CardGameApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    registerActivityLifecycleCallbacks(new DebugMainActivityLifecycleCallbacks());
  }

}
