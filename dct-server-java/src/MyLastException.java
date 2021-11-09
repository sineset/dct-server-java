import java.util.Date;

public class MyLastException {
	protected Exception ex;
	protected Date dt;

	public MyLastException() {
		ex = null;
		dt = null;
	}

	public void Save(Exception e) {
		ex = e;
		dt = new Date();
	}

	public Exception getException() {
		return ex;
	}

	public Date getDate() {
		return dt;
	}
}
