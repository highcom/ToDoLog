package com.highcom.todolog.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.highcom.todolog.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class SimpleCallbackHelper extends ItemTouchHelper.SimpleCallback {

    private static final int BUTTON_WIDTH = 75;
    private static final int FONT_SIZE = 14;
    private static int BUTTON_WIDTH_DP;
    private static int FONT_SIZE_DP;
    private RecyclerView recyclerView;
    private List<UnderlayButton> buttons;
    private GestureDetector gestureDetector;
    private int swipedPos = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<UnderlayButton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;
    private SimpleCallbackListener simpleCallbackListener;
    private boolean isMoved;

    public interface SimpleCallbackListener {
        boolean onSimpleCallbackMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);
        void clearSimpleCallbackView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder);
    }

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (UnderlayButton button : buttons){
                if(button.onClick(e.getX(), e.getY()))
                    break;
            }

            return true;
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent e) {
            if (swipedPos < 0) return false;
            Point point = new Point((int) e.getRawX(), (int) e.getRawY());

            RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos);
            if (swipedViewHolder == null) return false;
            View swipedItem = swipedViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP ||e.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y)
                    gestureDetector.onTouchEvent(e);
                else {
                    recoverQueue.add(swipedPos);
                    swipedPos = -1;
                    recoverSwipedItem();
                }
            }
            return false;
        }
    };

    public SimpleCallbackHelper(Context context, RecyclerView recyclerView, final float scale, SimpleCallbackListener listener) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
        BUTTON_WIDTH_DP = (int) (BUTTON_WIDTH * scale);
        FONT_SIZE_DP = (int) (FONT_SIZE * scale);
        this.recyclerView = recyclerView;
        this.simpleCallbackListener = listener;
        this.buttons = new ArrayList<>();
        this.gestureDetector = new GestureDetector(context, gestureListener);
        this.recyclerView.setOnTouchListener(onTouchListener);
        this.isMoved = false;
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>(){
            @Override
            public boolean add(Integer o) {
                if (contains(o))
                    return false;
                else
                    return super.add(o);
            }
        };

        attachSwipe();
    }

    public void setSwipeEnable(boolean enable) {
        if (enable) {
            setDefaultSwipeDirs(ItemTouchHelper.LEFT);
        } else {
            setDefaultSwipeDirs(ItemTouchHelper.ACTION_STATE_IDLE);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (target.itemView.getId() == R.id.row_footer) return false;
        return simpleCallbackListener.onSimpleCallbackMove(viewHolder, target);
    }

    @Override
    public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        isMoved = true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (viewHolder.itemView.getId() == R.id.row_footer) return;

        int pos = viewHolder.getAdapterPosition();

        if (swipedPos != pos)
            recoverQueue.add(swipedPos);

        swipedPos = pos;

        if (buttonsBuffer.containsKey(swipedPos))
            buttons = buttonsBuffer.get(swipedPos);
        else
            buttons.clear();

        buttonsBuffer.clear();
        swipeThreshold = 0.5f * buttons.size() * BUTTON_WIDTH_DP;
        recoverSwipedItem();
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (isMoved) {
            simpleCallbackListener.clearSimpleCallbackView(recyclerView, viewHolder);
        }
        isMoved = false;
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0){
            swipedPos = pos;
            return;
        }

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            if(dX < 0) {
                List<UnderlayButton> buffer = new ArrayList<>();

//                if (!buttonsBuffer.containsKey(pos)){
//                    instantiateUnderlayButton(viewHolder, buffer);
//                    buttonsBuffer.put(pos, buffer);
//                }
//                else {
//                    buffer = buttonsBuffer.get(pos);
//                }
                // データが更新されたら必ず作り直す
                instantiateUnderlayButton(viewHolder, buffer);
                buttonsBuffer.put(pos, buffer);

                translationX = dX * buffer.size() * BUTTON_WIDTH_DP / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void recoverSwipedItem(){
        while (!recoverQueue.isEmpty()){
            int pos = recoverQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }
    }

    private void drawButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX){
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();

        for (UnderlayButton button : buffer) {
            float left = right - dButtonWidth;
            button.onDraw(
                    c,
                    new RectF(
                            left,
                            itemView.getTop(),
                            right,
                            itemView.getBottom()
                    ),
                    pos
            );

            right = left;
        }
    }

    public void attachSwipe(){
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons);

    public static class UnderlayButton {
        private String text;
        private Bitmap imageRes;
        private int color;
        private int pos;
        private RectF clickRegion;
        private RecyclerView.ViewHolder viewHolder;
        private UnderlayButtonClickListener clickListener;

        public UnderlayButton(String text, Bitmap imageRes, int color, RecyclerView.ViewHolder holder, UnderlayButtonClickListener clickListener) {
            this.text = text;
            this.imageRes = imageRes;
            this.color = color;
            this.viewHolder = holder;
            this.clickListener = clickListener;
        }

        public boolean onClick(float x, float y){
            if (clickRegion != null && clickRegion.contains(x, y)){
                clickListener.onClick(viewHolder, pos);
                return true;
            }

            return false;
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
            // テキストを表示する場合はここを有効にする
//            float x = cWidth / 2f - r.width() / 2f - r.left;
//            float y = cHeight / 2f + r.height() / 2f - r.bottom;
//            c.drawText(text, rect.left + x, rect.top + y, p);
            // 画像を表示する
            RectF imgRect;
            if (rect.bottom - rect.top > 100) {
                float vcenter = rect.top + (rect.bottom - rect.top) / 2;
                rect.top = vcenter - 50;
                rect.bottom = vcenter + 50;
            }
            if ((rect.right - rect.left) > (rect.bottom - rect.top)) {
                // 横が長くなった場合には高さに合わせてクリップする
                float hcenter = rect.left + (rect.right - rect.left) / 2;
                float span = (rect.bottom - rect.top) / 2;
                imgRect = new RectF(hcenter - span, rect.top, hcenter + span, rect.bottom);
            } else {
                imgRect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
            }
            c.drawBitmap(imageRes, null, imgRect, p);

            clickRegion = rect;
            this.pos = pos;
        }
    }

    public interface UnderlayButtonClickListener {
        void onClick(RecyclerView.ViewHolder holder, int pos);
    }
}
