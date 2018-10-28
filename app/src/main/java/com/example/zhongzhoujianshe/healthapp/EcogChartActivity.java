package com.example.zhongzhoujianshe.healthapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.components.AxisBase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EcogChartActivity extends AppCompatActivity implements OnChartValueSelectedListener  {
    //UI objects
    //toolbar part
    private Toolbar toolbar;
    private TextView txt_menu_back;
    private TextView txt_menu_new;
    private MyRoundCornerButton btn1;
    private MyRoundCornerButton btn2;
    private MyRoundCornerButton btn3;
    private HorizontalBarChart hBarChart;
    private LineChart lineChart;
    private TextView txt_date_range;
    private TextView txt_overview;
    //firebase
    private String currentUserId;
    private DatabaseReference mRoot;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //data
    private ArrayList<String> dateAll = new ArrayList<>();
    private ArrayList<String> dateWeek = new ArrayList<>();
    private ArrayList<String> dateMonth = new ArrayList<>();
    private int[] dateIdAll;  //line: x, converted from dateAll
    private int[] dateIdMonth;  //line: x, converted from dateMonth
    private int[] dateIdWeek; //line: x, converted from dateWeek
    private ArrayList<Integer> typeIdAll;   //line: y
    private ArrayList<Integer> typeIdMonth;  //line: y
    private ArrayList<Integer> typeIdWeek; //line: y
    private int[] barAll;  //value of bar chart, i.e. count
    private int[] barMonth;
    private int[] barWeek;
    //charts
    private List<Entry> lineWeek;
    private List<Entry> lineMonth;
    private List<Entry> lineAll;
    private ArrayList<BarEntry> barWeekEntry;
    private ArrayList<BarEntry> barMonthEntry;
    private ArrayList<BarEntry> barAllEntry;

    //variables
    private String DateToday;
    private String DateLastWeekMon;
    private String DateLastWeekSun;
    private String DateLastMonthFirst;
    private String DateLastMonthLast;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecog_chart);



        /* * * * * initialize view  * * * * * */
        iniView();

        /* * * * * get date of today, last month and last week * * * * * */

        //get today's date
        //DateToday = TimeMethods.getDateToday();
        //Log.e("today","----"+DateToday);

        //get last week (date range)
        //Date date = new Date();
        String[] lastweek = TimeMethods.getDateLastWeek();
        DateLastWeekMon = lastweek[0];
        DateLastWeekSun = lastweek[1];

        //get last month
        String[] lastmonth = TimeMethods.getDateLastMonth();
        DateLastMonthLast = lastmonth[1];
        DateLastMonthFirst = lastmonth[0];



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
                    Log.e("TAG", "onAuthStateChanged:signed_in:" + currentUserId);
                    mRoot = FirebaseDatabase.getInstance().getReference();
                    userRef = mRoot.child(currentUserId).child("ecog");

                    getDataLastWeek(); //get data for : dateWeek & typeIdWeek & barWeek
                    // getChartData();
                    setBtn1Click();   //same as default
                }else {
                    Log.e("TAG", "onAuthStateChanged:signed_out");
                }
            }
        };

        //click
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                getDataLastWeek(); //get data for : dateWeek & typeIdWeek & barWeek
                setBtn1Click();   //same as default
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                getDataLastMonth(); //get data for : dateWeek & typeIdWeek & barWeek


                btn2.setTextColori(getResources().getColor(R.color.chartDarkBlue));
                btn1.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
                btn3.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
                btn2.setBackColor(getResources().getColor(R.color.ecogChartTopBlue));
                btn1.setBackColor(getResources().getColor(R.color.chartDarkBlue));
                btn3.setBackColor(getResources().getColor(R.color.chartDarkBlue));

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                getDataAll(); //get data for : dateWeek & typeIdWeek & barWeek
                btn3.setTextColori(getResources().getColor(R.color.chartDarkBlue));
                btn2.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
                btn1.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
                btn3.setBackColor(getResources().getColor(R.color.ecogChartTopBlue));
                btn2.setBackColor(getResources().getColor(R.color.chartDarkBlue));
                btn1.setBackColor(getResources().getColor(R.color.chartDarkBlue));



            }
        });


    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setBtn1Click(){
        //lineChart = findViewById(R.id.lineChart);

        btn1.setTextColori(getResources().getColor(R.color.chartDarkBlue));
        btn2.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
        btn3.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
        btn1.setBackColor(getResources().getColor(R.color.ecogChartTopBlue));
        btn2.setBackColor(getResources().getColor(R.color.chartDarkBlue));
        btn3.setBackColor(getResources().getColor(R.color.chartDarkBlue));

    }


    @RequiresApi(api = Build.VERSION_CODES.N)

    /**
     * initialize the attributes of the line chart
     */
    private void initLineChart(final List<Entry> lineData) {

        lineChart.setOnChartValueSelectedListener(this);
        // 设置是否可以缩放图表
        lineChart.setScaleEnabled(true);
        // 设置是否可以用手指移动图表
        lineChart.setDragEnabled(true);

        lineChart.setNoDataText("No chart data available.");  //没数据后的显示
        lineChart.setNoDataTextColor(Color.WHITE);
        lineChart.getAxisRight().setEnabled(false); //禁用右侧y轴
        lineChart.getDescription().setEnabled(false);  // 不显示标签
        lineChart.animateY(2500);
        lineChart.animateX(1200);


        //自定义适配器，适配于X轴
        //String[] xStrs = new String[]{ "","就来", "阿建", "阿四","阿海","k","p","l","s","end"}; // 线图横坐标文字
        //myBarChartFormatter aoz = new myBarChartFormatter(xStrs);



        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        /*
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xAxisDisplay.get((int) value); // xVal is a string array
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
*/

        if (lineData.size() > 0) {
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    if ((int) value >= lineData.size()) {
                        return "";
                    } else {
                        return lineData.get((int) value).getData() + "";
                    }
                }
            });
        }

        //xAxis.setValueFormatter((value, axis) -> lineData.get((int)      value).getData()+"");
       // xAxis.setValueFormatter(formatter);

        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(1f);
        //xAxis.setValueFormatter((value, axis) -> lineData.get((int)      value).getData()+"");
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelCount(3,true);
        //xAxis.setLabelCount(xAxisDisplay.size());
        // xAxis.setAxisMinimum(1f);  // from which data

        //自定义适配器，适配于Y轴
        IAxisValueFormatter patint = new MyAxisValueFormatter();

        YAxis leftAxis = lineChart.getAxisLeft();

        leftAxis.setValueFormatter(patint);

        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(true);

        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setTextColor(Color.WHITE);

        Legend l = lineChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setTextColor(Color.WHITE);



        setLineChartData(lineData);
    }

    private void setLineChartData(List<Entry> lineData) {


        LineDataSet setComp1 = new LineDataSet(lineData, " Score ");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.WHITE);
        setComp1.setDrawCircles(true);
        setComp1.setDrawValues(false);

        setComp1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        List<ILineDataSet> dataSets = new ArrayList<>();

        dataSets.add(setComp1);

        LineData lineChartData = new LineData(dataSets);


        lineChart.setData(lineChartData);

        lineChart.invalidate();// refresh




    }

    private void initHBarChart(ArrayList<BarEntry> barDataset) {
        hBarChart.setOnChartValueSelectedListener(this);
        hBarChart.setDrawBarShadow(false);
        hBarChart.setDrawValueAboveBar(true);
        hBarChart.setMaxVisibleValueCount(60);
        hBarChart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // mChart.setDrawBarShadow(true);


        //自定义坐标轴适配器，设置在X轴
        //String[] xStrs = new String[]{ "","Grade0", "Grade1", "Grade2","Grade3","Grade4"};
        String[] xStrs = new String[]{"", "Grade0", "Grade1", "Grade2","Grade3","Grade4"};

        //int[] fourColor = new int[]{Color.rgb(102, 205, 0), Color.rgb(162, 205, 90),Color.rgb(205, 190, 112),Color.rgb(238, 180, 34),Color.rgb(255, 130, 71)};
        int[] fiveColor = new int[]{
                Color.rgb(0, 238, 0),
                Color.rgb(0, 205, 0),
                Color.rgb(102, 205, 0),
                Color.rgb(238, 180, 34),
                Color.rgb(255, 130, 71)};
        //int[] sevenColor = new int[]{Color.rgb(0, 238, 0),Color.rgb(0, 205, 0),Color.rgb(102, 205, 0), Color.rgb(162, 205, 90),Color.rgb(205, 190, 112),Color.rgb(238, 180, 34),Color.rgb(255, 130, 71)};

        //自定义适配器，适配于X轴
        myBarChartFormatter xAxisFormatter = new myBarChartFormatter(xStrs);// 自定义y轴
        XAxis xl = hBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        xl.setGranularity(1f);
        xl.setAxisMinimum(0.2f);
        xl.setValueFormatter(xAxisFormatter);



        xl.setTextColor(Color.WHITE);
        xl.setAxisLineColor(Color.WHITE);
        //xl.setDrawLabels(false);   // 不要y轴的标签！


        //对Y轴进行设置
        YAxis yl = hBarChart.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(false);
        yl.setGranularity(1f);
        yl.setAxisMinimum(0f);
        yl.setTextColor(Color.WHITE);
        yl.setAxisLineColor(Color.WHITE);



        // this replaces setStartAtZero(true)
        // yl.setInverted(true);

        hBarChart.getAxisRight().setEnabled(false);

        //hBarChart.getAxisRight().setDrawGridLines(false);
        //  hBarChart.getAxisRight().setDrawGridLines(false);


        //图例设置
        Legend l = hBarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(true);
        l.setTextColor(Color.WHITE);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);


        setHBarChartData(barDataset,fiveColor);
        hBarChart.setFitBars(true);
        hBarChart.getDescription().setEnabled(false);
        hBarChart.animateY(2500);  // animation
    }


    /**
     * 设置水平柱形图数据的方法
     */
    private void setHBarChartData(ArrayList<BarEntry> barDataset,int[] fourColor) {
        //填充数据，在这里换成自己的数据源


        BarDataSet set1;


        if (hBarChart.getData() != null &&
                hBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) hBarChart.getData().getDataSetByIndex(0);
            set1.setValues(barDataset);

            hBarChart.getData().notifyDataChanged();
            hBarChart.notifyDataSetChanged();
        } else {

            set1 = new BarDataSet(barDataset, "Type");

            set1.setDrawIcons(false);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setBarWidth(0.9f);
            data.setValueTextSize(10f);
            data.setValueTextColor(Color.WHITE);
            set1.setColors(fourColor);


            hBarChart.setData(data);
        }
    }

    private void iniView() {
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
        txt_menu_new = (TextView) toolbar.findViewById(R.id.toolbar_new);
        txt_menu_new.setTypeface(font);

        //set click event
        txt_menu_new.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(EcogChartActivity.this, EcogSurveyActivity.class);
                startActivity(intent);

            }
        });
        txt_menu_back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                EcogChartActivity.this.finish();
            }
        });

        /* * * * * body * * * * * */

        btn1 = (MyRoundCornerButton) findViewById(R.id.btn1);
        btn2 = (MyRoundCornerButton) findViewById(R.id.btn2);
        btn3 = (MyRoundCornerButton) findViewById(R.id.btn3);
        //shape left to right: left corner, rectangle, right corner
        btn1.setFillet(true);
        btn1.setPartRadius(15, 0, 0, 15);
        btn3.setFillet(true);
        btn3.setPartRadius(0, 15, 15, 0);
        //border
        btn1.setStroke(3, getResources().getColor(R.color.ecogChartTopBlue));
        btn3.setStroke(3, getResources().getColor(R.color.ecogChartTopBlue));
        btn2.setBorderTop(true);
        btn2.setBorderBottom(true);
        btn2.setBorderWidth(5);
        btn2.setBorderColor(getResources().getColor(R.color.ecogChartTopBlue));
        //background color
        btn1.setBackColor(getResources().getColor(R.color.chartDarkBlue));
        btn2.setBackColor(getResources().getColor(R.color.chartDarkBlue));
        btn3.setBackColor(getResources().getColor(R.color.chartDarkBlue));
        //text color
        btn1.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
        btn2.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
        btn3.setTextColori(getResources().getColor(R.color.ecogChartTopBlue));
        //text
        btn1.setText(getResources().getString(R.string.chart_week));
        btn2.setText(getResources().getString(R.string.chart_month));
        btn3.setText(getResources().getString(R.string.chart_all));

        //chart
        hBarChart = findViewById(R.id.hBarChart);
        lineChart = findViewById(R.id.lineChart);

        txt_overview = findViewById(R.id.overview);
        txt_date_range = findViewById(R.id.textView13);


    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }


    //set view shows when no data available
    private void setNoDataView(){
        lineChart.setNoDataText("No chart data available");
        lineChart.setNoDataTextColor(getResources().getColor(R.color.white));
        hBarChart.setNoDataText("");
        txt_date_range.setText("");
        txt_overview.setText("");
    }

    //get data: last week
    private void getDataLastWeek() {
        // Get a reference to our record

        Query filter = userRef.orderByChild("time").startAt(DateLastWeekMon).endAt(DateLastWeekSun);
        // Attach an listener to read the data at our posts reference
        filter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dateWeek = new ArrayList<>();
                typeIdWeek = new ArrayList<>();
                Log.e("WEEK ", "-- There are " +String.valueOf(snapshot.getChildrenCount()));
                if (snapshot.exists()){
                    for (DataSnapshot bssSnapshot: snapshot.getChildren()) {
                        EcogAndBssAnswerModel bssA = bssSnapshot.getValue(EcogAndBssAnswerModel.class);
                        String answerDate = bssA.getTime();
                        dateWeek.add(answerDate);
                        int type = bssA.getType();
                        typeIdWeek.add(type);

                        Log.e("Go to", "getMonthChartData");
                        getWeekChartData();
                    }
                }else{
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ",databaseError.getMessage());
            }

        });


    }
    private void getWeekChartData(){
        if (!dateWeek.isEmpty()){
            //set text for time ranges for bar chart
            txt_overview.setText(getResources().getString(R.string.overview));
            txt_date_range.setText(TimeMethods.getDateRange(dateWeek, dateWeek.size(), false));

            lineWeek = new ArrayList<>();
            barWeekEntry = new ArrayList<>();
            barWeek = new int[]{0,0,0,0,0,0};
            dateIdWeek = TimeMethods.getDayDuration(dateWeek, dateWeek.size());
            //dateIdWeek = getDurationEqual(dateWeek, dateWeek.size());
            for(int j = 0; j < dateIdWeek.length; j++){

                String[] label = TimeMethods.getXAxisText(dateWeek, dateWeek.size(), false);
                lineWeek.add(new Entry(dateIdWeek[j], typeIdWeek.get(j),label[j]));
                // lineWeek.add(new Entry(dateIdWeek[j], typeIdWeek.get(j)));

                for(int k = 1; k < barWeek.length; k++){
                    if(typeIdWeek.get(j) == k - 1){
                        barWeek[k] = barWeek[k] + 1;
                    }
                }
            }
            for (int k = 1; k < barWeek.length; k++){
                barWeekEntry.add(new BarEntry(k, barWeek[k]));
            }

            initHBarChart(barWeekEntry);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                initLineChart(lineWeek);
            }
            lineChart.isDrawMarkersEnabled();
            lineChart.setDrawMarkers(true);
        }else {
            setNoDataView();
        }
    }
    //get data: last month
    private void getDataLastMonth() {
        // Get a reference to our record
        Query filter = userRef.orderByChild("time").startAt(DateLastMonthFirst).endAt(DateLastMonthLast);
        // Attach an listener to read the data at our posts reference
        filter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dateMonth = new ArrayList<>();
                typeIdMonth = new ArrayList<>();
                Log.e("MONTH ", "-- There are " +String.valueOf(snapshot.getChildrenCount()));
                for (DataSnapshot bssSnapshot: snapshot.getChildren()) {
                    //add the data to the arraylist
                    EcogAndBssAnswerModel bssA = bssSnapshot.getValue(EcogAndBssAnswerModel.class);
                    String answerDate = bssA.getTime();
                    dateMonth.add(answerDate);
                    int type = bssA.getType();
                    typeIdMonth.add(type);

                    //Log.e("MONTH ", answerDate +": "+type);

                    Log.e("Go to", "getMonthChartData");
                    getMonthChartData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ",databaseError.getMessage());
            }

        });
    }
    private void getMonthChartData(){
        if (!dateMonth.isEmpty()){
            //set text for time ranges for bar chart
            txt_overview.setText(getResources().getString(R.string.overview));
            txt_date_range.setText(TimeMethods.getDateRange(dateMonth, dateMonth.size(), false));

            lineMonth = new ArrayList<>();
            barMonthEntry = new ArrayList<>();
            barMonth = new int[]{0,0,0,0,0,0};
            dateIdMonth = TimeMethods.getDayDuration(dateMonth, dateMonth.size());
            //dateIdMonth = getDurationEqual(dateMonth, dateMonth.size());

            for(int j = 0; j < dateIdMonth.length; j++){
                String[] label = TimeMethods.getXAxisText(dateMonth, dateMonth.size(), false);
                lineMonth.add(new Entry(dateIdMonth[j], typeIdMonth.get(j),label[j]));
                //lineMonth.add(new Entry(dateIdMonth[j], typeIdMonth.get(j)));
                for(int k = 1; k < barMonth.length; k++){
                    if(typeIdMonth.get(j) == k - 1){
                        barMonth[k] = barMonth[k] + 1;
                    }
                }
            }

            for (int k = 1; k < barMonth.length; k++){
                barMonthEntry.add(new BarEntry(k, barMonth[k]));
            }

            initHBarChart(barMonthEntry);  //统计完以后插入纵坐标就好
            //List<String> xAxisDisplay = TimeMethods.getXAxis(dateMonth, dateMonth.size(), false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                initLineChart(lineMonth);
            }
            lineChart.isDrawMarkersEnabled();
            lineChart.setDrawMarkers(true);
        }else {
            setNoDataView();
        }
    }
    //get data: all
    private void getDataAll() {
        // Get a reference to our record
        // Attach an listener to read the data at our posts reference
        Query filter = userRef.orderByChild("time");
        filter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dateAll = new ArrayList<>();
                typeIdAll = new ArrayList<>();
                Log.e("ALL ", "-- There are " +String.valueOf(snapshot.getChildrenCount()));
                for (DataSnapshot bssSnapshot: snapshot.getChildren()) {
                    //add the data to the arraylist
                    EcogAndBssAnswerModel bssA = bssSnapshot.getValue(EcogAndBssAnswerModel.class);
                    String answerDate = bssA.getTime();
                    dateAll.add(answerDate);
                    int type = bssA.getType();
                    typeIdAll.add(type);
                    Log.e("ALL ", answerDate +": "+type);

                    Log.e("Go to", "getMonthChartData");
                    getAllChartData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: ",databaseError.getMessage());
            }

        });
    }
    private void getAllChartData(){
        if (!dateAll.isEmpty()){
            //set text for time ranges for bar chart
            txt_overview.setText(getResources().getString(R.string.overview));
            txt_date_range.setText(TimeMethods.getDateRange(dateAll, dateAll.size(), false));

            lineAll = new ArrayList<>();
            barAllEntry = new ArrayList<>();
            barAll = new int[]{0,0,0,0,0,0};
            dateIdAll = TimeMethods.getDayDuration(dateAll, dateAll.size());
            //dateIdAll = getDurationEqual(dateAll, dateAll.size());
            for(int j = 0; j < dateIdAll.length; j++){
                String[] label = TimeMethods.getXAxisText(dateAll, dateAll.size(), false);
                lineAll.add(new Entry(dateIdAll[j], typeIdAll.get(j),label[j]));
                //lineAll.add(new Entry(dateIdAll[j], typeIdAll.get(j)));
                for(int k = 1; k < barAll.length; k++){
                    if(typeIdAll.get(j) == k - 1){
                        barAll[k] = barAll[k] + 1;
                    }
                }
            }

            for (int k = 1; k < barAll.length; k++){
                barAllEntry.add(new BarEntry(k , barAll[k]));
            }

            initHBarChart(barAllEntry);  //统计完以后插入纵坐标就好
            //List<String> xAxisDisplay = TimeMethods.getXAxis(dateAll, dateAll.size(), false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                initLineChart(lineAll);
            }
            lineChart.isDrawMarkersEnabled();
            lineChart.setDrawMarkers(true);
        }else {
            setNoDataView();
        }
    }



}


