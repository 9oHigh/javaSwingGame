

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;


//패널 부모클래스 - 그레디언트입히기(백그라운드)
class BackgroundPanel extends JPanel{
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int W = getWidth(),H = getHeight();
		Graphics2D gPaint = (Graphics2D) g;
		gPaint.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		Color color1 = Color.BLACK;
		Color color2 = new Color(72,61,139);
		GradientPaint gp = new GradientPaint(0, 0, color1, 0, H, color2);
		gPaint.setPaint(gp);
		gPaint.fillRect(0, 0, W, H);
	}
}
//오브젝트 추상클래스
abstract class GameObject{
	abstract void drawing(Graphics g);
	void collisionHandling(GameObject input) {};
	void updatePosition(float dt) {};
	boolean isDead() {return false;}
}
//벽 클래스
//공이랑만 부딛힘
class Wall extends GameObject {
	int x,y;
	int recWidth,recHeight;
	Color back1,back2,forth1,forth2;

	Wall(int inputX,int inputY,int inputW,int inputH){
		x = inputX;
		y = inputY;
		recWidth = inputW;
		recHeight = inputH;
	}
	@Override
	void drawing(Graphics g) {
		Graphics2D gPaint = (Graphics2D) g;
		gPaint.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		back1 = new Color(220,220,220);
		back2 = Color.black;
		forth1 = new Color(105,105,105);
		forth2 = new Color(104,104,104);

		GradientPaint gp1 = new GradientPaint(x, y, back1, x+recWidth, y+recHeight, back2);
		GradientPaint gp2 = new GradientPaint(x+2, y+2, forth1, x+recWidth-2, y+recHeight-2, forth2);

		gPaint.setPaint(gp1);
		gPaint.fillRect(x, y, recWidth, recHeight);
		gPaint.setPaint(gp2);
		gPaint.fillRect(x+2, y+2, recWidth-4, recHeight-4);
	}
	boolean collision(Balls inputBall) {
		if(x<inputBall.x+inputBall.radius && inputBall.x-inputBall.radius<x+recWidth &&y<inputBall.y+inputBall.radius&&inputBall.y-inputBall.radius<y+recHeight) {
			return true;
		}
		return false;
	}
}
//무빙바 클래스 - 스틱
//공이랑만 부딛힘
class MovingBar extends GameObject{
	int x,y;
	int recWidth,recHeight;
	Color top,bottom;

	MovingBar(int inputX,int inputY,int inputW,int inputH){
		x = inputX;
		y = inputY;
		recWidth = inputW;
		recHeight = inputH;
	}
	@Override
	void drawing(Graphics g) {

		Graphics2D gPaint = (Graphics2D) g;
		gPaint.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//런메소드로 깜빡임효과주기
		top = Color.red;
		bottom = Color.blue;
		GradientPaint gp = new GradientPaint(x, y, top, x+recWidth, y+recHeight, bottom);
		gPaint.setPaint(gp);
		gPaint.fillRect(x, y, recWidth, recHeight);
	}
	void moving(int direction,int speed) {
		if(direction == 1) {
			if(x+recWidth+speed<565) x = x+speed;
			else if(x+recWidth+speed>565) x = 465;
		}
		else {
			if(x-speed>26) x = x-speed;
			else if(x-speed<26) x = 26;
		}
	}
	boolean collision(Balls inputBall) {
		if(x<inputBall.x+inputBall.radius && inputBall.x-inputBall.radius<x+recWidth &&y<inputBall.y+inputBall.radius&&inputBall.y-inputBall.radius<y+recHeight) {
			return true;
		}
		return false;
	}
}
//블럭 클래스
//공이랑만 부딛힘
class Blocks extends GameObject{
	int x,y;
	int recWidth,recHeight;
	boolean Dead = false;
	boolean newColor = false;
	Color top,bottom;

	Blocks(int inputX,int inputY,int inputW,int inputH,boolean inputC){
		x = inputX;
		y = inputY;
		recWidth = inputW;
		recHeight = inputH;
		newColor = inputC;
	}
	@Override
	void drawing(Graphics g) {

		Graphics2D gPaint = (Graphics2D) g;
		gPaint.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//런메소드로 깜빡임효과주기
		if(newColor == true) {
			top = Color.yellow;
			bottom = Color.white;
		}
		else{
			top = Color.white;
			bottom = Color.gray;
		}
		GradientPaint gp = new GradientPaint(x, y, top, x+recWidth, y+recHeight, bottom);
		gPaint.setPaint(gp);
		gPaint.fillRect(x, y, recWidth, recHeight);
	}
	boolean collision(Balls inputBall) {
		if(x<inputBall.x+inputBall.radius && inputBall.x-inputBall.radius<x+recWidth &&y<inputBall.y+inputBall.radius&&inputBall.y-inputBall.radius<y+recHeight) {
			return true;
		}
		return false;
	}
	boolean isDead() {
		if(Dead)
			return true;
		return false;
	}

}
// 볼 클래스
//벽과 블럭
class Balls extends GameObject{
	float x,y;
	float vx,vy;
	float radius;
	float agox,agoy;

