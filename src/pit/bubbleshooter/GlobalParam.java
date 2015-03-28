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
	// Картинки шара и фона. Пока через глобальные свойства, так как не знаю, как
	// получить их из ресурсов без ссылки на Activity
	public static Bitmap mBitmap;
	public static Bitmap mBitmapFon; 
	
	public static int mScores; //очки
	public static int mMsInFrame=20; //милисекунд до следующего кадра (40 - 25кадр в сек)
	public static byte mStepToMooveGridDown=3; //через сколько промахов опустить сетку вниз
	public static int mBubbleFlySpeed; //скорость полета шарика
}
