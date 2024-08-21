package com.websarva.wings.android.zuboradiary.ui.list.diarylist;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

public class LeftSwipingSimpleCallBack extends ItemTouchHelper.SimpleCallback {

    private float swipingStartingX = 0f;  // TODO:スワイプ戻し用の変数を用意した方がいいかも
    private int swipedAdapterPosition = -1;
    private final RecyclerView recyclerView;

    // Leaking this in constructor of non-final class
    private WeakReference<ItemTouchHelper> helper = null; // TODO:WeakReferenceである意味？

    public LeftSwipingSimpleCallBack(RecyclerView recyclerView) {
        super(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
    }

    public void build() {
        helper = new WeakReference<>(new ItemTouchHelper(this));
        helper.get().attachToRecyclerView(recyclerView);

        // スワイプメニューのボタンは ItemTouchHelper が効いていて onClickListener が反応しないので onTouchListener を使ってボタンの境界を判定して発動させる
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // タッチしたViewHolder取得
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
                    DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder;
                    if (viewHolder instanceof DiaryDayListAdapter.DiaryDayListViewHolder) {
                        diaryDayListViewHolder = (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
                    } else {
                        return false;
                    }
                    int tolerance =
                            (int) (3 * v.getResources().getDisplayMetrics().density); // スワイプ位置誤差許容値
                    LinearLayout foreground = diaryDayListViewHolder.binding.linerLayoutForeground;
                    TextView backgroundButton = diaryDayListViewHolder.binding.includeBackground.textDeleteButton;
                    if (foreground.getTranslationX() <= -backgroundButton.getWidth() + tolerance) {
                        Rect rect = new Rect();
                        backgroundButton.getGlobalVisibleRect(rect);
                        if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            backgroundButton.performClick();
                            closeForAdapterPosition(adapterPosition);
                            return true;
                        }
                        if (swipedAdapterPosition == adapterPosition) {
                            closeForAdapterPosition(adapterPosition);
                            return true;
                        }
                    }
                }
                return false;
            }
        });
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
        Log.d("LeftSwipingCallBack_", "closeForAdapterPosition()");
        Log.d("LeftSwipingCallBack_", "closeForAdapterPosition()_position:" + position);
        RecyclerView.ViewHolder viewHolder =
                recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder == null) {
            return;
        }
        DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder;
        if (viewHolder instanceof DiaryDayListAdapter.DiaryDayListViewHolder) {
            diaryDayListViewHolder = (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
        } else {
            return;
        }

        animateSwipingView(
                diaryDayListViewHolder.binding.linerLayoutForeground,
                300,
                0f,
                new AnimationAction() {
                    @Override
                    public void process() {
                        helper.get().onChildViewDetachedFromWindow(diaryDayListViewHolder.itemView);
                        helper.get().onChildViewAttachedToWindow(diaryDayListViewHolder.itemView);
                    }
                },
                new AnimationAction() {
                    @Override
                    public void process() {
                        // MEMO:StartActionのリセットのみでは、スワイプしたアイテムをタッチしてスワイプ状態を戻した後、
                        //      アイテムをクリックしてもアイテム前面Viewのクリックリスナーが反応しない。
                        //      2回目以降は反応する。対策として下記コードを記述。
                        helper.get().onChildViewDetachedFromWindow(diaryDayListViewHolder.itemView);
                        helper.get().onChildViewAttachedToWindow(diaryDayListViewHolder.itemView);

                        if (swipedAdapterPosition == position) {
                            clearSwipedAdapterPosition();
                        }
                    }
                }
        );
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        Log.d("LeftSwipingCallBack_", "onSelectedChanged()");
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE || actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            onSelectedChangedForSwipe(viewHolder, actionState);
        }
    }

    private void onSelectedChangedForSwipe(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        Log.d("LeftSwipingCallBack_", "onSelectedChangedForSwipe()");
        DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder;
        if (viewHolder instanceof DiaryDayListAdapter.DiaryDayListViewHolder) {
            diaryDayListViewHolder = (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
        } else {
            return;
        }
        LinearLayout foregroundView = diaryDayListViewHolder.binding.linerLayoutForeground;
        if (!foregroundView.isClickable()) {
            return;
        }

        if (swipedAdapterPosition != diaryDayListViewHolder.getBindingAdapterPosition()) {
            // 他ViewHolderがスワイプメニュー表示中なら閉じる
            closeForAdapterPosition(swipedAdapterPosition);
        }

        // スワイプメニューはリロードとスクロールで自動的に閉じられて状況が変わってしまうので、開閉の判断はスワイプ開始時に行う方が合理的
        // スワイプメニューを閉じる時は、全開状態の dX になっているので、半開き分を調整する必要がある

        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            Log.d("LeftSwipingCallBack_", "onSelectedChangedForSwipe()_ACTION_STATE_IDLE");
            if (foregroundView.getTranslationX() < 0f) {
                swipingStartingX =
                        recyclerView.getWidth()
                                - diaryDayListViewHolder.binding.includeBackground.textDeleteButton.getWidth();
            } else {
                swipingStartingX = 0f;
            }
        }
        Log.d("LeftSwipingCallBack_", "onSelectedChangedForSwipe()swipingStartingX:" + swipingStartingX);

    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d("LeftSwipingCallBack_", "onMove()");
        return false;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d("LeftSwipingCallBack_", "getSwipeThreshold()");
        DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder;
        if (viewHolder instanceof DiaryDayListAdapter.DiaryDayListViewHolder) {
            diaryDayListViewHolder = (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
        } else {
            return 1f;
        }

        LinearLayout foregroundView = diaryDayListViewHolder.binding.linerLayoutForeground;
        TextView backgroundDeleteButton =
                diaryDayListViewHolder.binding.includeBackground.textDeleteButton;
        float dX = foregroundView.getTranslationX();

        // TODO:下記必要？
        // スワイプメニューの半開き状態について
        // スワイプメニューを開く時に全開しないように、独自のアニメーションで堰き止める
        if (swipingStartingX == 0f) {
            float x = backgroundDeleteButton.getWidth() / 2f;
            if (dX < -x) {
                animateSwipingView(
                        foregroundView,
                        250,
                        -backgroundDeleteButton.getWidth(),
                        null,
                        null
                );
            }
        }



        // スワイプ境界を背面ボタンの中心にする
        float value = backgroundDeleteButton.getWidth() / 2f / recyclerView.getWidth();

        // スワイプメニューを閉じる時は、反対方向からの割合に変えないといけない
        if (swipingStartingX <= 0f) {
            Log.d("LeftSwipingCallBack_", "getSwipeThreshold()_return:" + value);
            return value;
        } else {
            Log.d("LeftSwipingCallBack_", "getSwipeThreshold()_return:" + (1 - value));
            return 1 - value;
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d("LeftSwipingCallBack_", "onSwiped()");
        swipedAdapterPosition = viewHolder.getBindingAdapterPosition();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        Log.d("LeftSwipingCallBack", "onChildDraw()");
        Log.d("LeftSwipingCallBack", "onChildDraw()_dX:" + dX);

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            Log.d("LeftSwipingCallBack", "onChildDraw()_ACTION_STATE_SWIPE");
            DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder;
            if (viewHolder instanceof DiaryDayListAdapter.DiaryDayListViewHolder) {
                diaryDayListViewHolder = (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
            } else {
                return;
            }
            if (!diaryDayListViewHolder.binding.linerLayoutForeground.isClickable()) {
                // 半開き状態までアニメーション中
                return;
            }

            if (dX <= 0f) {
                float backgroundButtonWidth =
                        diaryDayListViewHolder.binding.includeBackground.textDeleteButton.getWidth();
                float translationValueX = Math.min(0, Math.max(-backgroundButtonWidth, dX + swipingStartingX));
                Log.d("LeftSwipingCallBack", "onChildDraw()_translationValueX:" + translationValueX);
                diaryDayListViewHolder.binding.linerLayoutForeground.setTranslationX(translationValueX);

                // 手動でスワイプを戻した時にクリア
                if ((dX == 0 || translationValueX ==0)
                        && swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
                    Log.d("LeftSwipingCallBack", "onChildDraw()_swipedAdapterPosition:Clear");
                    clearSwipedAdapterPosition();
                }
            }
        }
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d("LeftSwipingCallBack_", "clearView()");
        Log.d("LeftSwipingCallBack_", "clearView()_viewHolder.getBindingAdapterPosition():" + viewHolder.getBindingAdapterPosition());
        super.clearView(recyclerView, viewHolder);

        // TODO:下記用途不明
        if (viewHolder.getBindingAdapterPosition() == -1) {
            DiaryDayListAdapter.DiaryDayListViewHolder diaryDayListViewHolder;
            if (viewHolder instanceof DiaryDayListAdapter.DiaryDayListViewHolder) {
                diaryDayListViewHolder = (DiaryDayListAdapter.DiaryDayListViewHolder) viewHolder;
            } else {
                return;
            }
            diaryDayListViewHolder.binding.linerLayoutForeground.setTranslationX(0f);
        }
    }

    private void clearSwipedAdapterPosition() {
        swipedAdapterPosition = -1;
    }

    private void animateSwipingView(View view, int duration, float translationValue, @Nullable AnimationAction startAction, @Nullable AnimationAction endAction) {
        Log.d("LeftSwipingCallBack_", "animateSwipingView()");
        view.animate()
                .setDuration(duration)
                .setInterpolator(new FastOutSlowInInterpolator())
                .translationX(translationValue)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        view.setClickable(false);
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
                        view.setClickable(true);
                    }
                })
                .start();
    }

    @FunctionalInterface
    private interface AnimationAction {
        void process();
    }
}
