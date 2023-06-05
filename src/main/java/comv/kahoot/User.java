package comv.kahoot;

public class User {
	private int winningStreak, score;
	private final String username;

	public User(String username, int winningStreak, int score) {
		this.winningStreak = winningStreak;
		this.score = score;
		this.username = username;
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

	public String getUsername() {
		return username;
	}
}
