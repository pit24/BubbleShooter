/**
 * 
 */
package pit.bubbleshooter;

import android.graphics.Bitmap;

/**
 * @author pit
 *
 */
public final class GlobalParam {
	public static Bitmap mBitmap;
	public static Bitmap mBitmapFon;
	public static int mScores;
	public static int mMsInFrame=20; //милисекунд до следующего кадра (40 - 25кадр в сек)
	public static byte mStepToMooveGridDown=3;
}