	Balls(){
		x = 300;
		y = 400;
		agox = x;
		agoy = y;
		vx = (int)(Math.random()*100)-50.0f;
		vy = 300;
		radius = 8;
	}
	Balls(float inputX, float inputY){
		x = inputX;
		y = inputY;
		agox = x;
		agoy = y;
		vx = (int)(Math.random()*100)-50.0f;
		vy = 300;
		radius = 8;
	}
	@Override 
	boolean isDead() {
		if(y+radius>=600)
			return true;
		return false;
	}
	@Override
	void updatePosition(float dt) {
		agox = x;
		agoy = y;
		x = x+(vx*dt);
		y = y+(vy*dt);
	}
	@Override
	void drawing(Graphics g) {
		g.setColor(new Color(189,183,107));
		g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius),(int)(radius));	
	}
	@Override
	void collisionHandling(GameObject input) {
		if(input == this) return; 
		if(input instanceof Wall) {
			Wall wall = (Wall) input;
			if(wall.collision(this)){
				// 벽의 아랫면하고 충돌
				if(agoy-radius>wall.y+wall.recHeight) {vy = -vy; y = wall.y +wall.recHeight+radius;}
				// 벽의 왼쪽하고 충돌
				if(agox+radius<wall.x) { vx = -vx; x = wall.x - radius; }
				// 벽의 오른쪽면하고 충돌
				if(agox-radius>wall.x+ wall.recWidth) { vx = -vx; x = wall.x +wall.recWidth + radius; }
			}
		}
		if(input instanceof Blocks) {
			Blocks block = (Blocks) input;
			if(block.collision(this)) {
				block.Dead = true;
				// 블럭의 윗면하고 충돌
				if(agox+radius<block.y) { vy = -vy; y = block.y - radius; }
				// 블럭의 아래면하고 충돌
				if(agoy-radius>block.y+block.recHeight) { vy = -vy; y = block.y +block.recHeight + radius; }
				// 블럭의 왼쪽하고 충돌
				if(agox+radius<block.x) { vx = -vx; x = block.x - radius; }
				// 블럭의 오른쪽면하고 충돌
				if(agox-radius>block.x+ block.recWidth) { vx = -vx; x = block.x +block.recWidth + radius; }
			}
		}
		if(input instanceof MovingBar) {
			MovingBar move = (MovingBar) input;
			if(move.collision(this)) {
				int gap = move.recWidth/3;
				// 블럭의 윗면하고 충돌
				if(agox+radius<move.y) {
					//왼쪽부분(3분할)
					if(move.x+(gap/3) >= x) {vy = -vy; vx -=40; y = move.y - radius;}
					if(move.x+(gap/3)*2 >= x) {vy = -vy; vx-=30; y = move.y - radius;}
					if(move.x+(gap/3)*3 >= x) {vy = -vy; vx -=20; y = move.y - radius;}
					//중앙부분
					if(move.x+gap <x && x<move.x+gap*2) { vy = -vy; y = move.y - radius;}
					//오른쪽부분(3분할)
					if(move.x+gap*2<=x&&x< move.x+gap*2+(gap/3)) {vy = -vy; vx +=20; y = move.y - radius;}
					if(move.x+gap*2+(gap/3)<=x&&x< move.x+gap*2+(gap/3)*2) {vy = -vy; vx +=30; y = move.y - radius;}
					if(move.x+gap*2+(gap/3)*2<=x&&x< move.x+gap*2+gap) {vy = -vy; vx+=30; y = move.y - radius;}
				}
			}
		}
	}
}
//시작화면 클래스
class BlockBreakerStartPanel{

	String introName = "Java Programming";
	String homeworkName = "Homework #5";
	String gameName = "BLOCK BREAKER";
	String flickersName = "PRESS SPACEBAR TO PLAY!";

