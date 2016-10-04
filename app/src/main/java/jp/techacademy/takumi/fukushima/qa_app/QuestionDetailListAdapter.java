package jp.techacademy.takumi.fukushima.qa_app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.realm.Realm;
import io.realm.RealmResults;

public class QuestionDetailListAdapter extends BaseAdapter{
    private final static int TYPE_QESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQuestion;

    private Realm mRealm;
    private RealmResults<Favorite> mRealmResults;
    private Button favoriteButton;
    private boolean favoriteFlag = false;

    private Activity parentActivity;

    public QuestionDetailListAdapter(Context context, Question question){
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQuestion = question;
        parentActivity = (Activity) context;
    }

    @Override
    public int getCount(){
        return 1 + mQuestion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0) {
            return TYPE_QESTION;
        }else{
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    public Object getItem(int position){
        return mQuestion;
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup parent){

        if(getItemViewType(position) == TYPE_QESTION){
            if(contentView == null){
                contentView = mLayoutInflater.inflate(R.layout.list_question_detail, parent,false);
            }
            String body = mQuestion.getBody();
            String name = mQuestion.getName();

            mRealm = Realm.getDefaultInstance();
            favoriteButton = (Button) contentView.findViewById(R.id.favoriteButton);

            try {
                mRealmResults = mRealm.where(Favorite.class).equalTo("id", mQuestion.getQuestionUid()).findAll();
                if (mRealmResults.size() != 0) {
                    favoriteButton.setBackgroundResource(R.color.myFavorite);
                    favoriteFlag = true;
                }
            }catch (NullPointerException e){

            }

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(favoriteFlag){
                        mRealmResults = mRealm.where(Favorite.class).equalTo("id", mQuestion.getQuestionUid()).findAll();
                        mRealm.beginTransaction();
                        mRealmResults.clear();
                        mRealm.commitTransaction();
                        favoriteButton.setBackgroundResource(R.color.notFavorite);
                    }else{
                        Favorite favorite = new Favorite();
                        favorite.setId(mQuestion.getQuestionUid());
                        Log.d("QA_Android", favorite.getId());
                        mRealm.beginTransaction();
                        mRealm.copyToRealmOrUpdate(favorite);
                        mRealm.commitTransaction();

                        favoriteButton.setBackgroundResource(R.color.myFavorite);
                        Snackbar.make(parentActivity.findViewById(android.R.id.content), "お気に入りに登録しました。", Snackbar.LENGTH_LONG).show();
                    }
                    favoriteFlag = !favoriteFlag;
                }
            });

            //ログイン済みのユーザーを収録する
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null){
                favoriteButton.setVisibility(View.GONE);
            }else{
                favoriteButton.setVisibility(View.VISIBLE);
            }

            TextView bodyTextView = (TextView) contentView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) contentView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQuestion.getImageBytes();
            if(bytes.length != 0){
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) contentView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }
        }else{
            if(contentView == null){
                contentView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQuestion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) contentView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) contentView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return contentView;
    }
}
