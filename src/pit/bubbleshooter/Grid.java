package pit.bubbleshooter;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

///////////////////////////////////////////
// Хитрая сетка для Прилипших пузырей
// Позиция в одномерном массиве используется как координата в сетке
// ////////////////////////////////////////

public class Grid implements RadiusChange {
	private int mBHorisontal, mBVertical; // Ширина в пузырях по горизонтали и
											// вертикали
	private int mBCountInTwoRows; // количество пузырей в двух смежных рядах
	private int mBubbleRadius;
	private int mSpacing; // отступ между пузырями в сетке
	private ArrayList<Bubble> mBubbleArrFix; // динамический массив прилипших
												// пузырей
	public boolean mFirstRowLong; // говорит о том, что первый ряд сетки длинее
									// второго
	private boolean mRoofId; // текущий ид потолка. Меняется каждый проход
								// цикла. Для определения оторванных пузырей

	private Random mrnd; // для случайного выбора цвета
	private ArrayList<Integer> mColors; // динамический массив цветов
	
	private byte mStepToMooveDown; // количество шагов до опускания сетки вниз 

	public Grid(int BHorisontal, int BVertical, int BubbleRadius, int Spacing) {

		mrnd = new Random(System.currentTimeMillis());
		mBHorisontal = BHorisontal;
		mBVertical = BVertical;
		mBubbleRadius = BubbleRadius;
		mSpacing = Spacing;
		mBCountInTwoRows = mBHorisontal * 2 - 1;
		mBubbleArrFix = new ArrayList<Bubble>();

		mStepToMooveDown=GlobalParam.mStepToMooveGridDown;
		
		// заполним массив цветов
		mColors = new ArrayList<Integer>();
		mColors.add(Color.rgb(255, 230, 10)); // желтый
		mColors.add(Color.BLUE);
		mColors.add(Color.CYAN);
		mColors.add(Color.rgb(0, 200, 40)); // зеленый
		mColors.add(Color.RED);
		mColors.add(Color.MAGENTA);

	}

	// ///////////// Математика /////////////////////////////

	// Возвращает номер строки по Индексу в массиве. Нумерация с 1
	int GetRow(int id) {
		int temp = id % mBCountInTwoRows; // положение в смежных рядах
											// 0..mBCountInTwoRows-1
		int temp1 = mBHorisontal - (mFirstRowLong ? 0 : 1); // длинна первого
															// ряда
		return (int) (1 + Math.floor(id / mBCountInTwoRows) * 2 + ((temp < temp1) ? 0
				: 1));
	}

	// Возвращает номер колонки по Индексу в массиве. Нумерация с 1
	int GetCol(int id) {
		int temp = id % mBCountInTwoRows; // положение в смежных рядах
											// 0..mBCountInTwoRows-1
		int temp1 = mBHorisontal - (mFirstRowLong ? 0 : 1); // длинна первого
															// ряда

		return (int) 1 + ((temp < temp1) ? temp : (temp - temp1));
	}

	// функция возвращает координаты центра пузыря по Индексу в массиве
	Point GetXYbyID(int id) {
		Point p;
		p = new Point(0, 0);

		// определим четный ли ряд
		int row = GetRow(id);

		// если ряд короткий нужно смещений по оси x вправо на пол пузыря
		// (temp*mBubbleRadius)
		//
		// mBubbleRadius*2*(GetCol(id)-1) // шаг на пузырь вправо
		p.x = (ifRowLong(row) * (mBubbleRadius + mSpacing))
				+ (mBubbleRadius + mSpacing) + (mBubbleRadius + mSpacing) * 2
				* (GetCol(id) - 1);

		// Math.sqrt(3)*mBubbleRadius шаг по оси Y
		p.y = (int) Math.round((Math.sqrt(3) * (mBubbleRadius + mSpacing))
				* (row - 1))
				+ (mBubbleRadius + mSpacing);

		return p;
	}

	// проверяет длинный ряд или короткий // 0-длинный, 1- короткий
	private int ifRowLong(int row) {
		int temp = row % 2; // 0-если четный, 1- нечетный ряд
		// определим длинный ли ряд
		if (temp == 1) {
			temp = mFirstRowLong ? 0 : 1;
		} else {
			temp = mFirstRowLong ? 1 : 0;
		}// 0-длинный, 1- короткий

		return temp;
	}

	// по строке и столбцу получить id или -1 если такого не может быть
	public int GetId(int row, int col) {
		int result = -1;
		int rLong = ifRowLong(row); // 0-длинн
		if (col < 1 || col > (mBHorisontal - rLong)) {
			return -1;
		}
		if (row < 1 || row > mBVertical) {
			return -1;
		}

		int ad;
		if (mFirstRowLong) {
			ad = mBHorisontal * rLong;
		} else {
			ad = (mBHorisontal - 1) * (1 - rLong);
		}
		result = (int) ((col - 1) + Math.floor((row - 1) / 2)
				* mBCountInTwoRows + ad);
		return result;
	}

