package pit.bubbleshooter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;

public class Bubble implements RadiusChange {
	private int mRadius;

	private PointF mPoint; // центр пузыря

	private float mXIncr, mYIncr; // инкремент по осям для движения пузыря

	private int mColor; // цвет пузыря

	private boolean mRoofId; // текущий ид потолка. Меняется каждый проход
								// цикла. Для определения оторванных пузырей
	private boolean mBurst; // Пузырь в процессе лопанья

	private Bitmap mBitmap; // картинка для рисования пузыря на экран

	private boolean mDisposed; // Пузырь более не используется

	private static Paint mPaint = new Paint(); // Общая кисть для рисования

	// private PlayingField mField;

	public Bubble(int xS, int yS, int rS, int Color) {
		mDisposed = false;

		// Создадим картинку пузыря
		mBitmap = Bitmap.createBitmap(GlobalParam.mBitmap.getWidth(),
				GlobalParam.mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

		// Задаим радиус пызыря. Тамже подготовится битмап с нужным цветом
		mColor = Color;
		setRadius(rS);

		mBurst = false;
		// mField = FieldS;
		mPoint = new PointF(0f, 0f);

		SetSpeed(0f, 0f);
		SetPosition(xS, yS);
	}

	// Реализация интерфейса для слежения за изменением размеров пузыря
	@Override
	public void onRadiusChange(int radius, int spacing) {
		setRadius(radius);
	}

	@Override
	public boolean isAlive() {
		return !mDisposed;
	}
	
	// Отрисовка пузыря
	public void draw(Canvas mCanvas, int xS, int yS) {
		// лопанье
		if (mBurst) {
			setRadius(mRadius - 1);
		}
		mCanvas.drawBitmap(mBitmap, mPoint.x + xS - mRadius, mPoint.y + yS
				- mRadius, mPaint);
	}

	//Обновление позиции для летящего пузыря
	public void UpdatePosition(int width, int height) {
		// отражение от стен
		if (mPoint.x <= mRadius || mPoint.x >= (width - mRadius))
			mXIncr = -mXIncr;
		if (mPoint.y > (height - mRadius))
			mYIncr = -mYIncr;

		// полет
		mPoint.y = mPoint.y + mYIncr;
		mPoint.x = mPoint.x + mXIncr;
	}

	// Рассчет дистанции между пузырями
	public float Distance(Bubble b2) {
		return (float) Math.sqrt(Math.pow((mPoint.x - b2.GetPosition().x), 2)
				+ Math.pow((mPoint.y - b2.GetPosition().y), 2));
	}

	// лопнуть пузырь
	public void burst() {
		mBurst = true;
	}
	
	// уничтожить пузырь
	public void dispose() {
		mDisposed = true;
	}

	// ////////////////////
	// GET методы
	// ////////////////////

	public boolean getBursted() {
		return mBurst;
	}

	public boolean getDeleted() {
		return (mBurst && mRadius <= 0) || mDisposed;
	}

	public int getColor() {
		return mColor;
	}

	public boolean getRoofId() {
		return mRoofId;
	}

	public PointF GetPosition() {
		return mPoint;
	}

	// ////////////////////
	// SET методы
	// ////////////////////

	public void SetPosition(int xS, int yS) {
		mPoint.x = (float) xS;
		mPoint.y = (float) yS;
	}

	public void setRadius(int rS) {
		mRadius = rS;
		setColor(mColor);
	}

	public void setColor(int c) {
		mColor = c;

		Canvas tempCanvas = new Canvas(mBitmap);
		tempCanvas.drawColor(Color.BLACK, Mode.CLEAR);
		tempCanvas.drawBitmap(GlobalParam.mBitmap,
				new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()),
				new Rect(0, 0, mRadius * 2, mRadius * 2), mPaint);
		tempCanvas.drawColor(mColor, Mode.MULTIPLY);
	}

	// задает скорость движения
	public void SetSpeed(float xS, float yS) {
		mXIncr = xS;
		mYIncr = yS;
	}

	// задает скорость для движения к указанной точке
	public void SetSpeedToPoint(float x2, float y2) {
		// Посчитаем количество шагов до точки (x2,y2)
		float Steps = (float) (Math.sqrt((Math.pow((mPoint.x - x2), 2) + Math
				.pow((mPoint.y - y2), 2))) / GlobalParam.mBubbleFlySpeed);
		SetSpeed((x2 - mPoint.x) / Steps, (y2 - mPoint.y) / Steps);
	}
	
	public void setRoofId(boolean id) {
		mRoofId = id;
	}
}