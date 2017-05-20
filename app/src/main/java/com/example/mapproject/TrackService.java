package com.example.mapproject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MyLocationOverlay;
import com.example.client.Client;
import com.example.client.ClientInputThread;
import com.example.client.MessageListener;
import com.example.mapproject.R;
import com.example.tran.TranObject;
import com.example.tran.TranObjectType;
import com.example.util.LocationMessage;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

/*Service是一个应用程序组件，没有图形化界面，通常用来处理一些耗时比较长的操作，可以使用Service
 * 更新ContentProvider,发送Intent，启动系统的通知等等。
 *
 * Service不是一个单独的进程，也不是一个线程，如果你想使用一个单独的线程来做一些操作，
 * 你需要在Service的onStartCommand里启动一个线程
 *
 *
 * Service是运行在后台的一种服务程序，一般很少与用户交互，没有可视化的界面
 *
 * 1、定义一个Service只要继承Service，实现其生命周期的函数即可。
 * 2、一个定义好的Service必须在AndroidManifest配置文件中通过<service>元素声明才能使用。
 *     在<service>元素中添加<intent-filter>指定如何访问该service
 * 3、启动和停止Service：
 *    一个定义好的Service就可以在其他组建中使用startService()启动该Service来使用它了。
 *    此时被调用的Service会调用它的onCreate()（如果该Service还未创建），接着调用onStart()方法。
 *    一旦Service启动后将一直运行。（所以如果此时再次使用startService()启动该Service，则其只调用
 *    onStart()方法，而不调用onCreate()。）
 *    直到调用了stopService()方法或者stopSelf()方法，此时Service调用onDestroy() 方法停止该Service。
 * 4、绑定Service：
 *    也可以通过bindService()方法来绑定一个Service。
 *    此时被调用的Service会调用它的onCreate()（如果该Service还未创建），但是它不会调用onStart()方法
 *    而是调用onBind()方法返回客户端一个IBinder接口。绑定Serivce一般使用在远程Service调用。
 *
 */

/*
 * 一个Service启动后，会一直在后台运行
 */

/*
 * 使用Android自带的LocationManager和Location获取位置的时候，经常会有获取的location为null的情况，
 * 并且操作起来也不是很方便，在这个Demo里我使用了百度地图API中的定位SDK，可以一次性获取当前位置经纬度以及详细地址
 * 信息，还可以获取周边POI信息，同时可以设定位置通知点，当到达某一位置时，发出通知信息等方式来告知用户。
 */

//这个服务用来不断获取自己的位置信息和收取对方发来的位置数据
public class TrackService extends Service {
	public static LocationClient locationClient = null;
	private static final int UPDATE_TIME = 500;
	private static int LOCATION_COUTNS = 0;
	private String toUser, fromUser;

	private MyApplication application;
	private Client client;

	private static final int MSG = 0x001;

	// 百度Android定位SDK实现获取当前经纬度及位置

	/*
	 * 当创建一个Service对象，首先调用onCreate函数
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		MainActivity.location_share=true;
		System.out.println("已同意位置共享，并且设置location_share为true");

		System.out.println("TrackService onCreate......");

		application = (MyApplication) getApplication();
		client = application.getClient();

		locationClient = new LocationClient(this);

		// 设置定位条件
		LocationClientOption option = new LocationClientOption();
		// 是否打开GPS
		option.setOpenGps(true);
		// 设置返回值的坐标类型。
		option.setCoorType("bd09ll");
		// 设置定位优先级
		option.setPriority(LocationClientOption.NetWorkFirst);
		// 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
		option.setProdName("LocationDemo");
		// 设置定时定位的时间间隔。单位毫秒
		option.setScanSpan(UPDATE_TIME);

		locationClient.setLocOption(option);
		locationClient.start();

		//这里不断发送自己的位置信息
		// 注册位置监听器
		locationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation location) {
				// TODO Auto-generated method stub

				System.out.println("TrackService onCreate......");

				if (location == null) {
					return;
				}
				//一下暂时注释掉
				LocationMessage myLocation=new LocationMessage();
				myLocation.setLat(location.getLatitude());
				myLocation.setLng(location.getLongitude());
				TranObject<LocationMessage> message = new TranObject<LocationMessage>(
						TranObjectType.LOCATION);
				message.setObject(myLocation);
				message.setFromUser(fromUser);
				message.setToUser(toUser);

				if (application.isClientStart()) {

					System.out.println("发送位置新消息");

					client.getClientOutputThread().setMessage(message);
//					locationClient.requestLocation();
				} else
					Toast.makeText(getApplicationContext(), "客户端线程未正常未启动!",
							Toast.LENGTH_LONG).show();

				/************************************************* 用于调试 *************************************************/
				StringBuffer sb = new StringBuffer(256);
				sb.append("Time : ");
				sb.append(location.getTime());
				sb.append("\nError code : ");
				sb.append(location.getLocType());
				sb.append("\nLatitude : ");
				sb.append(location.getLatitude());
				sb.append("\nLontitude : ");
				sb.append(location.getLongitude());
				sb.append("\nRadius : ");
				sb.append(location.getRadius());
				if (location.getLocType() == BDLocation.TypeGpsLocation) {
					sb.append("\nSpeed : ");
					sb.append(location.getSpeed());
					sb.append("\nSatellite : ");
					sb.append(location.getSatelliteNumber());
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
					sb.append("\nAddress : ");
					sb.append(location.getAddrStr());
				}
				LOCATION_COUTNS++;
				sb.append("\n检查位置更新次数：");
				sb.append(String.valueOf(LOCATION_COUTNS));
				Toast.makeText(getApplicationContext(), sb, Toast.LENGTH_LONG).show();
				/************************************************* 用于调试 *************************************************/
			}

			@Override
			public void onReceivePoi(BDLocation location) {
			}
		});
		super.onCreate();
	}

	/*
	 * 如果我们希望从Activity中得到一些数据，并将其发送到Service中进行相关运算，我们可以将数据放在Intent中，
	 * 发送到Service,onStartCommand就可以取出数据。
	 *
	 * 在这个函数中会执行任务，通常会在其中启动新线程进行任务执行
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		System.out.println("TrackService onStartCommand......");

		toUser = intent.getStringExtra("toUser");
		fromUser = intent.getStringExtra("fromUser");
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// 当Service不再使用时，系统调用该方法。
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

		System.out.println("TrackService onDestroy......");

		super.onDestroy();
		if (locationClient != null && locationClient.isStarted()) {
			locationClient.stop();
			locationClient = null;
		}
	}

}
