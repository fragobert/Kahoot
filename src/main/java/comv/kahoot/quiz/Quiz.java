package comv.kahoot.quiz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Quiz {

	public static final String legalChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";
	public static Scanner userInput = new Scanner(System.in);
	public ArrayList<Question> questions = new ArrayList<>();
	public String quizName;


	public Quiz(String quizName, ArrayList<Question> questions) {
		this.questions = questions;
		this.quizName = quizName;
	}



	public void createQuiz() {

		ArrayList<Question> collection = new ArrayList<>();

		String tmp = "";

		System.out.println("Hello user!\nTo create your quiz you'll need to give us a few questions to put in");

		while(!tmp.equals("//exit")) {

			String question = "";
			String [] answers = new String[4];
			int[] correctAnswers = new int[4];
			int maxTime;
			System.out.println("What kind of question do you want to create?\n1.\tTrue or False\n2.\tMultiple choice");
			tmp = userInput.nextLine();

			switch(tmp) {

				case "1":

					System.out.println("Your question: ");

					tmp = userInput.nextLine();
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

							tmp = userInput.nextLine();

							if(tmp.equals("t")) {
								answers[0] = "t";
								answers[1] = "f";
								correctAnswers[0] = 1;
								correctAnswers[1] = 0;
							} else if(tmp.equals("f")) {
								answers[0] = "t";
								answers[1] = "f";
								correctAnswers[0] = 0;
								correctAnswers[1] = 1;
							} else {
								System.out.println("Something went wrong sry :/");
							}
							break;

						} catch(InputMismatchException e) {
							System.out.println("Input must be t or f");
							userInput.nextLine();
						}
					}

					answers[2] = null;
					answers[3] = null;
					correctAnswers[2] = 2;
					correctAnswers[3] = 2;

					while(true) {
						try {
							System.out.println("How much time do the players get?");
							tmp = userInput.nextLine();
							if( Integer.parseInt(tmp) > 60) {
								System.out.println("The maximum is 60 seconds");
							} else if(Integer.parseInt(tmp) < 10) {
								System.out.println("You must give at least 10 seconds to your players ");
							} else {
								break;
							}
						} catch(NumberFormatException e) {
							System.out.println("You must enter digits!");
							userInput.nextLine();
						}
					}

					maxTime = Integer.parseInt(tmp);

					collection.add(new Question(question, answers, maxTime, correctAnswers));
					break;


				case "2":

					System.out.println("Your question: ");

					tmp = userInput.nextLine();

					if(tmp.contains(";")) {
						tmp = tmp.replaceAll(";", ",");
						question = tmp;
					}else {
						question = tmp;
					}

					System.out.println("Next we will need possible answers for the question: ");
					for(int i = 0; i < 4; i++) {

						System.out.println("Your answer: ");

						tmp = userInput.nextLine();


						if(tmp.contains(";")) {
							tmp = tmp.replaceAll(";", ",");
							answers[i] = tmp;
						}else {
							answers[i] = tmp;
						}

						System.out.println("Is this answer correct?[y/n]");

						tmp = userInput.nextLine();

						if(tmp.equals("y")) {
							correctAnswers[i] = 1;
						} else if(tmp.equals("n")) {
							correctAnswers[i] = 0;
						} else {
							System.out.println("Wrong input please type your answer again");
							i--;
						}

						tmp = "";
					}
					while(true) {
						try {
							System.out.println("How much time do the players get?");
							tmp = userInput.next();
							if( Integer.parseInt(tmp) > 60) {
								System.out.println("The maximum is 60 seconds");
							} else if(Integer.parseInt(tmp) < 10) {
								System.out.println("You must give at least 10 seconds to your players ");
							} else {
								break;
							}
						} catch(NumberFormatException e) {
							System.out.println("You must enter digits!");
							userInput.nextLine();
						}
					}

					maxTime = Integer.parseInt(tmp);
					userInput.nextLine();

					collection.add(new Question(question, answers, maxTime, correctAnswers));
					break;
				default:
					System.out.println("That option does not exist");

					break;
			}


			System.out.println("Do you want to add another question? If not type //exit");
			tmp = userInput.nextLine();

		}
		System.out.println("And finally, what name should your quiz have?");

		setQuizName(userInput.nextLine());


		setQuestions(collection);

		return;
	}


	public void saveQuiz() {
		File newQuiz = new File("./src/Quiz/" + this.quizName + ".csv");
		String fullContent = "";

		try {
			newQuiz.createNewFile();

			FileWriter writeQuiz = new FileWriter(newQuiz);



			for(int i = 0; i < this.questions.size(); i++) {

				fullContent += this.questions.get(i).getQuestion() + ";";

				for(int j = 0; j < this.questions.get(i).getAnswers().length; j++) {
					fullContent += this.questions.get(i).getAnswers()[j] + ";";
				}
				for(int j = 0; j < this.questions.get(i).getIndex().length; j++) {
					fullContent += this.questions.get(i).getIndex()[j] + ";";
				}

				fullContent += this.questions.get(i).getMaxSeconds() + ";";

				writeQuiz.write(fullContent + "\n");

				fullContent = "";
			}

			writeQuiz.close();
		} catch (IOException e) {
			e.printStackTrace();
		}



	}

	public static Quiz loadQuiz() {
		File quizDir = new File("./src/Quiz");
		int count = 0;
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
				quizToLoad = userInput.nextInt() -1;
				quiz = new File("./src/Quiz/" + allFiles.get(quizToLoad));
				break;

			}catch(InputMismatchException e) {
				System.out.println("You can only enter digits");
				userInput.nextLine();
			}catch(IndexOutOfBoundsException e) {
				System.out.println("Your entered digit is out of bounds");
				userInput.nextLine();

			}
		}



		ArrayList<Question> quizContent = new ArrayList<Question>();
		String data;
		String question;

		String quizName = allFiles.get(quizToLoad);

		try {
			Scanner loadContent = new Scanner(quiz);
			while(loadContent.hasNextLine()) {
				int[] correctAnswers = new int[4];
				String [] splittedData = new String[6];
				String [] answers = new String[4];
				int maxSecs;

				data = loadContent.nextLine();

				splittedData = data.split(";");

				question = splittedData[0];

				for(int i = 1; i < 5; i++) {
					answers[i- 1] = splittedData[i];
				}


				for(int i = 5; i < 9; i++) {
					correctAnswers[i-5] = Integer.parseInt(splittedData[i]);
				}

				maxSecs = Integer.parseInt(splittedData[9]);

				quizContent.add(new Question(question, answers, maxSecs, correctAnswers));
			}
			loadContent.close();

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
