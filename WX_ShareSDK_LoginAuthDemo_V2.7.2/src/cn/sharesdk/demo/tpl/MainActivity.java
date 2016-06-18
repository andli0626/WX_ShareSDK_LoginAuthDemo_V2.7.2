package cn.sharesdk.demo.tpl;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import com.mob.tools.utils.UIHandler;

/**
 * 
 * @author andli
 * @date 2016年6月18日 下午6:01:06
 * @annotation 微信授权登录
 */
public class MainActivity extends Activity implements Callback,PlatformActionListener,OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 初始化ShareSDK（不可缺失）
		ShareSDK.initSDK(this);
		
		Button authButton = (Button) findViewById(R.id.authButton);
		authButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 授权登录按钮
		case R.id.authButton:
			authorize(new Wechat(MainActivity.this));
			break;
		default:
			break;
		}

	}

	// 授权登录
	private void authorize(Platform plat) {
		// 判断指定平台是否已经完成授权
		if (plat.isValid()) {
			// 已经完成授权，直接读取本地授权信息，执行相关逻辑操作（如登录操作）
			String userId = plat.getDb().getUserId();
			if (!TextUtils.isEmpty(userId)) {
				UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
				login(plat.getName(), userId, null);
				return;
			}
		}
		plat.setPlatformActionListener(this);
		// 是否使用SSO授权：true不使用，false使用
		plat.SSOSetting(true);
		// 获取用户资料
		plat.showUser(null);
	}

	// 回调：授权成功
	public void onComplete(Platform platform, int action,HashMap<String, Object> res) {
		if (action == Platform.ACTION_USER_INFOR) {
			UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
			// 业务逻辑处理：比如登录操作
			String userName = platform.getDb().getUserName(); // 用户昵称
			String userId	= platform.getDb().getUserId();	  // 用户Id
			String platName = platform.getName();			  // 平台名称
				
			login(platName, userId, res);
		}
	}
	// 回调：授权失败
	public void onError(Platform platform, int action, Throwable t) {
		if (action == Platform.ACTION_USER_INFOR) {
			UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
		}
		t.printStackTrace();
	}
	// 回调：授权取消
	public void onCancel(Platform platform, int action) {
		if (action == Platform.ACTION_USER_INFOR) {
			UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
		}
	}
	// 业务逻辑：登录处理
	private void login(String plat, String userId,HashMap<String, Object> userInfo) {
		Toast.makeText(this, "用户ID:"+userId, Toast.LENGTH_SHORT).show();
		Message msg = new Message();
		msg.what    = MSG_LOGIN;
		msg.obj     = plat;
		UIHandler.sendMessage(msg, this);
	}
	
	// 统一消息处理
	private static final int MSG_USERID_FOUND 	= 1; // 用户信息已存在
	private static final int MSG_LOGIN 			= 2; // 登录操作
	private static final int MSG_AUTH_CANCEL 	= 3; // 授权取消
	private static final int MSG_AUTH_ERROR 	= 4; // 授权错误
	private static final int MSG_AUTH_COMPLETE 	= 5; // 授权完成
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		
		case MSG_USERID_FOUND: 
			Toast.makeText(this, "用户信息已存在，正在跳转登录操作......", 	Toast.LENGTH_SHORT).show();
			break;
		case MSG_LOGIN: 
			Toast.makeText(this, "使用微信帐号登录中...", 					Toast.LENGTH_SHORT).show();
			break;
		case MSG_AUTH_CANCEL:
			Toast.makeText(this, "授权操作已取消", 						Toast.LENGTH_SHORT).show();
			break;
		case MSG_AUTH_ERROR: 
			Toast.makeText(this, "授权操作遇到错误，请阅读Logcat输出", 		Toast.LENGTH_SHORT).show();
			break;
		case MSG_AUTH_COMPLETE: 
			Toast.makeText(this,"授权成功，正在跳转登录操作…", 				Toast.LENGTH_SHORT).show();
			break;
		}
		return false;
	}
}
