package test.framework.concurrence.condition.job;

public class TestJob extends AbstractJob {
	private int id;
	
	public TestJob(int id){
		this.id = id;
	}

	@Override
	public void doBeforeJob() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doConcurrentJob() {
		// TODO Auto-generated method stub
		System.out.println("I am doing some thing. the id is " + id);
	}

	@Override
	public void doAfterJob() {
		// TODO Auto-generated method stub

	}

}
