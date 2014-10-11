package service.framework.io.server.statistics;

public class DefaultServerStatistics implements ServerStatistics{
	/**
	 * 获取服务的信息，发送给服务注册中心
	 * @return
	 */
	public StatisticsInformation gatherStatisticsInformation()
	{
		StatisticsInformation objStatisticsInformation = new StatisticsInformation();
		return objStatisticsInformation;
	}
}
