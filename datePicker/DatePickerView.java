package com.song1.musicno1.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.song1.musicno1.R;
import com.song1.musicno1.helpers.TimeHelper;
import com.song1.musicno1.helpers.Util;
import com.song1.musicno1.tool.ScreenSizeCaculation;
import com.song1.musicno1.views.wheel.NumericWheelAdapter;
import com.song1.musicno1.views.wheel.OnWheelScrollListener;
import com.song1.musicno1.views.wheel.WheelView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author: nnero
 * <p>
 * time : 15/11/9  上午10:13
 * <p>
 * Description:日期选择器 基于开源项目WheelView
 */
public class DatePickerView extends RelativeLayout{

  private static final String YEAR = "年";
  private static final String MONTH = "月";
  private static final String DAY = "日";
  private static final int MIN_YEAR = 1990; //年最低值

  @InjectView(R.id.year_view) WheelView yearView;
  @InjectView(R.id.month_view) WheelView monthView;
  @InjectView(R.id.day_view) WheelView dayView;

  private Calendar calendar;
  private View datePickView; //布局填充形式 植入选择器

  private int defCurTvSize = 40; //默认中心位置字体
  private int defCurItemSize = 35;//默认外围位置字体

  private NumericWheelAdapter yearAdapter;
  private NumericWheelAdapter monthAdapter;
  private NumericWheelAdapter dayAdapter;

  public DatePickerView(Context context) {
    super(context);
    init();
  }

  public DatePickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public DatePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init(){
    setPadding(0, ScreenSizeCaculation.dip2px(getContext(),25),0,ScreenSizeCaculation.dip2px(getContext(),25));
    calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    initViews();
    initAdapter();
    initListeners();
    initCurrentDay();
  }
  //设置当前时间是 今天
  private void initCurrentDay() {
    int currentYear = calendar.get(Calendar.YEAR);
    int currentMonth = calendar.get(Calendar.MONTH);
    int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

    changeDayByMonth(currentMonth);

    yearView.setCurrentItem(currentYear-MIN_YEAR);
    monthView.setCurrentItem(currentMonth);
    dayView.setCurrentItem(currentDay-1);
  }

  private void initViews(){
    datePickView = View.inflate(getContext(), R.layout.date_picker_view, null);
    ButterKnife.inject(this, datePickView);

    LinearLayout centerArea = new LinearLayout(getContext());
    centerArea.setOrientation(LinearLayout.VERTICAL);
    RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    centerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    centerArea.setLayoutParams(centerParams);

    View lineViewTop = new View(getContext());
    View lineViewDown = new View(getContext());
    lineViewTop.setBackgroundColor(getResources().getColor(R.color.black));
    lineViewDown.setBackgroundColor(getResources().getColor(R.color.black));
    LinearLayout.LayoutParams lineTopParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ScreenSizeCaculation.dip2px(getContext(),1));
    LinearLayout.LayoutParams lineDownParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ScreenSizeCaculation.dip2px(getContext(),1));
    lineDownParams.topMargin =  ScreenSizeCaculation.dip2px(getContext(),35);
    centerArea.addView(lineViewTop,lineTopParams);
    centerArea.addView(lineViewDown,lineDownParams);

    yearView.setVisibleItems(5);
    monthView.setVisibleItems(5);
    dayView.setVisibleItems(5);

    int curTextSize = Util.getFrontSize(getContext(), defCurTvSize);
    int itemSize = Util.getFrontSize(getContext(),defCurItemSize);
    yearView.setTextSize(curTextSize, itemSize);
    monthView.setTextSize(curTextSize, itemSize);
    dayView.setTextSize(curTextSize, itemSize);

    yearView.setCyclic(true);
    monthView.setCyclic(true);
    dayView.setCyclic(true);

    addView(datePickView);
    addView(centerArea);
  }

  private void initAdapter(){
    //dayAdapter 会在 不停的变化 所以在changeDayByMonth里创建
    yearAdapter = new NumericWheelAdapter(MIN_YEAR, calendar.get(Calendar.YEAR), "%1s" + YEAR);
    monthAdapter = new NumericWheelAdapter(1, 12, "%1s" + MONTH);
    dayView.setAdapter(dayAdapter);
    yearView.setAdapter(yearAdapter);
    monthView.setAdapter(monthAdapter);
  }

  private void initListeners(){
    yearView.addScrollingListener(new OnWheelScrollListener() {
      @Override
      public void onScrollingStarted(WheelView wheel) {
      }

      @Override
      public void onScrollingFinished(WheelView wheel) {
        changeDayByMonth(monthView.getCurrentItem() + 1);
      }
    });
    monthView.addScrollingListener(new OnWheelScrollListener() {
      @Override
      public void onScrollingStarted(WheelView wheel) {
      }

      @Override
      public void onScrollingFinished(WheelView wheel) {
        changeDayByMonth(monthView.getCurrentItem()+1);
      }
    });
  }
  //根据月份先改变 day的 数值  分为 大月 小月 2月 包括闰年判断
  private void changeDayByMonth(int month){
    int day = 0;
    switch (month){
      case 1:
      case 3:
      case 5:
      case 7:
      case 8:
      case 10:
      case 12:
        day = 31;
        break;
      case 4:
      case 6:
      case 9:
      case 11:
        day = 30;
        break;
      case 2:
        String year = yearAdapter.getItem(yearView.getCurrentItem());
        year = year.replace(YEAR,"");
        try { //防止意外
          if(TimeHelper.isLeapYear(Integer.parseInt(year))){
            day = 29;
          } else {
            day = 28;
          }
        } catch (NumberFormatException e) {
          day = 28;
        }
        break;
    }
    dayAdapter = new NumericWheelAdapter(1,day,"%1s"+DAY);
    dayView.setAdapter(dayAdapter);
  }

  /**
   * 获取选中的日期 yyyy-MM-dd
   * @return
   */
  public String getDate(){
    return yearView.getCurrentItem()+"-"+monthView.getCurrentItem()+"-"+dayView.getCurrentItem();
  }

  /**
   * 支持 yyyy-MM-dd 的日期
   */
  public void setDate(String date){
    String[] elements = date.split("-");
    try {
      int year = Integer.parseInt(elements[0]);
      int month = Integer.parseInt(elements[1]);
      int day = Integer.parseInt(elements[2]);
      yearView.setCurrentItem(year-MIN_YEAR);
      monthView.setCurrentItem(month);
      dayView.setCurrentItem(day-1);
    } catch (NumberFormatException e) {
    }
  }
}
