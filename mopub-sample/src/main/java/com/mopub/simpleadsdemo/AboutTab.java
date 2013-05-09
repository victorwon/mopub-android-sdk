package com.mopub.simpleadsdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutTab extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Button mOpenSiteButton = (Button) findViewById(R.id.opensite);
        mOpenSiteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://www.mopub.com/")));
            }
        });
    }
}