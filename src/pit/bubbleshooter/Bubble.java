package pit.bubbleshooter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;

public class Bubble implements RadiusChange {
	private int mRadius;
	private Point mPoint; // центр пузыря

	private float mXIncr, mYIncr; // инкремент по осям для движения пузыря
	

	private int mColor;

	private boolean mRoofId; // текущий ид потолка. Меняется каждый проход
								// цикла. Для определения оторванных пузырей

	private int mBurst; // Пузырь лопаем в несколько шагов

	private Bitmap mBitmap;
	private Canvas tempCanvas;

	private boolean mDisposed;

	// private PlayingField mField;

	public Bubble(int xS, int yS, int rS, int Color) {
		
		mDisposed=false;
		mRadius = rS;
		mBitmap = Bitmap.createBitmap(GlobalParam.mBitmap.getWidth(),
				GlobalParam.mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		tempCanvas = new Canvas(mBitmap);
		// tempCanvas.drawColor(android.graphics.Color.WHITE);
		Paint mPaint = new Paint();
		tempCanvas.drawBitmap(GlobalParam.mBitmap,new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()) , new Rect(0,0,mRadius*2,mRadius*2), mPaint);
		mColor = Color;
		tempCanvas.drawColor(mColor, Mode.MULTIPLY);

		mBurst = 0;
		// mField = FieldS;
		mPoint = new Point(0, 0);

		

		SetSpeed(0, 0);
		SetPosition(xS, yS);

	}
	@Override
	public void onRadiusChange(int radius, int spacing) {
		setRadius(radius);
		
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return !mDisposed;
	}
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
		//tempCanvas.drawBitmap(GlobalParam.mBitmap, 0, 0, null);
		//tempCanvas.drawColor(mColor, Mode.MULTIPLY);
		Paint mPaint = new Paint();
		tempCanvas.drawColor(Color.BLACK, Mode.CLEAR);
		tempCanvas.drawBitmap(GlobalParam.mBitmap,new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()) , new Rect(0,0,mRadius*2,mRadius*2), mPaint);
		tempCanvas.drawColor(mColor, Mode.MULTIPLY);

	}

	void draw(Canvas mCanvas, int xS, int yS) {
		Paint mPaint = new Paint();
		mPaint.setColor(mColor);

		if (mBurst > 0) {
			// mCanvas.drawCircle(mPoint.x + xS, mPoint.y + yS, mRadius-mBurst,
			// mPaint);
			Rect src = new Rect(0, 0, mRadius*2, mRadius*2);
			int rr = mRadius - mBurst;
			Rect dst = new Rect((int) (mPoint.x + xS - rr), (int) (mPoint.y
					+ yS - rr), (int) (mPoint.x + xS + rr), (int) (mPoint.y
					+ yS + rr));
			mCanvas.drawBitmap(mBitmap, src, dst, null);
			mBurst++;
		} else {

//			Rect src = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
//			Rect dst = new Rect((int) (mPoint.x + xS - mRadius),
//					(int) (mPoint.y + yS - mRadius),
//					(int) (mPoint.x + xS + mRadius),
//					(int) (mPoint.y + yS + mRadius));
//			mCanvas.drawBitmap(mBitmap, src, dst, mPaint);
			
			mCanvas.drawBitmap(mBitmap, mPoint.x + xS- mRadius,mPoint.y + yS- mRadius, mPaint);
			// mCanvas.drawBitmap(mBitmap, mPoint.x + xS-mRadius, mPoint.y +
			// yS-mRadius, mPaint);
			// mCanvas.drawCircle(mPoint.x + xS, mPoint.y + yS, mRadius,
			// mPaint);

		}

	}

	void UpdatePosition(int width, int height) {


		// отражение от стен
		if (mPoint.x <= mRadius || mPoint.x >= (width - mRadius))
			mXIncr = -mXIncr;
		if (mPoint.y > (height - mRadius))
			mYIncr = -mYIncr;

		mPoint.y = mPoint.y + mYIncr;
		mPoint.x = mPoint.x + mXIncr;

	}

	// задает скорость движения
	void SetSpeed(float xS, float yS) {
		mXIncr = xS;
		mYIncr = yS;
	}

	public void SetSpeedToPoint(float x2, float y2) {
		int Speed = mRadius / 2;
		// Посчитаем количество шагов до точки (x2,y2)
		float Steps = (float) (Math.sqrt((Math.pow((mPoint.x - x2), 2) + Math
				.pow((mPoint.y - y2), 2))) / Speed);
		SetSpeed((x2 - mPoint.x) / Steps, (y2 - mPoint.y) / Steps);
	}

	public Point GetPosition() {
		return mPoint;
	}

	// Рассчет дистанции между пузырями
	public float Distance(Bubble b2) {
		Point p2 = b2.GetPosition();
		return (float) Math.sqrt(Math.pow((mPoint.x - p2.x), 2)
				+ Math.pow((mPoint.y - p2.y), 2));
	}

	public int getColor() {
		return mColor;
	}

	public boolean getRoofId() {
		return mRoofId;
	}

	public void setRoofId(boolean id) {
		mRoofId = id;
	}

	public boolean getDeleted() {
		
		return (mBurst > mRadius)||mDisposed;
	}

	public void burst() {
		if (mBurst == 0)
			mBurst = 1;
	}

	public boolean getBursted() {
		return (mBurst > 0);
	}
	
	public void dispose(){
		mDisposed=true;
	}

}