package service.framework.io.server.statistics;

public class DefaultServerStatistics implements ServerStatistics{
	/**
	 * ��ȡ�������Ϣ�����͸�����ע������
	 * @return
	 */
	public StatisticsInformation gatherStatisticsInformation()
	{
		StatisticsInformation objStatisticsInformation = new StatisticsInformation();
		return objStatisticsInformation;
	}
}