	Font introAndHomeworkFont = new Font("Bodoni MT",Font.PLAIN,30);
	Font gameFont =new Font("Bernard MT Condensed",Font.BOLD,60);
	Font flickersFont = new Font("Ariel",Font.BOLD,15);

	void typing(Graphics g) {
		g.setFont(introAndHomeworkFont);
		g.setColor(Color.black);
		g.drawString(introName, 180, 170);
		g.drawString(homeworkName, 210, 230);

		g.setColor(Color.white);
		g.drawString(introName, 177, 170);
		g.drawString(homeworkName, 207, 230);

		g.setFont(gameFont);
		g.setColor(Color.black);
		g.drawString(gameName, 107, 350);
		g.setColor(Color.white);
		g.drawString(gameName, 104, 350);

		g.setFont(flickersFont);
		g.setColor(Color.black);
		g.drawString(flickersName, 200, 450);
		g.setColor(Color.red);
		g.drawString(flickersName, 197, 450);
	}
}
//게임화면 클래스
class BlockBreakerGamePanel{

	LinkedList<Balls> balls = new LinkedList<Balls>();
	LinkedList<Blocks> blocks = new LinkedList<Blocks>();
	LinkedList<Wall> walls = new LinkedList<Wall>();

	MovingBar moving;
	Balls newBall;

	int gap=10;
	int movingBlockSizeX = 100,movingBlockSizeY = 15;
	int fixedBlockX = 175,fixedBlockY = 100;
	int blocksCnt = 3;
	int level = 1;
	boolean checkPoint = false;

	//랜덤반환
	int randomInt() {
		int out = (int)(Math.random()*100);
		return out;
	}

	BlockBreakerGamePanel(){
		newBall = new Balls();
		balls.add(newBall);
		moving = new MovingBar(250,550,movingBlockSizeX,movingBlockSizeY);

		Wall fixedWallLeft = new Wall(0,26,26,600);
		Wall fixedWallTop = new Wall(0,0,600,25);
		Wall fixedWallRight = new Wall(565,26,26,600);

		walls.add(fixedWallTop);
		walls.add(fixedWallLeft);
		walls.add(fixedWallRight);

		//Level 1
		for(int i = 0;i<fixedBlockY*blocksCnt;i+=fixedBlockY) {
			for(int j = 0;j<fixedBlockX*blocksCnt;j+=fixedBlockX) {
				if(randomInt()%2==0 && randomInt()>50) {
					checkPoint = true;
					Blocks newBlock = new Blocks(28+gap+j,27+gap+i,fixedBlockX-gap,fixedBlockY-gap,checkPoint);
					blocks.add(newBlock);
				}
				else {
					checkPoint = false;
					Blocks newBlock = new Blocks(28+gap+j,27+gap+i,fixedBlockX-gap,fixedBlockY-gap,checkPoint);
					blocks.add(newBlock);
				}
			}
		}
	}

	void nextLevel() {

		if(level == 2) {	
			newBall = new Balls();
			balls.add(newBall);
			moving = new MovingBar(250,550,movingBlockSizeX,movingBlockSizeY);

			newBall.vy +=10;
			blocksCnt+=3;

			fixedBlockX/=2;
			fixedBlockY/=2;

			for(int i = 0;i<fixedBlockY*blocksCnt;i+=fixedBlockY) {
				for(int j = 0;j<fixedBlockX*blocksCnt;j+=fixedBlockX) {
					if(randomInt()%2==0 && randomInt()>50) {
						checkPoint = true;
						Blocks newBlock = new Blocks(28+gap+j,27+gap+i,fixedBlockX-gap,fixedBlockY-gap,checkPoint);
						blocks.add(newBlock);
					}
					else {
						checkPoint = false;
						Blocks newBlock = new Blocks(28+gap+j,27+gap+i,fixedBlockX-gap,fixedBlockY-gap,checkPoint);
						blocks.add(newBlock);
					}
				}
			}
		}
		if(level == 3) {
			newBall = new Balls();
			balls.add(newBall);
			moving = new MovingBar(250,550,movingBlockSizeX,movingBlockSizeY);

			newBall.vy +=20;
			blocksCnt+=3;

			fixedBlockX/=2;
			fixedBlockY/=2;

			for(int i = 0;i<400;i+=fixedBlockY) {
				for(int j = 0;j<500;j+=fixedBlockX) {
					if(randomInt()%2==0 && randomInt()>50) {
						checkPoint = true;
						Blocks newBlock = new Blocks(28+gap+j,27+gap+i,fixedBlockX-gap,fixedBlockY-gap,checkPoint);
						blocks.add(newBlock);
					}
					else {
						checkPoint = false;
						Blocks newBlock = new Blocks(28+gap+j,27+gap+i,fixedBlockX-gap,fixedBlockY-gap,checkPoint);
						blocks.add(newBlock);
					}
				}
			}
		}
	}
}
//종료화면 클래스
class BlockBreakerEndPanel{

