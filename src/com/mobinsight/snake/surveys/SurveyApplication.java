package com.mobinsight.snake.surveys;


import com.mobinsight.client.Mobinsight;

import android.app.Application;

public class SurveyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Mobinsight.initialize(getApplicationContext(), getApplicationContext().getPackageName());
    }

}
