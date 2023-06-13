package comv.kahoot.quiz;

import java.time.LocalDateTime;

public class Question {
	private String question;
	private String [] answers;
	private int maxSeconds;
	private int[] index;

	public Question(String question, String[] answers, int maxSeconds, int[] index) {
		super();
		this.question = question;
		this.answers = answers;
		this.maxSeconds = maxSeconds;
		this.index = index;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String[] getAnswers() {
		return answers;
	}

	public void setAnswers(String[] answers) {
		this.answers = answers;
	}


	public int getMaxSeconds() {
		return maxSeconds;
	}

	public void setMaxSeconds(int maxSeconds) {
		this.maxSeconds = maxSeconds;
	}

	public int[] getIndex() {
		return index;
	}

	public void setIndex(int[] index) {
		this.index = index;
	}

	public String getProtocol() {

		StringBuilder sb = new StringBuilder();

		sb.append(this.getQuestion() + ";");

		for(String tmp : this.getAnswers()) {
			sb.append(tmp + ";");
		}

		sb.deleteCharAt(sb.length()-1);
		return "q" + sb.toString();
	}

}