package hitqz.com.updateapponline.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import java.io.IOException;

import hitqz.com.updateapponline.util.ApkVersionUtil;
import hitqz.com.updateapponline.util.DownloadAppUtils;
import hitqz.com.updateapponline.util.LogUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UpdateAppTool {
    private static final String TAG = "UpdateAppTool";

    private static final String VERSION_URL = "http://192.168.2.176:8080/hitpay/getVersion.do";
    private static final String APK_DOWNLOAD_URL = "http://192.168.2.176:8080/test.apk";

    /**
     * 获取服务器上版本信息
     */
    public static void getAPPServerVersion(final VersionCallBack callBack) {
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(VERSION_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    if (responseBody != null) {
                        String bodyString = responseBody.string();

                        try {
                            int verCode = Integer.valueOf(bodyString);
                            callBack.callBack(verCode);
                        } catch (NumberFormatException e) {
                            LogUtil.w(TAG, "获取的版本号不正确");
                        }
                    }
                }
            }
        });

    }

    public static int getAPPLocalVersion(Context context) {
        return ApkVersionUtil.getVersionCode(context);
    }

    public static void updateApp(final Context context) {
        getAPPServerVersion(new VersionCallBack() {
            @Override
            public void callBack(final int verCode) {
                LogUtil.i(TAG,"版本信息：当前"+getAPPLocalVersion(context)+",服务器："+ verCode);

                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (verCode > getAPPLocalVersion(context)) {
                            new AlertDialog.Builder(context)
                                    .setTitle("发现新版本, 是否下载更新?")
                                    .setMessage("是否更新APP")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DownloadAppUtils.downloadForAutoInstall(context, APK_DOWNLOAD_URL, "test.apk", "程序更新");
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create()
                                    .show();
                        } else {
                            Toast.makeText(context, "当前已经是最新版本不需要更新", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    public interface VersionCallBack{
        void callBack(int verCode);
    }
}
