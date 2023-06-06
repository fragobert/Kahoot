package comv.kahoot.quiz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Quiz {
	
	public static final String legalChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";
	public ArrayList<Question> questions = new ArrayList<>();
	public String quizName;
	private static final String PATH = "src/main/java/comv/kahoot/quiz/";
	public Quiz(String quizName, ArrayList<Question> questions) {
		this.questions = questions;
		this.quizName = quizName;
	}



	public void createQuiz() {
		
		ArrayList<Question> collection = new ArrayList<>();
		
		Scanner userQuestion = new Scanner(System.in);
		String tmp = "";

		System.out.println("Hello user!\nTo create your quiz you'll need to give us a few questions to put in");
		
		while(!tmp.equals("//exit")) {
			
			String question = "";
			String [] answers = new String[4];
			String correctAnswers = "";
		System.out.println("What kind of question do you want to create?\n1.\tTrue or False\n2.\tMultiple choice");
		tmp = userQuestion.next();
		
		switch(tmp) {
		
		case "1":
			userQuestion.nextLine();

				System.out.println("Your question: ");
				
				tmp = userQuestion.nextLine();
				System.out.println(tmp);
				
				if(tmp.contains(";")) {
					tmp = tmp.replaceAll(";", ",");
					question = tmp;
				}else {
					question = tmp;
				}
				
				while(true) {
					try {
						System.out.println("Is this question true or false?[t/f]");
						
						tmp = userQuestion.nextLine();
						
						if(tmp.equals("t")) {
							answers[0] = "t";
							answers[1] = "f";
							correctAnswers += "10";
						} else if(tmp.equals("f")) {
							answers[0] = "f";
							answers[1] = "t";
							correctAnswers += "01";
						} else {
							System.out.println("Something went wrong sry :/");
						}
						break;
							
					} catch(InputMismatchException e) {
						System.out.println("Input must be t or f");
						userQuestion.nextLine();
					}
			}

				answers[2] = null;
				answers[3] = null;
				correctAnswers += "22";
				collection.add(new Question(question, answers, correctAnswers));
				correctAnswers = "";
				break;
			
			
		case "2":
			userQuestion.nextLine();
			
				System.out.println("Your question: ");
						
				tmp = userQuestion.nextLine();
				
				if(tmp.contains(";")) {
					tmp = tmp.replaceAll(";", ",");
					question = tmp;
				}else {
					question = tmp;
				}
				
				System.out.println("Next we will need possible answers for the question: ");
				for(int i = 0; i < 4; i++) {
					
					System.out.println("Your answer: ");
					
					tmp = userQuestion.nextLine();
					
					
					if(tmp.contains(";")) {
						tmp = tmp.replaceAll(";", ",");
						answers[i] = tmp;
					}else {
						answers[i] = tmp;
					}
					
					System.out.println("Is this answer correct?[y/n]");
					
					tmp = userQuestion.nextLine();
					
					if(tmp.equals("y")) {
						correctAnswers += "1";
					} else if(tmp.equals("n")) {
						correctAnswers += "0";
					} else {
						System.out.println("Wrong input please type your answer again");
						i--;
					}
					tmp = "";
				}
					
					

				collection.add(new Question(question, answers, correctAnswers));
				correctAnswers = "";
				break;
	
		}
		System.out.println("Do you want to add another question? If not type //exit");
		tmp = userQuestion.nextLine();
		
		}
		System.out.println("And finally, what name should your quiz have?");
		
		setQuizName(userQuestion.nextLine());
		

		setQuestions(collection);	
		userQuestion.close();
		return;
	}
	
	
	public void saveQuiz() {
		File newQuiz = new File(PATH + this.quizName + ".csv");
		String fullContent = "";
		
		try {
			newQuiz.createNewFile();
			
			FileWriter writeQuiz = new FileWriter(newQuiz);
			
			
			
			for(int i = 0; i < this.questions.size(); i++) {
				
				fullContent += this.questions.get(i).question + ";";
				
				for(int j = 0; j < this.questions.get(i).answers.length; j++) {
					fullContent += this.questions.get(i).answers[j] + ";";
				}
				
				fullContent += this.questions.get(i).correctAnswers;
				
				writeQuiz.write(fullContent + "\n");
				
				fullContent = "";
			}
			
			writeQuiz.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public static Quiz loadQuiz() {
		File quizDir = new File(PATH);
		int count = 0;
		Scanner whichQuiz = new Scanner(System.in);
		ArrayList<String> allFiles = new ArrayList<>();
		int quizToLoad = 0;
		File quiz = null;
		
		
		System.out.println("Hello User which quiz do you want to load?");
		
		for(File fileName : quizDir.listFiles()) {
			count++;
			System.out.println(count + ".\t" + fileName.getName());
			allFiles.add(fileName.getName());
		}
	
		while(true) {
			
			try {
				quizToLoad = whichQuiz.nextInt() -1;
				quiz = new File(PATH + allFiles.get(quizToLoad));
				break;

			}catch(InputMismatchException e) {
				System.out.println("You can only enter digits");
				whichQuiz.nextLine();
			}catch(IndexOutOfBoundsException e) {
				System.out.println("Your entered digit is out of bounds");	
				whichQuiz.nextLine();
				
			}
		}
		
		whichQuiz.close();

		ArrayList<Question> quizContent = new ArrayList<Question>();
		String data;
		String question;
		String correctAnswers;
		String quizName = allFiles.get(quizToLoad);
		
		try {
			Scanner loadContent = new Scanner(quiz);
			while(loadContent.hasNextLine()) {
				String [] splittedData = new String[6];
				String [] answers = new String[4];
				
				data = loadContent.nextLine();
			
				splittedData = data.split(";");
				
				question = splittedData[0];
				
				for(int i = 1; i < 5; i++) {
					answers[i- 1] = splittedData[i];
				}

				correctAnswers = splittedData[5];
				
				quizContent.add(new Question(question, answers, correctAnswers));
				loadContent.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
		return new Quiz(quizName, quizContent);
	}
	
	
	
	public ArrayList<Question> getQuestions() {
		return questions;
	}


	public void setQuestions(ArrayList<Question> questions) {
		this.questions = questions;
	}


	public String getQuizName() {
		return quizName;
	}


	public void setQuizName(String quizName) {
		
		if(quizName.contains(" ")) {
			quizName = quizName.replaceAll(" ", "_");
		}
		
		for(char c : quizName.toCharArray()) {
			if(!legalChars.contains(String.valueOf(c))) {
				quizName = quizName.replaceAll(String.valueOf(c), "");
			}
		}
		
		System.out.println(quizName);
		this.quizName = quizName;
	}
	
	
}
