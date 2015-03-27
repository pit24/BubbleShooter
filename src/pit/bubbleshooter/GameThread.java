package pit.bubbleshooter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;

public class GameThread extends Thread {

	// ссылка на менеджер игры
	protected Manager mManager;

	// игровое поле с объектами
	private PlayingField mField;

	// пауза потока
	public boolean mPause;
	
	//TODO для отладки. Удалить
	public String mst;

	// вложеный интерфейсный класс. Для упрощения работы с канвой
	private interface DrawHelper {
		void draw(Canvas canvas);
	}

	// интерфейс рисования игровых объектов
	private DrawHelper mDrawGameObject;
	private DrawHelper mDrawMessageWin, mDrawMessageLost;

	// Конструктор
	public GameThread(Manager mManager) {
		this.mManager = mManager;
		// mst=String.valueOf(mManager.mGameView.getWidth())+" "+String.valueOf(mManager.mGameView.getHeight());
		mField = new PlayingField(mManager.mGameView.getWidth(),
				mManager.mGameView.getHeight());

		// инициализируем интерфейс рисования игровых объектов
		mDrawGameObject = new DrawHelper() {

			@Override
			public void draw(Canvas canvas) {
				// чистим экран
				canvas.drawColor(0, Mode.CLEAR);

				

				mField.draw(canvas);

				// mPaint.setTextSize(20);
				// canvas.drawText(mst, 10, 100, mPaint);
			}
		};
		// инициализируем интерфейс рисования сообщений
		mDrawMessageWin = new DrawHelper() {

			@Override
			public void draw(Canvas canvas) {
				// чистим экран
				//canvas.drawColor(0, Mode.CLEAR);

				//
				Paint mPaint = new Paint();
				mPaint.setColor(Color.GRAY);
				canvas.drawRect(10, canvas.getHeight()/2, canvas.getWidth()-10, canvas.getHeight()/2+canvas.getHeight()/10+10, mPaint);
				
				mPaint.setColor(Color.GREEN);
				mPaint.setTextSize(canvas.getHeight()/20);
				canvas.drawText("Вы победили!!!.", canvas.getWidth()/3+10, canvas.getHeight()/2+canvas.getHeight()/20+10, mPaint);
				
			}
		};
		mDrawMessageLost = new DrawHelper() {

			@Override
			public void draw(Canvas canvas) {
				// чистим экран
				//canvas.drawColor(0, Mode.CLEAR);

				// рисуем игровое поле
				Paint mPaint = new Paint();
				mPaint.setColor(Color.GRAY);
				canvas.drawRect(10, canvas.getHeight()/2, canvas.getWidth()-10, canvas.getHeight()/2+canvas.getHeight()/10+10, mPaint);
				
				mPaint.setColor(Color.GREEN);
				mPaint.setTextSize(canvas.getHeight()/20);
				canvas.drawText("Вы проиграли.", canvas.getWidth()/3+10, canvas.getHeight()/2+canvas.getHeight()/20+10, mPaint);
				
			}
		};
	};

	public void refreshField(int w, int h) {
		mField.СalculateSize(w, h);
	}

	@Override
	// Действия, выполняемые в потоке
	public void run() {
		mPause = false;
		boolean work = true; // флаг работы цикла в потоке
		long StartTime = 0; // для замера времени
		long CurrentTime = 0;
		int i=0;
		while (work) {
			// обновляем объекты
			i=updateObjects();
						
			// рисуем на экран
			draw(mDrawGameObject);
			
			//если проигрыш
			if (i == 1) {
				GlobalParam.mScores = 0;
				draw(mDrawMessageLost);
			} else if (i==2) draw(mDrawMessageWin);

			// Засечем время, чтоб притормозить отрисовку(С заданной частотой
			// кадров 25 в сек)
			CurrentTime = System.currentTimeMillis();

			work = !isInterrupted(); // если поток прирвут - выйдем из цикла

			// если поставили на паузу - притормозим поток
			while (mPause) {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// скорее всего поток просят завершиться
						// завершим поток
						mPause = false;
						work = false;
					}
				}
			}

			// притормозим если управились быстрее 40мс (25 кадров в сек)
			if ((CurrentTime - StartTime) < GlobalParam.mMsInFrame) {

				try {
					sleep(GlobalParam.mMsInFrame - CurrentTime + StartTime); // задержка, чтоб не
															// нагружать
															// систему.
				} catch (InterruptedException e) {
					// скорее всего поток просят завершиться
					// завершим поток
					work = false;
					break; // если поток прирвут - выйдем из цикла без паузы.
				}
			}
			StartTime = CurrentTime;

		}

	}

	// Метод принимает интерфейс предоставляе ему канву
	// Для упрощения рисования
	private void draw(DrawHelper helper) {
		Canvas canvas = null;
		try {
			// подготовка Canvas-а
			canvas = mManager.mSurfaceHolder.lockCanvas();
			synchronized (mManager.mSurfaceHolder) {
				helper.draw(canvas);
			}
		} catch (Exception e) {
		} finally {
			if (canvas != null) {
				mManager.mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	
	// Обновление состояния игровых объектов
	private int updateObjects() {
		int i = mField.UpdatePosition();
		
		if (i > 0) {
			interrupt();

		}
		return i;
	}

	// выстрел по координатам
	public void Shoot(float x2, float y2) {
		
		if (y2 < mField.mY + mField.mHeight
				- (mField.getBubbleRadius() + mField.getSpacing()) * 2
				&& y2 > mField.mY
				&& x2 > mField.mX
				&& x2 < mField.mX + mField.mWidth
				&& mField.mBubbleFly==null) {
			mField.mBubbleInGun.SetSpeedToPoint(x2 - mField.mX, y2 - mField.mY);
			mField.mBubbleFly=mField.mBubbleInGun;
			mField.GunCharge();
		}
		if (y2 > mField.mY + mField.mHeight
				- (mField.getBubbleRadius() + mField.getSpacing()) * 2
				&& y2 <= mField.mY + mField.mHeight
				&& x2 > mField.mX +mField.mWidth / 2 - (mField.getBubbleRadius() + mField.getSpacing()) * 3
				&& x2 < mField.mX +mField.mWidth / 2 + (mField.getBubbleRadius() + mField.getSpacing())) {
			mField.GunChange();
		}
	}

}
