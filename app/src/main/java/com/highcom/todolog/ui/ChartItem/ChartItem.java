package com.highcom.todolog.ui.ChartItem;

import android.content.Context;
import android.view.View;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public abstract class ChartItem {

    static final int TYPE_BARCHART = 0;
    static final int TYPE_LINECHART = 1;
    static final int TYPE_PIECHART = 2;

    ChartData<?> mChartData;

    ChartItem(ChartData<?> cd) {
        this.mChartData = cd;
    }

    public abstract int getItemType();

    public abstract View getView(int position, View convertView, Context c);

    protected class DateValueFormatter extends ValueFormatter {
        private List<Date> mDateRange;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");

        public DateValueFormatter(List<Date> dates) {
            mDateRange = dates;
        }

        @Override
        public String getFormattedValue(float value) {
            return sdf.format(mDateRange.get((int)value));
        }
    }

}
