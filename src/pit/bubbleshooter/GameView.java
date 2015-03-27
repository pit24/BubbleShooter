package pit.bubbleshooter;

//Это компонент для рисования на форме.

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class GameView extends SurfaceView {
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//Чтоб компонента принимала клики по ней.
		setFocusable(true);
	}

	
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//	//    return mThread.doKeyDown(keyCode);
//	}
//
//	@Override
//	public boolean onKeyUp(int keyCode, KeyEvent event)
//	{
//	  //  return mThread.doKeyUp(keyCode);
//	}
	
}

