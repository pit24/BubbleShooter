package pit.bubbleshooter;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;

public class PlayingField {
	private int mWidth, mHeight; // Габариты поля
	private int mX, mY; // Положение поля
	private int mBHorisontal, mBVertical; // Ширина в пузырях по горизонтали и
											// вертикали

	private int mBubbleRadius; // радиус пузырей
	private int mSpacing; // отступ между пузырями в сетке

	private ArrayList<RadiusChange> mListener; // слушатели изменеия радиуса

	private Bubble mBubbleNext, mBubbleInGun, mBubbleFly; // пузыри:
															// следующий,заряженый
															// и летящий.

	private Grid mGrid;
	
	private Paint mPaint; // общая кисть
	
	private Bitmap mBitmapFon; //Картинка фона

	public PlayingField(int width, int height) {
		mPaint = new Paint();
			
		mListener = new ArrayList<RadiusChange>();
		// width, height - габариты view
		mBHorisontal = 10; // задаем количество пузырей по горизонтали
		mBVertical = 15; // количество пузырей по вертикали

		СalculateSize(width, height);

		// сетка для размещения прилипших пузырей
		mGrid = new Grid(mBHorisontal, mBVertical, mBubbleRadius, mSpacing);
		addRadiusListener(mGrid);

		// зарядим пушку
		GunCharge();

		// накидаем пузырьков
		mGrid.CreateFixedBubbles(this);

	}

	public void СalculateSize(int width, int height) {
		// рассчитаем размер игрового поля

		mBubbleRadius = Math.min(width / mBHorisontal / 2,
				(int) (height / (Math.sqrt(3) * mBVertical + 2)));

		GlobalParam.mBubbleFlySpeed = mBubbleRadius / 2;

		mSpacing = (int) mBubbleRadius / 10;
		mBubbleRadius = mBubbleRadius - mSpacing;
		mWidth = (mBubbleRadius + mSpacing) * 2 * mBHorisontal;
		mHeight = (int) ((Math.sqrt(3) * (mBVertical) + 2) * (mBubbleRadius + mSpacing));
		mX = (width - mWidth) / 2;
		mY = (height - mHeight) / 2;

		// Смаштабируем картинку фона
		mBitmapFon= Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(mBitmapFon);
		tempCanvas.drawColor(Color.BLACK, Mode.CLEAR);
		tempCanvas.drawBitmap(GlobalParam.mBitmapFon,
				new Rect(0, 0, GlobalParam.mBitmapFon.getWidth(), GlobalParam.mBitmapFon.getHeight()),
				new Rect(0, 0, mWidth, mHeight), mPaint);
		
		
		// оповестим шарики о изменении радиуса
		for (int i = 0; i < mListener.size(); i++) {
			mListener.get(i).onRadiusChange(mBubbleRadius, mSpacing);
		}

		// сменим позиции шаров в пушке
		if (mBubbleNext != null)
			mBubbleNext.SetPosition(
					mWidth / 2 - (mBubbleRadius + mSpacing) * 2, mHeight
							- (mBubbleRadius + mSpacing));
		if (mBubbleInGun != null)
			mBubbleInGun.SetPosition(mWidth / 2, mHeight
					- (mBubbleRadius + mSpacing));
	}

