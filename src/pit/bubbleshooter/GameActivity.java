package pit.bubbleshooter;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity {

	protected Manager mManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// делает окно на весь экран
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_game);
		
		//получаем уже инициализированный компонент для рисования
		GameView mGameView=(GameView) findViewById(R.id.gameview);
		
		//инициализируем объект для управления игрой
		if (mManager==null)
			mManager=new Manager(mGameView,this);
		
		
	}
	
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//	    super.onConfigurationChanged(newConfig);
//
//	    // Checks the orientation of the screen
//	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//	        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//	    }
//	}
	
	//Создаем меню
	// Работает только для api_9
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.mymenu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	// обработка нажатий в меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
    	case R.id.menu_new:
    		mManager.StartNewGame();
    		break;
    	case R.id.menu_pause:
    		mManager.PauseGame();
    		break;
    	case R.id.menu_resume:
    		mManager.ResumeGame();
    		break;
    	case R.id.menu_end:
    		mManager.EndGame();
    		finish();
    	break;
    	}
    	
      
      return super.onOptionsItemSelected(item);
    }
}
