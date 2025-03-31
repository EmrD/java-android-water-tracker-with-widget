package com.emr.watertracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;
import java.time.LocalDate;

public class MainWidgetProvider extends AppWidgetProvider {
    public static void updateWidgetManually(Context context) {
        Intent intent = new Intent(context, MainWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, MainWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);
    }
    private static final String AZALT_BTN_CLICK_ACTION = "com.emr.watertracker.AZALT_BTN_CLICK";
    private static final String ARTIR_BTN_CLICK_ACTION = "com.emr.watertracker.ARTIR_BTN_CLICK";
    private static final int MIN_WATER = 0;
    private static final int STEP = 100;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String todayWaterKey = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            todayWaterKey = LocalDate.now().toString();
        }

        String todayWaterString = sharedPreferences.getString(todayWaterKey, String.valueOf(MIN_WATER));
        float todayWater = Float.parseFloat(todayWaterString);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widgetWaterText, (int) todayWater + " ml");

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetWaterText, pendingIntent);

        Intent intent1 = new Intent(context, MainWidgetProvider.class);
        intent1.setAction(AZALT_BTN_CLICK_ACTION);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.button_add_azalt, pendingIntent1);

        Intent intent2 = new Intent(context, MainWidgetProvider.class);
        intent2.setAction(ARTIR_BTN_CLICK_ACTION);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 1, intent2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.button_add_artir, pendingIntent2);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String todayWaterKey = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            todayWaterKey = LocalDate.now().toString();
        }

        if (!sharedPreferences.contains(todayWaterKey)) {
            sharedPreferences.edit().putString(todayWaterKey, String.valueOf(MIN_WATER)).apply();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, MainWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String todayWaterKey = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            todayWaterKey = LocalDate.now().toString();
        }

        String todayWaterString = sharedPreferences.getString(todayWaterKey, String.valueOf(MIN_WATER));
        float todayWater = Float.parseFloat(todayWaterString);

        if (AZALT_BTN_CLICK_ACTION.equals(intent.getAction())) {
            if (todayWater > MIN_WATER) {
                todayWater -= STEP;
            }
        } else if (ARTIR_BTN_CLICK_ACTION.equals(intent.getAction())) {
            todayWater += STEP;
        }

        sharedPreferences.edit().putString(todayWaterKey, String.valueOf(todayWater)).apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, MainWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
