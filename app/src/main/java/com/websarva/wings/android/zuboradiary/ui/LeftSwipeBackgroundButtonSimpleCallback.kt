package com.websarva.wings.android.zuboradiary.ui;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class LeftSwipeBackgroundButtonSimpleCallback extends LeftSwipeSimpleCallback {

    protected float swipingOffset = 0f;

    public LeftSwipeBackgroundButtonSimpleCallback(RecyclerView recyclerView) {
        super(recyclerView);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void build() {
        super.build();
        getRecyclerView().setOnTouchListener(new CustomOnTouchSwipedItemListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    protected class CustomOnTouchSwipedItemListener extends OnTouchSwipedItemListener {

        // MEMO:スワイプ状態はItemTouchHelperが効いていてonClickListenerが反応しない為、
        //      onTouchListenerを使ってボタンの境界を判定して処理させる。
        //      通常スワイプ時、ACTION_DOWN -> MOVE -> UPとなるが
        //      未スワイプ状態からはACTION_DOWNは取得できず、ACTION_MOVE -> UPとなる。
        @Override
        public boolean onTouch(View v, @NonNull MotionEvent event) {
            Objects.requireNonNull(v);

            boolean result = false;
            Log.d("onTouch()", "MotionEvent:" + event.getAction());
            if (event.getAction() == MotionEvent.ACTION_UP) {
                clearInvalidSwipeViewHolder();
                // 疑似クリック処理
                if (getPreviousMotionEvent() == MotionEvent.ACTION_DOWN) {
                    result = OnClickSwipedViewHolder(v, event);
                }
            }
            setPreviousMotionEvent(event.getAction());
            return result;
        }

        protected boolean OnClickSwipedViewHolder(View v, @NonNull MotionEvent event) {
            Objects.requireNonNull(v);

            // タッチViewHolder取得
            View childView = getRecyclerView().findChildViewUnder(event.getX(), event.getY());
            if (childView == null) return false;

            int adapterPosition = getRecyclerView().getChildAdapterPosition(childView);
            RecyclerView.ViewHolder viewHolder =
                    getRecyclerView().findViewHolderForAdapterPosition(adapterPosition);
            Objects.requireNonNull(viewHolder);
            LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;

            int tolerance =
                    (int) (3 * v.getResources().getDisplayMetrics().density); // スワイプ位置誤差許容値
            View foregroundView = leftSwipeViewHolder.foregroundView;
            View backgroundButtonView = leftSwipeViewHolder.backgroundButtonView;

            // アニメーション中無効
            if (!foregroundView.isClickable()) return false;
            // スワイプ状態でない
            if (foregroundView.getTranslationX() > -backgroundButtonView.getWidth() + tolerance) return false;

            Rect rect = new Rect();
            backgroundButtonView.getGlobalVisibleRect(rect);
            // 背面ボタン押下時処理
            if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                backgroundButtonView.performClick();
                closeSwipedViewHolder(adapterPosition);
                return true;
            }
            // スワイプアイテム押下時処理
            if (getSwipedAdapterPosition() == adapterPosition) {
                closeSwipedViewHolder(adapterPosition);
                return true;
            }
            return false;
        }

    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d("LeftSwipeBackButton", "getSwipeThreshold()_position:" + viewHolder.getBindingAdapterPosition());
        LeftSwipeViewHolder leftSwipeViewHolder= (LeftSwipeViewHolder) viewHolder;

        // スワイプ境界を背面ボタンの中心にする
        float threshold =
                leftSwipeViewHolder.backgroundButtonView.getWidth() / 2f / getRecyclerView().getWidth();

        // スワイプメニューを閉じる時は、反対方向からの割合に変更
        // MEMO:ViewHolderの前面Viewは背面ボタン位置までのスワイプ状態になっているが、
        //      スワイプ機能の値(ItemTouchHelper.Callback#onChildDraw()の引数であるdX)としては
        //      ViewHolderの端までスワイプしている事になっている。その為下記コードが必要となる。
        if (getSwipedAdapterPosition() != viewHolder.getBindingAdapterPosition()) {
            Log.d("LeftSwipeBackButton", "getSwipeThreshold()_return:" + threshold);
            return threshold;
        }

        Log.d("LeftSwipeBackButton", "getSwipeThreshold()_return:" + (1 - threshold));
        return 1 - threshold;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction != ItemTouchHelper.LEFT) return;

        Log.d("LeftSwipeBackButton", "onSwiped()_position:" + viewHolder.getBindingAdapterPosition());
        LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;

        if (getSwipingAdapterPosition() != viewHolder.getBindingAdapterPosition()) {
            throw new IllegalStateException();
        }
        setSwipedAdapterPosition(getSwipingAdapterPosition());
        clearSwipingAdapterPosition();
        swipingOffset =
                getRecyclerView().getWidth() - leftSwipeViewHolder.backgroundButtonView.getWidth();
    }

    @Override
    protected void translateForegroundView(
            @NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return;

        LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        float backgroundButtonWidth =
                leftSwipeViewHolder.backgroundButtonView.getWidth();
        float translationValueX;
        if (getSwipedAdapterPosition() == viewHolder.getBindingAdapterPosition()) {
            translationValueX = Math.min(0, Math.max(-backgroundButtonWidth, dX + swipingOffset));
        } else {
            translationValueX = Math.min(0, Math.max(-backgroundButtonWidth, dX));
        }
        Log.d("LeftSwipeBackButton", "onChildDraw()_translationValueX:" + translationValueX);
        leftSwipeViewHolder.foregroundView.setTranslationX(translationValueX);
    }
}
