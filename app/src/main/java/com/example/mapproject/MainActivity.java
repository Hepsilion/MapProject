package com.example.mapproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKStep;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.PoiOverlay;
import com.baidu.mapapi.RouteOverlay;
import com.example.client.Client;
import com.example.client.ClientOutputThread;
import com.example.login.RegisterActivity;
import com.example.mapproject.R;
import com.example.mapproject.R.drawable;
import com.example.mapproject.R.id;
import com.example.mapproject.R.layout;
import com.example.mapproject.R.menu;
import com.example.sqlite.AddFriendActivity;
import com.example.sqlite.DBHelper;
import com.example.sqlite.FriendListActivity;
import com.example.tran.TranObject;
import com.example.tran.TranObjectType;
import com.example.util.Constants;
import com.example.util.DialogFactory;
import com.example.util.LocationMessage;
import com.example.util.SharePreferenceUtil;
import com.example.util.TextMessage;
import com.example.util.User;

public class MainActivity extends MapActivity {
	// 百度地图的key
	private String mMapKey = "pObijtvOHq8wQuQiNY8Q4FH5ZeGpEAh3";

	private MapView Map = null;
	// 获取用于在地图上标注一个地理坐标点的图标
	private Drawable drawable;
	// 定义管理sdk的对象,加载地图的引擎
	private BMapManager MapManager = null;
	private MapController mapController = null;
	private MyLocationOverlay myLocationOverlay = null;

	// onResume时注册此listener，onPause时需要Remove,注意此listener不是Android自带的，是百度API中的
	private LocationListener locationListener;

	private GeoPoint pt;

	private final String[] mItems1 = { "驾车线路", "步行线路", "公交线路" };
	private final String[] mItems = { "卫星模式", "交通模式", "街景模式","正常模式" };
	private int mSingleChoiceID = -1;

	// 用于检索的类：位置检索、周边检索、范围检索、公交检索、驾乘检索、步行检索
	private MKSearch mkSearch;
	// 设置起始地（当前位置）
	MKPlanNode startNode = new MKPlanNode();
	// 设置目的地
	MKPlanNode endNode = new MKPlanNode();

	private EditText destinationEditText = null;

	private List<OverlayItem> list = new ArrayList<OverlayItem>();

	private MyApplication application;
	private Client client;

	// 是否位置共享标志
	public static boolean location_share = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 必须在加载content之前
		setContentView(R.layout.activity_main);

		System.out.println("MainActivity onCreate....");

		// 判断网络是否可用
		if (!isNetworkAvailable()) {
			toast(this);
			System.out.println("MainActivity NetworkAvailable?.....");
		}

		MapManager = new BMapManager(getApplication());
		MapManager.init(mMapKey, new myMKGeneralListener());
		super.initMapActivity(MapManager);

		// 实例化地图对象
		Map = (MapView) findViewById(R.id.mv);
		// 设置启用内置的缩放控件
		Map.setBuiltInZoomControls(true);
		// 获取地图控制器，可以用它控制平移和缩放
		mapController = Map.getController();
		// 设置地图的缩放级别。 这个值的取值范围是[3,18]
		mapController.setZoom(12);
		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		Map.setDrawOverlayWhenZooming(true);
		// 获取当前位置层
		myLocationOverlay = new MyLocationOverlay(this, Map);
		// 将当前位置的层添加到地图底层中
		Map.getOverlays().add(myLocationOverlay);

		// 注册定位事件
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {

					System.out
							.println("MainActivity locationListener onLocationChanged....");

					/*
					 * 注：GeoPoint对象构造方法的参数列表：第一个是参数表示纬度， 第二个是经度
					 * GPS的值官方给的就是纬度经度，也就是说纬度是在前的，我们一直没太注意。
					 */

					// 生成GEO类型坐标并在地图上定位到该坐标标示的地点
					pt = new GeoPoint((int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					// 设置地图的中心点
					mapController.setCenter(pt);
					mapController.animateTo(pt);
				}
			}
		};

