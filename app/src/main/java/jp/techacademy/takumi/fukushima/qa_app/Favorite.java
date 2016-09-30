package jp.techacademy.takumi.fukushima.qa_app;


import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Favorite extends RealmObject implements Serializable {

    @PrimaryKey
    private String id;

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
}