	int totalScore;
	int score;

	Font outroFont = new Font("Bernard MT Condensed",Font.PLAIN,80);
	Font scoreFont =new Font("Bodoni MT",Font.PLAIN,35);
	Font flickersFont = new Font("Ariel",Font.BOLD,15);

	String outro=new String("GAME OVER");
	String highScore=new String("HIGH SCORE : "+totalScore);

	String yourScore=new String("YOUR SCORE : "+score);
	String flic=new String("PRESS SPACEBAR!");

	void typing(Graphics g){

		if(PlayingGame.clear) {
			g.setFont(outroFont);
			g.setColor(Color.BLACK);
			g.drawString(outro, 70, 170);
			g.setColor(Color.white);
			g.drawString(outro, 73, 170);
		}
		else{
			g.setFont(outroFont);
			g.setColor(Color.BLACK);
			g.drawString(outro, 125, 170);
			g.setColor(Color.white);
			g.drawString(outro, 128, 170);
		}
		g.setFont(scoreFont);
		g.setColor(Color.BLACK);
		g.drawString(highScore, 160, 260);
		g.drawString(yourScore, 160, 350);
		g.setColor(Color.white);
		g.drawString(highScore, 157, 260);
		g.drawString(yourScore, 157, 350);

		g.setFont(flickersFont);
		g.setColor(Color.BLACK);
		g.drawString(flic, 220, 450);
		g.setColor(Color.red);
		g.drawString(flic, 217, 450);
	}
}
//메인 패널
class PlayingGame extends BackgroundPanel implements KeyListener,Runnable{

	final static int introPanel = 1;
	final static int gammingPanel = 2;
	final static int endPanel = 3;
	int whichPanel = introPanel;
	int yourscore = 0,totalScore = 0;
	static boolean clear = false;
	float dt = 0.029f;

	BlockBreakerStartPanel Start = new BlockBreakerStartPanel();
	BlockBreakerEndPanel End = new BlockBreakerEndPanel();
	BlockBreakerGamePanel Game;

	Clip clip1,clip2,clip3,clip4,clip5,clip6;
	Thread flickerAndAudio = new Thread(this);

