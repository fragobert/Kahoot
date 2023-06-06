package comv.kahoot.client;

import java.time.LocalDateTime;

public class Answer {
	private int [] index;
	private LocalDateTime endtime;
	
	public Answer(int[] index, LocalDateTime endtime) {
		this.index = index;
		this.endtime = endtime;
	}
	
	public int[] getIndex() {
		return index;
	}
	
	public void setIndex(int[] index) {
		this.index = index;
	}
	
	public LocalDateTime getEndtime() {
		return endtime;
	}
	
	public void setEndtime(LocalDateTime endtime) {
		this.endtime = endtime;
	}

}
