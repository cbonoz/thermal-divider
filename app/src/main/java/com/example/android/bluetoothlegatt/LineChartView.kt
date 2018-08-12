package com.example.android.bluetoothlegatt

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.android.bluetoothlegatt.models.TempRecord
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlin.collections.ArrayList
import kotlin.math.min


class LineChartView(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var mChart: LineChart

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val ctx: Context
        if (context == null) {
            ctx = ThermApplication.app as Context
        } else {
            ctx = context
        }

        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.line_chart_view, this, true)


        mChart = view.findViewById<LineChart>(R.id.lineChart)

        // no description text
        mChart.getDescription().setEnabled(false)

        // enable touch gestures
        mChart.setTouchEnabled(true)

        mChart.setDragDecelerationFrictionCoef(0.9f)

        // enable scaling and dragging
        mChart.setDragEnabled(true)
        mChart.setScaleEnabled(true)
        mChart.setDrawGridBackground(false)
        mChart.setHighlightPerDragEnabled(true)

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE)
        mChart.setViewPortOffsets(0f, 0f, 0f, 0f)

        // add data
//        setData(100, 30)
        mChart.invalidate()

        // get the legend (only possible after setting data)
        val l = mChart.getLegend()
        l.setEnabled(false)

        val xAxis = mChart.getXAxis()
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE)
        xAxis.setTextSize(10f)
        xAxis.setTextColor(Color.WHITE)
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.setTextColor(Color.rgb(255, 192, 56))
        xAxis.setCenterAxisLabels(true)
        xAxis.setGranularity(1f) // one hour
        xAxis.valueFormatter = object : IAxisValueFormatter {

            private val mFormat = SimpleDateFormat("dd MMM HH:mm")

            override fun getFormattedValue(value: Float, axis: AxisBase): String {

                val millis = TimeUnit.HOURS.toMillis(value.toLong())
                return mFormat.format(Date(millis))
            }
        }

        val leftAxis = mChart.getAxisLeft()
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        leftAxis.setTextColor(ColorTemplate.getHoloBlue())
        leftAxis.setDrawGridLines(true)
        leftAxis.setGranularityEnabled(true)
        leftAxis.setAxisMinimum(0f)
        leftAxis.setAxisMaximum(170f)
        leftAxis.setYOffset(-9f)
        leftAxis.setTextColor(Color.rgb(255, 192, 56))

        val rightAxis = mChart.getAxisRight()
        rightAxis.setEnabled(false)
    }

    // https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/LineChartTime.java
    fun setData(rawColdValues: List<TempRecord>, rawHotValues: List<TempRecord>) {

        val sortedCold = rawColdValues.sortedBy { it.timestamp }
        val sortedHot = rawHotValues.sortedBy { it.timestamp }

        val minTimestamp: Long
        if (sortedCold.isEmpty() && sortedHot.isEmpty()) {
            return
        } else if (sortedCold.isEmpty()) {
            minTimestamp = sortedHot[0].timestamp
        } else if (sortedHot.isEmpty()){
            minTimestamp = sortedCold[0].timestamp
        } else {
            minTimestamp = min(sortedCold[0].timestamp, sortedHot[0].timestamp)
        }

        // now in hours
//        val now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis())

        val sets = ArrayList<LineDataSet>()
        if (sortedCold.isNotEmpty()) {
            val coldValues = sortedCold.map {
                Entry((it.timestamp - minTimestamp).toFloat(), it.value.toFloat())
            }

            // create a dataset and give it a type
            val set1 = LineDataSet(coldValues, "Cold Values")
            set1.axisDependency = AxisDependency.LEFT
            set1.color = ColorTemplate.PASTEL_COLORS[0]
            set1.valueTextColor = ColorTemplate.PASTEL_COLORS[0]
            set1.lineWidth = 1.5f
            set1.setDrawCircles(false)
            set1.setDrawValues(false)
            set1.fillAlpha = 65
            set1.fillColor = ColorTemplate.getHoloBlue()
            set1.highLightColor = Color.rgb(244, 117, 117)
            set1.setDrawCircleHole(false)
            sets.add(set1)
        }

        if (sortedHot.isNotEmpty()) {

            val hotValues = sortedCold.map {
                Entry((it.timestamp - minTimestamp).toFloat(), it.value.toFloat())
            }

            // create a dataset and give it a type
            val set2 = LineDataSet(hotValues, "Hot Values")
            set2.axisDependency = AxisDependency.LEFT
            set2.color = ColorTemplate.PASTEL_COLORS[2]
            set2.valueTextColor = ColorTemplate.PASTEL_COLORS[2]
            set2.lineWidth = 2.5f
            set2.setDrawCircles(false)
            set2.setDrawValues(false)
            set2.fillAlpha = 65
            set2.fillColor = ColorTemplate.getHoloBlue()
            set2.highLightColor = Color.rgb(244, 227, 227)
            set2.setDrawCircleHole(false)
            sets.add(set2)
        }

        // create a data object with the datasets
        val data = LineData(sets as MutableList<ILineDataSet>)
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(9f)

        // set data
        mChart.data = data
    }


}