package pit.bubbleshooter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.widget.Toast;

public class GameThread extends Thread {

	// ссылка на менеджер игры
	protected Manager mManager;

	// ////игровое поле с объектами
	private PlayingField mField;

	// пауза потока
	public boolean mPause;
	String mst;

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

	//
	// // Инициализация положения объектов, в соответствии с размерами экрана
	// // @param screenHeight Высота экрана
	// // @param screenWidth Ширина экрана
	// public void initPositions(int screenHeight, int screenWidth) {
	// int left = (screenWidth - FIELD_WIDTH) / 2;
	// int top = (screenHeight - FIELD_HEIGHT) / 2;
	//
	// mField.set(left, top, left + FIELD_WIDTH, top + FIELD_HEIGHT);
	//
	// // // мячик ставится в центр поля
	// // mBall.setCenterX(mField.centerX());
	// // mBall.setCenterY(mField.centerY());
	// //
	// // // ракетка игрока - снизу по центру
	// // mUs.setCenterX(mField.centerX());
	// // mUs.setBottom(mField.bottom);
	// //
	// // // ракетка компьютера - сверху по центру
	// // mThem.setCenterX(mField.centerX());
	// // mThem.setTop(mField.top);
	// // mInitialized = true;
	// }
	//
	// // Обновление объектов на экране
	// private void refreshCanvas(Canvas canvas) {
	// // очистка
	// canvas.drawColor(0, Mode.CLEAR);
	//
	// // рисуем игровое поле
	// canvas.drawRect(mField, mPaint);
	//
	// // // рисуем игровые объекты
	// // mBall.draw(canvas);
	// // mUs.draw(canvas);
	// // mThem.draw(canvas);
	// // // вывод счета
	// // mScorePaint.setColor(Color.RED);
	// // canvas.drawText(String.valueOf(mThem.getScore()), mField.centerX(),
	// // mField.top - 10, mScorePaint);
	// // mScorePaint.setColor(Color.GREEN);
	// // canvas.drawText(String.valueOf(mUs.getScore()), mField.centerX(),
	// // mField.bottom + 25, mScorePaint);
	// }
	//
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
		int Speed = 1;// скорость движения шара в точках за одно обновление.
		if (y2 < mField.mY + mField.mHeight
				- (mField.getBubbleRadius() + mField.getSpacing()) * 2
				&& y2 > mField.mY
				&& x2 > mField.mX
				&& x2 < mField.mX + mField.mWidth) {
			mField.mBubbleInGun.SetSpeedToPoint(x2 - mField.mX, y2 - mField.mY,
					Speed);
			mField.mBubbleArrFly.add(mField.mBubbleInGun);
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
	// //
	// // if (mBall.getTop() > mUs.getTop()) {
	// // mThem.incScore();
	// // reset();
	// // }
	// // // проверка окончания игры
	// // if (mUs.getScore() == mMaxScore || mThem.getScore() == mMaxScore) {
	// // this.mRunning = false;
	// // }
	// }
	//
	// /**
	// * Обработка нажатия кнопки
	// *
	// * @param keyCode
	// * Код нажатой кнопки
	// * @return Было ли обработано нажатие
	// */
	// public boolean doKeyDown(int keyCode) {
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_DPAD_LEFT:
	// // mUs.setDirection(GameObject.DIR_LEFT);
	// return true;
	// case KeyEvent.KEYCODE_DPAD_RIGHT:
	// // mUs.setDirection(GameObject.DIR_RIGHT);
	// return true;
	// case KeyEvent.KEYCODE_DPAD_CENTER:
	// mPaused = !mPaused;
	// draw(mDrawPause);
	// return true;
	// default:
	// return false;
	// }
	// }
	//
	// /**
	// * Обработка отпускания кнопки
	// *
	// * @param keyCode
	// * Код кнопки
	// * @return Было ли обработано действие
	// */
	// public boolean doKeyUp(int keyCode) {
	// if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
	// || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
	// // mUs.setDirection(GameObject.DIR_NONE);
	// return true;
	// }
	// return false;
	// }
	//
	// // Интелект для ракетки соперника
	// private void moveAI() {
	// // if (mThem.getLeft() > mBall.getRight())
	// // mThem.setDirection(GameObject.DIR_LEFT);
	// // else if (mThem.getRight() < mBall.getLeft())
	// // mThem.setDirection(GameObject.DIR_RIGHT);
	// // mThem.update();
	// }
	//
	// // ограничивае передвижение ракеток
	// // private void placeInBounds(Racquet r) {
	// // if (r.getLeft() < mField.left)
	// // r.setLeft(mField.left);
	// // else if (r.getRight() > mField.right)
	// // r.setRight(mField.right);
	// // }
	//
	// // сброс игры
	// private void reset() {
	// // ставим мячик в центр
	// // mBall.setCenterX(mField.centerX());
	// // mBall.setCenterY(mField.centerY());
	// // // задаем ему новый случайный угол
	// // mBall.resetAngle();
	// //
	// // // ставим ракетки в центр
	// // mUs.setCenterX(mField.centerX());
	// // mThem.setCenterX(mField.centerX());
	//
	// // делаем паузу
	// try {
	// sleep(2000);
	// } catch (InterruptedException iex) {
	// }
	// }
	//
	//

}
