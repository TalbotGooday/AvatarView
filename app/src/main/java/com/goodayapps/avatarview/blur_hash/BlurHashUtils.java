package com.goodayapps.avatarview.blur_hash;

import android.graphics.Bitmap;

final class BlurHashUtils {

    static double sRGBToLinear(long value) {
        double v = value / 255.0;
        if (v <= 0.04045) {
            return v / 12.92;
        } else {
            return Math.pow((v + 0.055) / 1.055, 2.4);
        }
    }

    static long linearTosRGB(double value) {
        double v = Math.max(0, Math.min(1, value));
        if (v <= 0.0031308) {
            return (long)(v * 12.92 * 255 + 0.5);
        } else {
            return (long)((1.055 * Math.pow(v, 1 / 2.4) - 0.055) * 255 + 0.5);
        }
    }

    static double signPow(double val, double exp) {
        return Math.copySign(Math.pow(Math.abs(val), exp), val);
    }

    static double max(double[][] values, int from, int endExclusive) {
        double result = Double.NEGATIVE_INFINITY;
        for (int i = from; i < endExclusive; i++) {
            for (int j = 0; j < values[i].length; j++) {
                double value = values[i][j];
                if (value > result) {
                    result = value;
                }
            }
        }
        return result;
    }

    public static int[] getBitmapPixels(Bitmap bitmap, int x, int y, int width, int height) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), x, y,
                width, height);
        final int[] subsetPixels = new int[width * height];
        for (int row = 0; row < height; row++) {
            System.arraycopy(pixels, (row * bitmap.getWidth()),
                    subsetPixels, row * width, width);
        }
        return subsetPixels;
    }
    private BlurHashUtils() {
    }
}