		// 初始化搜索模块
		mkSearch = new MKSearch();
		// 设置驾车的最优策略：时间优先
		mkSearch.setDrivingPolicy(MKSearch.ECAR_TIME_FIRST);
		// 设置驾车路线搜索策略，时间优先、费用最少或距离最短
		// 放在这里供学习
		/*
		 * // 驾乘检索策略常量：时间优先
		 * mMKSearch.setDrivingPolicy(MKSearch.ECAR_TIME_FIRST);
		 * mMKSearch.drivingSearch(null, start, null, end);
		 *
		 * // 驾乘检索策略常量：较少费用 mMKSearch.setDrivingPolicy(MKSearch.ECAR_FEE_FIRST);
		 * mMKSearch.drivingSearch(null, start, null, end);
		 *
		 * // 驾乘检索策略常量：最短距离 mMKSearch.setDrivingPolicy(MKSearch.ECAR_DIS_FIRST);
		 * mMKSearch.drivingSearch(null, start, null, end);
		 */

		mkSearch.init(MapManager, new myMKSearchListener());

		// 好友位置标记
		drawable = this.getResources().getDrawable(R.drawable.iconmarka);

		application = (MyApplication) getApplication();
		client = application.getClient();

		/********************************* 我测试MyOverLayItem用的 ***********************************/
		// 声明double类型的变量存储北京天安门的纬度、经度值
		// double mLat1 = 39.915; // point1纬度
		// double mLon1 = 116.404; // point1经度
		// GeoPoint geoPoint = new GeoPoint((int) (mLat1 * 1E6),
		// (int) (mLon1 * 1E6));
		//
		// Map.getOverlays().add(
		// new MyOverLayItem(drawable, getApplicationContext(), geoPoint,
		// "邓伟波"));
		/********************************* 我测试MyOverLayItem用的 ***********************************/
	}

	/************************************ 菜单及其支持函数 *****************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(Menu.NONE, 0, 0, "定位");
		menu.add(Menu.NONE, 1, 1, "搜索");
		menu.add(Menu.NONE, 2, 2, "线路");
		menu.add(Menu.NONE, 3, 3, "图层");
		menu.add(Menu.NONE, 4, 4, "位置共享");
		menu.add(Menu.NONE, 5, 5, "工具");
		menu.add(Menu.NONE, 6, 6, "帮助");
		menu.add(Menu.NONE, 7, 7, "设置");
		menu.add(Menu.NONE, 8, 8, "清空结果");
		menu.add(Menu.NONE, 9, 9, "刷新");
		menu.add(Menu.NONE, 10, 10, "登录");
		menu.add(Menu.NONE, 11, 11, "退出");

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		switch (item.getItemId()) {
			case 0:// 定位
				Map.getController().animateTo(pt);
				mapController.setCenter(pt);
				break;
			case 1: // 搜索
				search();
				break;
			case 2:// 线路
				line();
				break;
			case 3:// 图层
				overlay();
				break;
			case 4:// 位置共享
				// 打开好友列表，与好友实现位置共享
				intent.setClass(MainActivity.this,
						com.example.sqlite.FriendListActivity.class);
				startActivity(intent);
				break;
			case 5:// 工具
				showDialog("工具");
				break;
			case 6:// 帮助
				intent.setClass(MainActivity.this,
						com.example.others.helpActivity.class);
				startActivity(intent);
				break;
			case 7:// 设置
				intent.setClass(MainActivity.this,
						com.example.others.settingActivity.class);
				startActivity(intent);
				break;
			case 8:// 清空结果
				Map.getOverlays().clear();
				Map.getOverlays().add(myLocationOverlay);
				break;
			case 9:// 刷新，请求位置更新
				if (TrackService.locationClient != null)
					TrackService.locationClient.requestLocation();
				else
					showDialog("您还未位置共享");
				break;
			case 10:// 登录
				intent.setClass(MainActivity.this,
						com.example.login.LoginActivity.class);
				startActivity(intent);
				break;
			case 11:// 退出
				builder.setIcon(R.drawable.icon);
				builder.setTitle("确定退出移动地图吗？");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
								finish();
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
							}
						});
				builder.create().show();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	// 显示信息的对话框
	private void showDialog(String str) {
		new AlertDialog.Builder(MainActivity.this).setMessage(str).show();
	}

	// 搜索实现方法
	private void search() {
		// 输入指定的查询地方，包括经纬度、查询的名称和查询的范围，搜索附近的东西
		// 查询某一个地方的周围的指定建筑物和商场之类的
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.test, null);
		builder.setIcon(R.drawable.icon);
		builder.setTitle("搜索");
		builder.setView(textEntryView);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				destinationEditText = (EditText) textEntryView
						.findViewById(R.id.something);
				String destination = destinationEditText.getText().toString();
				// 1、测试根据范围和检索词发起范围检索
				// POI搜索及PoiOverlay
				// 参数列表：关键词、中心点地理坐标、半径，单位:米
				mkSearch.poiSearchNearBy(destination, pt, 100000);
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).create().show();
	}

	// 线路实现方法
	private void line() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.test, null);
		builder.setIcon(R.drawable.icon).setTitle("请先选择线路");
		builder.setView(textEntryView);
		builder.setSingleChoiceItems(mItems1, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSingleChoiceID = whichButton;
					}
				});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				if (mSingleChoiceID >= 0) {
					destinationEditText = (EditText) textEntryView
							.findViewById(R.id.something);
					String destination = destinationEditText.getText()
							.toString();
					startNode.pt = pt;
					endNode.name = destination;
					switch (mSingleChoiceID) {
						case 0:
							// 2、测试驾车路线，查找驾车最优的路线
							mkSearch.drivingSearch("上海", startNode, "上海", endNode);
							showDialog("你选择的是驾车线路");// 测试用的
							break;
						case 1:
							// 3、测试行走路线,步行路线图
							mkSearch.walkingSearch("上海", startNode, "上海", endNode);
							showDialog("你选择的是步行线路");// 测试用的
							break;
						case 2:
							// 4、测试公交换乘路线
							mkSearch.transitSearch("上海", startNode, endNode);
							showDialog("你选择的是公交线路");// 测试用的
							break;
					}
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder.create().show();
	}

	// 图层实现方法
	private void overlay() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setIcon(R.drawable.icon)
				.setTitle("请选择图层")
				.setSingleChoiceItems(mItems, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
								mSingleChoiceID = whichButton;
							}
						});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mSingleChoiceID >= 0) {
					switch (mSingleChoiceID) {
						// 实现不了，故出现提示信息用于测试
						case 0:// 卫星地图是卫星拍摄的真实的地理面貌，所以卫星地图可用来检测地面的信息，你可以了解到地理位置，地形等。
							showDialog("你选择的是卫星模式");
							Map.setSatellite(true);
							Map.setTraffic(false);
//						 Map.setStreetView(false);
							break;
						case 1:// 在地图中显示实时交通信息示图（当前，全国范围内已支持多个城市实时路况查询，且会陆续开通其他城市。）
							showDialog("你选择的是交通模式");
							Map.setTraffic(true);
							Map.setSatellite(false);
//						 Map.setStreetView(false);
							break;
						case 2:
							showDialog("你选择的是街景模式");
//						 Map.setStreetView(true);
//						// Map.setTraffic(false);
//						// Map.setSatellite(false);
							break;
//					case 3:
//						showDialog("你选择的是交通模式");
//						// Map.setTraffic(true);
//						// Map.setSatellite(false);
//						// Map.setStreetView(false);
//						break;
						default:
							showDialog("你选择的是正常模式");
							Map.setTraffic(false);
							Map.setSatellite(false);
							// Map.setStreetView(false);
					}
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder.create().show();
	}

	/************************************ 菜单及其支持函数 ***************************************************/

	/************************************** 线路搜索实现 ****************************************/
	private class myMKSearchListener implements MKSearchListener {

		// 经纬度和地址的转换
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			// TODO Auto-generated method stub
			if (iError != 0) {
				String str = String.format("错误号：%d", iError);
				Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG)
						.show();
				return;
			}
			Map.getController().animateTo(result.geoPt);
			String strInfo = String.format("纬度：%f 经度：%f\r\n",
					result.geoPt.getLatitudeE6() / 1e6,
					result.geoPt.getLongitudeE6() / 1e6);
			Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG)
					.show();
			Drawable marker = getResources().getDrawable(R.drawable.iconmarka); // 得到需要标在地图上的资源
			marker.setBounds(0, 0, marker.getIntrinsicWidth(),
					marker.getIntrinsicHeight()); // 为maker定义位置和边界
			Map.getOverlays().clear();
			Map.getOverlays().add(myLocationOverlay);
			Map.getOverlays().add(
					new MyOverLayItem(marker, MainActivity.this, result.geoPt,
							result.strAddr));
		}

		// 公交路线详情搜索
		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			// TODO Auto-generated method stub
			if (result == null || iError != 0) {
				Toast.makeText(MainActivity.this, "对不起，未找到结果！！", 1).show();
				return;
			}
			RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this, Map);
			routeOverlay.setData(result.getBusRoute());// 获得公交线路图
			Map.getOverlays().clear();// 清除公交站点的标识
			Map.getOverlays().add(myLocationOverlay);
			Map.getOverlays().add(routeOverlay);
			Map.invalidate();
			// 采用动画形式描述公交站点
			Map.getController().animateTo(result.getBusRoute().getStart());
		}

		// 获取驾车路线
		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result,
											int error) {
			// TODO Auto-generated method stub
			// 错误号可参考MKEvent中的定义
			if (error != 0 || result == null) {
				Toast.makeText(MainActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
			RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this, Map);

			// 此处仅展示一个方案作为示例
			MKRoute route = result.getPlan(0).getRoute(0);
			int distanceM = route.getDistance();
			String distanceKm = String.valueOf(distanceM / 1000) + "."
					+ String.valueOf(distanceM % 1000);
			System.out.println("距离:" + distanceKm + "公里---节点数量:"
					+ route.getNumSteps());
			for (int i = 0; i < route.getNumSteps(); i++) {
				MKStep step = route.getStep(i);
				System.out.println("节点信息：" + step.getContent());
			}
			routeOverlay.setData(route);
			Map.getOverlays().clear();
			Map.getOverlays().add(myLocationOverlay);
			Map.getOverlays().add(routeOverlay);
			Map.invalidate();
			Map.getController().animateTo(result.getStart().pt);
		}

		// 获得公交线路图的Uid，并且根据此UID来发起公交线路详情的检索
		@Override
		public void onGetPoiResult(MKPoiResult result, int arg1, int error) {
			// TODO Auto-generated method stub
			if (error != 0) {
				Toast.makeText(MainActivity.this, "抱歉，未查找到结果",
						Toast.LENGTH_SHORT).show();
			} else {
				if (result == null) {
					Toast.makeText(MainActivity.this,
							"抱歉，您填写的搜索条件，未查找到结果，换个条件试试！", Toast.LENGTH_SHORT)
							.show();
					return;
				}

				// 创建POI内置的Overlay对象
				PoiOverlay poiOverlay = new PoiOverlay(MainActivity.this, Map);
				// 符合搜索条件的所有点
				poiOverlay.setData(result.getAllPoi());
				Map.getOverlays().clear();
				Map.getOverlays().add(myLocationOverlay);
				// 向覆盖物列表中添加覆盖物对象PoiOverlay
				Map.getOverlays().add(poiOverlay);
				// 刷新地图
				Map.invalidate();

				/*
				 * 注： POI是中国POI(Point of Interest)数据库的缩写，可以翻译成“兴趣点”，
				 * 每个POI包含四方面信息，名称、类别、经度、纬度。 这个计划的远景目标是建立全国的POI数据库，并且全部开放。
				 */

				// 当执行完POI检索后，我们会得到一个POI的列表。
				// ArrayList<MKPoiInfo> mkPois = result.getAllPoi();
				// 取POI列表中的第二个元素
				// MKPoiInfo mkPoi = mkPois.get(1);
				/*
				 * 每个POI节点都有个uid属性，我们可以根据这个uid获取关于这个poi的一些更详细的信息。
				 * 比如：评论、图片、商户描述等。
				 */

				// 发起查看详细信息的请求
				// mkSearch.poiDetailSearch(mkPoi.uid);

				// 经纬度和地址的转换
				// if (iError != 0 || result == null) {
				// Toast.makeText(MainActivity2.this, "解析失败",
				// Toast.LENGTH_LONG).show();
				// return;
				// }
				// if (result != null && result.getCurrentNumPois() > 0) {
				// GeoPoint ptGeo = result.getAllPoi().get(0).pt;
				// // 移动地图到该点：
				// Map.getController().animateTo(ptGeo);
				// String strInfo = String.format("纬度：%f 经度：%f\r\n",
				// ptGeo.getLatitudeE6() / 1e6,
				// ptGeo.getLongitudeE6() / 1e6);
				// strInfo += "\r\n附近有：";
				// for (int i = 0; i < result.getAllPoi().size(); i++) {
				// strInfo += (result.getAllPoi().get(i).name + ";");
				// }
				// Toast.makeText(MainActivity2.this, strInfo,
				// Toast.LENGTH_LONG).show();
			}

		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		// 获取公交线路
		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult result,
											int error) {
			// TODO Auto-generated method stub
			// 错误号可参考MKEvent中的定义
			if (error != 0 || result == null) {
				Toast.makeText(MainActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
			RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this, Map);

			// 此处仅展示一个方案作为示例
			MKRoute route = result.getPlan(0).getRoute(0);
			int distanceM = route.getDistance();
			String distanceKm = String.valueOf(distanceM / 1000) + "."
					+ String.valueOf(distanceM % 1000);
			System.out.println("距离:" + distanceKm + "公里---节点数量:"
					+ route.getNumSteps());
			for (int i = 0; i < route.getNumSteps(); i++) {
				MKStep step = route.getStep(i);
				System.out.println("节点信息：" + step.getContent());
			}
			routeOverlay.setData(route);
			Map.getOverlays().clear();
			Map.getOverlays().add(myLocationOverlay);
			Map.getOverlays().add(routeOverlay);
			Map.invalidate();
			Map.getController().animateTo(result.getStart().pt);

			// 在一个地图上显示多条导航路线
			// int planNum = result.getNumPlan();// 获得路线方案的个数
			//
			// for (int i = 0; i < planNum; i++) {
			// TransitOverlay transitOverlay1 = new
			// TransitOverlay(MainActivity2.this,
			// Map);
			// // 此处仅展示一个方案作为示例
			// transitOverlay1.setData(result.getPlan(i));
			// Map.getOverlays().add(transitOverlay1);
			// }
			//
			// Map.invalidate(); // 刷新地图
			// Map.getController().animateTo(result.getStart().pt);
		}

		// 获取步行路线
		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult result,
											int error) {
			// TODO Auto-generated method stub
			// 错误号可参考MKEvent中的定义
			if (error != 0 || result == null) {
				Toast.makeText(MainActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
				return;
			}
			RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this, Map);

			// 此处仅展示一个方案作为示例
			MKRoute route = result.getPlan(0).getRoute(0);
			int distanceM = route.getDistance();
			String distanceKm = String.valueOf(distanceM / 1000) + "."
					+ String.valueOf(distanceM % 1000);
			System.out.println("距离:" + distanceKm + "公里---节点数量:"
					+ route.getNumSteps());
			for (int i = 0; i < route.getNumSteps(); i++) {
				MKStep step = route.getStep(i);
				System.out.println("节点信息：" + step.getContent());
			}
			routeOverlay.setData(route);
			Map.getOverlays().clear();
			Map.getOverlays().add(myLocationOverlay);
			Map.getOverlays().add(routeOverlay);
			Map.invalidate();
			Map.getController().animateTo(result.getStart().pt);
		}

	}

	/**************************************** 监听消息 *********************************************/
	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	private class myMKGeneralListener implements MKGeneralListener {
		@Override
		public void onGetPermissionState(int arg0) {
			// TODO Auto-generated method stub
			if (arg0 == 300) {
				Toast.makeText(MainActivity.this, "输入的Key有错！请核实！！", 1).show();
			}
		}

		@Override
		public void onGetNetworkState(int iError) {
			// TODO Auto-generated method stub
			Log.d("MyGeneralListener", "onGetNetworkState error is " + iError);
			// Toast.makeText(MainActivity.this, "您的网络出错啦！",
			// Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 广播接收者，接收GetMsgService发送过来的消息
	 */
	private BroadcastReceiver MsgReceiver1 = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			TranObject msg = (TranObject) intent
					.getSerializableExtra(Constants.MSGKEY);
			if (msg != null) {
				// 如果不是空，说明是消息广播
				getMessage(msg);// 把收到的消息传递给子类
			} else {
				// 如果是空消息，说明是关闭应用的广播
				close();
			}
		}
	};

	public void getMessage(TranObject msg) {
		final TranObject Msg = msg;

		// TODO Auto-generated method stub
		if (msg != null) {
			switch (msg.getType()) {
				case MESSAGE:
					TextMessage message = (TextMessage) msg.getObject();
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("温馨提示")
							.setMessage(message.getMessage())
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface arg0,
															int arg1) {
											// TODO Auto-generated method stub

										}

									}).create().show();
					break;
				// 好友请求
				case FRIEND_REQUEST:
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("温馨提示")
							.setMessage(msg.getFromUser() + "请求成为您的好友！")
							.setPositiveButton("同意",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
															int which) {
											// 好友同意请求后将好友姓名插入到自己的数据库中
											String name = Msg.getFromUser();
											ContentValues values = new ContentValues();
											values.put("name", name);

											DBHelper helper = new DBHelper(
													MainActivity.this);
											helper.insert(values);

											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_YES_FRIEND_REQUEST);
												o.setFromUser(Msg.getToUser());
												o.setToUser(Msg.getFromUser());
												out.setMessage(o);
											}

											// 好友同意请求后进入好友列表
											Intent intent = new Intent(
													MainActivity.this,
													FriendListActivity.class);
											startActivity(intent);

										}
									})
							.setNegativeButton("不同意",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
															int which) {
											// TODO Auto-generated method stub
											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_NO_FRIEND_REQUEST);
												o.setFromUser(Msg.getToUser());
												o.setToUser(Msg.getFromUser());
												out.setMessage(o);
											}
										}

									}).create().show();

					break;
				case LOCATION_SHARE:
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("温馨提示")
							.setMessage(msg.getFromUser() + "请求与您位置共享！")
							.setPositiveButton("同意",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
															int which) {
											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_YES_LOCATION_SHARE);
												o.setFromUser(Msg.getToUser());
												o.setToUser(Msg.getFromUser());
												out.setMessage(o);
											}
											startService(Msg.getToUser(),
													Msg.getFromUser());
										}
									})
							.setNegativeButton("不同意",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
															int which) {
											// TODO Auto-generated method stub
											// 通过Socket验证信息
											if (application.isClientStart()) {
												ClientOutputThread out = client
														.getClientOutputThread();
												// 定义一个传输对象，对象类型为注册
												TranObject<User> o = new TranObject<User>(
														TranObjectType.ANSWER_NO_LOCATION_SHARE);
												o.setFromUser(Msg.getToUser());
												o.setToUser(Msg.getFromUser());
												out.setMessage(o);
											}
										}

									}).create().show();
					break;
				// 收到的为好友的位置信息，待测试
				case LOCATION:
					System.out.println("获得位置新消息");

					if (location_share) {
						LocationMessage location = (LocationMessage) msg
								.getObject();
						double lat = location.getLat();
						double lng = location.getLng();
						GeoPoint geoPoint = new GeoPoint((int) (lat * 1E6),
								(int) (lng * 1E6));
						// Map.getOverlays().clear();
						Map.getOverlays().add(myLocationOverlay);
						Map.getOverlays().add(
								new MyOverLayItem(drawable,
										getApplicationContext(), geoPoint, msg
										.getFromUser()));
					}
					break;
				default:
					break;
			}
		}
	}

	private void startService(String fromUser, String toUser) {

		System.out.println("MainActivity start trackService.....");

		Intent intent = new Intent();
		intent.putExtra("fromUser", fromUser);
		intent.putExtra("toUser", toUser);

		intent.setClass(MainActivity.this, TrackService.class);
		startService(intent);
	}

	public void close() {
		Intent i = new Intent();
		i.setAction(Constants.ACTION);
		sendBroadcast(i);
		finish();
	}
	/**************************************** 监听消息 *********************************************/


	/*************************************** 生命周期 ****************************************/
	@Override
	public void onStart() {
		super.onStart();

		System.out.println("MainActivity onStart.....");

		// 在start方法中注册广播接收者
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION);
		registerReceiver(MsgReceiver1, intentFilter);// 注册接受消息广播
	}

	// 注意在onResume、onDestroy和onPause中控制mapview和地图管理对象的状态
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		System.out.println("MainActivity onDestroy....");

		if (application.isClientStart()) {
			ClientOutputThread out = client.getClientOutputThread();
			TranObject<User> o = new TranObject<User>(TranObjectType.LOGOUT);

			// 其实这一块是有问题的，如果用户没有登录，那么何来的登出呢？？？？
			// 找到自己的名字,设置fromUser为自己的名字
			SharedPreferences sharedPreferences = getSharedPreferences(
					Constants.Personal_Information, MODE_WORLD_READABLE);
			String user = sharedPreferences.getString("userName", "");

			User u = new User();
			u.setName(user);
			o.setObject(u);
			out.setMessage(o);

			// 发送完之后，关闭client,即关闭消息输入输出流
			out.setStart(false);
			client.getClientInputThread().setStart(false);
		}

		if (MapManager != null) {
			MapManager.destroy();
			MapManager = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		System.out.println("MainActivity onResume.....");

		MapManager.getLocationManager()
				.requestLocationUpdates(locationListener);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass(); // 打开指南针
		if (MapManager != null) {
			MapManager.start();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		System.out.println("MainActivity onPause.....");

		MapManager.getLocationManager().removeUpdates(locationListener);
		myLocationOverlay.disableMyLocation(); // 显示当前位置
		myLocationOverlay.disableCompass(); // 关闭指南针
		if (MapManager != null) {
			MapManager.stop();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		System.out.println("MainActivity onStop.....");

		// 在stop方法中注销广播接收者
		unregisterReceiver(MsgReceiver1);// 注销接受消息广播
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	/*************************************** 生命周期 *********************************************/


	/********************************* 判断网路是否可用和处理方法 ***********************************/
	/**
	 * 判断手机网络是否可用
	 *
	 * @param context
	 * @return
	 */
	private boolean isNetworkAvailable() {
		ConnectivityManager mgr = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = mgr.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	private void toast(Context context) {
		new AlertDialog.Builder(context)
				.setTitle("温馨提示")
				.setMessage("您的网络连接未打开！")
				.setPositiveButton("前往打开",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int which) {
								Intent intent = new Intent(
										android.provider.Settings.ACTION_WIRELESS_SETTINGS);
								startActivity(intent);
							}
						}).setNegativeButton("取消", null).create().show();
	}
	/********************************* 判断网路是否可用和处理方法 ***********************************/
}
