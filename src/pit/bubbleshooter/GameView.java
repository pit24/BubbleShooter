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
}

