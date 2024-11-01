package com.websarva.wings.android.zuboradiary.ui;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class LeftSwipeSimpleCallback extends ItemTouchHelper.SimpleCallback {

    public static abstract class LeftSwipeViewHolder extends RecyclerView.ViewHolder {
        public View foregroundView;
        public View backgroundButtonView;

        public LeftSwipeViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            Objects.requireNonNull(binding);
            setUpView(binding);
            foregroundView.setClickable(true);
            backgroundButtonView.setClickable(true);
        }

        public void setClickableAllView(boolean clickable) {
            foregroundView.setClickable(clickable);
            backgroundButtonView.setClickable(clickable);
        }

        /**
         * フィールド変数 View foregroundView、View backgroundButtonView に対象Viewを代入すること。
         */
        protected abstract void setUpView(@NonNull ViewDataBinding binding);
    }

    protected final RecyclerView recyclerView;
    protected ItemTouchHelper itemTouchHelper = null;

    protected int swipingAdapterPosition = -1;
    protected int swipedAdapterPosition = -1;
    protected int invalidSwipeAdapterPosition = -1;
    protected int previousMotionEvent = -1;


    public LeftSwipeSimpleCallback(RecyclerView recyclerView) {
        super(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void build() {
        itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setOnTouchListener(new OnTouchSwipedItemListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    protected class OnTouchSwipedItemListener implements View.OnTouchListener {

        // MEMO:スワイプ状態はItemTouchHelperが効いていてonClickListenerが反応しない為、
        //      onTouchListenerを使ってボタンの境界を判定して処理させる。
        //      通常スワイプ時、ACTION_DOWN -> MOVE -> UPとなるが
        //      未スワイプ状態からはACTION_DOWNは取得できず、ACTION_MOVE -> UPとなる。
        @Override
        public boolean onTouch(View v,@NonNull MotionEvent event) {
            Objects.requireNonNull(v);

            Log.d("LeftSwipeSimpleCallBack", "onTouch()_MotionEvent:" + event.getAction());
            if (event.getAction() == MotionEvent.ACTION_UP) clearInvalidSwipeViewHolder();
            previousMotionEvent = event.getAction();
            return false;
        }
    }

    // MEMO:スワイプクローズアニメーション開始時にfalseとなり、終了時にtrueとなるようにしているが、
    //      終了時にタッチ中の場合はfalseのままとしているため、ここでtrueにする。
    //      理由は"InvalidSwipeAdapterPosition"書き込みコード参照。
    protected void clearInvalidSwipeViewHolder() {
        if (invalidSwipeAdapterPosition == -1) return;

        RecyclerView.ViewHolder lockedViewHolder =
                recyclerView.findViewHolderForAdapterPosition(invalidSwipeAdapterPosition);
        Objects.requireNonNull(lockedViewHolder);

        LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) lockedViewHolder;
        leftSwipeViewHolder.foregroundView.setClickable(true);

        clearInvalidSwipeAdapterPosition();
    }

    // MEMO:スワイプメニューを閉じるアニメーション中も onChildDraw が反応する
    //
    //      1、閉じる前に clearView すると onChildDraw は反応しなくなるが、アニメーションが効かなくなって半開きのまま再利用されてしまう
    //      2、アニメーション終了後に notifyItemChanged して再度開く時の onChildDraw の dX をリセットしておく必要がある
    //      3、notifyItemChanged を使うと、高速でスワイプさせた時に複数箇所を開けてしまい挙動が安定しない
    //
    //      アニメーション開始前に ItemTouchHelper#onChildViewDetached/AttachedFromWindow でリセットしておくと、これらの問題を解消できる
    //      アニメーション中のフラグ isAnimating が無いので isClickable で代用する
    //      (参照:https://mt312.com/3182)
    protected void closeSwipedViewHolder(int position) {
        if (position < 0) throw new IllegalArgumentException();
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        Objects.requireNonNull(adapter);
        int listSize = adapter.getItemCount();
        if (position >= listSize) throw new IllegalArgumentException();

        Log.d("LeftSwipeSimpleCallBack", "closeSwipedViewHolder()_position:" + position);
        RecyclerView.ViewHolder viewHolder =
                recyclerView.findViewHolderForAdapterPosition(position);
        Objects.requireNonNull(viewHolder);

        animateSwipingView(
                position,
                viewHolder,
                300,
                0f,
                new AnimationAction() {
                    @Override
                    public void process() {
                        itemTouchHelper.onChildViewDetachedFromWindow(viewHolder.itemView);
                        itemTouchHelper.onChildViewAttachedToWindow(viewHolder.itemView);
                    }
                },
                new AnimationAction() {
                    @Override
                    public void process() {
                        // MEMO:StartActionのリセットのみでは、スワイプしたアイテムをタッチしてスワイプ状態を戻した後、
                        //      アイテムをクリックしてもアイテム前面Viewのクリックリスナーが反応しない。
                        //      2回目以降は反応する。対策として下記コードを記述。
                        itemTouchHelper.onChildViewDetachedFromWindow(viewHolder.itemView);
                        itemTouchHelper.onChildViewAttachedToWindow(viewHolder.itemView);

                        if (swipingAdapterPosition == position) clearSwipingAdapterPosition();
                        if (swipedAdapterPosition == position) clearSwipedAdapterPosition();
                    }
                }
        );
    }

    @FunctionalInterface
    protected interface AnimationAction {
        void process();
    }

    protected void animateSwipingView(
            int position, RecyclerView.ViewHolder viewHolder, int duration, float translationValue,
            @Nullable AnimationAction startAction, @Nullable AnimationAction endAction) {
        Log.d("LeftSwipeSimpleCallBack", "animateSwipingView()");
        LeftSwipeViewHolder leftSwipeViewHolder= (LeftSwipeViewHolder) viewHolder;
        leftSwipeViewHolder.foregroundView.animate()
                .setDuration(duration)
                .setInterpolator(new FastOutSlowInInterpolator())
                .translationX(translationValue)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        // MEMO:アニメーション中のViewHolderをタッチすると、
                        //      ItemTouchHelper.Callback#getMovementFlags()で
                        //      スワイプ機能を無効にするようにしている為、クリック機能が有効となる。
                        //      アニメーション中はリスナーを機能させたくないので下記コードを記述。
                        leftSwipeViewHolder.setClickableAllView(false);

                        if (startAction == null) return;
                        startAction.process();
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (endAction == null) return;
                        endAction.process();

                        // MEMO:アニメーション中にスワイプしてそのままタッチを継続されると、
                        //      アニメーション終了後にスワイプ分、前面Viewが移動してしまう。
                        //      対策として、下記条件コード記述。
                        if (previousMotionEvent != MotionEvent.ACTION_UP) {
                            invalidSwipeAdapterPosition = position;
                            return;
                        }

                        leftSwipeViewHolder.setClickableAllView(true);
                    }
                })
                .start();
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder){
        Log.d("LeftSwipeSimpleCallBack", "getMovementFlags()_position:" + viewHolder.getBindingAdapterPosition());
        LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;

        // アニメーション中スワイプ機能無効
        if (!leftSwipeViewHolder.foregroundView.isClickable()) return 0;

        return super.getMovementFlags(recyclerView, viewHolder);
    }

    // MEMO:タッチダウン、アップで呼び出し
    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        String position = "null";
        if (viewHolder != null) {
            position = String.valueOf(viewHolder.getBindingAdapterPosition());
        }
        Log.d("LeftSwipeSimpleCallBack", "onSelectedChanged()_position:" + position);
        Log.d("LeftSwipeSimpleCallBack", "onSelectedChanged()_actionState:" + actionState);

        super.onSelectedChanged(viewHolder, actionState);

        if (viewHolder == null) return;
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return;

        // 他ViewHolderがスワイプ中時の処理
        Log.d("LeftSwipeSimpleCallBack", "onSelectedChanged()_swipingAdapterPosition:" + swipingAdapterPosition);
        if (swipingAdapterPosition >= 0
                && swipingAdapterPosition != viewHolder.getBindingAdapterPosition()) {
            closeSwipedViewHolder(swipingAdapterPosition);
        }
        swipingAdapterPosition = viewHolder.getBindingAdapterPosition();

        // 他ViewHolderがスワイプ状態時の処理
        Log.d("LeftSwipeSimpleCallBack", "onSelectedChanged()_swipedAdapterPosition:" + swipedAdapterPosition);
        if (swipedAdapterPosition >= 0
                && swipedAdapterPosition != viewHolder.getBindingAdapterPosition()) {
            closeSwipedViewHolder(swipedAdapterPosition);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d("LeftSwipeSimpleCallBack", "onMove()");
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction != ItemTouchHelper.LEFT) return;

        LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        leftSwipeViewHolder.backgroundButtonView.performClick();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        Log.d("LeftSwipeSimpleCallBack", "onChildDraw()_position:" + viewHolder.getBindingAdapterPosition());
        Log.d("LeftSwipeSimpleCallBack", "onChildDraw()_dX:" + dX);

        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return;

        Log.d("LeftSwipeSimpleCallBack", "onChildDraw()_ACTION_STATE_SWIPE");
        LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;

        // アニメーション中は無効
        if (!leftSwipeViewHolder.foregroundView.isClickable()) return;
        // 右スワイプ
        if (dX > 0f) return;

        // 手動でスワイプを戻した時にクリア
        if (dX == 0) {
            if (swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
                clearSwipedAdapterPosition();
            }
            Log.d("LeftSwipeSimpleCallBack", "onChildDraw()_swipedAdapterPosition:Clear");
        }

        translateForegroundView(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    protected void translateForegroundView(
            @NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        LeftSwipeViewHolder leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        leftSwipeViewHolder.foregroundView.setTranslationX(dX);
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d("LeftSwipeSimpleCallBack", "clearView()_position:" + viewHolder.getBindingAdapterPosition());
        super.clearView(recyclerView, viewHolder);
    }

    protected void clearSwipingAdapterPosition() {
        swipingAdapterPosition = -1;
    }

    protected void clearSwipedAdapterPosition() {
        swipedAdapterPosition = -1;
    }

    protected void clearInvalidSwipeAdapterPosition() {
        invalidSwipeAdapterPosition = -1;
    }

    public void closeSwipedItem() {
        if (swipingAdapterPosition != -1) closeSwipedViewHolder(swipingAdapterPosition);
        if (swipedAdapterPosition != -1) closeSwipedViewHolder(swipedAdapterPosition);
    }
}
