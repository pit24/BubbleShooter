package pit.bubbleshooter;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class PlayingField {
	public int mWidth, mHeight; // Габариты поля
	public int mX, mY; // Положение поля
	private int mBHorisontal, mBVertical; // Ширина в пузырях по горизонтали и
											// вертикали

	private int mBubbleRadius; // радиус пузырей
	private int mSpacing; // отступ между пузырями в сетке

	public ArrayList<Bubble> mBubbleArrFly; // динамический массив летящих
											// пузырей

	public ArrayList<RadiusChange> mListener; // слушатели изменеия радиуса

	public Bubble mBubbleNext, mBubbleInGun, mBubbleFly; // пузыри:
															// следующий,заряженый
															// и летящий.

	private Grid mGrid;

	PlayingField(int width, int height) {
		mListener = new ArrayList<RadiusChange>();
		// width, height - габариты view
		mBHorisontal = 10; // задаем количество пузырей по горизонтали
		mBVertical = 15; // количество пузырей по вертикали

		mBubbleArrFly = new ArrayList<Bubble>();
		СalculateSize(width, height);

		// сетка для размещения прилипших пузырей
		mGrid = new Grid(mBHorisontal, mBVertical, mBubbleRadius, mSpacing);
		addRadiusListener(mGrid);
		// зарядим пушку
		GunCharge();

		// накидаем пузырьков
		mGrid.CreateFixedBubbles();

	}

	public void СalculateSize(int width, int height) {
		// рассчитаем размер игрового поля

		mBubbleRadius = Math.min(width / mBHorisontal / 2,
				(int) (height / (Math.sqrt(3) * mBVertical + 2)));

		mSpacing = (int) mBubbleRadius / 10;
		mBubbleRadius = mBubbleRadius - mSpacing;
		mWidth = (mBubbleRadius + mSpacing) * 2 * mBHorisontal;
		// mBVertical = height / (mBubbleRadius + mSpacing) / 2;
		// mHeight = (int) ((Math.sqrt(3) * (mBVertical-1) +3)*(mBubbleRadius +
		// mSpacing));
		mHeight = (int) ((Math.sqrt(3) * (mBVertical) + 2) * (mBubbleRadius + mSpacing));
		// mHeight = (mBubbleRadius + mSpacing) * 2 * mBVertical;
		mX = (width - mWidth) / 2;
		mY = (height - mHeight) / 2;

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

	void draw(Canvas mCanvas) {

		Paint mPaint = new Paint();
		// mPaint.setColor(Color.GRAY);
		// mCanvas.drawRect(mX, mY, mX+mWidth,mY+mHeight, mPaint);
		Rect src = new Rect(0, 0, GlobalParam.mBitmapFon.getWidth(),
				GlobalParam.mBitmapFon.getHeight());
		Rect dst = new Rect(mX, mY, mX + mWidth, mY + mHeight);
		mCanvas.drawBitmap(GlobalParam.mBitmapFon, src, dst, null);

		mPaint.setColor(Color.DKGRAY);
		mCanvas.drawRect(mX, mY + mHeight - (mBubbleRadius + mSpacing) * 2, mX
				+ mWidth, mY + mHeight, mPaint);
		// if (mBubbleInGun.ReDrawNeed())
		mBubbleInGun.draw(mCanvas, mX, mY);
		// if (mBubbleNext.ReDrawNeed())
		mBubbleNext.draw(mCanvas, mX, mY);

		// отрисовка летящих пузырей
		for (int i = 0; i < mBubbleArrFly.size(); i++) {
			mBubbleArrFly.get(i).draw(mCanvas, mX, mY);
		}

		// отрисовка висящих пузырей
		mGrid.draw(mCanvas, mX, mY);

		// отрисовка счета
		mPaint.setColor(Color.LTGRAY);
		mPaint.setTextSize((mBubbleRadius + mSpacing) * 1.5f);
		mCanvas.drawText(String.valueOf(GlobalParam.mScores), mX + mWidth / 10,
				mY + mHeight - (mBubbleRadius + mSpacing) * 0.5f, mPaint);

		// отрисовка шага до опускания сетки
		mCanvas.drawText(String.valueOf(mGrid.getStepGridDown()), mX + mWidth / 10*8,
				mY + mHeight - (mBubbleRadius + mSpacing) * 0.5f, mPaint);

	}

	public int UpdatePosition() {
		int FixId;
		for (int i = 0; i < mBubbleArrFly.size(); i++) {
			mBubbleArrFly.get(i).UpdatePosition(mWidth, mHeight);
			FixId = mGrid.ifTouch(mBubbleArrFly.get(i));
			if (FixId >= 0) {
				int[] mas = mGrid.GetNear(FixId);
				int sector = mGrid.GetTouchSector(FixId, mBubbleArrFly.get(i));
				// тут нужно поставить летящий пузырь в сетку с id=mas[sector]
				if (mGrid.addInGrig(mBubbleArrFly.get(i), mas[sector]))
					mBubbleArrFly.remove(i);
				break;

			}
			if (mBubbleArrFly.get(i).GetPosition().y < 0) {
				mBubbleArrFly.get(i).dispose();
				mBubbleArrFly.remove(i);

			}
		}
		mGrid.delFixBubble();

		return mGrid.getEndGame();
	}

	void GunCharge() {
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
	void GunChange() {
		int c = mBubbleNext.getColor();
		mBubbleNext.setColor(mBubbleInGun.getColor());
		mBubbleInGun.setColor(c);
	}

	// GET - Теры
	public int getBubbleRadius() {
		return mBubbleRadius;
	}

	public int getSpacing() {
		return mSpacing;
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
