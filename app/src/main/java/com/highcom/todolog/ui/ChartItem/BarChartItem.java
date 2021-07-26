package com.highcom.todolog.ui.ChartItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.highcom.todolog.R;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class BarChartItem extends ChartItem {

    private List<Date> mDateRange;
//    private final Typeface mTf;

    public BarChartItem(List<Date> dateRange, ChartData<?> cd, Context c) {
        super(cd);
        mDateRange = dateRange;
//        mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
    }

    @Override
    public int getItemType() {
        return TYPE_BARCHART;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, Context c) {

        ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.chart_item_bar, null);
            holder.chart = convertView.findViewById(R.id.bar_chart);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        holder.chart.getDescription().setEnabled(false);
        holder.chart.setDrawGridBackground(false);
        holder.chart.setDrawBarShadow(false);

        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setTypeface(mTf);
        xAxis.setValueFormatter(new DateValueFormatter(mDateRange));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);

        YAxis leftAxis = holder.chart.getAxisLeft();
//        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(5, false);
        leftAxis.setSpaceTop(20f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = holder.chart.getAxisRight();
//        rightAxis.setTypeface(mTf);
        rightAxis.setLabelCount(5, false);
        rightAxis.setSpaceTop(20f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

//        mChartData.setValueTypeface(mTf);

        // set data
        holder.chart.setData((BarData) mChartData);
        holder.chart.setFitBars(true);

        // do not forget to refresh the chart
//        holder.chart.invalidate();
        holder.chart.animateY(700);

        return convertView;
    }

    private static class ViewHolder {
        BarChart chart;
    }
}
