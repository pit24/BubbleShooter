package pit.bubbleshooter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

public class Manager implements SurfaceHolder.Callback, OnTouchListener {
	private Activity mActivity;
	public GameView mGameView;

	public SurfaceHolder mSurfaceHolder;
	
	// флаг говорящий о том, что можно использовать mSurfaceHolder
	private boolean mSurfaceActive;
		
	// флаг игра не завершена
	private boolean mGameStarted;
		
	// поток рисования и вычислений (игровой логики)
	public GameThread mGameThread;
	
	

	public Manager(GameView GView, Activity mAct) {
		mActivity = mAct;
		mGameView = GView;
		// подписываемся на события косания экрана
		mGameView.setOnTouchListener(this);

		// подписываемся на события SurfaceHolder
		mSurfaceHolder = mGameView.getHolder();
		mSurfaceHolder.addCallback(this);

		
		mGameStarted=false;
		
		//считаем ресурсы в глобальные свойства
		GlobalParam.mBitmap=BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.bubble);
		GlobalParam.mBitmapFon=BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.fon2);
		
		GlobalParam.mScores=0;
	}

	// Реализуем интерфейс обратного вызова для mGameView
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // нажатие

			break;
		case MotionEvent.ACTION_MOVE: // движение
			
			break;
		case MotionEvent.ACTION_UP: // отпускание
			// начнем новую игру, если не начата
			if (!mGameStarted || !mGameThread.isAlive()) {
				StartNewGame();
				break;
			}
			
			//выстрел. Решил сделать на отпускание, 
			// чтоб можно было добавить прицеливание по нажатию
			if (mGameThread.isAlive()) mGameThread.Shoot(x, y);
			
		case MotionEvent.ACTION_CANCEL:
			
			break;
		}
		return true;
	}

	// Реализуем интерфейс обратного вызова для SurfaceHolder
	@Override
	// Создание области рисования
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceActive = true;
		if (mGameStarted) 
			ResumeGame();
		else
			StartNewGame();
	}

	@Override
	// Изменение области рисования
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mSurfaceActive = false;
		if (mGameStarted) PauseGame();
		// тут надо изменить настройки экрана
		mGameThread.refreshField(mGameView.getWidth(),mGameView.getHeight());	
		
		
		mSurfaceActive = true;
		if (mGameStarted) ResumeGame();
	
	}

	@Override
	// Уничтожение области рисования
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceActive = false;
		if (mGameStarted) PauseGame();
	}

	// Запуск новой игры
	public void StartNewGame() {
		// Подготовимся к новой игре
		if (mGameStarted) EndGame();
		mGameStarted=true;
		
		mGameThread = new GameThread(this);
		// запустим поток игры
		mGameThread.start();
	}

	// Пауза игрового процесса
	public void PauseGame() {

		// поставим поток на паузу
		synchronized (mGameThread) {
			mGameThread.mPause=true;
		}
	}

	// Возобновление игрового процесса
	public void ResumeGame() {

		// запустим поток игры
		synchronized (mGameThread) {
			mGameThread.mPause=false;
			mGameThread.notifyAll();
		}
	}

	// Завершение игры
	public void EndGame() {
		mGameStarted=false;
		mGameThread.interrupt();
		
		while (mGameThread.isAlive()){
			try {
				mGameThread.join(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Activity getActivity(){
		return mActivity;
	}
}
