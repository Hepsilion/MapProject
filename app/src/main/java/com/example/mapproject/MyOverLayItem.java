package com.example.mapproject;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.OverlayItem;

/********************************************标记****************************************************/
/* 1、想要在地图上标注一个物体，得有一个标识
 * 2、在基础图上添加覆盖物（添加图层）
 *   a. 编写覆盖物类，自己定义一个类，继承自ItemizedOverlay类，需要重写父类的构造函数、createItem(int index)和size()方法。
 *    注：从2.0.0开始，SDK不支持直接继承Overlay , 用户可通过继承ItemizedOverlay来添加覆盖物。
 *   b. 在自定义的覆盖物类（继承自ItemizedOverlay）中， 声明一个用于存放覆盖物的集合：
 * 3、记得构造函数定义好，我是接受一个地理位置点和一个姓名
 *   构造OverlayItem对象并添加到集合里
 */
/**********************************************************/

// 这个函数是一个接口类，将来用于接收好友位置，创建overlayitem对象，代表图层上的一个标记

// 另外要写一个函数用于创建该对象，并将其将入到图层中

public class MyOverLayItem extends ItemizedOverlay<OverlayItem> {
	// 创建一个List对象，用于存放所有标记对象
	private List<OverlayItem> list = new ArrayList<OverlayItem>();
	private Context context;

	// 传进来一个位置和好友名字，然后将其显示在地图上
	// 第一个参数用于指定标记所使用的默认图片
	public MyOverLayItem(Drawable drawable, Context context, GeoPoint point,
						 String name) {
		// 必须调用调用父类的构造函数
		// boundCenterBottom用来将指示的点放在标记的下方的正中央
		super(boundCenterBottom(drawable));
		this.context = context;
		// 用于将生成好的overlayItem对象添加到list当中
		// 位置，标题，内容
		list.add(new OverlayItem(point, name, name));

		/*
		 * 官方的解释：在一个新ItemizedOverlay上执行所有操作的工具方法。 没搞明白啥意思，但是必须的调用这个方法，否则程序运行报错
		 */
		populate();// 刷新地图的功能
	}

	// 用于创建一个overlayItem对象
	// arg0指的是现在创建的第几个overlayitem对象
	// 返回指定的list集合中每一个坐标
	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	// 返回当前overlay包含的overlayitem对象
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return list.size();
	}

	// 点用户点击了标记时，执行的操作
	// arg0指示点击的标记
	@Override
	public boolean onTap(int i) {
		// TODO Auto-generated method stub
		Toast.makeText(context, list.get(i).getSnippet(), 1).show();
		return true;
	}
}
