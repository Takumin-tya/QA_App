package jp.techacademy.takumi.fukushima.qa_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int mGenre = 0;

    private Realm realm;
    private RealmResults<Favorite> realmResults;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    private boolean favoriteFlag = false;

    private NavigationView navigationView;
    private Menu menu;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d("QA_Android","チェンジイベントリスナー(Added)");

            HashMap map = (HashMap) dataSnapshot.getValue();
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            int genre = (int) map.get("genre");
            Bitmap image = null;
            byte[] bytes;
            if (imageString != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            if(favoriteFlag){
                realmResults = realm.where(Favorite.class).equalTo("id",dataSnapshot.getKey()).findAll();
                Log.d("QA_Android", dataSnapshot.getKey());


                if(realmResults.size() != 0){
                    Log.d("QA_Android","お気に入りを表示");
                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), genre, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                    Log.d("QA_Android", question.getQuestionUid());
                }
            }else {
                Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                mQuestionArrayList.add(question);
                mAdapter.notifyDataSetChanged();
                Log.d("QA_Android", question.getQuestionUid());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("QA_Android","チェンジイベントリスナー(Changed)");

            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        favoriteFlag = false;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGenre == 0){
                    Snackbar.make(view, "ジャンルを選択してください", Snackbar.LENGTH_LONG).show();
                    return;
                }

                //ログイン済みのユーザーを収録する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //ログインしていなければログイン画面に遷移させる
                if(user == null){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    intent.putExtra("id", mQuestionArrayList.size());
                    startActivity(intent);
                }
            }
        });

        realm = Realm.getDefaultInstance();

        //ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.nav_favorite){
                    mToolbar.setTitle("お気に入り");
                    mGenre = 0;

                    Log.d("QA_Android","お気に入りを選択");
                    //質問のリストをクリアしてから再度AdapterをListViewにセットし直す
                    mQuestionArrayList.clear();
                    mAdapter.setmQuestionArrayList(mQuestionArrayList);
                    mListView.setAdapter(mAdapter);

                    favoriteFlag = true;

                    //選択したジャンルにリスナーを登録する
                    if (mGenreRef != null) {
                        mGenreRef.removeEventListener(mEventListener);
                    }

                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child("1");
                    mGenreRef.addChildEventListener(mEventListener);

                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child("2");
                    mGenreRef.addChildEventListener(mEventListener);

                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child("3");
                    mGenreRef.addChildEventListener(mEventListener);

                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child("4");
                    mGenreRef.addChildEventListener(mEventListener);

                }else {
                    if (id == R.id.nav_hobby) {
                        mToolbar.setTitle("趣味");
                        mGenre = 1;
                    } else if (id == R.id.nav_life) {
                        mToolbar.setTitle("生活");
                        mGenre = 2;
                    } else if (id == R.id.nav_health) {
                        mToolbar.setTitle("健康");
                        mGenre = 3;
                    } else if (id == R.id.nav_compter) {
                        mToolbar.setTitle("コンピューター");
                        mGenre = 4;
                    }

                    favoriteFlag = false;

                    //質問のリストをクリアしてから再度AdapterをListViewにセットし直す
                    mQuestionArrayList.clear();
                    mAdapter.setmQuestionArrayList(mQuestionArrayList);
                    mListView.setAdapter(mAdapter);

                    //選択したジャンルにリスナーを登録する
                    if (mGenreRef != null) {
                        mGenreRef.removeEventListener(mEventListener);
                    }
                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                    mGenreRef.addChildEventListener(mEventListener);
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return true;
            }
        });

        //Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        SetView();
    }

    private void SetView(){
        menu.clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);

        //ログイン済みのユーザーを収録する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            navigationView.inflateMenu(R.menu.activity_main_option_menu);
        }
    }
}