	// Вычислить сектор столкновения
	// FixId - ИД пузыря в сетке
	// b - пузырь летящий
	public int GetTouchSector(int FixId, Bubble b) {
		Point p1 = mBubbleArrFix.get(FixId).GetPosition();
		Point p2 = b.GetPosition();
		int sector = 0;
		float g = (float) (2 * mBubbleRadius * 0.866025403784); // граница
																// интервала
																// по X
		float x = (p2.x - p1.x); // прилежащий катет
		float y = (p1.y - p2.y); // противолежащий катет
		if (g < x)
			sector = 0;
		else if (0 < x && x <= g) {
			if (y >= 0)
				sector = 1;
			else
				sector = 5;
		} else if (-g < x && x <= 0) {
			if (y >= 0)
				sector = 2;
			else
				sector = 4;
		} else
			sector = 3;

		return sector;
	}

	// //////////// Физика //////////////////////////////////

	// проверка доступности цветов
	private void testAvalableColor() {
		Bubble b;
		mColors.clear();
		for (int i = 0; i < mBubbleArrFix.size(); i++) {
			b = mBubbleArrFix.get(i);
			if (b != null && !mBubbleArrFix.get(i).getBursted()
					&& !mColors.contains(b.getColor())) {
				mColors.add(b.getColor());
			}

		}
	}

	// случайный цвет из доступных
	public int RandColor() {
		int mColor = 0;
		return mColors.get(mrnd.nextInt(mColors.size()));
	}

	// Добавление указанного пузыря в сетку по заданному id
	public boolean addInGrig(Bubble b, int id) {
		Point p;
		// если id<0
		if (id < 0)
			return false;

		// если в массиве нет нужной ячейки
		int to = id - mBubbleArrFix.size() + 1;
		for (int i = 0; i < to; i++) {
			mBubbleArrFix.add(null);
		}

		// если в заданной ячейке уже лежит пузырь
		if (mBubbleArrFix.get(id) == null) {
			p = GetXYbyID(id);
			b.SetSpeed(0, 0);
			b.SetPosition((int) p.x, (int) p.y);
			mBubbleArrFix.set(id, b);
		} else
			return false;

		int sc = 0;
		sc = burstSameColor(id);
		sc = sc + burstNoRoof();
		if (sc > 0) {
			GlobalParam.mScores = GlobalParam.mScores
					+ (int) (sc > 3 ? sc * (sc - 2) : sc);
			mStepToMooveDown=GlobalParam.mStepToMooveGridDown;
			testAvalableColor();
		} else {
			// если не лопнул не один пузырь - надо добавить пузырей
			mStepToMooveDown--;
			if (mStepToMooveDown<=0) {
				mStepToMooveDown=GlobalParam.mStepToMooveGridDown;
				addFixedBubblesLine();
			}
		}
		return true;
	}

	// пересчитывает координаты шаров при изменении размера шаров или движении в сетке
	private void reCalcPosition() {
		Point p;
		for (int i = 0; i < mBubbleArrFix.size(); i++) {

			if (mBubbleArrFix.get(i) != null) {
				p = GetXYbyID(i);
				mBubbleArrFix.get(i).SetPosition((int) p.x, (int) p.y);
			}
		}
	}

	// создание висячих пузырей
	public void CreateFixedBubbles() {
		Point p;
		for (int i = 0; i < (mBCountInTwoRows * (int) (mBVertical / 4)); i++) {
			if (mrnd.nextInt(4) > 0) {
				p = GetXYbyID(i);

				mBubbleArrFix.add(new Bubble((int) p.x, (int) p.y,
						mBubbleRadius, RandColor()));
			} else
				mBubbleArrFix.add(null);
		}
		burstNoRoof();
	}

	// добавление пузырей в верх сетки.
	public void addFixedBubblesLine() {
		Point p;
		Bubble b;
		int FirstLineLength=mBHorisontal-(mFirstRowLong?1:0);
		mFirstRowLong=!mFirstRowLong;
		for (int i = 0; i < FirstLineLength; i++) {
			//if (mrnd.nextInt(4) > 0) {
				b = new Bubble(0, 0, mBubbleRadius, RandColor());
				b.setRoofId(mRoofId);
				mBubbleArrFix.add(i, b);
			//} else
			//	mBubbleArrFix.add(i ,null);
		}
		reCalcPosition();
	}

	// проверка летящего пузыря на столкновение с висящими
	public int ifTouch(Bubble bubble) {
		int temp = -1;
		int s = mBubbleArrFix.size();
		for (int i = 0; i < s; i++) {
			if (mBubbleArrFix.get(i) != null
					&& !mBubbleArrFix.get(i).getBursted()
					&& bubble.Distance(mBubbleArrFix.get(i)) < mBubbleRadius * 2) {

				temp = i;
				break;
			}

		}
		return temp;
	}