	// перерисовка игрового поля
	public void draw(Canvas mCanvas) {
		mCanvas.drawBitmap(mBitmapFon, mX, mY, mPaint);

		mPaint.setColor(Color.DKGRAY);
		mCanvas.drawRect(mX, mY + mHeight - (mBubbleRadius + mSpacing) * 2, mX
				+ mWidth, mY + mHeight, mPaint);
		// if (mBubbleInGun.ReDrawNeed())
		mBubbleInGun.draw(mCanvas, mX, mY);
		// if (mBubbleNext.ReDrawNeed())
		mBubbleNext.draw(mCanvas, mX, mY);

		// отрисовка летящего пузыря
		if (mBubbleFly != null)
			mBubbleFly.draw(mCanvas, mX, mY);

		// отрисовка висящих пузырей
		mGrid.draw(mCanvas, mX, mY);

		// отрисовка счета
		mPaint.setColor(Color.LTGRAY);
		mPaint.setTextSize((mBubbleRadius + mSpacing) * 1.5f);
		mCanvas.drawText(String.valueOf(GlobalParam.mScores), mX + mWidth / 10,
				mY + mHeight - (mBubbleRadius + mSpacing) * 0.5f, mPaint);

		// отрисовка шага до опускания сетки
		mCanvas.drawText(String.valueOf(mGrid.getStepGridDown()), mX + mWidth
				/ 10 * 8, mY + mHeight - (mBubbleRadius + mSpacing) * 0.5f,
				mPaint);
	}

	public int UpdatePosition() {
		int FixId;
		if (mBubbleFly != null) {
			mBubbleFly.UpdatePosition(mWidth, mHeight);
			FixId = mGrid.ifTouch(mBubbleFly);
			if (FixId >= 0) {
				int[] mas = mGrid.GetNear(FixId);
				int sector = mGrid.GetTouchSector(FixId, mBubbleFly);
				// тут нужно поставить летящий пузырь в сетку с id=mas[sector]
				mGrid.addInGrig(mBubbleFly, mas[sector], this);
				mBubbleFly = null;
			}

			if (mBubbleFly != null && mBubbleFly.GetPosition().y < 0) {
				mBubbleFly.dispose();
				mBubbleFly = null;
			}
		}
		// Удалим из сетки лопнутые пузыри
		mGrid.delFixBubble();

		return mGrid.getEndGame();
	}

	// перезарядка пушки
	private void GunCharge() {
		if (mBubbleNext == null) {
			mBubbleNext = new Bubble(mWidth / 2 - (mBubbleRadius + mSpacing)
					* 2, mHeight - (mBubbleRadius + mSpacing),
					(int) mBubbleRadius / 2, mGrid.RandColor());
			addRadiusListener(mBubbleNext);
		}
		mBubbleInGun = mBubbleNext;
		mBubbleInGun.SetPosition(mWidth / 2, mHeight
				- (mBubbleRadius + mSpacing));
		mBubbleInGun.setRadius(mBubbleRadius);
		mBubbleNext = new Bubble(mWidth / 2 - (mBubbleRadius + mSpacing) * 2,
				mHeight - (mBubbleRadius + mSpacing), (int) mBubbleRadius / 2,
				mGrid.RandColor());
		addRadiusListener(mBubbleNext);
	}

	// поменять местами шарики в пушке
	private void GunChange() {
		int c = mBubbleNext.getColor();
		mBubbleNext.setColor(mBubbleInGun.getColor());
		mBubbleInGun.setColor(c);
	}

	// Обработаем нажатие на поле
	public void onTouch(float x2, float y2) {
		// выстрел
		if (y2 < mY + mHeight- (mBubbleRadius + mSpacing) * 2
				&& y2 > mY
				&& x2 > mX
				&& x2 < mX + mWidth && mBubbleFly == null) {
			mBubbleInGun.SetSpeedToPoint(x2 - mX, y2 - mY);
			mBubbleFly = mBubbleInGun;
			GunCharge();
		}
		//перемена местами зарядов в пушке
		if (y2 > mY + mHeight
				- (mBubbleRadius + mSpacing) * 2
				&& y2 <= mY + mHeight
				&& x2 > mX + mWidth / 2
						- (mBubbleRadius + mSpacing) * 3
				&& x2 < mX + mWidth / 2
						+ (mBubbleRadius + mSpacing)) {
			GunChange();
		}
	}

	public void addRadiusListener(RadiusChange rl) {
		mListener.add(rl);
		// почистим массив слушателей события
		for (int i = 0; i < mListener.size(); i = (!mListener.get(i).isAlive()) ? i
				: i + 1) {
			if (!mListener.get(i).isAlive())
				mListener.remove(i);
		}
	}
}
