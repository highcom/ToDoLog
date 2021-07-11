package com.highcom.todolog.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * コレクションウィジェットを生成するためのRemoteViewsFactoryを呼び出すためのサービスクラス
 */
public class ToDoWidgetRemoteViewsService extends RemoteViewsService {
    /**
     * RemoteViewsFactoryを生成する処理
     *
     * @param intent
     * @return ToDoリスト用のウィジェット生成処理クラス
     */
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ToDoWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
