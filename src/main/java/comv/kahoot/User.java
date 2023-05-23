
public class User {
	private int winningStreak, score;

	public User(int winningStreak, int score) {
		this.winningStreak = winningStreak;
		this.score = score;
	}

	public int getWinningStreak() {
		return winningStreak;
	}

	public void setWinningStreak(int winningStreak) {
		this.winningStreak = winningStreak;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	} 
	
}
