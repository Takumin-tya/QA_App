package jp.techacademy.takumi.fukushima.qa_app;


import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

//コメント
public class QA_App extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
