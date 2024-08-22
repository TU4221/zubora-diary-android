package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.websarva.wings.android.zuboradiary.ui.list.DiaryYearMonthListAdapter;

import java.lang.ref.WeakReference;

public class DiaryListSimpleCallback extends ItemTouchHelper.SimpleCallback {

    public static abstract class LeftSwipeViewHolder extends RecyclerView.ViewHolder {
        View foregroundView;
        View backgroundButtonView;

        public LeftSwipeViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            setUpView(binding);
            foregroundView.setClickable(true);
            backgroundButtonView.setClickable(true);
        }

        /**
         * フィールド変数 View foregroundView、View backgroundButtonView に対象Viewを代入すること。
         */
        abstract void setUpView(@NonNull ViewDataBinding binding);
    }

    private final RecyclerView parentRecyclerView;
    private final RecyclerView recyclerView;
    private float swipingOffset = 0f;
    private int swipingAdapterPosition = -1;
    private int swipedAdapterPosition = -1;
    private int invalidSwipeAdapterPosition = -1;
    private int lastMotionEvent = -1;

    // Leaking this in constructor of non-final class
    private WeakReference<ItemTouchHelper> helper = null; // TODO:WeakReferenceである意味？

    public DiaryListSimpleCallback(RecyclerView parentRecyclerView, RecyclerView recyclerView) {
        super(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT);
        this.parentRecyclerView = parentRecyclerView;
        this.recyclerView = recyclerView;
    }

    public void build() {
        helper = new WeakReference<>(new ItemTouchHelper(this));
        helper.get().attachToRecyclerView(recyclerView);

        // MEMO:スワイプ状態はItemTouchHelperが効いていてonClickListenerが反応しない為、
        //      onTouchListenerを使ってボタンの境界を判定して処理させる。
        //      通常スワイプ時、ACTION_DOWN -> MOVE -> UPとなるが
        //      未スワイプ状態からはACTION_DOWNは取得できず、ACTION_MOVE -> UPとなる。
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("onTouch()", "MotionEvent:" + event.getAction());
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    clearInvalidSwipeViewHolder();
                    // 疑似クリック処理
                    if (lastMotionEvent == MotionEvent.ACTION_DOWN) {
                        lastMotionEvent = event.getAction();
                        return processOnTouchingSwipedViewHolder(v, event);
                    }
                }
                lastMotionEvent = event.getAction();
                return false;
            }
        });
    }

    // MEMO:スワイプクローズアニメーション開始時にfalseとなり、終了時にtrueとなるようにしているが、
    //      終了時にタッチ中の場合はfalseのままとしているため、ここでtrueにする。
    //      理由は"InvalidSwipeAdapterPosition"書き込みコード参照。
    private void clearInvalidSwipeViewHolder() {
        if (invalidSwipeAdapterPosition == -1) {
            return;
        }
        RecyclerView.ViewHolder lockedViewHolder =
                recyclerView.findViewHolderForAdapterPosition(invalidSwipeAdapterPosition);
        if (lockedViewHolder == null) {
            return;
        }
        LeftSwipeViewHolder leftSwipeViewHolder;
        if (lockedViewHolder instanceof LeftSwipeViewHolder) {
            leftSwipeViewHolder = (LeftSwipeViewHolder) lockedViewHolder;
        } else {
            return;
        }
        leftSwipeViewHolder.foregroundView.setClickable(true);
        clearInvalidSwipeAdapterPosition();
    }

    private boolean processOnTouchingSwipedViewHolder(View v, MotionEvent event) {
        // タッチViewHolder取得
        View childView = recyclerView.findChildViewUnder(event.getX(), event.getY());
        if (childView == null) {
            return false;
        }
        int adapterPosition = recyclerView.getChildAdapterPosition(childView);
        RecyclerView.ViewHolder viewHolder =
                recyclerView.findViewHolderForAdapterPosition(adapterPosition);
        if (viewHolder == null) {
            return false;
        }

        // タッチ箇所をもとに分岐処理
        LeftSwipeViewHolder leftSwipeViewHolder;
        if (viewHolder instanceof LeftSwipeViewHolder) {
            leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        } else {
            return false;
        }

        int tolerance =
                (int) (3 * v.getResources().getDisplayMetrics().density); // スワイプ位置誤差許容値
        View foregroundView = leftSwipeViewHolder.foregroundView;
        View backgroundButtonView = leftSwipeViewHolder.backgroundButtonView;

        // アニメーション中無効
        if (!foregroundView.isClickable()) {
            return false;
        }

        if (foregroundView.getTranslationX() <= -backgroundButtonView.getWidth() + tolerance) {
            Rect rect = new Rect();
            backgroundButtonView.getGlobalVisibleRect(rect);
            if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                backgroundButtonView.performClick();
                closeForAdapterPosition(adapterPosition);
                return true;
            }
            if (swipedAdapterPosition == adapterPosition) {
                closeForAdapterPosition(adapterPosition);
                return true;
            }
        }
        return false;
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
    private void closeForAdapterPosition(int position) {
        Log.d("LeftSwipingCallBack_", "closeForAdapterPosition()_position:" + position);
        RecyclerView.ViewHolder viewHolder =
                recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder == null) {
            return;
        }

        animateSwipingView(
                position,
                viewHolder,
                200,
                0f,
                new AnimationAction() {
                    @Override
                    public void process() {
                        helper.get().onChildViewDetachedFromWindow(viewHolder.itemView);
                        helper.get().onChildViewAttachedToWindow(viewHolder.itemView);
                    }
                },
                new AnimationAction() {
                    @Override
                    public void process() {
                        // MEMO:StartActionのリセットのみでは、スワイプしたアイテムをタッチしてスワイプ状態を戻した後、
                        //      アイテムをクリックしてもアイテム前面Viewのクリックリスナーが反応しない。
                        //      2回目以降は反応する。対策として下記コードを記述。
                        helper.get().onChildViewDetachedFromWindow(viewHolder.itemView);
                        helper.get().onChildViewAttachedToWindow(viewHolder.itemView);

                        if (swipingAdapterPosition == position) {
                            clearSwipingAdapterPosition();
                        }
                        if (swipedAdapterPosition == position) {
                            clearSwipedAdapterPosition();
                        }
                    }
                }
        );
    }

    @FunctionalInterface
    private interface AnimationAction {
        void process();
    }

    private void animateSwipingView(int position, RecyclerView.ViewHolder viewHolder, int duration, float translationValue, @Nullable AnimationAction startAction, @Nullable AnimationAction endAction) {
        Log.d("LeftSwipingCallBack_", "animateSwipingView()");
        LeftSwipeViewHolder leftSwipeViewHolder;
        if (viewHolder instanceof LeftSwipeViewHolder) {
            leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        } else {
            return;
        }
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
                        leftSwipeViewHolder.foregroundView.setClickable(false);
                        leftSwipeViewHolder.backgroundButtonView.setClickable(false);
                        if (startAction != null) {
                            startAction.process();
                        }
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (endAction != null) {
                            endAction.process();
                        }
                        // MEMO:アニメーション中にスワイプしてそのままタッチを継続されると、
                        //      アニメーション終了後にスワイプ分、前面Viewが移動してしまう。
                        //      対策として、下記条件コード記述。
                        if (lastMotionEvent == MotionEvent.ACTION_UP) {
                            leftSwipeViewHolder.foregroundView.setClickable(true);
                            leftSwipeViewHolder.backgroundButtonView.setClickable(true);
                        } else {
                            invalidSwipeAdapterPosition = position;
                        }
                    }
                })
                .start();
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder){
        Log.d("LeftSwipingCallBack_", "getMovementFlags()_position:" + viewHolder.getBindingAdapterPosition());
        LeftSwipeViewHolder leftSwipeViewHolder;
        if (viewHolder instanceof LeftSwipeViewHolder) {
            leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        } else {
            return super.getMovementFlags(recyclerView, viewHolder);
        }
        // アニメーション中スワイプ機能無効
        if (!leftSwipeViewHolder.foregroundView.isClickable()) {
            return 0;
        }
        return super.getMovementFlags(recyclerView, viewHolder);
    }

    // MEMO:タッチダウン、アップで呼び出し
    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        String position = "null";
        if (viewHolder != null) {
            position = String.valueOf(viewHolder.getBindingAdapterPosition());
        }
        Log.d("LeftSwipingCallBack_", "onSelectedChanged()_position:" + position);
        Log.d("LeftSwipingCallBack_", "onSelectedChanged()_actionState:" + actionState);
        super.onSelectedChanged(viewHolder, actionState);
        if (viewHolder == null) {
            return;
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // 他ViewHolderがスワイプメニュー表示中なら閉じる
            Log.d("LeftSwipingCallBack_", "onSelectedChanged()_swipingAdapterPosition:" + swipingAdapterPosition);
            if (swipingAdapterPosition != viewHolder.getBindingAdapterPosition()) {
                closeForAdapterPosition(swipingAdapterPosition);
            }
            swipingAdapterPosition = viewHolder.getBindingAdapterPosition();
            Log.d("LeftSwipingCallBack_", "onSelectedChanged()_swipedAdapterPosition:" + swipedAdapterPosition);
            if (swipedAdapterPosition != viewHolder.getBindingAdapterPosition()) {
                closeForAdapterPosition(swipedAdapterPosition);
            }
            RecyclerView.Adapter adapter = parentRecyclerView.getAdapter();
            if (adapter instanceof DiaryYearMonthListAdapter) {
                DiaryYearMonthListAdapter diaryYearMonthListAdapter = (DiaryYearMonthListAdapter) adapter;
                diaryYearMonthListAdapter.closeSwipedItemOtherDayList(this);
            }

        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d("LeftSwipingCallBack_", "onMove()");
        return false;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d("LeftSwipingCallBack_", "getSwipeThreshold()_position:" + viewHolder.getBindingAdapterPosition());
        LeftSwipeViewHolder leftSwipeViewHolder;
        if (viewHolder instanceof LeftSwipeViewHolder) {
            leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        } else {
            if (swipedAdapterPosition != viewHolder.getBindingAdapterPosition()) {
                return 1f;
            } else {
                return 0f;
            }
        }

        // スワイプ境界を背面ボタンの中心にする
        float value =
                leftSwipeViewHolder.backgroundButtonView.getWidth() / 2f / recyclerView.getWidth();

        // スワイプメニューを閉じる時は、反対方向からの割合に変更
        // MEMO:ViewHolderの前面Viewは背面ボタン位置までのスワイプ状態になっているが、
        //      スワイプ機能の値(ItemTouchHelper.Callback#onChildDraw()の引数であるdX)としては
        //      ViewHolderの端までスワイプしている事になっている。その為下記コードが必要となる。
        if (swipedAdapterPosition != viewHolder.getBindingAdapterPosition()) {
            Log.d("LeftSwipingCallBack_", "getSwipeThreshold()_return:" + value);
            return value;
        } else {
            Log.d("LeftSwipingCallBack_", "getSwipeThreshold()_return:" + (1 - value));
            return 1 - value;
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d("LeftSwipingCallBack_", "onSwiped()_position:" + viewHolder.getBindingAdapterPosition());
        LeftSwipeViewHolder leftSwipeViewHolder;
        if (viewHolder instanceof LeftSwipeViewHolder) {
            leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
        } else {
            return;
        }

        if (swipingAdapterPosition == viewHolder.getBindingAdapterPosition()) {
            swipedAdapterPosition = swipingAdapterPosition;
            clearSwipingAdapterPosition();
            swipingOffset =
                    recyclerView.getWidth() - leftSwipeViewHolder.backgroundButtonView.getWidth();
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        Log.d("LeftSwipingCallBack", "onChildDraw()_position:" + viewHolder.getBindingAdapterPosition());
        Log.d("LeftSwipingCallBack", "onChildDraw()_dX:" + dX);

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            Log.d("LeftSwipingCallBack", "onChildDraw()_ACTION_STATE_SWIPE");
            LeftSwipeViewHolder leftSwipeViewHolder;
            if (viewHolder instanceof LeftSwipeViewHolder) {
                leftSwipeViewHolder = (LeftSwipeViewHolder) viewHolder;
            } else {
                return;
            }

            // アニメーション中は無効
            if (!leftSwipeViewHolder.foregroundView.isClickable()) {
                return;
            }

            // 前面View移動
            if (dX <= 0f) {
                float backgroundButtonWidth =
                        leftSwipeViewHolder.backgroundButtonView.getWidth();
                float translationValueX;
                if (swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
                    translationValueX = Math.min(0, Math.max(-backgroundButtonWidth, dX + swipingOffset));
                } else {
                    translationValueX = Math.min(0, Math.max(-backgroundButtonWidth, dX));
                }
                Log.d("LeftSwipingCallBack", "onChildDraw()_translationValueX:" + translationValueX);
                leftSwipeViewHolder.foregroundView.setTranslationX(translationValueX);

                // 手動でスワイプを戻した時にクリア
                if (dX == 0) {
                    if (swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
                        clearSwipedAdapterPosition();
                    }
                    Log.d("LeftSwipingCallBack", "onChildDraw()_swipedAdapterPosition:Clear");
                }
            }
        }
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d("LeftSwipingCallBack_", "clearView()_position:" + viewHolder.getBindingAdapterPosition());
        super.clearView(recyclerView, viewHolder);
    }

    private void clearSwipingAdapterPosition() {
        swipingAdapterPosition = -1;
    }

    private void clearSwipedAdapterPosition() {
        swipedAdapterPosition = -1;
    }

    private void clearInvalidSwipeAdapterPosition() {
        invalidSwipeAdapterPosition = -1;
    }

    public void closeSwipedItem() {
        if (swipingAdapterPosition != -1) {
            closeForAdapterPosition(swipingAdapterPosition);
        }
        if (swipedAdapterPosition != -1) {
            closeForAdapterPosition(swipedAdapterPosition);
        }
    }
}
