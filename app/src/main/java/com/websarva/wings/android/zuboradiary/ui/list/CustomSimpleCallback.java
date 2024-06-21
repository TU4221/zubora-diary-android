package com.websarva.wings.android.zuboradiary.ui.list;

//リサイクルビューのアイテムにスワイプ機能、背面ボタンを追加するためのクラス。
//参考：https://ameblo.jp/highcommunicate/entry-12651319381.html

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CustomSimpleCallback extends ItemTouchHelper.SimpleCallback {

    //背面ボタン横サイズ
    private static final int BUTTON_WIDTH = 60;

    //背面ボタン内表示文字サイズ
    private static final int FONT_SIZE = 12;

    //端末のスケール(コンストラクタ引数を代入)
    private float scale;

    //スケール変換後背面ボタン横サイズ
    private int BUTTON_WIDTH_DP;

    //スケール変換後背面ボタン内表示文字サイズ
    private int FONT_SIZE_DP;

    //表示する数分の背面ボタンのインスタンスを格納するリスト
    private List<UnderlayButton> buttons;

    //ジェスチャーリスナを格納するクラス
    private GestureDetector gestureDetector;

    //前回スワイプした(スワイプ中の)リサイクルビューのアイテム位置
    //(スワイプ中も更新される)
    private int swipedPos = -1;

    //スワイプ認識領域値？
    private float swipeThreshold = 0.5f;

    //ドラッグ認識領域値？
    private float moveThreshold = 0.0f;

    //リサイクルビュー作成後、スワイプしてインスタンス化した背面ボタンのインスタンスを格納するマップ
    private Map<Integer, List<UnderlayButton>> buttonsBuffer;

    //前回までにスワイプしたリサイクルビューのアイテム位置を格納するリスト
    private Queue<Integer> recoverQueue = new LinkedList<Integer>() {
        @Override
        public boolean add(Integer o) {
            if (contains(o))
                return false;
            else
                return super.add(o);
        }
    };

    //スワイプ機能を実装するリサイクルビュー(コンストラクタの引数を代入)
    private RecyclerView recyclerView;

    //ダイアログ表示等に使用するクラス
    private FragmentManager fragmentManager;
    private NavController navController;

    //親リサイクルビュー
    private RecyclerView parentRecyclerView;
    private DiaryListFragment.DiaryYearMonthListAdapter parentRecyclerViewAdapter;

    //ジェスチャー機能を使用した時に処理されるリスナ
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        //シングルタップ検出時の処理
        //スワイプ後はクリックリスナが無効になる。(trueを返却してるから？)
        //スワイプ後、タップした箇所をみて背面ボタンの処理有無を判断。
        //スワイプ前に背面ボタンの箇所をタップしてもクリックリスナが処理される為か、タッチとして認識されず下記メソッドは呼び出されない。
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d("スワイプ処理確認", "onSingleTapConfirmed：アイテム背面ボタンタップ");
            for (UnderlayButton button : buttons) {
                if (button.onClick(e.getX(), e.getY()))
                    break;
            }

            return true;
        }
    };

    //
    //false を戻すことによりタッチ中は常時 onTouch を呼び出す
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            Log.d("スワイプ処理確認", "onTouch：アイテムタッチ");
            parentRecyclerViewAdapter.recoverOtherSwipedItem(CustomSimpleCallback.this);
            //リサイクルビューに対して初回のスワイプかの確認。
            //swipedPosは初回は -1 。次回以降は基本1つ前のアイテム Pos 。
            if (swipedPos < 0) return false;
            //ユーザーが前回スワイプした(スワイプ中の)アイテム位置を取得
            Point point = new Point((int) e.getRawX(), (int) e.getRawY());

            //前回スワイプした(スワイプ中の)アイテムのビューホルダーを取得。
            RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
            if (swipedViewHolder == null) return false;
            View swipedItem = swipedViewHolder.itemView;
            //前回スワイプした(スワイプ中の)アイテム座標保持用インスタンス取得。
            Rect rect = new Rect();
            //前回スワイプした(スワイプ中の)アイテムの座標をrectに登録。
            swipedItem.getGlobalVisibleRect(rect);

            Log.d("スワイプ処理確認", Integer.toString(e.getAction()));
            //スワイプ中(タッチからリリースするまで)の処理
            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                //検出座標がアイテム座標内時の処理
                Log.d("スワイプ処理確認", "onTouch：アイテムタッチ_アイテム座標内判断");
                Log.d("スワイプ処理確認", "top:" + Integer.toString(rect.top) + "< y:" + Integer.toString(point.y));
                Log.d("スワイプ処理確認", "bottom:" + Integer.toString(rect.bottom) + "> y:" + Integer.toString(point.y));
                if (rect.top < point.y && rect.bottom > point.y) {
                    Log.d("スワイプ処理確認", "onTouch：アイテムタッチ_アイテム座標内");
                    //gestureDetectorにセットされたジェスチャーリスナを起動する。(背面ボタン処理)
                    gestureDetector.onTouchEvent(e);
                } else {
                    Log.d("スワイプ処理確認", "onTouch：アイテムタッチ_アイテム座標外");
                    //アイテム座標外の処理
                    //前回スワイプした(スワイプ中の)アイテムの位置で保存。
                    recoverQueue.add(swipedPos);
                    swipedPos = -1;
                    //recoverQueue内のスワイプアイテムの履歴を全削除(前回スワイプしたアイテムの状態を更新(スワイプ前の状態に戻す))
                    recoverSwipedItem();

                }
            }
            return false;
        }
    };

    public CustomSimpleCallback(int dragDirs, int swipeDirs, RecyclerView recyclerView, Context context, float scale, FragmentManager fragmentManager, NavController navController, RecyclerView parentRecyclerView) {
        super(dragDirs, swipeDirs);
        this.recyclerView = recyclerView;
        this.recyclerView.setOnTouchListener(onTouchListener);
        this.scale = scale;
        this.BUTTON_WIDTH_DP = (int) (BUTTON_WIDTH * scale);
        this.FONT_SIZE_DP = (int) (FONT_SIZE * scale);
        this.buttons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.buttonsBuffer = new HashMap<>();
        this.recoverQueue = new LinkedList<Integer>() {
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };
        this.fragmentManager = fragmentManager;
        this.navController = navController;
        this.parentRecyclerView = parentRecyclerView;
        this.parentRecyclerViewAdapter = (DiaryListFragment.DiaryYearMonthListAdapter) parentRecyclerView.getAdapter();
    }

    //ドラッグされたとみなされる閾値を返す。
    @Override
    public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return moveThreshold;
    }

    //ドラッグすると呼び出される。
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    //スワイプされたとみなされる閾値を返す。
    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    //スワイプとみなす最小速度の閾値を返す。
    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    //スワイプ最大速度の閾値を返す。
    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    //スワイプが完了すると呼び出される。
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d("スワイプ処理確認", "onSwiped：アイテムスワイプ完了");
        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos){
            recoverQueue.add(swipedPos);
        }
        swipedPos = pos;

        //？？？
        if (buttonsBuffer.containsKey(swipedPos)) {
            buttons = buttonsBuffer.get(swipedPos);
        } else {
            buttons.clear();
        }
        buttonsBuffer.clear();
        swipeThreshold = 0.5f * buttons.size() * BUTTON_WIDTH_DP;
    }

    //アイテムスワイプ後に表示するボタンを作成
    //タッチ中常時呼び出される。
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        /*Log.d("スワイプ処理確認", "onChildDraw：呼び出し");
        int pos = viewHolder.getAdapterPosition();
        Log.d("スワイプ処理確認", "onChildDraw：pos " + pos);

        //スワイプ時のアイテムの変位量
        float translationX = dX;
        Log.d("スワイプ処理確認", Float.toString(dX));

        View itemView = viewHolder.itemView;

        //下記プログラム不要(今後の為に下記コメントを残す)
        //onChildDraw はアイテムスライド中以外にも起動する為か、不具合が生じる。
        //アイテムスライド後、他のアイテムをスライドすると、前回スライドしたアイテムに対して onChildDraw が起動する。
        //その為、swipedPosに現在のスライドの位置ではなく、前回のスライド位置情報が格納され、
        //onTouch() メソッド内の recoverSwipedItem() メソッドが何回も起動し、アイテムの表示がちらついてしまう。
        *//*
        if (swipedPos < 0){
            swipedPos = pos;
            Log.d("スワイプ処理確認", "onChildDraw：swipedPosセット確認");
            return;
        }

         *//*

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            Log.d("スワイプ処理確認", "onChildDraw：スワイプ確認");
            if(dX < 0) {
                Log.d("スワイプ処理確認", "onChildDraw：左へスワイプ");
                List<UnderlayButton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)){
                    //アイテムの後ろのボタンをインスタンス化し buffer に格納。
                    instantiateUnderlayButton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                }
                //既に対象のアイテムの後ろボタンがインスタンス化されている場合はそれを使う。
                else {
                    buffer = buttonsBuffer.get(pos);
                }

                //そのままの変位量だと左端までスワイプするので、ボタンサイズまでの変位量になるように細工。
                translationX = dX * buffer.size() * BUTTON_WIDTH_DP / itemView.getWidth();

                //ボタン作成
                //アイテムの右端の位置取得
                float right = itemView.getRight();
                float dButtonWidth = (-1) * translationX / buffer.size();

                for (UnderlayButton button : buffer) {
                    float left = right - dButtonWidth;
                    button.onDraw(c, new RectF(left, itemView.getTop(), right, itemView.getBottom()), pos);

                    right = left;
                }
            } else if (dX == 0.0f) {
                Log.d("スワイプ処理確認", "onChildDraw：右へスワイプ(アイテムが元の位置へ)");
                //スワイプしたアイテムをスワイプで元の位置に戻した場合、buttons の情報が残り、スワイプ前の状態でも背面ボタンが処理される。
                //その為下記の対策をとる。
                //(メモ：「ズボラ日記」製作時はDayListのアダプター内でOnClickListenerをセットしている。
                // この場合クリックリスナが優先される為、下記対策は不要だが、汎用性を考慮して下記対策をとる。
                // 通常はタッチリスナが優先される。)
                buttons.clear();
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);*/
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        DiaryListFragment.DiaryDayListViewHolder viewHolder1 = (DiaryListFragment.DiaryDayListViewHolder) viewHolder;
        View itemView = viewHolder.itemView;
        getDefaultUIUtil().onDraw(c, recyclerView, itemView.findViewById(R.id.frame_layout_row_diary_day_list), 0, 0, actionState, isCurrentlyActive);
        getDefaultUIUtil().onDraw(c, recyclerView, itemView.findViewById(R.id.liner_layout_front), dX, dY, actionState, isCurrentlyActive);
    }

    //ボタンをインスタンス化
    private void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
        underlayButtons.add(new UnderlayButton(
                "削除",
                Color.parseColor("#ff0000"),//赤を直接指定
                viewHolder,
                new UnderlayButtonClickListener() {
                    @Override
                    public void onClick(RecyclerView.ViewHolder holder, int pos) {
                        // 背面ボタン(削除)の処理内容

                        //削除確認ダイアログ起動
                        DiaryListFragment.DiaryDayListAdapter diaryDayListAdapter = (DiaryListFragment.DiaryDayListAdapter) CustomSimpleCallback.this.recyclerView.getAdapter();
                        DiaryListFragment.DiaryDayListViewHolder diaryListDayViewHolder = (DiaryListFragment.DiaryDayListViewHolder) holder;

                        // 削除確認ダイアログ起動前に対象のアイテムの状態をスワイプ前の状態に戻す。
                        // (ダイアログクラスからCustomSimpleCallbackのインスタンスを参照できないため、ダイアログ起動前に状態を戻す)
                        // メモ) UI からアイテム削除後、同じ位置のアイテムを削除しようとすると、前回削除したアイテムの日付で削除しようとする。
                        //      他の位置のアイテムは正常に削除できる。
                        //      原因は recoverSwipeItem() の最終的に処理される notifyItemChanged() である。
                        //      このメソッドはアイテムの状態を戻すと同時に削除前のアイテムも復元している。
                        //      (UI に表示されているアイテムの表示内容は最新のデータとなっている。原因不明。)
                        //      対策として notifyDataSetChanged() を処理させる。
                        //      このメソッドを使用すればすべてのアイテムデータを最新状態にする為、 recoverSwipeItem() は不要になるが、
                        //      スワイプした位置を記憶する変数をリセットする必要と、スワイプ状態を戻す正規の処理を踏ませたいため残しておく。
                        recoverSwipeItem();
                        diaryDayListAdapter.notifyDataSetChanged();

                        String deleteDiaryDate = diaryListDayViewHolder.date;
                        NavDirections action =
                                DiaryListFragmentDirections
                                        .actionDiaryListFragmentToDeleteConfirmationDialog(
                                                deleteDiaryDate
                                        );
                        CustomSimpleCallback.this.navController.navigate(action);
                    }
                }
        ));
        //背面ボタンが複数の場合は上記同様に追加すること。
    }

    @Override
    public void clearView(     @NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                               @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder ) {
        super.clearView(recyclerView, viewHolder);
        Log.d("スワイプ処理確認", "clear");
        View itemView = viewHolder.itemView;
        getDefaultUIUtil().clearView(itemView.findViewById(R.id.frame_layout_row_diary_day_list));
        getDefaultUIUtil().clearView(itemView.findViewById(R.id.liner_layout_front));
    }


    //対象のアイテムの状態をスワイプ前の状態に戻すクラス
    private synchronized void recoverSwipedItem(){
        Log.d("スワイプ処理確認", "recoverSwipedItem：スワイプリセット処理確認");
        while (!recoverQueue.isEmpty()){
            //配列先頭の値を取得して削除
            int pos = recoverQueue.poll();
            if (pos > -1) {
                //対象のアイテムの表示を更新する。(アイテムの状態をスワイプ前の状態に戻す)
                DiaryListFragment.DiaryDayListAdapter listAdapter =
                        (DiaryListFragment.DiaryDayListAdapter) this.recyclerView.getAdapter();
                List<DiaryDayListItem> currentList = listAdapter.getCurrentList();
                DiaryDayListItem currentItem = currentList.get(pos);
                List<DiaryDayListItem> newList = new ArrayList<>(currentList);
                DiaryDayListItem newItem = new DiaryDayListItem(currentItem);
                newList.remove(pos);
                newList.add(pos, newItem);
                listAdapter.submitList(newList);
                Log.d("20240619", "recover");
            }
        }
    }

    private class UnderlayButton {
        private String text;
        private int imageResId;
        private int color;
        private int pos;
        private RectF clickRegion;
        private RecyclerView.ViewHolder viewHolder;
        private UnderlayButtonClickListener clickListener;

        public UnderlayButton(String text, int color, RecyclerView.ViewHolder holder, UnderlayButtonClickListener clickListener) {
            this.text = text;
            this.color = color;
            this.viewHolder = holder;
            this.clickListener = clickListener;
        }

        //背面ボタンのリスナを呼び出す。
        public boolean onClick(float x, float y){
            if (clickRegion != null && clickRegion.contains(x, y)){
                clickListener.onClick(viewHolder, pos);
                return true;
            }

            return false;
        }

        public void resetClickRegion(){
            Log.d("スワイプ処理確認", "resetClickRegion：呼び出し");
            clickRegion = new RectF();
        }

        public void onDraw(Canvas c, RectF rect, int pos){
            Paint p = new Paint();

            // Draw background
            p.setColor(color);
            c.drawRect(rect, p);

            // Draw Text
            p.setColor(Color.WHITE);
            p.setTextSize(FONT_SIZE_DP);

            Rect r = new Rect();
            float cHeight = rect.height();
            float cWidth = rect.width();
            p.setTextAlign(Paint.Align.LEFT);
            p.getTextBounds(text, 0, text.length(), r);
            float x = cWidth / 2f - r.width() / 2f - r.left;
            float y = cHeight / 2f + r.height() / 2f - r.bottom;
            c.drawText(text, rect.left + x, rect.top + y, p);

            clickRegion = rect;
            this.pos = pos;
        }
    }
    //下記はリストアイテムスワイプ後ボタン用のリスナインターフェース(CustomSimpleCallbackクラスを抽象ではなく通常で作成したため、ここに仮で作成)
    public interface UnderlayButtonClickListener {
        void onClick(RecyclerView.ViewHolder holder, int pos);
    }

    //全てのアイテムをスワイプ前の状態に戻す。
    public void recoverSwipeItem() {
        recoverQueue.add(swipedPos);
        swipedPos = -1;
        recoverSwipedItem();
    }
}
