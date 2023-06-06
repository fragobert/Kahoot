package comv.kahoot.quiz;

import java.util.Scanner;

public class Question { 
	
	
	public String question;
	public String[] answers;
	public String correctAnswers;
	
	Question(String question, String[] answers, String correctAnswers) {
		this.question = question;
		this.answers = answers;
		this.correctAnswers = correctAnswers;
	}
	
}