	PlayingGame(){

		whichPanel = introPanel;
		flickerAndAudio.start();
		try {
			clip1 = AudioSystem.getClip();
			clip2 = AudioSystem.getClip();
			clip3 = AudioSystem.getClip();
			clip4 = AudioSystem.getClip();
			clip5 = AudioSystem.getClip();
			clip6 = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		URL url1 = getClass().getClassLoader().getResource("insertCoin.wav");
		URL url2 = getClass().getClassLoader().getResource("gameintro.wav");
		URL url3 = getClass().getClassLoader().getResource("gameover.wav");
		URL url4 = getClass().getClassLoader().getResource("crashtobar.wav");
		URL url5 = getClass().getClassLoader().getResource("crashtoblock.wav");
		URL url6 = getClass().getClassLoader().getResource("crashtothreeblock.wav");
		AudioInputStream audioStream1 = null;
		AudioInputStream audioStream2 = null;
		AudioInputStream audioStream3 = null;
		AudioInputStream audioStream4 = null;
		AudioInputStream audioStream5 = null;
		AudioInputStream audioStream6 = null;
		try {
			audioStream1 = AudioSystem.getAudioInputStream(url1);
			audioStream2 = AudioSystem.getAudioInputStream(url2);
			audioStream3 = AudioSystem.getAudioInputStream(url3);
			audioStream4 = AudioSystem.getAudioInputStream(url4);
			audioStream5 = AudioSystem.getAudioInputStream(url5);
			audioStream6 = AudioSystem.getAudioInputStream(url6);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		try {
			clip1.open(audioStream1);
			clip2.open(audioStream2);
			clip3.open(audioStream3);
			clip4.open(audioStream4);
			clip5.open(audioStream5);
			clip6.open(audioStream6);
		} catch (LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
		clip1.start();
		clip2.start();
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
	}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {

		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			if(whichPanel == introPanel) {
				whichPanel = gammingPanel;
				Game = new BlockBreakerGamePanel();
				clip1.stop();
				clip2.stop();

			}
			else if(whichPanel == endPanel) {
				clip3.stop();
				whichPanel  = introPanel;
				//클립 처음부터
				clip1.setMicrosecondPosition(0);
				clip2.setMicrosecondPosition(0);
				clip1.start();
				clip2.start();	
			}
		}
		if(whichPanel == gammingPanel) {
			if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				Game.moving.moving(1,15);
			}
			else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				Game.moving.moving(2,15);
			}
			repaint();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		requestFocus();
		setFocusable(true);

		if(whichPanel ==1) {
			End.score = 0;
			Start.typing(g);
		}
		else if(whichPanel == 2) {
			for(Balls out1 : Game.balls) 
				out1.drawing(g);
			for(Blocks out2 : Game.blocks) 
				out2.drawing(g);
			for(Wall out3 : Game.walls) 
				out3.drawing(g);
			Game.moving.drawing(g);
		}
		else {
			End.yourScore = "YOUR SORE : "+yourscore;
			End.highScore = "HIGH SCORE : "+totalScore;
			End.typing(g);
		}
	}
	@Override
	public void run() {
		try {
			while(true) {
				if(whichPanel == introPanel) {
					yourscore = 0;
					Start.flickersName="";
					repaint();
					Thread.sleep(400);
					Start.flickersName = "PRESS SPACEBAR TO PLAY!";
					repaint();
					Thread.sleep(400);
				}
				else if(whichPanel == gammingPanel) {

					for(Balls out : Game.balls) {
						out.updatePosition(dt);
					}
					Iterator<Balls> hrs1 = Game.balls.iterator();
					while(hrs1.hasNext()) {
						Balls ball = hrs1.next();
						if(ball.isDead()==true) {
							hrs1.remove();
						}
					}
					for(Balls outball1 : Game.balls) {
						for(GameObject out2 : Game.blocks) {
							outball1.collisionHandling(out2);
						}
					}
					Iterator<Blocks> hrs2 = Game.blocks.iterator();
					while(hrs2.hasNext()) {
						Blocks block = hrs2.next();
						if(block.isDead()==true && block.newColor == false) {
							clip5.setFramePosition(0);
							clip5.start();
							hrs2.remove();
							yourscore+=100;
						}
						else if(block.isDead() == true && block.newColor == true) {
							Balls [] chanceBalls = new Balls[3];
							clip6.setFramePosition(0);
							clip6.start();
							chanceBalls[0] = new Balls(block.x+block.recWidth/2,block.y+block.recHeight);
							chanceBalls[1] = new Balls(block.x+block.recWidth/2,block.y+block.recHeight);
							chanceBalls[2] = new Balls(block.x+block.recWidth/2,block.y+block.recHeight);
							for(int i = 0;i<3;i++)
								Game.balls.add(chanceBalls[i]);
							hrs2.remove();
							yourscore+=200;
						}
					}
					for(GameObject outball2 : Game.balls) {
						for(GameObject out3 : Game.walls) {
							outball2.collisionHandling(out3);
						}
					}
					for(GameObject outball3 : Game.balls) {
						outball3.collisionHandling(Game.moving);
					}

					if(Game.blocks.size() <=0) {
						Game.balls.removeAll(Game.balls);
						Game.blocks.removeAll(Game.blocks);
						Game.level++;
						Game.nextLevel();
						if(Game.level>3) {
							Game.level = 1;
							End.outro = "**GAME CLEAR**";
							clear = true;
							whichPanel = endPanel;
							Game.balls.removeAll(Game.balls);
							Game.blocks.removeAll(Game.blocks);
							clip3.start();
						}
					}
					if(Game.balls.size()<=0) {
						if(yourscore>totalScore)
							totalScore = yourscore;
						Game.balls.removeAll(Game.balls);
						Game.blocks.removeAll(Game.blocks);
						whichPanel = endPanel;
						clear = false;
						clip3.start();
					}
					Thread.sleep((int)(dt*1000));
					repaint();
				}
				else {
					End.flic="";
					repaint();
					Thread.sleep(400);
					End.flic = "PRESS SPACEBAR!";
					repaint();
					Thread.sleep(400);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
public class BlockBreaker extends JFrame{

	PlayingGame playingGamePanel;

	BlockBreaker(){
		playingGamePanel = new PlayingGame();
		setTitle("PLAY GAME");
		setSize(600,600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		add(playingGamePanel);
		playingGamePanel.setFocusable(true);
		setVisible(true);
	}

	public static void main(String[] args) {
		new BlockBreaker();
	}
}