package cordova.plugin.bhim_upi;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
 public class BhimUpi extends CordovaPlugin {

    private int uniqueRequestCode = 5120;
    private Activity activity;
    private boolean exception = false;
    private CallbackContext finalResult;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // finalResult = callbackContext;
        if (action.equals("coolMethod")) {
            // String message = args.getString(0);
            this.startTranascation(args, callbackContext);
            return true;
        } 
        return false;
    }



    private void startTranascation(JSONArray args, CallbackContext callbackContext) {
        // if (message != null && message.length() > 0) {
        //     callbackContext.success(message);
        // } else {
        //     callbackContext.error("Expected one non-empty string argument.");
        // }
        String app;
        if(args.getJSONObject(0).getString("app") == null) {
            app = "in.org.npci.upiapp";
        } else {
            app =  args.getJSONObject(0).getString("app");
        }
        String receiverUpiId = args.getJSONObject(0).getString("receiverUpId");
        String receiverName = args.getJSONObject(0).getString("receiverName");
        String transactionRefId = args.getJSONObject(0).getString("transactionRefId");
        String transactionNote = args.getJSONObject(0).getString("transactionNote");
        String amount = args.getJSONObject(0).getString("amount");
        String currency = args.getJSONObject(0).getString("currency");
        String url = args.getJSONObject(0).getString("url");
        String merchantId = args.getJSONObject(0).getString("merchantId");

        try {
            exception = false;
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("upi").authority("pay");
            uriBuilder.appendQueryParameter("pa", receiverUpiId);
            uriBuilder.appendQueryParameter("pn", receiverName);
            uriBuilder.appendQueryParameter("tn", transactionNote);
            uriBuilder.appendQueryParameter("am", amount);
            if (transactionRefId != null) {
                uriBuilder.appendQueryParameter("tr", transactionRefId);
            }
            if (currency == null) {
                uriBuilder.appendQueryParameter("cr", "INR");
            } else
                uriBuilder.appendQueryParameter("cu", currency);
            if (url != null) {
                uriBuilder.appendQueryParameter("url", url);
            }
            if (merchantId != null) {
                uriBuilder.appendQueryParameter("mc", merchantId);
            }

            Uri uri = uriBuilder.build();

            // Built Query. Ready to call intent.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(app);

            if (isAppInstalled(app)) {
                activity.startActivityForResult(intent, uniqueRequestCode);
                // finalResult = result;
            } else {
                Log.d("BhimUpi NOTE: ", app + " not installed on the device.");
                callbackContext.success("app_not_installed");
            }
        } catch (Exception ex) {
            exception = true;
            Log.d("BhimUpi NOTE: ", "" + ex);
            // callbackContext.error("FAILED", "invalid_parameters", null);
        }
    }

        // On receiving the response.
    
        // public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        //     if (uniqueRequestCode == requestCode && finalResult != null) {
        //         if (data != null) {
        //             try {
        //                 String response = data.getStringExtra("response");
        //                 // if (!exception) finalResult.success(response);
        //             } catch (Exception ex) {
        //                 // if (!exception) finalResult.success("null_response");
        //             }
        //         } else {
        //             Log.d("BhimUpi NOTE: ", "Received NULL, User cancelled the transaction.");
        //             // if (!exception) finalResult.success("user_canceled");
        //         }
        //     }
        //     return true;
        // }
    private boolean isAppInstalled(String uri) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException pme) {
            pme.printStackTrace();
            Log.e("BhimUpi Error:",""+pme);
        }
        return false;
    }

    private List<Map<String, Object>> getAllUpiApps(){
        List<Map<String,Object>> packages = new ArrayList<>();
        Intent intent =  new Intent(Intent.ACTION_VIEW);
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("upi").authority("pay");
        uriBuilder.appendQueryParameter("pa","test@ybl");
        uriBuilder.appendQueryParameter("pn","Test");
        uriBuilder.appendQueryParameter("tn","Get All Apps");
        uriBuilder.appendQueryParameter("am","1.0");
        uriBuilder.appendQueryParameter("cr","INR");
        Uri uri = uriBuilder.build();
        intent.setData(uri);
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent,0);

        for(ResolveInfo resolveInfo:resolveInfoList) {

            try {

                String packageName = resolveInfo.activityInfo.packageName;
                
                String name = String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)));

                Drawable dIcon = pm.getApplicationIcon(packageName);

                Bitmap bIcon = getBitmapFromDrawable(dIcon);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bIcon.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] icon =  stream.toByteArray();


                Map<String,Object> m = new HashMap<>();
                m.put("packageName", packageName);
                m.put("name",name);
                m.put("icon",icon);

                packages.add(m);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return packages;
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0,0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }
}