	// Возврящает id соседних ячеек или -1
	public int[] GetNear(int id) {
		int n[] = new int[6];

		int row = GetRow(id);
		int col = GetCol(id);
		int rLong = ifRowLong(row); // 0-длинный, 1- короткий

		// справа
		n[0] = GetId(row, col + 1);
		// сверху справа
		n[1] = GetId(row - 1, col + rLong);
		// сверху слева
		n[2] = GetId(row - 1, col - (1 - rLong));
		// слева
		n[3] = GetId(row, col - 1);
		// снизу слева
		n[4] = GetId(row + 1, col - (1 - rLong));
		// снизу справа
		n[5] = GetId(row + 1, col + rLong);
		return n;
	}

	// Поиск одноцветных
	private void findSameColor(int id, ArrayList<Integer> FindId) {
		int near[] = GetNear(id);
		int idNear = 0;
		Bubble b1, b2;
		for (int i = 0; i < near.length; i++) {
			idNear = near[i];
			if (idNear < 0 || idNear >= mBubbleArrFix.size())
				continue;
			b1 = mBubbleArrFix.get(idNear);
			b2 = mBubbleArrFix.get(id);
			if (b1 != null && b2 != null && b1.getColor() == b2.getColor()) {
				if (!FindId.contains(idNear)) {
					FindId.add(idNear);
					findSameColor(idNear, FindId);
				}
			}
		}
	}

	// Определение одноцветных и лопанье. Вернет количество лопнутых
	private int burstSameColor(int id) {
		ArrayList<Integer> FindId = new ArrayList<Integer>();

		findSameColor(id, FindId);

		int IdBurst = 0;
		int count = 0;
		if (FindId.size() >= 3) {
			for (int i = 0; i < FindId.size(); i++) {
				IdBurst = FindId.get(i);
				if (IdBurst >= 0) {
					mBubbleArrFix.get(IdBurst).burst();
					count++;
				}
			}
		}
		return count;
	}

	// Проставляем пузырям текущий ид потолка. Рекурсивная
	private void setRoofId(int id) {
		Bubble b = mBubbleArrFix.get(id);
		if (b != null && !b.getBursted() && b.getRoofId() != mRoofId) {
			b.setRoofId(mRoofId);
			int mas[] = GetNear(id);
			int idnear = -1;
			for (int i = 0; i < 6; i++) {
				idnear = mas[i];
				if (idnear >= 0 && idnear < mBubbleArrFix.size()
						&& mBubbleArrFix.get(idnear) != null)
					setRoofId(idnear);
			}
		}
	}

	// Удаляем оторвавшиеся от потолка пузыри. Вернет количество оторваных
	private int burstNoRoof() {
		// сначало проставим новый ид потолка
		mRoofId = !mRoofId;
		// для всех верхних цикл
		for (int i = 0; i < (mBHorisontal - (mFirstRowLong ? 0 : 1)); i++) {
			setRoofId(i);
		}

		int count = 0;
		// затем проверим у всех пузырей ид, если не изменился - удаляем пузырь
		for (int i = 0; i < mBubbleArrFix.size(); i++) {
			if (mBubbleArrFix.get(i) != null
					&& !mBubbleArrFix.get(i).getBursted()
					&& mBubbleArrFix.get(i).getRoofId() != mRoofId) {
				mBubbleArrFix.get(i).burst();
				count++;
			}
		}
		return count;
	}

	// удаление из сетки лопнутых пузырей
	public void delFixBubble() {
		Bubble b;
		for (int i = 0; i < mBubbleArrFix.size(); i++) {
			b = mBubbleArrFix.get(i);
			if (b != null && b.getDeleted()) {
				mBubbleArrFix.set(i, null);
			}

		}
	}

	// проверка условий победы или поражения
	// вернет 0 - игра не закончена, 1-поражение, 2-победа
	public int getEndGame() {
		Bubble b;
		int temp = 2;
		for (int i = 0; i < mBubbleArrFix.size(); i++) {
			b = mBubbleArrFix.get(i);
			if (b != null) {
				temp = 0;

			}
			if (b != null && GetRow(i) >= mBVertical && !b.getBursted()) {
				temp = 1;
				break;
			}

		}
		return temp;
	}

	@Override
	public void onRadiusChange(int radius, int spacing) {
		for (int i = 0; i < mBubbleArrFix.size(); i++) {
			if (mBubbleArrFix.get(i) != null)
				mBubbleArrFix.get(i).onRadiusChange(radius, spacing);
		}
		mBubbleRadius = radius;
		mSpacing = spacing;
		reCalcPosition();
	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return true;
	}
	
	// вернет на каком шаге до смещения сетки вниз находимся
	public byte getStepGridDown(){
		return (byte) (mStepToMooveDown-1);
	}

	// ///////////// Графика //////////////////////////////////

	public void draw(Canvas mCanvas, int xS, int yS) {
		Bubble b;
		for (int i = 0; i < mBubbleArrFix.size(); i++) {
			b = mBubbleArrFix.get(i);
			if (b != null) {
				b.draw(mCanvas, xS, yS);
				// b.draw(temp, xS, yS);
			}
		}
		
		//mCanvas.drawBitmap(temp.g, left, top, paint);
	}
}
