package com.uas.appme.pedometer.utils;

import android.graphics.Color;
import android.view.View;

import com.uas.appme.pedometer.bean.ComPolylineBean;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by FANGlh on 2017/9/25.
 * function: 折线工具类
 */

public class PolylineUtils {
    /**
     * 初始化LineChart的一些设置
     * @param lineChart   控件id 的对象
     * @param cBeanList     包含 X，y轴对象的列表
     */
    public static void initLineChart(LineChartView lineChart,List<ComPolylineBean> cBeanList){
//        List<ComPolylineBean> cBeanList = new ArrayList<>();
//        cBeanList = doReverseOrder(list);
         List<PointValue> mPointValues = new ArrayList<>();
         List<AxisValue> mAxisXValues = new ArrayList<>();

        getAxisXLables(mAxisXValues,cBeanList);//获取x轴的标注
        getAxisPoints(mPointValues,cBeanList);//获取坐标点

        Line line = new Line(mPointValues).setColor(Color.WHITE);  //折线的颜色
        List<Line> lines = new ArrayList<>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.SQUARE）
        line.setCubic(false);//曲线是否平滑
        line.setStrokeWidth(1);//线条的粗细，默认是3
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
//		line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用直线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X轴下面坐标轴字体是斜的显示还是直的，true是斜的显示
	    axisX.setTextColor(Color.WHITE);  //设置字体颜色
//        axisX.setTextColor(Color.parseColor("#D6D6D9"));//灰色

//	    axisX.setName("日期");  //表格名称
        axisX.setTextSize(11);//设置字体大小
        axisX.setMaxLabelChars(7); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        axisX.setHasLines(true); //x 轴分割线


        Axis axisY = new Axis();  //Y轴
        axisY.setName(" ");//y轴标注
        axisY.setTextSize(11);//设置字体大小
        axisY.setTextColor(Color.WHITE);

        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边
        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);  //缩放类型，水平
        lineChart.setMaxZoom((float) 1);//缩放比例
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 尼玛搞的老子好辛苦！！！参考见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         * 下面几句可以设置X轴数据的显示个数（x轴0-7个数据），当数据点个数小于（29）的时候，缩小到极致hellochart默认的是所有显示。当数据点个数大于（29）的时候，
         * 若不设置axisX.setMaxLabelChars(int count)这句话,则会自动适配X轴所能显示的尽量合适的数据个数。
         * 若设置axisX.setMaxLabelChars(int count)这句话,
         * 33个数据点测试，若 axisX.setMaxLabelChars(10);里面的10大于v.right= 7; 里面的7，则
         刚开始X轴显示7条数据，然后缩放的时候X轴的个数会保证大于7小于10
         若小于v.right= 7;中的7,反正我感觉是这两句都好像失效了的样子 - -!
         * 并且Y轴是根据数据的大小自动设置Y轴上限
         * 若这儿不设置 v.right= 7; 这句话，则图表刚开始就会尽可能的显示所有数据，交互性太差
         */
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        lineChart.setCurrentViewport(v);
    }


    /**
     * 倒序排序操作
     * @param cBeanList
     * @return
     */
    private static List<ComPolylineBean> doReverseOrder(List<ComPolylineBean> cBeanList) {
        List<ComPolylineBean>  list = new ArrayList<>();
        for (int i = cBeanList.size() - 1; i >= 0 ; i++) {
            list.add(cBeanList.get(i));
        }
        return list;
    }

    /**
     *  X 轴的显示
     * @param mAxisXValues
     * @param cBeanList
     */
    private static void getAxisXLables(List<AxisValue> mAxisXValues,List<ComPolylineBean> cBeanList){
        for (int i = 0; i < cBeanList.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(cBeanList.get(i).getxValue()));
        }
    }

    /**
     * Y 轴的显示
     * @param mPointValues
     * @param cBeanList
     */
    private static void getAxisPoints(List<PointValue> mPointValues,List<ComPolylineBean> cBeanList){
        for (int i = 0; i < cBeanList.size(); i++) {
            mPointValues.add(new PointValue(i, cBeanList.get(i).getyValue()));
        }
    }
}
