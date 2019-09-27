package com.bink.wallet.utils;

import android.graphics.*;

import java.util.Stack;

public class CardViewStyleKit {
    public enum ResizingBehavior {
        AspectFit,
        AspectFill,
        Stretch,
        Center,
    }

    private static class CacheForCanvas {
        private static Paint paint = new Paint();
        private static RectF originalFrame = new RectF(0f, 0f, 240f, 120f);
        private static RectF resizedFrame = new RectF();
        private static RectF rectangleRect = new RectF();
        private static Path rectanglePath = new Path();
        private static RectF rectangle2Rect = new RectF();
        private static Path rectangle2Path = new Path();
    }

    public static void drawCanvas(Canvas canvas, int firstColor, int secondColor) {
        CardViewStyleKit.drawCanvas(canvas, new RectF(0f, 0f, 240f, 120f), ResizingBehavior.AspectFit, firstColor, secondColor);
    }

    public static void drawCanvas(Canvas canvas, RectF targetFrame, ResizingBehavior resizing, int firstColor, int secondColor) {
        // General Declarations
        Stack<Matrix> currentTransformation = new Stack<Matrix>();
        currentTransformation.push(new Matrix());
        Paint paint = CacheForCanvas.paint;

        // Resize to Target Frame
        canvas.save();
        RectF resizedFrame = CacheForCanvas.resizedFrame;
        CardViewStyleKit.resizingBehaviorApply(resizing, CacheForCanvas.originalFrame, targetFrame, resizedFrame);
        canvas.scale(resizedFrame.width() / 240f, resizedFrame.height() / 120f);

        // Rectangle 1
        canvas.save();
        canvas.translate(.76f, 81.4f);
        currentTransformation.peek().postTranslate(120.76f, 81.4f);
        canvas.rotate(-45f);
        currentTransformation.peek().postRotate(-45f);
        RectF rectangleRect = CacheForCanvas.rectangleRect;
        rectangleRect.set(0f, 0f, 427.54f, 333.64f);
        Path rectanglePath = CacheForCanvas.rectanglePath;
        rectanglePath.reset();
        rectanglePath.addRoundRect(rectangleRect, 8f, 8f, Path.Direction.CW);

        paint.reset();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(firstColor);
        canvas.drawPath(rectanglePath, paint);
        canvas.restore();

        // Rectangle 2
        canvas.save();
        canvas.translate(14f, 38.72f);
        currentTransformation.peek().postTranslate(134f, 38.72f);
        canvas.rotate(-20f);
        currentTransformation.peek().postRotate(-20f);
        RectF rectangle2Rect = CacheForCanvas.rectangle2Rect;
        rectangle2Rect.set(0f, 0f, 514.29f, 370.52f);
        Path rectangle2Path = CacheForCanvas.rectangle2Path;
        rectangle2Path.reset();
        rectangle2Path.addRoundRect(rectangle2Rect, 8f, 8f, Path.Direction.CW);

        paint.reset();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(secondColor);
        canvas.drawPath(rectangle2Path, paint);
        canvas.restore();

        canvas.restore();
    }

    // Resizing Behavior
    private static void resizingBehaviorApply(ResizingBehavior behavior, RectF rect, RectF target, RectF result) {
        if (rect.equals(target) || target == null) {
            result.set(rect);
            return;
        }

        if (behavior == ResizingBehavior.Stretch) {
            result.set(target);
            return;
        }

        float xRatio = Math.abs(target.width() / rect.width());
        float yRatio = Math.abs(target.height() / rect.height());
        float scale = 0f;

        switch (behavior) {
            case AspectFit: {
                scale = Math.min(xRatio, yRatio);
                break;
            }
            case AspectFill: {
                scale = Math.max(xRatio, yRatio);
                break;
            }
            case Center: {
                scale = 1f;
                break;
            }
        }

        float newWidth = Math.abs(rect.width() * scale);
        float newHeight = Math.abs(rect.height() * scale);
        result.set(target.centerX() - newWidth / 2,
                target.centerY() - newHeight / 2,
                target.centerX() + newWidth / 2,
                target.centerY() + newHeight / 2);
    }
}