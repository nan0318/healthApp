package com.example.zhongzhoujianshe.healthapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class EcogSurveyActivity extends AppCompatActivity {
    //UI objects
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    //toolbar part
    private Toolbar toolbar;
    private TextView txt_menu_back;
    private TextView txt_menu_send;
    //body part
    private TextView txt_date;
    private RadioGroup radioGroup;
    private TextView scale_note;
    private TimePickerView pvTime;
    private TextView tv_time;
    //firebase
    private String currentUserId;
    private DatabaseReference mRoot;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //varialbe
    private int answer = -1;
    private EcogAndBssAnswerModel ecogAnswer;
    private String submitTime;

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecog_survey);

        /* * * * * firebase * * * * * */
        //get Uid
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    currentUserId = user.getUid();
                }
            }
        };

        /* * * * * initialize view  * * * * * */
        tv_time = (TextView) findViewById(R.id.tv_time);
        //default: current time
        Date date = new Date();
        submitTime = TimeMethods.getDateStringForDb(date); //format: "yyyy-MM-dd"
        tv_time.setText(TimeMethods.getTimeStringForTxt(date));//format: "dd/MMM/yyyy HH:mm"

        initTimePicker();
        iniView();
    }
    private void sendData(){
        //String date = "2018-10-24";
        //send data
        mRoot = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference userRef = mRoot.child(currentUserId).child("ecog");
        ecogAnswer = new EcogAndBssAnswerModel();
        ecogAnswer.setTime(submitTime);
        ecogAnswer.setType(answer);

        Query checkUnique = userRef.orderByChild("time").equalTo(submitTime);
        checkUnique.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //update existing data
                    DataSnapshot nodeDataSnapshot = dataSnapshot.getChildren().iterator().next();
                    String key = nodeDataSnapshot.getKey(); // this key is the eid of the existing data
                    userRef.child(key).child("type").setValue(answer);
                    Toast.makeText(getApplicationContext() , "Updated ~" , Toast.LENGTH_SHORT).show();
                } else { //add new data
                    userRef.push().setValue(ecogAnswer);
                    Toast.makeText(getApplicationContext() , "Added ~" , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException(); // don't ignore errors
            }
        });

    }
    public void iniView(){
        /* * * * * toolbar * * * * * */

        //used for setting icon-font
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/iconfont.ttf");
        toolbar = (Toolbar) findViewById(R.id.bssNewToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //set icon-font: back
        txt_menu_back = (TextView) toolbar.findViewById(R.id.toolbar_back);
        txt_menu_back.setTypeface(font);
        //set icon-font: send
        txt_menu_send = (TextView) toolbar.findViewById(R.id.toolbar_send);
        txt_menu_send.setTypeface(font);

        //set click event
        txt_menu_send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(answer != -1){
                    sendData();
                    EcogSurveyActivity.this.finish();
                } else {
                    showSendDialog();
                }

            }
        });
        txt_menu_back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                EcogSurveyActivity.this.finish();
            }
        });

        /* * * * * body * * * * * */

        //set icon font for date_txt
        txt_date = (TextView) findViewById(R.id.date_txt);
        txt_date.setTypeface(font);

        tv_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pvTime.show(view);
            }
        });

        //display the discription of each scale
        scale_note = (TextView) findViewById(R.id.scale_note);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId){
                    case R.id.rb_0:
                        scale_note.setVisibility(View.VISIBLE);
                        //scale_note.setText(getResources().getString(R.string.ecog_new_scale0));
                        scale_note.setText(R.string.ecog_new_scale0);
                        answer = 0;
                        break;
                    case R.id.rb_1:
                        scale_note.setVisibility(View.VISIBLE);
                        //scale_note.setText(getResources().getString(R.string.ecog_new_scale0));
                        scale_note.setText(R.string.ecog_new_scale1);
                        answer = 1;
                        break;
                    case R.id.rb_2:
                        scale_note.setVisibility(View.VISIBLE);
                        //scale_note.setText(getResources().getString(R.string.ecog_new_scale0));
                        scale_note.setText(R.string.ecog_new_scale2);
                        answer = 2;
                        break;
                    case R.id.rb_3:
                        scale_note.setVisibility(View.VISIBLE);
                        //scale_note.setText(getResources().getString(R.string.ecog_new_scale0));
                        scale_note.setText(R.string.ecog_new_scale3);
                        answer = 3;
                        break;
                    case R.id.rb_4:
                        scale_note.setVisibility(View.VISIBLE);
                        //scale_note.setText(getResources().getString(R.string.ecog_new_scale0));
                        scale_note.setText(R.string.ecog_new_scale4);
                        answer = 4;
                        break;
                    default:
                        scale_note.setVisibility(View.INVISIBLE);
                }
            }



        });
    }
    private void showSendDialog() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.qol_survey_onebtn_dialog,null);
        MyRoundCornerButton dialogBtnOk = (MyRoundCornerButton) dialogView.findViewById(R.id.btn_ok);
        dialogBtnOk.setFillet(true);
        dialogBtnOk.setRadius(13);
        dialogBtnOk.setBackColor(getResources().getColor(R.color.wv_dialog_red));
        dialogBtnOk.setBackColorSelected(getResources().getColor(R.color.wv_dialog_red_press));
        dialogBtnOk.setTextColori(getResources().getColor(R.color.white));
        dialogBtnOk.setText(getResources().getString(R.string.ok));

        TextView dialogTitle = (TextView) dialogView.findViewById(R.id.txt_warning);
        TextView dialogMessage = (TextView) dialogView.findViewById(R.id.txt_content);
        //reset the title
        dialogTitle.setText(R.string.incomplete);
        //reset the message
        dialogMessage.setText(R.string.ecog_new_alertmsg);

        builder = new AlertDialog.Builder(this);
        //builder.setTitle(getString(R.string.icon_warning));
        builder.setView(dialogView);
        //cannot cancel the dialog when click other places
        builder.setCancelable(false);
        alert = builder.create();
        alert.show();

        dialogBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
    }

    private void initTimePicker() {
        pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                submitTime = TimeMethods.getDateStringForDb(date); //format: "yyyy-MM-dd"
                tv_time.setText(TimeMethods.getTimeStringForTxt(date));//format: "dd/MMM/yyyy HH:mm"
                Log.e("pvTime", TimeMethods.getTimeStringForDb(date));

            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                        Log.i("pvTime", "onTimeSelectChanged");
                    }
                })
                .setType(new boolean[]{false, true, true, true, true, false})//year,month,day,hour,minute,second
                .setLabel("", "", "", ":", "", "")
                .isCenterLabel(true)
                .setTitleText("Select time")
                .setSubmitText("OK")
                .setCancelText("Cancel")
                .isDialog(true) //default: false
                .build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);
                dialogWindow.setGravity(Gravity.CENTER);  //display the picker in the center
            }
        }
    }
}