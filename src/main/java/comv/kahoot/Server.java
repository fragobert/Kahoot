import java.time.Duration;
import java.time.LocalDateTime;

public class Server {
	
	private static void calculateScore(User user, Question question, Answer answer) {
		if(CompareArrays.compareArray(question.getIndex(), answer.getIndex())) {
			Duration duration = Duration.between(question.getStartTime(), answer.getEndtime());
			if(question.getIndex().length == 1) {
				user.setScore(user.getScore() + (int)(1000 * (1 - ((duration.getSeconds() / question.getMaxSeconds()) / 2))));
			}else{
				user.setScore(user.getScore() + (int)((500 * question.getIndex().length) * (1 - (((double)duration.getSeconds() / question.getMaxSeconds()) / 2))));
				System.out.println(question.getMaxSeconds());
			}
		}		
	}
	
	/*

	public static void main(String[] args) {
		User user = new User(0, 0);
		String[] answers = {"a", "b", "c", "d"};
		int[] indexQ = {1, 2, 3};
		LocalDateTime startTime = LocalDateTime.now();
		System.out.println(startTime.getSecond());
		Question question = new Question("Was ist richtig?", answers, 30, indexQ, startTime);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int[] indexA = {1, 2, 3};
		LocalDateTime endTime = LocalDateTime.now();
		System.out.println(endTime.getSecond());
		Answer answer = new Answer(indexA, endTime);
		
		calculateScore(user, question, answer);
		
		System.out.println(user.getScore());
	}
	
	*/

}
