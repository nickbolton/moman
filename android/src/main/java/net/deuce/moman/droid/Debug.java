package net.deuce.moman.droid;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.ScrollView;
import android.widget.TextView;

public class Debug extends BaseActivity {

  /**
   * Called when the activity is first created.
   */
  @Override
  protected void doOnCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ScrollView sv = new ScrollView(this);

    TextView label = new TextView(this);
    label.setText("Enter server:");
    label.setGravity(Gravity.LEFT);
    sv.addView(label);

    setContentView(sv);

  }

}